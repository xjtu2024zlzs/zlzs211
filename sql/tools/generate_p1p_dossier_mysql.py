from __future__ import annotations

import hashlib
import re
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
SOURCE_SCHEMA = ROOT / "algorithm-service" / "algorithm-pwb" / "data" / "cf_data" / "cf" / "init-scripts" / "06_cf_dossier_schema.sql"
SOURCE_SEED = ROOT / "algorithm-service" / "algorithm-pwb" / "data" / "cf_data" / "cf" / "seed-scripts" / "dossier_seed.sql"
OUT_DIR = ROOT / "sql"
OUT_SCHEMA = OUT_DIR / "p1p_10_dossier_schema_mysql.sql"
OUT_SEED = OUT_DIR / "p1p_11_dossier_seed_mysql.sql"
OUT_VERIFY = OUT_DIR / "p1p_12_dossier_verify.sql"

TABLE_PREFIX = "p1p_dossier_"
MYSQL_DATABASE = "ry-cloud"
MAX_IDENTIFIER_LEN = 64


@dataclass(frozen=True)
class Column:
    name: str
    raw_definition: str


@dataclass(frozen=True)
class Table:
    name: str
    columns: list[Column]


@dataclass(frozen=True)
class ForeignKey:
    table: str
    name: str
    columns: list[str]
    ref_table: str
    ref_columns: list[str]


@dataclass(frozen=True)
class Index:
    unique: bool
    name: str
    table: str
    columns: list[str]


def q(identifier: str) -> str:
    return f"`{identifier}`"


def physical_table(logical_table: str) -> str:
    return f"{TABLE_PREFIX}{logical_table}"


