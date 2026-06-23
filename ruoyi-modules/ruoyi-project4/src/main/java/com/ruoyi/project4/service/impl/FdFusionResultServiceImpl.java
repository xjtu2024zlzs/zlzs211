package com.ruoyi.project4.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project4.domain.FdFusionResult;
import com.ruoyi.project4.domain.dto.FusionRunDto;
import com.ruoyi.project4.mapper.FdFusionResultMapper;
import com.ruoyi.project4.python.PythonAlgorithmRunner;
import com.ruoyi.project4.service.IFdFusionResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 故障诊断-融合特征结果Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-29
 */
@Service
public class FdFusionResultServiceImpl implements IFdFusionResultService
{
    @Autowired
    private FdFusionResultMapper fdFusionResultMapper;

    @Autowired
    private PythonAlgorithmRunner pythonAlgorithmRunner;

    /**
     * Python 脚本目录，例如：D:/科研/课题项目/航空装备智能故障诊断软件系统/若依/RuoYi-Vue3/algorithm/project4
     */
    @Value("${project4.script-root}")
    private String scriptRoot;

    /**
     * 算法输出目录，例如：D:/topic4-output
     */
    @Value("${project4.output-root}")
    private String outputRoot;

    /**
     * 查询故障诊断-融合特征结果
     *
     * @param fusionId 故障诊断-融合特征结果主键
     * @return 故障诊断-融合特征结果
     */
    @Override
    public FdFusionResult selectFdFusionResultByFusionId(Long fusionId)
    {
        return fdFusionResultMapper.selectFdFusionResultByFusionId(fusionId);
    }

    /**
     * 查询故障诊断-融合特征结果列表
     *
     * @param fdFusionResult 故障诊断-融合特征结果
     * @return 故障诊断-融合特征结果集合
     */
    @Override
    public List<FdFusionResult> selectFdFusionResultList(FdFusionResult fdFusionResult)
    {
        return fdFusionResultMapper.selectFdFusionResultList(fdFusionResult);
    }

    /**
     * 新增故障诊断-融合特征结果
     *
     * @param fdFusionResult 故障诊断-融合特征结果
     * @return 结果
     */
    @Override
    public int insertFdFusionResult(FdFusionResult fdFusionResult)
    {
        fdFusionResult.setCreateTime(DateUtils.getNowDate());
        return fdFusionResultMapper.insertFdFusionResult(fdFusionResult);
    }

    /**
     * 修改故障诊断-融合特征结果
     *
     * @param fdFusionResult 故障诊断-融合特征结果
     * @return 结果
     */
    @Override
    public int updateFdFusionResult(FdFusionResult fdFusionResult)
    {
        fdFusionResult.setUpdateTime(DateUtils.getNowDate());
        return fdFusionResultMapper.updateFdFusionResult(fdFusionResult);
    }

    /**
     * 批量删除故障诊断-融合特征结果
     *
     * @param fusionIds 需要删除的故障诊断-融合特征结果主键
     * @return 结果
     */
    @Override
    public int deleteFdFusionResultByFusionIds(Long[] fusionIds)
    {
        return fdFusionResultMapper.deleteFdFusionResultByFusionIds(fusionIds);
    }

    /**
     * 删除故障诊断-融合特征结果信息
     *
     * @param fusionId 故障诊断-融合特征结果主键
     * @return 结果
     */
    @Override
    public int deleteFdFusionResultByFusionId(Long fusionId)
    {
        return fdFusionResultMapper.deleteFdFusionResultByFusionId(fusionId);
    }

