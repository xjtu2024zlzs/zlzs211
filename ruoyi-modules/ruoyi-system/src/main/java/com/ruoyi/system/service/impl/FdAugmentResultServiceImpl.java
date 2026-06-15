package com.ruoyi.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.system.domain.FdAugmentResult;
import com.ruoyi.system.domain.dto.AugmentRunDto;
import com.ruoyi.system.mapper.FdAugmentResultMapper;
import com.ruoyi.system.service.IFdAugmentResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 样本增强结果Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-29
 */
@Service
public class FdAugmentResultServiceImpl implements IFdAugmentResultService
{
    @Autowired
    private FdAugmentResultMapper fdAugmentResultMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Python 脚本目录。
     *
     * 默认路径是你当前项目里的 algorithm/project4。
     * 如果你后面想放到配置文件，可以在 application.yml 里配置：
     *
     * project4:
     *   script-root: D:/科研/课题项目/航空装备智能故障诊断软件系统/若依/RuoYi-Vue3/algorithm/project4
     *   python-exe: python
     */
    @Value("${project4.script-root:D:/科研/课题项目/航空装备智能故障诊断软件系统/若依/RuoYi-Vue3/algorithm/project4}")
    private String scriptRoot;

    /**
     * Python 命令。
     * 一般写 python 即可。
     */
    @Value("${project4.python-exe:python}")
    private String pythonExe;

    /**
     * 查询样本增强结果
     *
     * @param augmentId 样本增强结果主键
     * @return 样本增强结果
     */
    @Override
    public FdAugmentResult selectFdAugmentResultByAugmentId(Long augmentId)
    {
        return fdAugmentResultMapper.selectFdAugmentResultByAugmentId(augmentId);
    }

    /**
     * 查询样本增强结果列表
     *
     * @param fdAugmentResult 样本增强结果
     * @return 样本增强结果
     */
    @Override
    public List<FdAugmentResult> selectFdAugmentResultList(FdAugmentResult fdAugmentResult)
    {
        return fdAugmentResultMapper.selectFdAugmentResultList(fdAugmentResult);
    }

    /**
     * 新增样本增强结果
     *
     * @param fdAugmentResult 样本增强结果
     * @return 结果
     */
    @Override
    public int insertFdAugmentResult(FdAugmentResult fdAugmentResult)
    {
        fdAugmentResult.setCreateTime(new Date());
        return fdAugmentResultMapper.insertFdAugmentResult(fdAugmentResult);
    }

    /**
     * 修改样本增强结果
     *
     * @param fdAugmentResult 样本增强结果
     * @return 结果
     */
    @Override
    public int updateFdAugmentResult(FdAugmentResult fdAugmentResult)
    {
        fdAugmentResult.setUpdateTime(new Date());
        return fdAugmentResultMapper.updateFdAugmentResult(fdAugmentResult);
    }

    /**
     * 批量删除样本增强结果
     *
     * @param augmentIds 需要删除的样本增强结果主键
     * @return 结果
     */
    @Override
    public int deleteFdAugmentResultByAugmentIds(Long[] augmentIds)
    {
        return fdAugmentResultMapper.deleteFdAugmentResultByAugmentIds(augmentIds);
    }

    /**
     * 删除样本增强结果信息
     *
     * @param augmentId 样本增强结果主键
     * @return 结果
     */
    @Override
    public int deleteFdAugmentResultByAugmentId(Long augmentId)
    {
        return fdAugmentResultMapper.deleteFdAugmentResultByAugmentId(augmentId);
    }