def sql_string(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def write_text_lf(path: Path, text: str) -> None:
    with path.open("w", encoding="utf-8", newline="\n") as file:
        file.write(text)


def limited_identifier(candidate: str, used: set[str]) -> str:
    if len(candidate) <= MAX_IDENTIFIER_LEN and candidate not in used:
        used.add(candidate)
        return candidate

    digest = hashlib.sha1(candidate.encode("utf-8")).hexdigest()[:8]
    prefix = candidate[: MAX_IDENTIFIER_LEN - len(digest) - 1]
    shortened = f"{prefix}_{digest}"
    counter = 1
    while shortened in used:
        suffix = f"_{digest[:6]}_{counter}"
        shortened = candidate[: MAX_IDENTIFIER_LEN - len(suffix)] + suffix
        counter += 1
    used.add(shortened)
    return shortened


def strip_leading_token(name: str, token: str) -> str:
    return name[len(token) :] if name.startswith(token) else name


def prefixed_fk_name(source_name: str, used: set[str]) -> str:
    base = "fk_p1p_dossier_" + strip_leading_token(source_name, "fk_")
    return limited_identifier(base, used)


def prefixed_index_name(source_name: str, unique: bool, used_for_table: set[str]) -> str:
    source_prefix = "uq_" if unique else "idx_"
    target_prefix = "uq_p1p_dossier_" if unique else "idx_p1p_dossier_"
    base = target_prefix + strip_leading_token(source_name, source_prefix)
    return limited_identifier(base, used_for_table)


def parse_quoted_names(value: str) -> list[str]:
    return re.findall(r'"([^"]+)"', value)


def parse_tables(schema_sql: str) -> list[Table]:
    tables: list[Table] = []
    for match in re.finditer(r'CREATE TABLE IF NOT EXISTS "([^"]+)" \((.*?)\);', schema_sql, flags=re.S):
        table_name = match.group(1)
        body = match.group(2)
        columns: list[Column] = []
        for line in body.splitlines():
            line = line.strip()
            if not line:
                continue
            line = line.rstrip(",")
            column_match = re.match(r'"([^"]+)"\s+(.+)$', line)
            if not column_match:
                raise ValueError(f"Unsupported column line in {table_name}: {line}")
            columns.append(Column(column_match.group(1), column_match.group(2).strip()))
        tables.append(Table(table_name, columns))
    return tables


def parse_comments(schema_sql: str) -> tuple[dict[str, str], dict[tuple[str, str], str]]:
    table_comments: dict[str, str] = {}
    column_comments: dict[tuple[str, str], str] = {}

    for match in re.finditer(r'COMMENT ON TABLE "([^"]+)" IS \'((?:[^\']|\'\')*)\';', schema_sql):
        table_comments[match.group(1)] = match.group(2).replace("''", "'")

    for match in re.finditer(r'COMMENT ON COLUMN "([^"]+)"\."([^"]+)" IS \'((?:[^\']|\'\')*)\';', schema_sql):
        column_comments[(match.group(1), match.group(2))] = match.group(3).replace("''", "'")

    return table_comments, column_comments


def parse_foreign_keys(schema_sql: str) -> list[ForeignKey]:
    foreign_keys: list[ForeignKey] = []
    pattern = (
        r'ALTER TABLE "([^"]+)" ADD CONSTRAINT "([^"]+)" '
        r"FOREIGN KEY \((.*?)\) REFERENCES "
        r'"([^"]+)" \((.*?)\);'
    )
    for match in re.finditer(pattern, schema_sql):
        foreign_keys.append(
            ForeignKey(
                table=match.group(1),
                name=match.group(2),
                columns=parse_quoted_names(match.group(3)),
                ref_table=match.group(4),
                ref_columns=parse_quoted_names(match.group(5)),
            )
        )
    return foreign_keys


def parse_indexes(schema_sql: str) -> list[Index]:
    indexes: list[Index] = []
    pattern = r'CREATE (UNIQUE )?INDEX IF NOT EXISTS "([^"]+)" ON "([^"]+)" \((.*?)\);'
    for match in re.finditer(pattern, schema_sql):
        indexes.append(
            Index(
                unique=bool(match.group(1)),
                name=match.group(2),
                table=match.group(3),
                columns=parse_quoted_names(match.group(4)),
            )
        )
    return indexes


def convert_column_definition(table: str, column: Column, fk_columns: set[tuple[str, str]], column_comments: dict[tuple[str, str], str]) -> str:
    raw = column.raw_definition
    upper = raw.upper()

    if upper == "SERIAL PRIMARY KEY":
        converted = "bigint not null auto_increment primary key"
    else:
        type_match = re.match(r"(VARCHAR\(\d+\)|DECIMAL\(\d+,\d+\)|INTEGER|BOOLEAN|TIMESTAMP|TEXT|DATE)(?:\s+(.*)|$)", raw, flags=re.I)
        if not type_match:
            raise ValueError(f"Unsupported type for {table}.{column.name}: {raw}")

        source_type = type_match.group(1).upper()
        modifiers = (type_match.group(2) or "").strip()

        if source_type == "INTEGER":
            mysql_type = "bigint" if (table, column.name) in fk_columns else "int"
        elif source_type == "BOOLEAN":
            mysql_type = "tinyint(1)"
        elif source_type == "TIMESTAMP":
            mysql_type = "datetime"
        else:
            mysql_type = source_type.lower()

        converted = mysql_type
        if modifiers:
            converted += " " + modifiers.lower()

    comment = column_comments.get((table, column.name))
    if comment:
        converted += f" comment {sql_string(comment)}"

    return f"  {q(column.name)} {converted}"


def convert_schema(
    tables: list[Table],
    table_comments: dict[str, str],
    column_comments: dict[tuple[str, str], str],
    foreign_keys: list[ForeignKey],
    indexes: list[Index],
) -> str:
    fk_columns = {(fk.table, column) for fk in foreign_keys for column in fk.columns}
    fk_names: set[str] = set()
    index_names_by_table: dict[str, set[str]] = {}

    lines: list[str] = [
        "-- Generated from algorithm-service/algorithm-pwb/data/cf_data/cf/init-scripts/06_cf_dossier_schema.sql",
        "-- Target: RuoYi-Cloud MySQL 8.0.46, database `ry-cloud`.",
        "-- Scope: only CF dossier target tables with prefix `p1p_dossier_`.",
        "",
        f"use {q(MYSQL_DATABASE)};",
        "set names utf8mb4;",
        "set foreign_key_checks = 0;",
        "",
        "-- Drop only dossier target tables for this subject.",
    ]

    for table in reversed(tables):
        lines.append(f"drop table if exists {q(physical_table(table.name))};")

    lines.extend(["", "-- Tables"])
    for table in tables:
        lines.append(f"create table {q(physical_table(table.name))} (")
        column_lines = [
            convert_column_definition(table.name, column, fk_columns, column_comments)
            for column in table.columns
        ]
        lines.append(",\n".join(column_lines))
        table_comment = table_comments.get(table.name, f"CF dossier target table: {table.name}")
        lines.append(
            f") engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment={sql_string(table_comment)};"
        )
        lines.append("")

    lines.append("-- Foreign keys")
    for fk in foreign_keys:
        fk_name = prefixed_fk_name(fk.name, fk_names)
        local_columns = ", ".join(q(column) for column in fk.columns)
        ref_columns = ", ".join(q(column) for column in fk.ref_columns)
        lines.append(
            f"alter table {q(physical_table(fk.table))} "
            f"add constraint {q(fk_name)} foreign key ({local_columns}) "
            f"references {q(physical_table(fk.ref_table))} ({ref_columns});"
        )

    lines.extend(["", "-- Source-defined indexes"])
    for index in indexes:
        used_for_table = index_names_by_table.setdefault(index.table, set())
        index_name = prefixed_index_name(index.name, index.unique, used_for_table)
        index_type = "unique index" if index.unique else "index"
        columns = ", ".join(q(column) for column in index.columns)
        lines.append(f"create {index_type} {q(index_name)} on {q(physical_table(index.table))} ({columns});")

    lines.extend(["", "set foreign_key_checks = 1;", ""])
    return "\n".join(lines)


def replace_bool_literals_outside_strings(sql: str) -> str:
    result: list[str] = []
    i = 0
    in_string = False
    while i < len(sql):
        char = sql[i]
        if char == "'":
            result.append(char)
            if in_string and i + 1 < len(sql) and sql[i + 1] == "'":
                result.append(sql[i + 1])
                i += 2
                continue
            in_string = not in_string
            i += 1
            continue
        if not in_string and sql.startswith("TRUE", i) and is_word_boundary(sql, i - 1) and is_word_boundary(sql, i + 4):
            result.append("1")
            i += 4
            continue
        if not in_string and sql.startswith("FALSE", i) and is_word_boundary(sql, i - 1) and is_word_boundary(sql, i + 5):
            result.append("0")
            i += 5
            continue
        result.append(char)
        i += 1
    return "".join(result)


def is_word_boundary(value: str, index: int) -> bool:
    if index < 0 or index >= len(value):
        return True
    return not (value[index].isalnum() or value[index] == "_")


def convert_insert(line: str, table_names: set[str]) -> str:
    match = re.match(r'INSERT INTO "([^"]+)" \((.*?)\) VALUES (.*);$', line)
    if not match:
        raise ValueError(f"Unsupported seed INSERT line: {line[:160]}")

    table_name = match.group(1)
    if table_name not in table_names:
        raise ValueError(f"Seed references unknown dossier table: {table_name}")

    columns = ", ".join(q(column) for column in parse_quoted_names(match.group(2)))
    values = replace_bool_literals_outside_strings(match.group(3))
    return f"insert into {q(physical_table(table_name))} ({columns}) values {values};"


def count_seed_inserts(seed_sql: str, table_names: list[str]) -> dict[str, int]:
    counts = {table_name: 0 for table_name in table_names}
    for match in re.finditer(r'^INSERT INTO "([^"]+)" ', seed_sql, flags=re.M):
        table_name = match.group(1)
        if table_name not in counts:
            raise ValueError(f"Seed references unknown dossier table: {table_name}")
        counts[table_name] += 1
    return counts


def convert_seed(seed_sql: str, table_names: list[str]) -> str:
    table_name_set = set(table_names)
    lines: list[str] = [
        "-- Generated from algorithm-service/algorithm-pwb/data/cf_data/cf/seed-scripts/dossier_seed.sql",
        "-- Target: RuoYi-Cloud MySQL 8.0.46, database `ry-cloud`.",
        "-- Scope: only CF dossier target tables with prefix `p1p_dossier_`.",
        "",
        f"use {q(MYSQL_DATABASE)};",
        "set names utf8mb4;",
        "set foreign_key_checks = 0;",
        "",
        "-- Reset target data. DELETE is used instead of TRUNCATE because MySQL InnoDB rejects",
        "-- TRUNCATE on tables participating in parent-child foreign key relationships.",
    ]

    for table_name in reversed(table_names):
        lines.append(f"delete from {q(physical_table(table_name))};")

    lines.extend(["", "-- Seed data"])
    for raw_line in seed_sql.splitlines():
        line = raw_line.strip()
        if not line or line.startswith("SET ") or line.startswith("SELECT setval(") or line in {"BEGIN;", "COMMIT;"}:
            continue
        if line.startswith("TRUNCATE "):
            continue
        if line.startswith("INSERT INTO "):
            lines.append(convert_insert(line, table_name_set))
            continue
        raise ValueError(f"Unsupported seed line: {line[:160]}")

    lines.extend(["", "set foreign_key_checks = 1;", ""])
    return "\n".join(lines)


def convert_verify(table_names: list[str], foreign_key_count: int, index_count: int, seed_counts: dict[str, int]) -> str:
    total_seed_rows = sum(seed_counts.values())
    total_count_lines = [
        f"  select count(*) as actual_count from {q(physical_table(table_name))}"
        for table_name in table_names
    ]
    per_table_count_lines = [
        f"  select '{physical_table(table_name)}_rows' as check_item, "
        f"(select count(*) from {q(physical_table(table_name))}) as actual_count, "
        f"{seed_counts[table_name]} as expected_count"
        for table_name in table_names
    ]

    lines: list[str] = [
        "-- Verification queries for p1p dossier target tables.",
        "-- Run after p1p_10_dossier_schema_mysql.sql and p1p_11_dossier_seed_mysql.sql.",
        "",
        f"use {q(MYSQL_DATABASE)};",
        "set names utf8mb4;",
        "",
        "select",
        "  'table_count' as check_item,",
        "  count(*) as actual_count,",
        f"  {len(table_names)} as expected_count,",
        f"  if(count(*) = {len(table_names)}, 'PASS', 'CHECK') as status",
        "from information_schema.tables",
        "where table_schema = database()",
        "  and table_name regexp '^p1p_dossier_';",
        "",
        "select",
        "  'foreign_key_count' as check_item,",
        "  count(*) as actual_count,",
        f"  {foreign_key_count} as expected_count,",
        f"  if(count(*) = {foreign_key_count}, 'PASS', 'CHECK') as status",
        "from information_schema.table_constraints",
        "where constraint_schema = database()",
        "  and table_name regexp '^p1p_dossier_'",
        "  and constraint_type = 'FOREIGN KEY';",
        "",
        "select",
        "  'source_defined_index_count' as check_item,",
        "  count(distinct concat(table_name, '.', index_name)) as actual_count,",
        f"  {index_count} as expected_count,",
        f"  if(count(distinct concat(table_name, '.', index_name)) = {index_count}, 'PASS', 'CHECK') as status",
        "from information_schema.statistics",
        "where table_schema = database()",
        "  and table_name regexp '^p1p_dossier_'",
        "  and index_name regexp '^(idx|uq)_p1p_dossier_';",
        "",
        "select",
        "  'seed_total_rows' as check_item,",
        "  sum(actual_count) as actual_count,",
        f"  {total_seed_rows} as expected_count,",
        f"  if(sum(actual_count) = {total_seed_rows}, 'PASS', 'CHECK') as status",
        "from (",
        "\n  union all\n".join(total_count_lines),
        ") row_counts;",
        "",
        "select",
        "  check_item,",
        "  actual_count,",
        "  expected_count,",
        "  if(actual_count = expected_count, 'PASS', 'CHECK') as status",
        "from (",
        "\n  union all\n".join(per_table_count_lines),
        ") row_counts",
        "order by check_item;",
        "",
    ]
    return "\n".join(lines)


def main() -> None:
    schema_sql = SOURCE_SCHEMA.read_text(encoding="utf-8-sig")
    seed_sql = SOURCE_SEED.read_text(encoding="utf-8-sig")

    tables = parse_tables(schema_sql)
    table_comments, column_comments = parse_comments(schema_sql)
    foreign_keys = parse_foreign_keys(schema_sql)
    indexes = parse_indexes(schema_sql)
    seed_counts = count_seed_inserts(seed_sql, table_names=[table.name for table in tables])

    if len(tables) != 48:
        raise ValueError(f"Expected 48 tables, parsed {len(tables)}")
    if len(foreign_keys) != 134:
        raise ValueError(f"Expected 134 foreign keys, parsed {len(foreign_keys)}")
    if len(indexes) != 100:
        raise ValueError(f"Expected 100 indexes, parsed {len(indexes)}")

    table_names = [table.name for table in tables]
    write_text_lf(
        OUT_SCHEMA,
        convert_schema(tables, table_comments, column_comments, foreign_keys, indexes),
    )
    write_text_lf(OUT_SEED, convert_seed(seed_sql, table_names))
    write_text_lf(OUT_VERIFY, convert_verify(table_names, len(foreign_keys), len(indexes), seed_counts))

    print(f"generated: {OUT_SCHEMA.relative_to(ROOT)}")
    print(f"generated: {OUT_SEED.relative_to(ROOT)}")
    print(f"generated: {OUT_VERIFY.relative_to(ROOT)}")
    print(f"tables={len(tables)} foreign_keys={len(foreign_keys)} indexes={len(indexes)}")


if __name__ == "__main__":
    main()
