"""Preventive maintenance optimization based on Monte Carlo simulation and EMBKA."""

from __future__ import annotations

import hashlib
from typing import Any, Callable, Dict, List, Optional, Tuple

import numpy as np

from task_manager import TaskCanceledError


DEFAULTS = {
    "sampleCount": 30,
    "meanTm": 0.5,
    "stdTm": 0.0005,
    "meanTpm": 1.5,
    "stdTpm": 0.0015,
    "meanTr": 2.0,
    "stdTr": 0.002,
    "cm": 7000.0,
    "cpm": 8000.0,
    "cp": 4000.0,
    "cr": 600000.0,
    "Rm": 0.75,
    "a": 0.15,
    "b": 1.15,
    "alpha": 190.0,
    "beta": 3.0,
    "population": 20,
    "iterations": 100,
    "attackThreshold": 0.3,
    "objNo": 2,
}

LOWER_BOUNDS = np.array([50.0, 50.0, 50.0, 1.0, 1.0, 1.0])
UPPER_BOUNDS = np.array([100.0, 100.0, 100.0, 12.0, 12.0, 12.0])


def _number(payload: Dict[str, Any], key: str, default: float) -> float:
    value = payload.get(key, default)
    if value is None or isinstance(value, bool):
        return float(default)
    return float(value)


def _integer(payload: Dict[str, Any], key: str, default: int) -> int:
    return int(round(_number(payload, key, default)))


def _resolve_seed(payload: Dict[str, Any]) -> int:
    value = payload.get("seed")
    if value is not None and not isinstance(value, bool) and str(value).strip() != "":
        return int(value)
    task_id = str(payload.get("taskId") or "").strip()
    if task_id:
        digest = hashlib.blake2b(task_id.encode("utf-8"), digest_size=8).digest()
        return int.from_bytes(digest, byteorder="big", signed=False)
    return int(np.random.SeedSequence().generate_state(1, dtype=np.uint64)[0])


def _normalized_payload(payload: Dict[str, Any]) -> Dict[str, Any]:
    data = dict(payload or {})
    normalized = {
        "equipmentId": str(data.get("equipmentId") or ""),
        "partInstanceId": str(data.get("partInstanceId") or ""),
        "processExecutionId": str(data.get("processExecutionId") or ""),
        "taskNo": str(data.get("taskNo") or ""),
        "sampleCount": _integer(data, "sampleCount", DEFAULTS["sampleCount"]),
        "meanTm": _number(data, "meanTm", DEFAULTS["meanTm"]),
        "stdTm": _number(data, "stdTm", DEFAULTS["stdTm"]),
        "meanTpm": _number(data, "meanTpm", DEFAULTS["meanTpm"]),
        "stdTpm": _number(data, "stdTpm", DEFAULTS["stdTpm"]),
        "meanTr": _number(data, "meanTr", DEFAULTS["meanTr"]),
        "stdTr": _number(data, "stdTr", DEFAULTS["stdTr"]),
        "cm": _number(data, "cm", DEFAULTS["cm"]),
        "cpm": _number(data, "cpm", DEFAULTS["cpm"]),
        "cp": _number(data, "cp", DEFAULTS["cp"]),
        "cr": _number(data, "cr", DEFAULTS["cr"]),
        "Rm": _number(data, "Rm", DEFAULTS["Rm"]),
        "a": _number(data, "a", DEFAULTS["a"]),
        "b": _number(data, "b", DEFAULTS["b"]),
        "alpha": _number(data, "alpha", DEFAULTS["alpha"]),
        "beta": _number(data, "beta", DEFAULTS["beta"]),
        "population": _integer(data, "population", DEFAULTS["population"]),
        "iterations": _integer(data, "iterations", DEFAULTS["iterations"]),
        "attackThreshold": _number(data, "attackThreshold", DEFAULTS["attackThreshold"]),
        "objNo": 2,
        "seed": _resolve_seed(data),
    }
    seed_values = []
    for key in ("T1", "T2", "T3", "N1", "N2", "N3"):
        value = data.get(key)
        seed_values.append(float(value) if value is not None else np.nan)
    normalized["seedSolution"] = seed_values
    _validate(normalized)
    return normalized


def _validate(params: Dict[str, Any]) -> None:
    if params["sampleCount"] <= 0:
        raise ValueError("sampleCount must be greater than 0")
    if params["population"] < 4:
        raise ValueError("population must be at least 4")
    if params["iterations"] <= 0:
        raise ValueError("iterations must be greater than 0")
    if not 0 <= params["attackThreshold"] <= 1:
        raise ValueError("attackThreshold must be between 0 and 1")
    if not 0 <= params["Rm"] <= 1:
        raise ValueError("Rm must be between 0 and 1")
    if params["alpha"] <= 0 or params["beta"] <= 0:
        raise ValueError("Weibull alpha and beta must be greater than 0")
    for key in ("stdTm", "stdTpm", "stdTr", "cm", "cpm", "cp", "cr"):
        if params[key] < 0:
            raise ValueError(f"{key} must not be negative")


