package com.ruoyi.project4.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project4.domain.FdDiagnosisResult;
import com.ruoyi.project4.domain.dto.DiagnosisRunDto;
import com.ruoyi.project4.mapper.FdDiagnosisResultMapper;
import com.ruoyi.project4.python.PythonAlgorithmRunner;
import com.ruoyi.project4.service.IFdDiagnosisResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 故障诊断-诊断结果Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-29
 */
@Service
public class FdDiagnosisResultServiceImpl implements IFdDiagnosisResultService
{
    @Autowired
    private FdDiagnosisResultMapper fdDiagnosisResultMapper;

    @Autowired
    private PythonAlgorithmRunner pythonAlgorithmRunner;

    @Value("${project4.script-root}")
    private String scriptRoot;

    @Value("${project4.model-path}")
    private String modelPath;

    @Value("${project4.output-root}")
    private String outputRoot;

    /**
     * 查询故障诊断-诊断结果
     *
     * @param diagnosisId 诊断ID
     * @return 故障诊断-诊断结果
     */
    @Override
    public FdDiagnosisResult selectFdDiagnosisResultByDiagnosisId(Long diagnosisId)
    {
        return fdDiagnosisResultMapper.selectFdDiagnosisResultByDiagnosisId(diagnosisId);
    }

    /**
     * 查询故障诊断-诊断结果列表
     *
     * @param fdDiagnosisResult 故障诊断-诊断结果
     * @return 故障诊断-诊断结果集合
     */
    @Override
    public List<FdDiagnosisResult> selectFdDiagnosisResultList(FdDiagnosisResult fdDiagnosisResult)
    {
        return fdDiagnosisResultMapper.selectFdDiagnosisResultList(fdDiagnosisResult);
    }

    /**
     * 新增故障诊断-诊断结果
     *
     * @param fdDiagnosisResult 故障诊断-诊断结果
     * @return 结果
     */
    @Override
    public int insertFdDiagnosisResult(FdDiagnosisResult fdDiagnosisResult)
    {
        fdDiagnosisResult.setCreateTime(DateUtils.getNowDate());
        setEntityValue(fdDiagnosisResult, "setDelFlag", "0");
        return fdDiagnosisResultMapper.insertFdDiagnosisResult(fdDiagnosisResult);
    }

    /**
     * 修改故障诊断-诊断结果
     *
     * @param fdDiagnosisResult 故障诊断-诊断结果
     * @return 结果
     */
    @Override
    public int updateFdDiagnosisResult(FdDiagnosisResult fdDiagnosisResult)
    {
        fdDiagnosisResult.setUpdateTime(DateUtils.getNowDate());
        return fdDiagnosisResultMapper.updateFdDiagnosisResult(fdDiagnosisResult);
    }

    /**
     * 批量删除故障诊断-诊断结果
     *
     * @param diagnosisIds 需要删除的诊断ID
     * @return 结果
     */
    @Override
    public int deleteFdDiagnosisResultByDiagnosisIds(Long[] diagnosisIds)
    {
        return fdDiagnosisResultMapper.deleteFdDiagnosisResultByDiagnosisIds(diagnosisIds);
    }

    /**
     * 删除故障诊断-诊断结果信息
     *
     * @param diagnosisId 诊断ID
     * @return 结果
     */
    @Override
    public int deleteFdDiagnosisResultByDiagnosisId(Long diagnosisId)
    {
        return fdDiagnosisResultMapper.deleteFdDiagnosisResultByDiagnosisId(diagnosisId);
    }