    /**
     * 执行样本增强
     *
     * 前端传入：
     * sampleIds、augAlgorithm、augMultiple、pipelineId
     *
     * 后端执行：
     * 1. 根据 sampleIds 查询 fd_raw_sample
     * 2. 联表 fd_data_file 获取真实 .mat 文件 storage_path
     * 3. 生成 augment_input.json
     * 4. 调用 Python augment.py
     * 5. 读取 augment_result.json
     * 6. 写入 fd_augment_result
     *
     * @param dto 样本增强运行参数
     * @return AjaxResult
     */
    @Override
    public AjaxResult runAugment(AugmentRunDto dto)
    {
        if (dto == null)
        {
            return AjaxResult.error("样本增强参数不能为空");
        }

        if (dto.getSampleIds() == null || dto.getSampleIds().isEmpty())
        {
            return AjaxResult.error("请选择需要增强的预处理样本");
        }

        String algorithm = normalizeAugmentAlgorithm(dto.getAugAlgorithm());
        Integer augMultiple = dto.getAugMultiple() == null ? 3 : dto.getAugMultiple();

        int successCount = 0;
        int failCount = 0;

        List<Map<String, Object>> records = new ArrayList<>();

        for (Long sampleId : dto.getSampleIds())
        {
            try
            {
                Map<String, Object> sample = queryRawSample(sampleId);

                if (sample == null)
                {
                    failCount++;
                    records.add(errorRecord(sampleId, "未找到样本记录"));
                    continue;
                }

                Long rawSampleId = getLong(sample, "sample_id");
                String rawSampleCode = getString(sample, "sample_code");
                String rawDesc = getString(sample, "raw_desc");
                String labelCode = getString(sample, "label_code");
                String labelName = getString(sample, "label_name");
                String originalFileName = getString(sample, "original_file_name");
                String storagePath = getString(sample, "storage_path");

                if (storagePath == null || storagePath.trim().isEmpty())
                {
                    failCount++;
                    records.add(errorRecord(sampleId, "样本对应的真实 .mat 文件路径为空"));
                    continue;
                }

                File matFile = new File(storagePath);
                if (!matFile.exists())
                {
                    failCount++;
                    records.add(errorRecord(sampleId, "真实 .mat 文件不存在：" + storagePath));
                    continue;
                }

                int keyNum = extractKeyNum(originalFileName);
                if (keyNum <= 0)
                {
                    keyNum = extractKeyNum(storagePath);
                }

                if (keyNum <= 0)
                {
                    failCount++;
                    records.add(errorRecord(sampleId, "无法从文件名中提取 keyNum：" + originalFileName));
                    continue;
                }

                String taskCode = "AUG-" + System.currentTimeMillis() + "-" + rawSampleId;
                String workDir = scriptRoot + File.separator + "runtime" + File.separator + taskCode;

                Files.createDirectories(Paths.get(workDir));

                String inputJsonPath = workDir + File.separator + "augment_input.json";
                String outputJsonPath = workDir + File.separator + "augment_result.json";

                Map<String, Object> inputJson = new HashMap<>();

                inputJson.put("taskCode", taskCode);
                inputJson.put("pipelineId", dto.getPipelineId());

                inputJson.put("datasetId", dto.getDatasetId() == null ? 1L : dto.getDatasetId());

                inputJson.put("sampleId", rawSampleId);
                inputJson.put("sampleCode", rawSampleCode);

                inputJson.put("rawSampleId", rawSampleId);
                inputJson.put("rawSampleCode", rawSampleCode);

                inputJson.put("rawDesc", rawDesc);
                inputJson.put("labelCode", labelCode);
                inputJson.put("labelName", labelName);

                inputJson.put("sourceFile", originalFileName);

                /*
                 * 为了兼容不同版本 augment.py，这里同时传 filePath 和 file_path。
                 * 你的 Python 里如果读 filePath，可以用。
                 * 如果读 file_path，也可以用。
                 */
                inputJson.put("filePath", normalizePath(storagePath));
                inputJson.put("file_path", normalizePath(storagePath));

                /*
                 * 同理，keyNum / key_num 都传。
                 */
                inputJson.put("keyNum", keyNum);
                inputJson.put("key_num", keyNum);

                inputJson.put("length", dto.getLength() == null ? 1024 : dto.getLength());

                /*
                 * 当前 Python 增强脚本支持 noise / scale / flip / shift / mask / random。
                 * 如果前端还传 SMOTE / GAN / VAE，这里会统一转换成 random。
                 */
                inputJson.put("algorithm", algorithm);
                inputJson.put("augAlgorithm", algorithm);
                inputJson.put("augmentAlgorithm", algorithm);

                inputJson.put("augMultiple", augMultiple);
                inputJson.put("augmentRatio", augMultiple);
                inputJson.put("augment_ratio", augMultiple);

                inputJson.put("label", parseLabel(labelCode));

                Files.write(
                        Paths.get(inputJsonPath),
                        JSON.toJSONString(inputJson).getBytes(StandardCharsets.UTF_8)
                );

                JSONObject pythonResult = callPythonAugment(inputJsonPath, outputJsonPath);

                JSONObject data = pythonResult.getJSONObject("data");
                if (data == null)
                {
                    data = pythonResult;
                }

                Integer generatedCount = data.getInteger("generatedCount");
                if (generatedCount == null)
                {
                    generatedCount = data.getInteger("sampleCount");
                }
                if (generatedCount == null)
                {
                    generatedCount = data.getInteger("generated_count");
                }
                if (generatedCount == null)
                {
                    generatedCount = augMultiple * 120;
                }

                String outputPath = data.getString("outputPath");
                if (outputPath == null || outputPath.trim().isEmpty())
                {
                    outputPath = data.getString("output_path");
                }
                if (outputPath == null || outputPath.trim().isEmpty())
                {
                    outputPath = normalizePath(workDir);
                }

                String augmentCode = data.getString("augmentCode");
                if (augmentCode == null || augmentCode.trim().isEmpty())
                {
                    augmentCode = data.getString("augment_code");
                }
                if (augmentCode == null || augmentCode.trim().isEmpty())
                {
                    augmentCode = taskCode;
                }

                insertAugmentResultToDb(
                        augmentCode,
                        rawSampleId,
                        rawSampleCode,
                        algorithm,
                        generatedCount,
                        outputPath
                );

                successCount++;

                Map<String, Object> ok = new HashMap<>();
                ok.put("success", true);
                ok.put("sampleId", rawSampleId);
                ok.put("sampleCode", rawSampleCode);
                ok.put("sourceFile", originalFileName);
                ok.put("filePath", normalizePath(storagePath));
                ok.put("keyNum", keyNum);
                ok.put("augmentCode", augmentCode);
                ok.put("algorithm", algorithm);
                ok.put("generatedCount", generatedCount);
                ok.put("outputPath", outputPath);
                records.add(ok);
            }
            catch (Exception e)
            {
                failCount++;
                records.add(errorRecord(sampleId, e.getMessage()));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", dto.getSampleIds().size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("records", records);

        if (successCount == 0)
        {
            return AjaxResult.error("样本增强失败，请检查 .mat 文件路径、Python脚本和增强参数");
        }

        return AjaxResult.success("样本增强完成", result);
    }

    /**
     * 根据 fd_raw_sample.sample_id 查询样本，并联表 fd_data_file 读取真实 .mat 路径。
     */
    private Map<String, Object> queryRawSample(Long sampleId)
    {
        String sql =
                "SELECT " +
                        "  r.sample_id, " +
                        "  r.sample_code, " +
                        "  r.file_id, " +
                        "  r.raw_desc, " +
                        "  r.label_code, " +
                        "  r.label_name, " +
                        "  f.original_file_name, " +
                        "  f.storage_path " +
                        "FROM fd_raw_sample r " +
                        "LEFT JOIN fd_data_file f ON r.file_id = f.file_id " +
                        "WHERE r.sample_id = ? " +
                        "LIMIT 1";

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, sampleId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 调用 Python augment.py。
     *
     * 命令格式：
     * python augment.py augment_input.json augment_result.json
     */
    private JSONObject callPythonAugment(String inputJsonPath, String outputJsonPath) throws Exception
    {
        String scriptPath = scriptRoot + File.separator + "augment.py";

        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists())
        {
            throw new RuntimeException("找不到 Python 脚本：" + scriptPath);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                pythonExe,
                scriptPath,
                inputJsonPath,
                outputJsonPath
        );

        processBuilder.directory(new File(scriptRoot));
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        StringBuilder log = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                log.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0)
        {
            throw new RuntimeException("Python 算法执行失败，日志：" + log);
        }

        File outputFile = new File(outputJsonPath);
        if (!outputFile.exists())
        {
            throw new RuntimeException("Python 未生成结果文件：" + outputJsonPath + "，日志：" + log);
        }

        String jsonText = new String(Files.readAllBytes(Paths.get(outputJsonPath)), StandardCharsets.UTF_8);

        if (jsonText == null || jsonText.trim().isEmpty())
        {
            throw new RuntimeException("Python 结果文件为空：" + outputJsonPath);
        }

        return JSON.parseObject(jsonText);
    }

    /**
     * 写入 fd_augment_result 表。
     *
     * 注意：
     * 这里使用 JdbcTemplate 直接插入，是为了避免 Domain 字段名和数据库字段不一致导致报错。
     */
    private void insertAugmentResultToDb(
            String augmentCode,
            Long rawSampleId,
            String rawSampleCode,
            String algorithmName,
            Integer generatedCount,
            String outputPath
    )
    {
        String sql =
                "INSERT INTO fd_augment_result " +
                        "(augment_code, raw_sample_id, raw_sample_code, algorithm_name, generated_count, output_path, create_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                augmentCode,
                rawSampleId,
                rawSampleCode,
                algorithmName,
                generatedCount,
                outputPath,
                new Timestamp(System.currentTimeMillis())
        );
    }