def _adjust(position: np.ndarray) -> np.ndarray:
    result = np.clip(np.asarray(position, dtype=float), LOWER_BOUNDS, UPPER_BOUNDS)
    result[:3] = np.sort(result[:3])[::-1]
    result[3:] = np.ceil(result[3:])
    return np.clip(result, LOWER_BOUNDS, UPPER_BOUNDS)


def _check_canceled(cancel_check: Optional[Callable[[], bool]], location: str) -> None:
    if cancel_check is not None and cancel_check():
        raise TaskCanceledError(f"任务已在{location}取消")


def _monte_carlo_samples(
    params: Dict[str, Any],
    rng: np.random.Generator,
    cancel_check: Optional[Callable[[], bool]] = None,
) -> Tuple[np.ndarray, np.ndarray, np.ndarray]:
    count = params["sampleCount"]

    def positive_normal(mean: float, std: float) -> np.ndarray:
        _check_canceled(cancel_check, "蒙特卡洛采样阶段")
        if std == 0:
            return np.full(count, mean, dtype=float)
        values = rng.normal(mean, std, count)
        invalid = values < 0
        while np.any(invalid):
            _check_canceled(cancel_check, "蒙特卡洛重采样阶段")
            values[invalid] = rng.normal(mean, std, int(np.sum(invalid)))
            invalid = values < 0
        return values

    return (
        positive_normal(params["meanTm"], params["stdTm"]),
        positive_normal(params["meanTpm"], params["stdTpm"]),
        positive_normal(params["meanTr"], params["stdTr"]),
    )


def _strategy_metrics(
    position: np.ndarray,
    samples: Tuple[np.ndarray, np.ndarray, np.ndarray],
    params: Dict[str, Any],
) -> Tuple[float, float, float]:
    x = _adjust(position)
    counts = x[3:].astype(int)
    intervals = np.repeat(x[:3], counts)
    cumulative_start = np.concatenate(([0.0], np.cumsum(intervals[:-1])))
    effective_start = params["a"] * cumulative_start
    effective_end = effective_start + intervals
    cycle = np.arange(intervals.size, dtype=float)
    hazards = (params["b"] ** cycle) * (
        (effective_end / params["alpha"]) ** params["beta"]
        - (effective_start / params["alpha"]) ** params["beta"]
    )
    reliability = np.exp(-hazards)
    min_reliability = float(np.min(reliability))
    if min_reliability < params["Rm"]:
        return 1e10, 0.001, min_reliability

    tm, tpm, tr = samples
    operating_time = float(np.sum(intervals))
    failure_count = float(np.sum(hazards))
    failure_time = tm * failure_count
    preventive_time = tpm * max(intervals.size - 1, 0)
    downtime = failure_time + preventive_time + tr
    total_time = operating_time + downtime
    availability = operating_time / total_time
    total_cost = (
        params["cm"] * failure_count
        + params["cpm"] * max(intervals.size - 1, 0)
        + params["cr"]
        + params["cp"] * downtime
    )
    unit_cost = total_cost / total_time
    return float(np.mean(unit_cost)), float(np.mean(availability)), min_reliability


def _fitness(position: np.ndarray, samples: Tuple[np.ndarray, np.ndarray, np.ndarray], params: Dict[str, Any]) -> np.ndarray:
    unit_cost, availability, _ = _strategy_metrics(position, samples, params)
    return np.array([unit_cost, -availability], dtype=float)


def _dominates(left: np.ndarray, right: np.ndarray) -> bool:
    return bool(np.all(left <= right) and np.any(left < right))


def _non_dominated(positions: np.ndarray, fitness: np.ndarray) -> Tuple[np.ndarray, np.ndarray]:
    keep = np.ones(fitness.shape[0], dtype=bool)
    for i in range(fitness.shape[0]):
        if not keep[i]:
            continue
        for j in range(fitness.shape[0]):
            if i == j or not keep[j]:
                continue
            if np.allclose(fitness[i], fitness[j], rtol=1e-10, atol=1e-12):
                if j > i:
                    keep[j] = False
            elif _dominates(fitness[j], fitness[i]):
                keep[i] = False
                break
    return positions[keep], fitness[keep]


