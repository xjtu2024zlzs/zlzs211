package com.ruoyi.designtask1.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class FrameBeamMaintenanceAdviceService {

    public Map<String, Object> buildAdvice(Map<String, Object> prediction) {
        String riskLevel = stringValue(prediction.get("riskLevel"), "MEDIUM").toUpperCase();
        double remainingCycles = doubleValue(prediction.get("predictedRemainingCycles"), 0D);
        double confidence = doubleValue(prediction.get("confidence"), 0D);

        Map<String, Object> advice = new LinkedHashMap<>();
        advice.put("riskLevel", riskLevel);
        advice.put("predictedRemainingCycles", remainingCycles);
        advice.put("confidence", confidence);
        advice.put("continueServiceAllowed", !"CRITICAL".equals(riskLevel));
        advice.put("loadRestrictionRequired", "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel));
        advice.put("expertReviewRequired", confidence < 0.7D || "CRITICAL".equals(riskLevel));

        switch (riskLevel) {
            case "LOW" -> {
                advice.put("adviceType", "ROUTINE_INSPECTION");
                advice.put("summary", "裂纹扩展风险较低，建议按常规周期复检。");
                advice.put("inspectionIntervalCycles", Math.max(1000D, remainingCycles * 0.5D));
            }
            case "HIGH" -> {
                advice.put("adviceType", "REPAIR_PLANNING");
                advice.put("summary", "裂纹扩展风险较高，建议限制载荷并安排补强或更换维修。");
                advice.put("inspectionIntervalCycles", Math.max(100D, remainingCycles * 0.15D));
            }
            case "CRITICAL" -> {
                advice.put("adviceType", "STOP_SERVICE");
                advice.put("summary", "裂纹接近或超过安全阈值，建议停用并立即进入专家评审和维修处置。");
                advice.put("inspectionIntervalCycles", 0D);
            }
            default -> {
                advice.put("adviceType", "SHORTENED_INSPECTION");
                advice.put("summary", "裂纹存在继续扩展风险，建议缩短复检周期并准备维修方案。");
                advice.put("inspectionIntervalCycles", Math.max(300D, remainingCycles * 0.3D));
            }
        }
        return advice;
    }

    private String stringValue(Object value, String defaultValue) {
        return value == null ? defaultValue : String.valueOf(value);
    }

    private double doubleValue(Object value, double defaultValue) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value == null ? defaultValue : Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
