"""Aircraft frame beam crack algorithm adapter."""

from __future__ import annotations

from typing import Any, Dict

from algorithms.early_warning_wrapper import predict_frame_beam_crack


def run_frame_beam_crack(payload: Dict[str, Any]) -> Dict[str, Any]:
    return predict_frame_beam_crack(payload or {}, task_id=(payload or {}).get("taskId"))