def _archive_density(fitness: np.ndarray) -> np.ndarray:
    if fitness.shape[0] <= 1:
        return np.ones(fitness.shape[0])
    span = np.ptp(fitness, axis=0)
    radius = np.where(span > 0, span / 20.0, 1e-12)
    density = np.ones(fitness.shape[0])
    for i in range(fitness.shape[0]):
        close = np.all(np.abs(fitness - fitness[i]) < radius, axis=1)
        density[i] += max(int(np.sum(close)) - 1, 0)
    return density


def _trim_archive(
    positions: np.ndarray,
    fitness: np.ndarray,
    max_size: int,
    rng: np.random.Generator,
) -> Tuple[np.ndarray, np.ndarray]:
    while positions.shape[0] > max_size:
        density = _archive_density(fitness)
        probabilities = density / np.sum(density)
        index = int(rng.choice(positions.shape[0], p=probabilities))
        positions = np.delete(positions, index, axis=0)
        fitness = np.delete(fitness, index, axis=0)
    return positions, fitness


def _update_archive(
    archive_positions: np.ndarray,
    archive_fitness: np.ndarray,
    positions: np.ndarray,
    fitness: np.ndarray,
    max_size: int,
    rng: np.random.Generator,
) -> Tuple[np.ndarray, np.ndarray]:
    merged_positions = np.vstack((archive_positions, positions)) if archive_positions.size else positions.copy()
    merged_fitness = np.vstack((archive_fitness, fitness)) if archive_fitness.size else fitness.copy()
    archive_positions, archive_fitness = _non_dominated(merged_positions, merged_fitness)
    return _trim_archive(archive_positions, archive_fitness, max_size, rng)


def _select_leader(archive_positions: np.ndarray, archive_fitness: np.ndarray, rng: np.random.Generator) -> np.ndarray:
    density = _archive_density(archive_fitness)
    weights = 1.0 / np.maximum(density, 1e-12)
    index = int(rng.choice(archive_positions.shape[0], p=weights / np.sum(weights)))
    return archive_positions[index]


def _mutate(position: np.ndarray, archive_positions: np.ndarray, rng: np.random.Generator) -> np.ndarray:
    result = position.copy()
    peer = archive_positions[int(rng.integers(0, archive_positions.shape[0]))]
    for dimension in range(result.size):
        choice = rng.random()
        step = rng.random() * (UPPER_BOUNDS[dimension] - LOWER_BOUNDS[dimension])
        if choice < 0.25:
            result[dimension] -= step
        elif choice < 0.5:
            result[dimension] += step
        else:
            result[dimension] = (result[dimension] + peer[dimension]) / 2.0
    return _adjust(result)


