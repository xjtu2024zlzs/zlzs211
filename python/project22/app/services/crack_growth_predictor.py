from app.schemas.crack_growth_schema import (
    CrackGrowthRequest,
    CrackGrowthResponse,
    GrowthPoint,
    MaintenanceAdvice,
)


def _float(data, key, default):
    try:
        return float(data.get(key, default))
    except Exception:
        return float(default)


def _risk_level(ratio, remaining_cycles):
    if ratio >= 0.95 or remaining_cycles <= 0:
        return "CRITICAL"
    if ratio >= 0.75 or remaining_cycles < 1500:
        return "HIGH"
    if ratio >= 0.45 or remaining_cycles < 5000:
        return "MEDIUM"
    return "LOW"


def _advice(risk_level, remaining_cycles, confidence):
    if risk_level == "LOW":
        summary = "裂纹扩展风险较低，建议按常规周期复检。"
        interval = max(1000, remaining_cycles * 0.5)
        return MaintenanceAdvice(
            riskLevel=risk_level,
            adviceType="ROUTINE_INSPECTION",
            summary=summary,
            inspectionIntervalCycles=interval,
            continueServiceAllowed=True,
            loadRestrictionRequired=False,
            expertReviewRequired=confidence < 0.7,
        )
    if risk_level == "HIGH":
        summary = "裂纹扩展风险较高，建议限制载荷并安排补强或更换维修。"
        interval = max(100, remaining_cycles * 0.15)
        return MaintenanceAdvice(
            riskLevel=risk_level,
            adviceType="REPAIR_PLANNING",
            summary=summary,
            inspectionIntervalCycles=interval,
            continueServiceAllowed=True,
            loadRestrictionRequired=True,
            expertReviewRequired=True,
        )
    if risk_level == "CRITICAL":
        summary = "裂纹接近或超过安全阈值，建议停用并立即进入专家评审和维修处置。"
        return MaintenanceAdvice(
            riskLevel=risk_level,
            adviceType="STOP_SERVICE",
            summary=summary,
            inspectionIntervalCycles=0,
            continueServiceAllowed=False,
            loadRestrictionRequired=True,
            expertReviewRequired=True,
        )
    summary = "裂纹存在继续扩展风险，建议缩短复检周期并准备维修方案。"
    return MaintenanceAdvice(
        riskLevel=risk_level,
        adviceType="SHORTENED_INSPECTION",
        summary=summary,
        inspectionIntervalCycles=max(300, remaining_cycles * 0.3),
        continueServiceAllowed=True,
        loadRestrictionRequired=False,
        expertReviewRequired=confidence < 0.7,
    )


def predict_growth(request: CrackGrowthRequest) -> CrackGrowthResponse:
    crack = request.crackInput or {}
    spectrum = request.loadSpectrum or {}
    initial = _float(crack, "initialCrackLength", 3.2)
    critical = max(_float(crack, "criticalCrackLength", 12.0), initial + 0.1)
    max_stress = _float(spectrum, "maxStress", 120)
    min_stress = _float(spectrum, "minStress", 20)
    cycles = max(1, int(_float(spectrum, "cycles", 10000)))
    hours = _float(spectrum, "currentFlightHours", 1800)
    confidence = _float(crack, "inspectionConfidence", 0.85)

    stress_range = max(1.0, max_stress - min_stress)
    growth_factor = min(2.5, max(0.35, stress_range / 100.0))
    consumed_ratio = min(0.98, max(0.0, (initial / critical) * growth_factor))
    remaining_cycles = max(0, int(cycles * (1.0 - consumed_ratio)))
    remaining_hours = round(max(0.0, hours * (remaining_cycles / cycles)), 2)
    risk = _risk_level(initial / critical, remaining_cycles)

    curve = [
        GrowthPoint(cycle=0, crackLength=round(initial, 3)),
        GrowthPoint(cycle=int(remaining_cycles * 0.25), crackLength=round(initial + (critical - initial) * 0.2, 3)),
        GrowthPoint(cycle=int(remaining_cycles * 0.6), crackLength=round(initial + (critical - initial) * 0.55, 3)),
        GrowthPoint(cycle=remaining_cycles, crackLength=round(critical, 3)),
    ]

    return CrackGrowthResponse(
        status="SUCCESS",
        initialCrackLength=round(initial, 3),
        criticalCrackLength=round(critical, 3),
        predictedRemainingCycles=remaining_cycles,
        predictedRemainingFlightHours=remaining_hours,
        riskLevel=risk,
        confidence=round(confidence, 2),
        growthCurve=curve,
        maintenanceAdvice=_advice(risk, remaining_cycles, confidence),
    )