    /**
     * 算法名称标准化。
     *
     * 当前 Python 脚本真实支持：
     * random / noise / scale / flip / shift / mask
     *
     * 如果前端还传 SMOTE / ADASYN / GAN / VAE，
     * 后端统一映射为 random，避免 Python 不识别。
     */
    private String normalizeAugmentAlgorithm(String algorithm)
    {
        if (algorithm == null || algorithm.trim().isEmpty())
        {
            return "random";
        }

        String value = algorithm.trim();

        if ("SMOTE".equalsIgnoreCase(value)
                || "ADASYN".equalsIgnoreCase(value)
                || "GAN".equalsIgnoreCase(value)
                || "VAE".equalsIgnoreCase(value))
        {
            return "random";
        }

        if ("noise".equalsIgnoreCase(value))
        {
            return "noise";
        }

        if ("scale".equalsIgnoreCase(value))
        {
            return "scale";
        }

        if ("flip".equalsIgnoreCase(value))
        {
            return "flip";
        }

        if ("shift".equalsIgnoreCase(value))
        {
            return "shift";
        }

        if ("mask".equalsIgnoreCase(value))
        {
            return "mask";
        }

        if ("random".equalsIgnoreCase(value))
        {
            return "random";
        }

        return "random";
    }

    /**
     * 从文件名中提取 keyNum。
     *
     * 例如：
     * 97_0.mat -> 97
     * 108.mat -> 108
     * 130_6.mat -> 130
     * 174.mat -> 174
     */
    private int extractKeyNum(String text)
    {
        if (text == null || text.trim().isEmpty())
        {
            return 0;
        }

        Matcher matcher = Pattern.compile("(\\d+)").matcher(text);

        if (matcher.find())
        {
            try
            {
                return Integer.parseInt(matcher.group(1));
            }
            catch (Exception ignored)
            {
                return 0;
            }
        }

        return 0;
    }

    /**
     * 标签转换。
     * NORMAL 作为 0，其它故障样本作为 1。
     */
    private Integer parseLabel(String labelCode)
    {
        if (labelCode == null || labelCode.trim().isEmpty())
        {
            return 1;
        }

        if ("NORMAL".equalsIgnoreCase(labelCode))
        {
            return 0;
        }

        return 1;
    }

    private String normalizePath(String path)
    {
        if (path == null)
        {
            return null;
        }

        return path.replace("\\", "/");
    }

    private Long getLong(Map<String, Object> map, String key)
    {
        Object value = map.get(key);

        if (value == null)
        {
            return null;
        }

        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }

        return Long.parseLong(String.valueOf(value));
    }

    private String getString(Map<String, Object> map, String key)
    {
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Map<String, Object> errorRecord(Long sampleId, String message)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("sampleId", sampleId);
        map.put("error", message);
        return map;
    }
}