def _optimize(
    params: Dict[str, Any],
    samples: Tuple[np.ndarray, np.ndarray, np.ndarray],
    rng: np.random.Generator,
    cancel_check: Optional[Callable[[], bool]] = None,
) -> Tuple[np.ndarray, np.ndarray]:
    _check_canceled(cancel_check, "优化初始化阶段")
    population_size = params["population"]
    positions = rng.uniform(LOWER_BOUNDS, UPPER_BOUNDS, size=(population_size, 6))
    positions = np.array([_adjust(item) for item in positions])
    seed_solution = np.asarray(params["seedSolution"], dtype=float)
    if np.all(np.isfinite(seed_solution)):
        positions[0] = _adjust(seed_solution)
    fitness = np.array([_fitness(item, samples, params) for item in positions])
    archive_positions, archive_fitness = _update_archive(
        np.empty((0, 6)),
        np.empty((0, 2)),
        positions,
        fitness,
        max(population_size // 2, 2),
        rng,
    )
    fixed_random = rng.random()

    for iteration in range(1, params["iterations"] + 1):
        _check_canceled(cancel_check, f"第 {iteration} 次优化迭代")
        leader = _select_leader(archive_positions, archive_fitness, rng)
        decay = 0.05 * np.exp(-2 * (iteration / params["iterations"]) ** 2)
        migration_scale = 2 * np.sin(fixed_random + np.pi / 2)
        for index in range(population_size):
            _check_canceled(cancel_check, f"第 {iteration} 次迭代的种群 {index + 1}")
            if params["attackThreshold"] < fixed_random:
                candidate = positions[index] + decay * (1 + np.sin(fixed_random)) * positions[index]
            else:
                candidate = positions[index] * (decay * (2 * rng.random(6) - 1) + 1)
            candidate = _adjust(candidate)
            candidate_fitness = _fitness(candidate, samples, params)
            if _dominates(candidate_fitness, fitness[index]):
                positions[index], fitness[index] = candidate, candidate_fitness

            peer_index = int(rng.integers(0, population_size))
            cauchy = np.tan((rng.random() - 0.5) * np.pi)
            if _dominates(fitness[index], fitness[peer_index]):
                candidate = positions[index] + cauchy * (positions[index] - leader)
            else:
                candidate = positions[index] + cauchy * (leader - migration_scale * positions[index])
            candidate = _adjust(candidate)
            candidate_fitness = _fitness(candidate, samples, params)
            if _dominates(candidate_fitness, fitness[index]):
                positions[index], fitness[index] = candidate, candidate_fitness

        archive_positions, archive_fitness = _update_archive(
            archive_positions,
            archive_fitness,
            positions,
            fitness,
            max(population_size // 2, 2),
            rng,
        )
        for archive_position in archive_positions.copy():
            candidate = _mutate(archive_position, archive_positions, rng)
            candidate_fitness = _fitness(candidate, samples, params)
            target = int(rng.integers(0, population_size))
            if _dominates(candidate_fitness, fitness[target]) or rng.random() < 0.5:
                positions[target], fitness[target] = candidate, candidate_fitness
        archive_positions, archive_fitness = _update_archive(
            archive_positions,
            archive_fitness,
            positions,
            fitness,
            max(population_size // 2, 2),
            rng,
        )
    return archive_positions, archive_fitness


def _solution_rows(
    positions: np.ndarray,
    samples: Tuple[np.ndarray, np.ndarray, np.ndarray],
    params: Dict[str, Any],
    cancel_check: Optional[Callable[[], bool]] = None,
) -> List[Dict[str, Any]]:
    rows = []
    for index, position in enumerate(positions):
        _check_canceled(cancel_check, f"第 {index + 1} 个 Pareto 解整理阶段")
        adjusted = _adjust(position)
        unit_cost, availability, min_reliability = _strategy_metrics(adjusted, samples, params)
        counts = adjusted[3:].astype(int)
        rows.append({
            "id": index + 1,
            "T1": round(float(adjusted[0]), 4),
            "T2": round(float(adjusted[1]), 4),
            "T3": round(float(adjusted[2]), 4),
            "N1": int(counts[0]),
            "N2": int(counts[1]),
            "N3": int(counts[2]),
            "totalCount": int(np.sum(counts)),
            "Cu": round(unit_cost, 6),
            "A": round(availability, 8),
            "minR": round(min_reliability, 8),
            "meetsReliability": min_reliability >= params["Rm"],
        })
    return sorted(rows, key=lambda item: (item["Cu"], -item["A"]))


def _recommended(rows: List[Dict[str, Any]], reliability_threshold: float) -> Dict[str, Any]:
    feasible = [row for row in rows if row["minR"] >= reliability_threshold] or rows
    costs = np.array([row["Cu"] for row in feasible], dtype=float)
    availability = np.array([row["A"] for row in feasible], dtype=float)
    cost_score = 1 - (costs - costs.min()) / max(float(np.ptp(costs)), 1e-12)
    availability_score = (availability - availability.min()) / max(float(np.ptp(availability)), 1e-12)
    index = int(np.argmax(0.5 * cost_score + 0.5 * availability_score))
    return dict(feasible[index])


def run_preventive_maintenance(
    payload: Dict[str, Any],
    cancel_check: Optional[Callable[[], bool]] = None,
) -> Dict[str, Any]:
    params = _normalized_payload(payload)
    _check_canceled(cancel_check, "参数校验后")
    rng = np.random.default_rng(params["seed"])
    samples = _monte_carlo_samples(params, rng, cancel_check)
    archive_positions, _ = _optimize(params, samples, rng, cancel_check)
    rows = _solution_rows(archive_positions, samples, params, cancel_check)
    _check_canceled(cancel_check, "结果写入前")
    if not rows:
        raise RuntimeError("optimizer returned no Pareto solutions")
    recommended = _recommended(rows, params["Rm"])
    recommended_id = recommended["id"]
    for row in rows:
        row["status"] = "RECOMMENDED" if row["id"] == recommended_id else "CANDIDATE"

    interval_sequence = (
        [recommended["T1"]] * recommended["N1"]
        + [recommended["T2"]] * recommended["N2"]
        + [recommended["T3"]] * recommended["N3"]
    )
    return {
        "success": True,
        "status": "SUCCESS",
        "message": "preventive maintenance optimization completed",
        "taskId": str(payload.get("taskId") or ""),
        "taskNo": params["taskNo"],
        "associations": {
            "equipmentId": params["equipmentId"],
            "partInstanceId": params["partInstanceId"],
            "processExecutionId": params["processExecutionId"],
        },
        "recommended": recommended,
        "paretoResults": rows,
        "monteCarloSamples": {
            "tm": np.round(samples[0], 8).tolist(),
            "tpm": np.round(samples[1], 8).tolist(),
            "tr": np.round(samples[2], 8).tolist(),
        },
        "intervalSequence": interval_sequence,
        "parameters": {key: value for key, value in params.items() if key != "seedSolution"},
    }
