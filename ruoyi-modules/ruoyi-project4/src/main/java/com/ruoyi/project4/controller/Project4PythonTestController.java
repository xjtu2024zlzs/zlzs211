package com.ruoyi.project4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project4.python.PythonAlgorithmRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
public class Project4PythonTestController {

    @Autowired
    private PythonAlgorithmRunner pythonAlgorithmRunner;

    @Value("${project4.script-root}")
    private String scriptRoot;

    @Value("${project4.model-path}")
    private String modelPath;

    @Value("${project4.output-root}")
    private String outputRoot;

    @GetMapping("/project4/test/diagnosis")
    public AjaxResult testDiagnosis() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String taskDir = outputRoot + "/test/diagnosis";
            File dir = new File(taskDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String inputJsonPath = taskDir + "/diagnosis_input.json";
            String outputJsonPath = taskDir + "/diagnosis_result.json";

            Map<String, Object> input = new HashMap<>();
            input.put("sampleCode", "SAMPLE-JAVA-TEST-001");

            // 这里改成你之前 Python 测试成功的 108.mat 路径
            input.put("filePath", "D:/科研/数据集/CWRU/12k Drive End Bearing Fault Data/0.007/3/Inner Race/108.mat");

            input.put("keyNum", 108);
            input.put("length", 1024);
            input.put("modelPath", modelPath);

            objectMapper.writeValue(new File(inputJsonPath), input);

            String scriptPath = scriptRoot + "/diagnosis.py";

            pythonAlgorithmRunner.run(scriptPath, inputJsonPath, outputJsonPath);

            Map<String, Object> result = objectMapper.readValue(new File(outputJsonPath), Map.class);

            return AjaxResult.success("Java调用Python诊断算法成功", result);

        } catch (Exception e) {
            return AjaxResult.error("Java调用Python诊断算法失败：" + e.getMessage());
        }
    }

    @GetMapping("/project4/test/augment")
    public AjaxResult testAugment() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String taskDir = outputRoot + "/test/augment";
            File dir = new File(taskDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String inputJsonPath = taskDir + "/augment_input.json";
            String outputJsonPath = taskDir + "/augment_result.json";

            Map<String, Object> input = new HashMap<>();

            // 这里也改成你之前 Python 测试成功的 108.mat 路径
            input.put("filePath", "D:/科研/数据集/CWRU/12k Drive End Bearing Fault Data/0.007/3/Inner Race/108.mat");

            input.put("keyNum", 108);
            input.put("sampleCodePrefix", "AUG-JAVA-TEST");
            input.put("length", 1024);
            input.put("label", 1);
            input.put("augmentRatio", 2);
            input.put("randomAug", true);
            input.put("outputDir", outputRoot + "/augmented/JAVA-TEST");

            objectMapper.writeValue(new File(inputJsonPath), input);

            String scriptPath = scriptRoot + "/augment.py";

            pythonAlgorithmRunner.run(scriptPath, inputJsonPath, outputJsonPath);

            Map<String, Object> result = objectMapper.readValue(new File(outputJsonPath), Map.class);

            return AjaxResult.success("Java调用Python增强算法成功", result);

        } catch (Exception e) {
            return AjaxResult.error("Java调用Python增强算法失败：" + e.getMessage());
        }
    }
}