    /**
     * 执行故障诊断算法。
     *
     * 前端传入 sampleId、sampleCode、filePath、keyNum、length；
     * Java 后端生成 diagnosis_input.json；
     * Python 调用 diagnosis.py 和 model.pt 完成诊断；
     * Java 读取 diagnosis_result.json 并写入 fd_diagnosis_result 表。
     *
     * @param dto 诊断运行参数
     * @return 诊断执行结果
     */
    @Override
    public AjaxResult runDiagnosis(DiagnosisRunDto dto)
    {
        try
        {
            if (dto == null)
            {
                return AjaxResult.error("诊断参数不能为空");
            }
            if (isBlank(dto.getFilePath()))
            {
                return AjaxResult.error("CWRU数据文件路径不能为空");
            }
            if (dto.getKeyNum() == null)
            {
                return AjaxResult.error("CWRU文件编号keyNum不能为空，例如108");
            }

            ObjectMapper objectMapper = new ObjectMapper();

            String taskId = "DG-" + System.currentTimeMillis();
            String taskDir = normalizePath(outputRoot + "/diagnosis/" + taskId);

            File dir = new File(taskDir);
            if (!dir.exists() && !dir.mkdirs())
            {
                return AjaxResult.error("创建诊断输出目录失败：" + taskDir);
            }

            String inputJsonPath = normalizePath(taskDir + "/diagnosis_input.json");
            String outputJsonPath = normalizePath(taskDir + "/diagnosis_result.json");
            String scriptPath = normalizePath(scriptRoot + "/diagnosis.py");

            String sampleCode = isBlank(dto.getSampleCode()) ? "SAMPLE-" + taskId : dto.getSampleCode();

            Map<String, Object> input = new HashMap<>();
            input.put("sampleCode", sampleCode);
            input.put("filePath", normalizePath(dto.getFilePath()));
            input.put("keyNum", dto.getKeyNum());
            input.put("length", dto.getLength() == null ? 1024 : dto.getLength());
            input.put("modelPath", normalizePath(modelPath));

            objectMapper.writeValue(new File(inputJsonPath), input);

            pythonAlgorithmRunner.run(scriptPath, inputJsonPath, outputJsonPath);

            Map<String, Object> result = objectMapper.readValue(
                    new File(outputJsonPath),
                    new TypeReference<Map<String, Object>>() {}
            );

            BigDecimal confidence = toBigDecimal(result.get("confidence"));
            BigDecimal healthScore = toBigDecimal(result.get("healthScore"));
            String resultJson = objectMapper.writeValueAsString(result);

            FdDiagnosisResult diagnosis = new FdDiagnosisResult();

            // 使用反射写入字段：实体类里存在对应 setter 就写入，不存在就自动跳过。
            // 这样可以避免实体字段名细微差异导致编译报错。
            setEntityValue(diagnosis, "setPipelineId", getDtoLongValue(dto, "getPipelineId", 1L));
            setEntityValue(diagnosis, "setDatasetId", getDtoLongValue(dto, "getDatasetId", 1L));
            setEntityValue(diagnosis, "setFusionId", getDtoLongValue(dto, "getFusionId", null));
            setEntityValue(diagnosis, "setModelId", getDtoLongValue(dto, "getModelId", null));
            setEntityValue(diagnosis, "setSampleId", getDtoLongValue(dto, "getSampleId", 1L));
            setEntityValue(diagnosis, "setDiagnosisCode", taskId);
            setEntityValue(diagnosis, "setSampleCode", String.valueOf(result.getOrDefault("sampleCode", sampleCode)));
            setEntityValue(diagnosis, "setModelName", String.valueOf(result.getOrDefault("modelName", "WDCNN-DE-FE")));
            setEntityValue(diagnosis, "setFaultType", String.valueOf(result.get("faultType")));
            setEntityValue(diagnosis, "setFaultLocation", String.valueOf(result.get("faultLocation")));
            setEntityValue(diagnosis, "setFaultSize", result.get("faultSize"));
            setEntityValue(diagnosis, "setConfidence", confidence);
            setEntityValue(diagnosis, "setDiagnosisConfidence", confidence);
            setEntityValue(diagnosis, "setThreshold", new BigDecimal("0.8000"));
            setEntityValue(diagnosis, "setThresholdValue", new BigDecimal("0.8000"));
            setEntityValue(diagnosis, "setHealthScore", healthScore);
            setEntityValue(diagnosis, "setAlarmLevel", buildAlarmLevel(healthScore));
            setEntityValue(diagnosis, "setDiagnosisTime", DateUtils.getNowDate());
            setEntityValue(diagnosis, "setRootStatus", "待根因分析");
            setEntityValue(diagnosis, "setDiagnosisStatus", "已完成");
            setEntityValue(diagnosis, "setDiagnosisResultJson", resultJson);
            setEntityValue(diagnosis, "setResultJson", resultJson);
            setEntityValue(diagnosis, "setCreateBy", "admin");
            setEntityValue(diagnosis, "setCreateTime", DateUtils.getNowDate());
            setEntityValue(diagnosis, "setDelFlag", "0");
            setEntityValue(diagnosis, "setRemark", normalizePath(outputJsonPath));

            fdDiagnosisResultMapper.insertFdDiagnosisResult(diagnosis);

            return AjaxResult.success("故障诊断执行完成", diagnosis);
        }
        catch (Exception e)
        {
            return AjaxResult.error("故障诊断执行失败：" + e.getMessage());
        }
    }