    /**
     * 执行特征融合算法。
     *
     * 前端传入增强样本输出路径 augmentOutputPath；
     * Java 生成 fusion_input.json；
     * Python 读取增强样本 .npy 并生成 fusion_result.json / fusion_vector.npy；
     * Java 读取 fusion_result.json，并写入 t4_fusion_result 表。
     *
     * @param dto 特征融合运行参数
     * @return 融合结果
     */
    @Override
    public AjaxResult runFusion(FusionRunDto dto)
    {
        try
        {
            if (dto == null)
            {
                return AjaxResult.error("特征融合参数不能为空");
            }
            if (isBlank(dto.getAugmentOutputPath()))
            {
                return AjaxResult.error("增强样本输出路径不能为空");
            }

            ObjectMapper objectMapper = new ObjectMapper();

            String taskId = "FUS-" + System.currentTimeMillis();
            String taskDir = normalizePath(outputRoot + "/fusion/" + taskId);

            File dir = new File(taskDir);
            if (!dir.exists() && !dir.mkdirs())
            {
                return AjaxResult.error("创建特征融合输出目录失败：" + taskDir);
            }

            String inputJsonPath = normalizePath(taskDir + "/fusion_input.json");
            String outputJsonPath = normalizePath(taskDir + "/fusion_result.json");
            String scriptPath = normalizePath(scriptRoot + "/fusion.py");

            String fusionMethod = isBlank(dto.getFusionMethod()) ? "PCA+Attention" : dto.getFusionMethod();
            Integer outputDim = dto.getOutputDim() == null ? 128 : dto.getOutputDim();
            Long sampleId = dto.getSampleId() == null ? dto.getRawSampleId() : dto.getSampleId();
            if (sampleId == null)
            {
                sampleId = 1L;
            }

            Map<String, Object> input = new HashMap<>();
            input.put("pipelineId", toLongOrDefault(dto.getPipelineId(), 1L));
            input.put("pipelineCode", dto.getPipelineCode());
            input.put("datasetId", 1L);
            input.put("augmentId", dto.getAugmentId());
            input.put("augmentCode", dto.getAugmentCode());
            input.put("rawSampleId", dto.getRawSampleId());
            input.put("sampleId", sampleId);
            input.put("sampleCode", dto.getSampleCode());
            input.put("augmentOutputPath", normalizePath(dto.getAugmentOutputPath()));
            input.put("fusionCode", taskId);
            input.put("fusionMethod", fusionMethod);
            input.put("outputDim", outputDim);
            input.put("outputDir", taskDir);

            objectMapper.writeValue(new File(inputJsonPath), input);

            pythonAlgorithmRunner.run(scriptPath, inputJsonPath, outputJsonPath);

            Map<String, Object> result = objectMapper.readValue(
                    new File(outputJsonPath),
                    new TypeReference<Map<String, Object>>() {}
            );

            FdFusionResult fusion = new FdFusionResult();

            String vectorPath = getString(result, "vectorPath");
            Long inputDim = toLong(result.get("inputDim"));
            Long resultOutputDim = toLong(firstNonNull(result.get("outputDim"), outputDim));
            Long sampleCount = toLong(result.get("sampleCount"));
            String featureComponents = buildFeatureComponents(result);
            String resultJson = objectMapper.writeValueAsString(result);

            // 与 t4_fusion_result 表字段对应。使用反射写入，避免实体类字段名略有差异时直接编译失败。
            setEntityValue(fusion, "setFusionCode", taskId);
            setEntityValue(fusion, "setPipelineId", toLongOrDefault(dto.getPipelineId(), 1L));
            setEntityValue(fusion, "setDatasetId", 1L);
            setEntityValue(fusion, "setSampleId", sampleId);
            setEntityValue(fusion, "setSampleCode", isBlank(dto.getSampleCode()) ? "SAMPLE-FUSION" : dto.getSampleCode());
            setEntityValue(fusion, "setAugmentId", dto.getAugmentId());
            setEntityValue(fusion, "setFeatureComponents", featureComponents);
            setEntityValue(fusion, "setFusionMethod", fusionMethod);
            setEntityValue(fusion, "setConfidenceWeight", new BigDecimal("0.8500"));
            setEntityValue(fusion, "setOutputDimension", resultOutputDim == null ? outputDim : resultOutputDim);
            setEntityValue(fusion, "setVectorLength", resultOutputDim == null ? outputDim : resultOutputDim);
            setEntityValue(fusion, "setFusionVectorJson", resultJson);
            setEntityValue(fusion, "setVectorPath", vectorPath);
            setEntityValue(fusion, "setCreateBy", "admin");
            setEntityValue(fusion, "setCreateTime", DateUtils.getNowDate());
            setEntityValue(fusion, "setRemark", outputJsonPath);

            // 兼容部分实体类可能额外生成的字段名。
            setEntityValue(fusion, "setAugmentCode", dto.getAugmentCode());
            setEntityValue(fusion, "setInputDim", inputDim);
            setEntityValue(fusion, "setOutputDim", resultOutputDim == null ? outputDim : resultOutputDim);
            setEntityValue(fusion, "setSampleCount", sampleCount);
            setEntityValue(fusion, "setFeatureVectorPath", vectorPath);
            setEntityValue(fusion, "setResultJson", resultJson);
            setEntityValue(fusion, "setResultSummary", buildResultSummary(taskId, fusionMethod, outputDim, vectorPath));
            setEntityValue(fusion, "setValidity", "有效");
            setEntityValue(fusion, "setDelFlag", "0");

            fdFusionResultMapper.insertFdFusionResult(fusion);

            return AjaxResult.success("特征融合执行完成", fusion);
        }
        catch (Exception e)
        {
            return AjaxResult.error("特征融合执行失败：" + e.getMessage());
        }
    }

