package com.ruoyi.project4.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project4.domain.dto.DossierWritebackReqDTO;
import com.ruoyi.project4.domain.entity.T4BearingDiagnose;
import com.ruoyi.project4.service.BearingDiagnoseService;
import com.ruoyi.project4.service.BearingDossierWritebackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class BearingDossierWritebackServiceImpl implements BearingDossierWritebackService {

    @Autowired
    private BearingDiagnoseService bearingDiagnoseService;

    @Value("${project1.dossier.base-url:}")
    private String baseUrl;

    @Value("${project1.dossier.instance-id:}")
    private String instanceId;

    @Value("${project1.dossier.bom-node-id:}")
    private String bomNodeId;

    @Value("${project1.dossier.source-component:project4}")
    private String sourceComponent;

    @Value("${project1.dossier.token:}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AjaxResult writebackDiagnoseResult(Long diagnoseId) {
        if (diagnoseId == null) {
            return AjaxResult.error("诊断结果ID不能为空");
        }

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return AjaxResult.error("课题一数字卷宗写回地址未配置，请检查 project1.dossier.base-url");
        }

        T4BearingDiagnose diagnose = bearingDiagnoseService.selectById(diagnoseId);
        if (diagnose == null) {
            return AjaxResult.error("未找到诊断结果，id=" + diagnoseId);
        }

        String bizResult = diagnose.getBizResult();
        if (bizResult == null || bizResult.trim().isEmpty()) {
            return AjaxResult.error("诊断结果 bizResult 为空，无法写回");
        }

        JSONObject root;
        try {
            root = JSON.parseObject(bizResult);
        } catch (Exception e) {
            return AjaxResult.error("诊断结果 JSON 解析失败：" + e.getMessage());
        }

        JSONObject diagnosisObj = extractDiagnosis(root);

        String faultName = getString(diagnosisObj, "fault_full_name", "faultFullName", "故障诊断结果");
        String faultAbbr = getString(diagnosisObj, "fault_abbr", "faultAbbr", "");
        Object label = getObject(diagnosisObj, "label", "fault_label", "faultLabel");
        Object faultSize = getObject(diagnosisObj, "fault_size_inch", "faultSizeInch");

        Double confidence = extractConfidence(root);

        Map<String, Object> resultData = new LinkedHashMap<>();
        resultData.put("partNumber", "HYD-TUBE-MLG-32A");
        resultData.put("faultType", faultName);
        resultData.put("faultAbbr", faultAbbr);
        resultData.put("faultLabel", label);
        resultData.put("faultSizeInch", faultSize);
        resultData.put("faultPosition", inferFaultPosition(faultAbbr, faultName));
        resultData.put("causeJudgement", buildCauseJudgement(root, faultName));
        resultData.put("conclusion", faultName);
        resultData.put("suggestion", "建议结合振动信号、设备状态与检修记录开展重点复核，并持续跟踪同类异常样本。");

        List<Map<String, Object>> evidence = buildEvidence(diagnoseId, root);

        DossierWritebackReqDTO req = new DossierWritebackReqDTO();
        req.setInstanceId("03a5b222-75b6-4186-b63c-14e394048ab3");
        req.setBomNodeId("f1000006-0006-4006-8006-000000000006");
        req.setSourceComponent("project4");
        req.setTaskName("航空液压管路故障诊断与根因分析结果写回");
        req.setResultTitle("主起液压供压弯管质量监测结果");
        req.setResultSummary("课题四诊断结果显示：" + faultName + "，已完成故障类型识别与根因分析结果整理。");
        req.setConfidence(confidence);
        req.setResultData(resultData);
        req.setEvidence(evidence);

        String url = baseUrl;
        if (!url.endsWith("/")) {
            url += "/";
        }

        /*
         * 文档第4节推荐课题四接口为 /writeback/quality-supervision。
         * 如果现场课题一实际只开放了 /writeback/quality-monitoring-results，
         * 就把下面这一行改成 quality-monitoring-results。
         */
        url += "writeback/quality-supervision";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (token != null && !token.trim().isEmpty()) {
                headers.set("Authorization", "Bearer " + token.trim());
            }

            HttpEntity<DossierWritebackReqDTO> entity = new HttpEntity<>(req, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("writebackUrl", url);
            result.put("request", req);
            result.put("responseStatus", response.getStatusCode().value());
            result.put("responseBody", response.getBody());

            return AjaxResult.success("数字卷宗写回请求已发送", result);
        } catch (Exception e) {
            return AjaxResult.error("数字卷宗写回失败：" + e.getMessage());
        }
    }

    private JSONObject extractDiagnosis(JSONObject root) {
        Object diagnosis = root.get("diagnosis");
        if (diagnosis instanceof JSONObject) {
            return (JSONObject) diagnosis;
        }

        Object data = root.get("data");
        if (data instanceof JSONObject) {
            JSONObject dataObj = (JSONObject) data;
            Object dataDiagnosis = dataObj.get("diagnosis");
            if (dataDiagnosis instanceof JSONObject) {
                return (JSONObject) dataDiagnosis;
            }
        }

        return root;
    }

    private Double extractConfidence(JSONObject root) {
        Object confidence = root.get("confidence");
        if (confidence instanceof Number) {
            return ((Number) confidence).doubleValue();
        }

        Object rootCause = root.get("rootCause");
        if (rootCause instanceof JSONObject) {
            Object c = ((JSONObject) rootCause).get("confidence");
            if (c instanceof Number) {
                return ((Number) c).doubleValue();
            }
        }

        Object causeConfidence = root.get("causeConfidence");
        if (causeConfidence instanceof Number) {
            return ((Number) causeConfidence).doubleValue();
        }

        return null;
    }

    private List<Map<String, Object>> buildEvidence(Long diagnoseId, JSONObject root) {
        List<Map<String, Object>> evidence = new ArrayList<>();

        evidence.add(evidenceItem("课题标识", "project4"));
        evidence.add(evidenceItem("诊断结果ID", String.valueOf(diagnoseId)));
        evidence.add(evidenceItem("数据来源", "航空轴承振动信号"));
        evidence.add(evidenceItem("分析流程", "数据预处理-样本增强-特征融合-故障诊断-根因分析"));

        Object images = root.get("images");
        if (images != null) {
            evidence.add(evidenceItem("可视化结果", "已生成诊断图谱与特征图"));
        }

        return evidence;
    }

    private Map<String, Object> evidenceItem(String name, String value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    private String buildCauseJudgement(JSONObject root, String faultName) {
        Object rootCause = root.get("rootCause");
        if (rootCause instanceof JSONObject) {
            Object conclusion = ((JSONObject) rootCause).get("conclusion");
            if (conclusion != null) {
                return String.valueOf(conclusion);
            }
        }

        Object cause = root.get("causeJudgement");
        if (cause != null) {
            return String.valueOf(cause);
        }

        return "根据振动信号特征、融合特征响应及诊断模型输出，判断该样本存在" + faultName + "相关异常特征。";
    }

    private String inferFaultPosition(String faultAbbr, String faultName) {
        String s = (faultAbbr + " " + faultName).toUpperCase();

        if (s.contains("IR") || s.contains("内圈")) {
            return "轴承内圈";
        }
        if (s.contains("OR") || s.contains("外圈")) {
            return "轴承外圈";
        }
        if (s.contains("B") || s.contains("滚动体")) {
            return "轴承滚动体";
        }
        return "航空轴承";
    }

    private String getString(JSONObject obj, String key1, String key2, String defaultValue) {
        Object value = getObject(obj, key1, key2);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private Object getObject(JSONObject obj, String... keys) {
        if (obj == null) {
            return null;
        }

        for (String key : keys) {
            Object value = obj.get(key);
            if (value != null) {
                return value;
            }
        }

        return null;
    }
}