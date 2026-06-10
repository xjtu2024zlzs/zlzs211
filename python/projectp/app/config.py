from __future__ import annotations

import os
from dataclasses import dataclass
from urllib.parse import quote_plus


@dataclass(frozen=True)
class SystemConfig:
    key: str
    source_label: str
    schema_key: str
    api_port: int
    description: str


SYSTEMS: dict[str, SystemConfig] = {
    "plm": SystemConfig("plm", "PLM", "cf_plm", 9711, "Product lifecycle management source system"),
    "erp": SystemConfig("erp", "ERP", "cf_erp", 9712, "Enterprise resource planning source system"),
    "mes": SystemConfig("mes", "MES", "cf_mes", 9713, "Manufacturing execution source system"),
    "qms": SystemConfig("qms", "QMS", "cf_qms", 9714, "Quality management source system"),
    "mro": SystemConfig("mro", "MRO", "cf_mro", 9715, "Maintenance, repair and overhaul source system"),
}


def get_system_config(system_key: str | None = None) -> SystemConfig:
    key = (system_key or os.getenv("PROJECTP_SYSTEM_KEY") or os.getenv("CF_SYSTEM_KEY") or "plm").strip().lower()
    aliases = {config.source_label.lower(): config.key for config in SYSTEMS.values()}
    key = aliases.get(key, key)
    if key not in SYSTEMS:
        allowed = ", ".join(sorted(SYSTEMS))
        raise ValueError(f"Unsupported PROJECTP_SYSTEM_KEY={key!r}. Allowed values: {allowed}")
    return SYSTEMS[key]


def get_database_connection_info() -> dict[str, object]:
    return {
        "host": os.getenv("PROJECTP_MYSQL_HOST", os.getenv("RUOYI_MYSQL_HOST", "127.0.0.1")),
        "port": int(os.getenv("PROJECTP_MYSQL_PORT", os.getenv("RUOYI_MYSQL_PORT", "3306"))),
        "database": os.getenv("PROJECTP_MYSQL_DATABASE", os.getenv("RUOYI_MYSQL_DATABASE", "ry-cloud")),
        "user": os.getenv("PROJECTP_MYSQL_USER", os.getenv("RUOYI_MYSQL_USER", "root")),
        "password": os.getenv("PROJECTP_MYSQL_PASSWORD", os.getenv("RUOYI_MYSQL_PASSWORD", "password")),
    }


def get_table_prefix(config: SystemConfig) -> str:
    base = os.getenv("PROJECTP_TABLE_PREFIX_BASE", "p1p_ext_")
    return f"{base}{config.key}_"


def build_database_url() -> str:
    info = get_database_connection_info()
    user = quote_plus(str(info["user"]))
    password = quote_plus(str(info["password"]))
    host = info["host"]
    port = info["port"]
    database = info["database"]
    return f"mysql+pymysql://{user}:{password}@{host}:{port}/{database}?charset=utf8mb4"