    private String buildFeatureComponents(Map<String, Object> result)
    {
        Object contribution = result.get("contribution");
        if (contribution instanceof Map)
        {
            @SuppressWarnings("unchecked")
            Map<String, Object> contributionMap = (Map<String, Object>) contribution;
            if (!contributionMap.isEmpty())
            {
                StringBuilder builder = new StringBuilder();
                for (String key : contributionMap.keySet())
                {
                    if (builder.length() > 0)
                    {
                        builder.append(",");
                    }
                    builder.append(key).append("特征");
                }
                return builder.toString();
            }
        }
        return "多传感器时序窗口特征,传感器空间关系特征,时间演化特征,故障类别条件特征";
    }

    private String buildResultSummary(String taskId, String fusionMethod, Integer outputDim, String vectorPath)
    {
        return "融合编号：" + taskId
                + "；融合方法：" + fusionMethod
                + "；输出维度：" + outputDim
                + "；向量路径：" + vectorPath;
    }

    private Object firstNonNull(Object first, Object second)
    {
        return first != null ? first : second;
    }

    private String getString(Map<String, Object> map, String key)
    {
        if (map == null || key == null || map.get(key) == null)
        {
            return null;
        }
        return String.valueOf(map.get(key));
    }

    private Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            if (value instanceof Number)
            {
                return ((Number) value).longValue();
            }
            String text = String.valueOf(value).trim();
            if (text.startsWith("P-"))
            {
                return null;
            }
            return Long.valueOf(text);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private Long toLongOrDefault(Object value, Long defaultValue)
    {
        Long longValue = toLong(value);
        return longValue == null ? defaultValue : longValue;
    }

    private void setEntityValue(Object entity, String setterName, Object value)
    {
        if (entity == null || setterName == null || value == null)
        {
            return;
        }

        Method[] methods = entity.getClass().getMethods();
        for (Method method : methods)
        {
            if (!setterName.equals(method.getName()) || method.getParameterCount() != 1)
            {
                continue;
            }

            try
            {
                Class<?> paramType = method.getParameterTypes()[0];
                Object convertedValue = convertValue(value, paramType);
                if (convertedValue != null)
                {
                    method.invoke(entity, convertedValue);
                }
                return;
            }
            catch (Exception ignored)
            {
                // 某些实体字段不存在或类型不一致时跳过，避免影响主流程。
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

        String text = String.valueOf(value);
        try
        {
            if (String.class.equals(targetType))
            {
                return text;
            }
            if (Long.class.equals(targetType) || long.class.equals(targetType))
            {
                Long longValue = toLong(value);
                return longValue == null ? null : longValue;
            }
            if (Integer.class.equals(targetType) || int.class.equals(targetType))
            {
                Long longValue = toLong(value);
                return longValue == null ? null : longValue.intValue();
            }
            if (BigDecimal.class.equals(targetType))
            {
                return new BigDecimal(text);
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return null;
    }

    private String normalizePath(String path)
    {
        return path == null ? null : path.replace("\\", "/");
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}

