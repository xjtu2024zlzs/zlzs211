from __future__ import annotations

import hashlib
import re
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
SOURCE_ROOT = ROOT / "algorithm-service" / "algorithm-pwb" / "data" / "cf_data" / "cf"
OUT_DIR = ROOT / "sql" / "projectp"

MYSQL_DATABASE = "ry-cloud"
TABLE_PREFIX_BASE = "p1p_ext_"
MAX_IDENTIFIER_LEN = 64


@dataclass(frozen=True)
class System:
    key: str
    label: str
    schema_key: str
    port: int
    schema_file: str
    seed_file: str


@dataclass(frozen=True)
class Column:
    name: str
    raw_definition: str


@dataclass(frozen=True)
class Table:
    system: System
    name: str
    columns: list[Column]


@dataclass(frozen=True)
class ForeignKey:
    system: System
    table: str
    name: str
    columns: list[str]
    ref_table: str
    ref_columns: list[str]


@dataclass(frozen=True)
class Index:
    system: System
    unique: bool
    name: str
    table: str
    columns: list[str]


SYSTEMS = [
    System("plm", "PLM", "cf_plm", 9711, "01_cf_plm_schema.sql", "plm_seed.sql"),
    System("erp", "ERP", "cf_erp", 9712, "02_cf_erp_schema.sql", "erp_seed.sql"),
    System("mes", "MES", "cf_mes", 9713, "03_cf_mes_schema.sql", "mes_seed.sql"),
    System("qms", "QMS", "cf_qms", 9714, "04_cf_qms_schema.sql", "qms_seed.sql"),
    System("mro", "MRO", "cf_mro", 9715, "05_cf_mro_schema.sql", "mro_seed.sql"),
]


def q(identifier: str) -> str:
    return f"`{identifier}`"