    private String buildAlarmLevel(BigDecimal healthScore)
    {
        if (healthScore == null)
        {
            return "未知";
        }
        if (healthScore.compareTo(new BigDecimal("85")) >= 0)
        {
            return "正常";
        }
        if (healthScore.compareTo(new BigDecimal("70")) >= 0)
        {
            return "一般";
        }
        if (healthScore.compareTo(new BigDecimal("60")) >= 0)
        {
            return "严重";
        }
        return "危急";
    }

    private String normalizePath(String path)
    {
        return path == null ? null : path.replace("\\", "/");
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private BigDecimal toBigDecimal(Object value)
    {
        if (value == null)
        {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    /**
     * 兼容 DTO 中可能没有 pipelineId / datasetId / fusionId 等 getter 的情况。
     */
    private Long getDtoLongValue(Object dto, String getterName, Long defaultValue)
    {
        if (dto == null || getterName == null)
        {
            return defaultValue;
        }
        try
        {
            Method method = dto.getClass().getMethod(getterName);
            Object value = method.invoke(dto);
            Long longValue = toLong(value);
            return longValue == null ? defaultValue : longValue;
        }
        catch (Exception ignored)
        {
            return defaultValue;
        }
    }

    /**
     * 通过 setter 名称写入实体字段。
     * 如果实体类中不存在该 setter，则自动跳过，避免字段名不一致导致编译失败。
     */
    private void setEntityValue(Object target, String setterName, Object value)
    {
        if (target == null || setterName == null || value == null)
        {
            return;
        }

        Method[] methods = target.getClass().getMethods();
        for (Method method : methods)
        {
            if (!setterName.equals(method.getName()) || method.getParameterCount() != 1)
            {
                continue;
            }

            try
            {
                Class<?> parameterType = method.getParameterTypes()[0];
                Object convertedValue = convertValue(value, parameterType);
                method.invoke(target, convertedValue);
                return;
            }
            catch (Exception ignored)
            {
                // 如果某个类型转换失败，继续尝试其他同名 setter。
            }
        }
    }

    private Object convertValue(Object value, Class<?> targetType)
    {
        if (value == null)
        {
            return null;
        }
        if (targetType.isAssignableFrom(value.getClass()))
        {
            return value;
        }
        if (targetType == String.class)
        {
            return String.valueOf(value);
        }
        if (targetType == Long.class || targetType == long.class)
        {
            Long longValue = toLong(value);
            return longValue == null ? 0L : longValue;
        }
        if (targetType == Integer.class || targetType == int.class)
        {
            Long longValue = toLong(value);
            return longValue == null ? 0 : longValue.intValue();
        }
        if (targetType == BigDecimal.class)
        {
            return new BigDecimal(String.valueOf(value));
        }
        if (targetType == Date.class && value instanceof Date)
        {
            return value;
        }
        return value;
    }
}
