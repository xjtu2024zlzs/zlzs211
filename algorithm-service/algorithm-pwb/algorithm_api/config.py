from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path
from urllib.parse import quote_plus

from .bootstrap import PROJECT_ROOT

try:
    from dotenv import load_dotenv
except ImportError:  # pragma: no cover - python-dotenv is declared in requirements
    load_dotenv = None


def _load_project_env() -> None:
    if load_dotenv is None:
        return
    for env_path in (PROJECT_ROOT / ".env", PROJECT_ROOT / "algorithm_api" / ".env"):
        if env_path.exists():
            load_dotenv(env_path, override=False)


_load_project_env()


@dataclass(frozen=True)
class SourceSystem:
    key: str
    label: str
    schema_key: str
    default_base_url: str


SOURCE_SYSTEMS: dict[str, SourceSystem] = {
    "plm": SourceSystem("plm", "PLM", "cf_plm", "http://127.0.0.1:9101"),
    "erp": SourceSystem("erp", "ERP", "cf_erp", "http://127.0.0.1:9102"),
    "mes": SourceSystem("mes", "MES", "cf_mes", "http://127.0.0.1:9103"),
    "qms": SourceSystem("qms", "QMS", "cf_qms", "http://127.0.0.1:9104"),
    "mro": SourceSystem("mro", "MRO", "cf_mro", "http://127.0.0.1:9105"),
}


def normalize_system_key(system_key: str) -> str:
    aliases = {item.label.lower(): key for key, item in SOURCE_SYSTEMS.items()}
    key = (system_key or "").strip().lower()
    key = aliases.get(key, key)
    if key not in SOURCE_SYSTEMS:
        allowed = ", ".join(sorted(SOURCE_SYSTEMS))
        raise ValueError(f"Unsupported source system {system_key!r}. Allowed: {allowed}")
    return key


def default_ground_truth_root() -> Path:
    return PROJECT_ROOT / "data" / "magneto_cf" / "ground-truth"


def default_target_table_prefix() -> str:
    return os.getenv("RUOYI_DOSSIER_TABLE_PREFIX", "p1p_dossier_")


def build_default_mysql_url() -> str:
    explicit = os.getenv("RUOYI_MYSQL_URL")
    if explicit:
        return explicit

    user = quote_plus(os.getenv("RUOYI_MYSQL_USER", "root"))
    password = quote_plus(os.getenv("RUOYI_MYSQL_PASSWORD", "password"))
    host = os.getenv("RUOYI_MYSQL_HOST", "127.0.0.1")
    port = os.getenv("RUOYI_MYSQL_PORT", "3306")
    database = os.getenv("RUOYI_MYSQL_DATABASE", "ry-cloud")
    return f"mysql+pymysql://{user}:{password}@{host}:{port}/{database}?charset=utf8mb4"


def bool_env(name: str, default: bool = False) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


def required_llm_key_env(llm_model: str | None) -> str | None:
    model = (llm_model or "").strip().lower()
    if not model or model == "none":
        return "LLM_MODEL"
    if model.startswith("ollama/"):
        return None
    if "deepseek" in model:
        return "DEEPSEEK_API_KEY"
    if model.startswith("openai/") or model.startswith("gpt-") or model.startswith("o1") or model.startswith("o3"):
        return "OPENAI_API_KEY"
    return None


def llm_availability(llm_model: str | None) -> dict[str, object]:
    env_name = required_llm_key_env(llm_model)
    model = (llm_model or "").strip()
    if env_name == "LLM_MODEL":
        return {
            "available": False,
            "requiresKey": True,
            "envVar": env_name,
            "message": "未选择 LLM 模型，已跳过 MagnetoGPT。",
        }
    if env_name is None:
        return {
            "available": bool(model),
            "requiresKey": False,
            "envVar": None,
            "message": "",
        }
    if os.getenv(env_name):
        return {
            "available": True,
            "requiresKey": True,
            "envVar": env_name,
            "message": "",
        }
    return {
        "available": False,
        "requiresKey": True,
        "envVar": env_name,
        "message": f"未配置 {env_name}，已跳过 MagnetoGPT，仅生成 Magneto/MagnetoBoost 的候选结果集。",
    }