def sql_string(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def table_prefix(system: System) -> str:
    return f"{TABLE_PREFIX_BASE}{system.key}_"


def physical_table(system: System, logical_table: str) -> str:
    return f"{table_prefix(system)}{logical_table}"


def write_text_lf(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
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


def prefixed_fk_name(system: System, source_name: str, used: set[str]) -> str:
    base = f"fk_{table_prefix(system)}" + strip_leading_token(source_name, "fk_")
    return limited_identifier(base, used)


def prefixed_index_name(system: System, source_name: str, unique: bool, used_for_table: set[str]) -> str:
    source_prefix = "uq_" if unique else "idx_"
    target_prefix = f"{'uq' if unique else 'idx'}_{table_prefix(system)}"
    base = target_prefix + strip_leading_token(source_name, source_prefix)
    return limited_identifier(base, used_for_table)


def parse_quoted_names(value: str) -> list[str]:
    return re.findall(r'"([^"]+)"', value)


def parse_tables(system: System, schema_sql: str) -> list[Table]:
    tables: list[Table] = []
    for match in re.finditer(r'CREATE TABLE IF NOT EXISTS "([^"]+)" \((.*?)\);', schema_sql, flags=re.S):
        table_name = match.group(1)
        body = match.group(2)
        columns: list[Column] = []
        for line in body.splitlines():
            line = line.strip().rstrip(",")
            if not line:
                continue
            column_match = re.match(r'"([^"]+)"\s+(.+)$', line)
            if not column_match:
                raise ValueError(f"Unsupported column line in {system.key}.{table_name}: {line}")
            columns.append(Column(column_match.group(1), column_match.group(2).strip()))
        tables.append(Table(system, table_name, columns))
    return tables


def parse_comments(schema_sql: str) -> tuple[dict[str, str], dict[tuple[str, str], str]]:
    table_comments: dict[str, str] = {}
    column_comments: dict[tuple[str, str], str] = {}

    for match in re.finditer(r'COMMENT ON TABLE "([^"]+)" IS \'((?:[^\']|\'\')*)\';', schema_sql):
        table_comments[match.group(1)] = match.group(2).replace("''", "'")

    for match in re.finditer(r'COMMENT ON COLUMN "([^"]+)"\."([^"]+)" IS \'((?:[^\']|\'\')*)\';', schema_sql):
        column_comments[(match.group(1), match.group(2))] = match.group(3).replace("''", "'")

    return table_comments, column_comments


def parse_foreign_keys(system: System, schema_sql: str) -> list[ForeignKey]:
    foreign_keys: list[ForeignKey] = []
    pattern = (
        r'ALTER TABLE "([^"]+)" ADD CONSTRAINT "([^"]+)" '
        r"FOREIGN KEY \((.*?)\) REFERENCES "
        r'"([^"]+)" \((.*?)\);'
    )
    for match in re.finditer(pattern, schema_sql):
        foreign_keys.append(
            ForeignKey(
                system=system,
                table=match.group(1),
                name=match.group(2),
                columns=parse_quoted_names(match.group(3)),
                ref_table=match.group(4),
                ref_columns=parse_quoted_names(match.group(5)),
            )
        )
    return foreign_keys


def parse_indexes(system: System, schema_sql: str) -> list[Index]:
    indexes: list[Index] = []
    pattern = r'CREATE (UNIQUE )?INDEX IF NOT EXISTS "([^"]+)" ON "([^"]+)" \((.*?)\);'
    for match in re.finditer(pattern, schema_sql):
        indexes.append(
            Index(
                system=system,
                unique=bool(match.group(1)),
                name=match.group(2),
                table=match.group(3),
                columns=parse_quoted_names(match.group(4)),
            )
        )
    return indexes


def convert_column_definition(table: Table, column: Column, fk_columns: set[tuple[str, str, str]], column_comments: dict[tuple[str, str], str]) -> str:
    raw = column.raw_definition
    upper = raw.upper()

    if upper == "SERIAL PRIMARY KEY":
        converted = "bigint not null auto_increment primary key"
    else:
        type_match = re.match(r"(VARCHAR\(\d+\)|DECIMAL\(\d+,\d+\)|INTEGER|BOOLEAN|TIMESTAMP|TEXT|DATE)(?:\s+(.*)|$)", raw, flags=re.I)
        if not type_match:
            raise ValueError(f"Unsupported type for {table.system.key}.{table.name}.{column.name}: {raw}")

        source_type = type_match.group(1).upper()
        modifiers = (type_match.group(2) or "").strip()

        if source_type == "INTEGER":
            mysql_type = "bigint" if (table.system.key, table.name, column.name) in fk_columns else "int"
        elif source_type == "BOOLEAN":
            mysql_type = "tinyint(1)"
        elif source_type == "TIMESTAMP":
            mysql_type = "datetime"
        else:
            mysql_type = source_type.lower()

        converted = mysql_type
        if modifiers:
            converted += " " + modifiers.lower()

    comment = column_comments.get((table.name, column.name))
    if comment:
        converted += f" comment {sql_string(comment)}"

    return f"  {q(column.name)} {converted}"


def load_all() -> tuple[list[Table], dict[str, dict[str, str]], dict[str, dict[tuple[str, str], str]], list[ForeignKey], list[Index]]:
    all_tables: list[Table] = []
    table_comments_by_system: dict[str, dict[str, str]] = {}
    column_comments_by_system: dict[str, dict[tuple[str, str], str]] = {}
    all_foreign_keys: list[ForeignKey] = []
    all_indexes: list[Index] = []

    for system in SYSTEMS:
        schema_sql = (SOURCE_ROOT / "init-scripts" / system.schema_file).read_text(encoding="utf-8")
        table_comments, column_comments = parse_comments(schema_sql)
        all_tables.extend(parse_tables(system, schema_sql))
        table_comments_by_system[system.key] = table_comments
        column_comments_by_system[system.key] = column_comments
        all_foreign_keys.extend(parse_foreign_keys(system, schema_sql))
        all_indexes.extend(parse_indexes(system, schema_sql))

    return all_tables, table_comments_by_system, column_comments_by_system, all_foreign_keys, all_indexes


def convert_schema() -> str:
    tables, table_comments_by_system, column_comments_by_system, foreign_keys, indexes = load_all()
    fk_columns = {(fk.system.key, fk.table, column) for fk in foreign_keys for column in fk.columns}
    fk_names: set[str] = set()
    index_names_by_physical_table: dict[str, set[str]] = {}

    lines: list[str] = [
        "-- projectp CF external source simulation schema.",
        "-- Generated from algorithm-service/algorithm-pwb/data/cf_data/cf/init-scripts/01-05_cf_*_schema.sql",
        "-- Target: RuoYi-Cloud MySQL 8.0.46, database `ry-cloud`.",
        "-- Scope: only simulated external source tables with prefix `p1p_ext_`.",
        "",
        f"use {q(MYSQL_DATABASE)};",
        "set names utf8mb4;",
        "set foreign_key_checks = 0;",
        "",
        "-- Drop only projectp simulated external source tables.",
    ]

    for table in reversed(tables):
        lines.append(f"drop table if exists {q(physical_table(table.system, table.name))};")

    lines.extend(["", "-- Tables"])
    for table in tables:
        lines.append(f"-- {table.system.label}: {table.name}")
        lines.append(f"create table {q(physical_table(table.system, table.name))} (")
        column_comments = column_comments_by_system[table.system.key]
        column_lines = [
            convert_column_definition(table, column, fk_columns, column_comments)
            for column in table.columns
        ]
        lines.append(",\n".join(column_lines))
        table_comment = table_comments_by_system[table.system.key].get(
            table.name,
            f"projectp simulated {table.system.label} source table: {table.name}",
        )
        lines.append(
            f") engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment={sql_string(table_comment)};"
        )
        lines.append("")

    lines.append("-- Foreign keys")
    for fk in foreign_keys:
        fk_name = prefixed_fk_name(fk.system, fk.name, fk_names)
        local_columns = ", ".join(q(column) for column in fk.columns)
        ref_columns = ", ".join(q(column) for column in fk.ref_columns)
        lines.append(
            f"alter table {q(physical_table(fk.system, fk.table))} "
            f"add constraint {q(fk_name)} foreign key ({local_columns}) "
            f"references {q(physical_table(fk.system, fk.ref_table))} ({ref_columns});"
        )

    lines.extend(["", "-- Source-defined indexes"])
    for index in indexes:
        physical = physical_table(index.system, index.table)
        used_for_table = index_names_by_physical_table.setdefault(physical, set())
        index_name = prefixed_index_name(index.system, index.name, index.unique, used_for_table)
        unique_sql = "unique " if index.unique else ""
        columns_sql = ", ".join(q(column) for column in index.columns)
        lines.append(f"create {unique_sql}index {q(index_name)} on {q(physical)} ({columns_sql});")

    lines.extend(["", "set foreign_key_checks = 1;", ""])
    return "\n".join(lines)


def convert_bool_literals(value: str) -> str:
    value = re.sub(r"\bTRUE\b", "1", value, flags=re.I)
    value = re.sub(r"\bFALSE\b", "0", value, flags=re.I)
    return value


def convert_seed() -> str:
    tables, *_ = load_all()
    lines: list[str] = [
        "-- projectp CF external source simulation seed data.",
        "-- Generated from algorithm-service/algorithm-pwb/data/cf_data/cf/seed-scripts/*_seed.sql",
        "-- Target: RuoYi-Cloud MySQL 8.0.46, database `ry-cloud`.",
        "",
        f"use {q(MYSQL_DATABASE)};",
        "set names utf8mb4;",
        "set foreign_key_checks = 0;",
        "",
        "-- Truncate only projectp simulated external source tables.",
    ]

    for table in reversed(tables):
        lines.append(f"truncate table {q(physical_table(table.system, table.name))};")

    lines.extend(["", "start transaction;"])
    insert_pattern = re.compile(r'INSERT INTO "([^"]+)" \((.*?)\) VALUES (.*);', flags=re.I)

    for system in SYSTEMS:
        seed_sql = (SOURCE_ROOT / "seed-scripts" / system.seed_file).read_text(encoding="utf-8")
        lines.extend(["", f"-- {system.label} seed data"])
        for source_line in seed_sql.splitlines():
            stripped = source_line.strip()
            if not stripped or stripped.upper().startswith(("SET ", "BEGIN", "COMMIT", "TRUNCATE", "SELECT SETVAL")):
                continue
            match = insert_pattern.match(stripped)
            if not match:
                raise ValueError(f"Unsupported seed line in {system.seed_file}: {stripped[:160]}")
            table_name = match.group(1)
            columns = ", ".join(q(column) for column in parse_quoted_names(match.group(2)))
            values = convert_bool_literals(match.group(3))
            lines.append(f"insert into {q(physical_table(system, table_name))} ({columns}) values {values};")

    lines.extend(["commit;", "set foreign_key_checks = 1;", ""])
    return "\n".join(lines)


def datasource_seed() -> str:
    lines = [
        "-- projectp API Pull datasource seed.",
        "-- Default base_url uses localhost. Replace 127.0.0.1 with the server IP when RuoYi runs on another host.",
        "",
        f"use {q(MYSQL_DATABASE)};",
        "set names utf8mb4;",
        "",
    ]
    for system in SYSTEMS:
        datasource_name = f"CF {system.label} API Pull"
        var_name = f"@projectp_{system.key}_datasource_id"
        base_url = f"http://127.0.0.1:{system.port}"
        extra_config = (
            f"json_object('schemaKey', {sql_string(system.schema_key)}, "
            f"'sourceLabel', {sql_string(system.label)}, "
            f"'simulatedStorage', 'mysql', "
            f"'physicalTablePrefix', {sql_string(table_prefix(system))})"
        )
        lines.extend(
            [
                f"-- {system.label}",
                "insert into `p1p_datasource` (",
                "  `datasource_name`, `access_mode`, `use_status`, `connection_status`,",
                "  `status`, `del_flag`, `create_by`, `create_time`, `remark`",
                ") values (",
                f"  {sql_string(datasource_name)}, 'api_pull', 'enabled', 'untested',",
                f"  '0', '0', 'projectp', now(), {sql_string('projectp simulated external ' + system.label + ' source system')}",
                ") on duplicate key update",
                "  `access_mode` = values(`access_mode`),",
                "  `use_status` = values(`use_status`),",
                "  `connection_status` = 'untested',",
                "  `update_by` = 'projectp',",
                "  `update_time` = now(),",
                "  `remark` = values(`remark`);",
                f"set {var_name} = (select `datasource_id` from `p1p_datasource` where `datasource_name` = {sql_string(datasource_name)} limit 1);",
                "insert into `p1p_api_pull_datasource` (",
                "  `datasource_id`, `base_url`, `pull_endpoint`, `schema_endpoint`, `request_method`,",
                "  `auth_type`, `health_endpoint`, `headers_config`, `extra_config`, `create_time`, `remark`",
                ") values (",
                f"  {var_name}, {sql_string(base_url)}, '/api/pull', '/api/schema', 'POST',",
                f"  'none', '/api/health', json_object(), {extra_config}, now(),",
                f"  {sql_string('projectp ' + system.label + ' API Pull adapter on port ' + str(system.port))}",
                ") on duplicate key update",
                "  `base_url` = values(`base_url`),",
                "  `pull_endpoint` = values(`pull_endpoint`),",
                "  `schema_endpoint` = values(`schema_endpoint`),",
                "  `request_method` = values(`request_method`),",
                "  `auth_type` = values(`auth_type`),",
                "  `health_endpoint` = values(`health_endpoint`),",
                "  `headers_config` = values(`headers_config`),",
                "  `extra_config` = values(`extra_config`),",
                "  `update_time` = now(),",
                "  `remark` = values(`remark`);",
                "",
            ]
        )
    return "\n".join(lines)


def verify_sql() -> str:
    tables, *_ = load_all()
    tables_by_system: dict[str, list[Table]] = {}
    for table in tables:
        tables_by_system.setdefault(table.system.key, []).append(table)

    expected_rows = {
        "plm": 2200,
        "erp": 1600,
        "mes": 2400,
        "qms": 1800,
        "mro": 1000,
    }
    lines = [
        "-- projectp verification SQL.",
        "",
        f"use {q(MYSQL_DATABASE)};",
        "set names utf8mb4;",
        "",
        "select count(*) as p1p_ext_table_count",
        "from information_schema.tables",
        f"where table_schema = {sql_string(MYSQL_DATABASE)}",
        "  and table_name like 'p1p_ext\\_%' escape '\\\\';",
        "",
        "select count(*) as p1p_ext_fk_count",
        "from information_schema.referential_constraints",
        f"where constraint_schema = {sql_string(MYSQL_DATABASE)}",
        "  and constraint_name like 'fk\\_p1p\\_ext\\_%' escape '\\\\';",
        "",
        "select count(*) as p1p_ext_non_primary_index_count",
        "from information_schema.statistics",
        f"where table_schema = {sql_string(MYSQL_DATABASE)}",
        "  and table_name like 'p1p_ext\\_%' escape '\\\\'",
        "  and index_name <> 'PRIMARY';",
        "",
    ]

    for system in SYSTEMS:
        lines.extend(
            [
                f"select {sql_string(system.label)} as source_system, count(*) as table_count",
                "from information_schema.tables",
                f"where table_schema = {sql_string(MYSQL_DATABASE)}",
                f"  and table_name like {sql_string(table_prefix(system) + '%')};",
                "",
            ]
        )

    row_count_selects = []
    for system in SYSTEMS:
        parts = [f"(select count(*) from {q(physical_table(system, table.name))})" for table in tables_by_system[system.key]]
        row_count_selects.append(
            f"select {sql_string(system.label)} as source_system, {' + '.join(parts)} as row_count, {expected_rows[system.key]} as expected_row_count"
        )
    lines.append("\nunion all\n".join(row_count_selects) + ";")
    lines.append("")

    datasource_names = ", ".join(sql_string(f"CF {system.label} API Pull") for system in SYSTEMS)
    lines.extend(
        [
            "select d.datasource_name, d.access_mode, a.base_url, a.schema_endpoint, a.pull_endpoint, a.health_endpoint",
            "from p1p_datasource d",
            "join p1p_api_pull_datasource a on a.datasource_id = d.datasource_id",
            f"where d.datasource_name in ({datasource_names})",
            "order by d.datasource_name;",
            "",
        ]
    )
    return "\n".join(lines)


def readme() -> str:
    return """# projectp SQL 执行说明

本目录存放 project1 使用的 5 个 CF 外部源系统模拟库 SQL。MySQL 版本按 `8.0.46` 处理，物理表统一放入 `ry-cloud`，并使用 `p1p_ext_` 前缀隔离。

## 文件说明

| 文件 | 用途 |
| --- | --- |
| `00_projectp_ext_schema_mysql8.sql` | 创建 PLM/ERP/MES/QMS/MRO 共 45 张模拟外部源系统表 |
| `01_projectp_ext_seed_mysql8.sql` | 导入 9000 条模拟源系统数据 |
| `02_projectp_api_pull_datasource_seed_mysql8.sql` | 写入或更新 5 个 API Pull 数据源配置 |
| `03_projectp_verify.sql` | 验证表数量、行数、外键、索引和 API Pull 数据源配置 |

## 首次执行顺序

在已经导入若依基础库和 project1 业务表后执行：

```text
1. 00_projectp_ext_schema_mysql8.sql
2. 01_projectp_ext_seed_mysql8.sql
3. 02_projectp_api_pull_datasource_seed_mysql8.sql
4. 03_projectp_verify.sql
```

## 注意事项

- 建表脚本只会 drop `p1p_ext_%` 表，不会 drop `p1p_dossier_%`、`p1p_datasource`、`p1p_match_%`、`sys_%`、`QRTZ_%`。
- `02_projectp_api_pull_datasource_seed_mysql8.sql` 默认写入 `http://127.0.0.1:9711-9715`。如果 projectp 服务和 RuoYi 后端不在同一台服务器，请先把 `127.0.0.1` 替换为 projectp 服务所在服务器 IP。
- 这些表只用于模拟外部企业源库。RuoYi 后端仍应通过 API Pull 接口读取数据，不直接读取 `p1p_ext_%` 表。
"""


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    write_text_lf(OUT_DIR / "00_projectp_ext_schema_mysql8.sql", convert_schema())
    write_text_lf(OUT_DIR / "01_projectp_ext_seed_mysql8.sql", convert_seed())
    write_text_lf(OUT_DIR / "02_projectp_api_pull_datasource_seed_mysql8.sql", datasource_seed())
    write_text_lf(OUT_DIR / "03_projectp_verify.sql", verify_sql())
    write_text_lf(OUT_DIR / "README.md", readme())


if __name__ == "__main__":
    main()
