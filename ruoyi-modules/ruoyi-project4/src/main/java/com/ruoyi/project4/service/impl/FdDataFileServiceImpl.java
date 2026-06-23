package com.ruoyi.project4.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project4.domain.FdDataFile;
import com.ruoyi.project4.domain.dto.PreprocessRunDto;
import com.ruoyi.project4.mapper.FdDataFileMapper;
import com.ruoyi.project4.python.PythonAlgorithmRunner;
import com.ruoyi.project4.service.IFdDataFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 故障诊断-原始数据文件Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-29
 */
@Service
public class FdDataFileServiceImpl implements IFdDataFileService
{
    @Autowired
    private FdDataFileMapper fdDataFileMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PythonAlgorithmRunner pythonAlgorithmRunner;

    @Value("${project4.script-root}")
    private String scriptRoot;

    @Value("${project4.output-root}")
    private String outputRoot;

    /**
     * 查询故障诊断-原始数据文件
     *
     * @param fileId 故障诊断-原始数据文件主键
     * @return 故障诊断-原始数据文件
     */
    @Override
    public FdDataFile selectFdDataFileByFileId(Long fileId)
    {
        return fdDataFileMapper.selectFdDataFileByFileId(fileId);
    }

    /**
     * 查询故障诊断-原始数据文件列表
     *
     * @param fdDataFile 故障诊断-原始数据文件
     * @return 故障诊断-原始数据文件
     */
    @Override
    public List<FdDataFile> selectFdDataFileList(FdDataFile fdDataFile)
    {
        return fdDataFileMapper.selectFdDataFileList(fdDataFile);
    }

    /**
     * 新增故障诊断-原始数据文件
     *
     * @param fdDataFile 故障诊断-原始数据文件
     * @return 结果
     */
    @Override
    public int insertFdDataFile(FdDataFile fdDataFile)
    {
        fdDataFile.setCreateTime(DateUtils.getNowDate());
        return fdDataFileMapper.insertFdDataFile(fdDataFile);
    }

    /**
     * 修改故障诊断-原始数据文件
     *
     * @param fdDataFile 故障诊断-原始数据文件
     * @return 结果
     */
    @Override
    public int updateFdDataFile(FdDataFile fdDataFile)
    {
        fdDataFile.setUpdateTime(DateUtils.getNowDate());
        return fdDataFileMapper.updateFdDataFile(fdDataFile);
    }

    /**
     * 批量删除故障诊断-原始数据文件
     *
     * @param fileIds 需要删除的故障诊断-原始数据文件主键
     * @return 结果
     */
    @Override
    public int deleteFdDataFileByFileIds(Long[] fileIds)
    {
        return fdDataFileMapper.deleteFdDataFileByFileIds(fileIds);
    }

    /**
     * 删除故障诊断-原始数据文件信息
     *
     * @param fileId 故障诊断-原始数据文件主键
     * @return 结果
     */
    @Override
    public int deleteFdDataFileByFileId(Long fileId)
    {
        return fdDataFileMapper.deleteFdDataFileByFileId(fileId);
    }

    /**
     * 执行数据预处理
     *
     * 说明：
     * 1. 如果前端传了 fileIds，则处理选中的文件；
     * 2. 如果前端没传 fileIds，则默认处理 t4_data_file 中全部 CWRU mat 文件；
     * 3. 每一个文件都会单独调用 preprocess.py；
     * 4. 每一个文件生成的窗口样本都会写入 t4_raw_sample；
     * 5. 这样样本增强页面就不会只看到 108.mat，而是能看到多个 CWRU 文件来源。
     *
     * @param dto 预处理参数
     * @return 预处理结果
     */

    /**
     * 查询预处理后的原始样本列表
     *
     * 用于样本增强页面读取 t4_raw_sample 中真实预处理样本，
     * 不再使用前端模拟样本。
     *
     * @return 样本列表
     */
    @Override
    public AjaxResult listRawSamples()
    {
        try
        {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT " +
                            "sample_id AS sampleId, " +
                            "sample_code AS sampleCode, " +
                            "file_id AS fileId, " +
                            "data_source AS dataSource, " +
                            "raw_desc AS rawDesc, " +
                            "label_code AS labelCode, " +
                            "label_name AS labelName, " +
                            "sample_status AS sampleStatus, " +
                            "preprocess_status AS preprocessStatus, " +
                            "quality_score AS qualityScore, " +
                            "create_time AS createTime, " +
                            "remark AS samplePath " +
                            "FROM t4_raw_sample " +
                            "ORDER BY sample_id DESC"
            );

            for (Map<String, Object> row : rows)
            {
                String rawDesc = str(row.get("rawDesc"));

                row.put("sourceFile", extractRawDescValue(rawDesc, "sourceFile"));
                row.put("filePath", extractRawDescValue(rawDesc, "filePath"));
                row.put("sampleFilePath", extractRawDescValue(rawDesc, "sampleFilePath"));
                row.put("signalKey", extractRawDescValue(rawDesc, "signalKey"));

                Integer startIndex = intValue(extractRawDescValue(rawDesc, "startIndex"), 0);
                Integer endIndex = intValue(extractRawDescValue(rawDesc, "endIndex"), startIndex + 1024);

                row.put("startIndex", startIndex);
                row.put("endIndex", endIndex);
                row.put("windowSize", endIndex - startIndex);
                row.put("stride", 512);
                row.put("overlapRate", "50%");
            }

            return AjaxResult.success(rows);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return AjaxResult.error("查询预处理样本失败：" + e.getMessage());
        }
    }
    @Override
    public AjaxResult runPreprocess(PreprocessRunDto dto)
    {
        try
        {
            List<Map<String, Object>> fileList = loadPreprocessFiles(dto);

            if (fileList == null || fileList.isEmpty())
            {
                return AjaxResult.error("没有找到需要预处理的数据文件，请先在数据文件管理中导入或选择文件");
            }

            ObjectMapper objectMapper = new ObjectMapper();

            int totalSampleCount = 0;
            List<Map<String, Object>> allRecords = new ArrayList<>();

            String taskCode = "PRE-" + System.currentTimeMillis();

            Integer windowSize = intValue(readValue(dto, "getWindowSize"), 1024);
            Integer stride = intValue(readValue(dto, "getStride"), 512);

            File taskOutputDir = new File(new File(outputRoot, "preprocess"), taskCode);
            File inputDir = new File(taskOutputDir, "input");
            File outputDir = new File(taskOutputDir, "output");

            if (!inputDir.exists())
            {
                inputDir.mkdirs();
            }

            if (!outputDir.exists())
            {
                outputDir.mkdirs();
            }

            String scriptPath = joinPath(scriptRoot, "preprocess.py");

            for (Map<String, Object> fileRow : fileList)
            {
                Long fileId = longValue(fileRow.get("file_id"));
                Long datasetId = longValue(fileRow.get("dataset_id"));
                String originalFileName = str(fileRow.get("original_file_name"));
                String storagePath = str(fileRow.get("storage_path"));
                String sourceType = str(fileRow.get("source_type"));

                if (fileId == null)
                {
                    continue;
                }

                if (isBlank(storagePath))
                {
                    updateFileParseFailed(fileId, "文件存储路径为空");
                    continue;
                }

                File realFile = new File(storagePath);
                if (!realFile.exists())
                {
                    updateFileParseFailed(fileId, "文件不存在：" + storagePath);
                    continue;
                }

                LabelInfo labelInfo = inferLabelInfo(originalFileName, storagePath);

                String fileTaskCode = taskCode + "-F" + fileId;
                File inputJsonFile = new File(inputDir, fileTaskCode + "_input.json");
                File outputJsonFile = new File(outputDir, fileTaskCode + "_result.json");

                Map<String, Object> input = new HashMap<>();
                input.put("taskCode", fileTaskCode);
                input.put("fileId", fileId);
                input.put("datasetId", datasetId);
                input.put("datasetCode", "CWRU");
                input.put("filePath", normalizePath(storagePath));
                input.put("sourceFile", originalFileName);
                input.put("keyNum", inferKeyNum(originalFileName, storagePath));
                input.put("windowSize", windowSize);
                input.put("stride", stride);
                input.put("labelCode", labelInfo.labelCode);
                input.put("labelName", labelInfo.labelName);
                input.put("faultType", labelInfo.labelName);
                input.put("outputDir", normalizePath(taskOutputDir.getAbsolutePath()));

                objectMapper.writeValue(inputJsonFile, input);

                pythonAlgorithmRunner.run(
                        scriptPath,
                        inputJsonFile.getAbsolutePath(),
                        outputJsonFile.getAbsolutePath()
                );

                Map<String, Object> output = objectMapper.readValue(
                        outputJsonFile,
                        new TypeReference<Map<String, Object>>() {}
                );

                List<Map<String, Object>> records = extractRecords(output);

                if (records == null || records.isEmpty())
                {
                    updateFileParseFailed(fileId, "Python预处理未返回样本记录");
                    continue;
                }

                int fileSampleCount = 0;

                for (Map<String, Object> record : records)
                {
                    fileSampleCount++;

                    /*
                     * 重要：
                     * Python 输出中的 sampleCode 可能是 SAMPLE-001、SAMPLE-002 这类通用编号。
                     * t4_raw_sample.sample_code 有唯一索引，如果直接使用 Python 的编号，
                     * 多个文件或多次执行预处理时会出现 Duplicate entry。
                     *
                     * 因此这里统一由 Java 生成全局唯一样本编号：
                     * RAW-F文件ID-任务时间戳-窗口序号
                     */
                    String sampleCode = buildUniqueSampleCode(fileId, taskCode, fileSampleCount);

                    String sampleFilePath = str(record.get("sampleFilePath"));
                    if (isBlank(sampleFilePath))
                    {
                        sampleFilePath = normalizePath(new File(taskOutputDir, sampleCode + "_window.npy").getAbsolutePath());
                    }

                    String signalKey = str(record.get("signalKey"));
                    Integer startIndex = intValue(record.get("startIndex"), 0);
                    Integer endIndex = intValue(record.get("endIndex"), startIndex + windowSize);

                    String rawDesc = "sourceFile=" + originalFileName
                            + ", signalKey=" + signalKey
                            + ", filePath=" + normalizePath(storagePath)
                            + ", sampleFilePath=" + normalizePath(sampleFilePath)
                            + ", startIndex=" + startIndex
                            + ", endIndex=" + endIndex;

                    jdbcTemplate.update(
                            "INSERT INTO t4_raw_sample " +
                                    "(sample_code, dataset_id, file_id, equipment_id, sensor_id, equipment_type, fault_time, " +
                                    "data_source, raw_desc, sample_status, preprocess_status, quality_score, " +
                                    "label_code, label_name, create_by, create_time, remark) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            sampleCode,
                            datasetId,
                            fileId,
                            1L,
                            1L,
                            "轴承",
                            new Timestamp(System.currentTimeMillis()),
                            isBlank(sourceType) ? "CWRU" : sourceType,
                            rawDesc,
                            "待增强",
                            "已处理",
                            96.00,
                            labelInfo.labelCode,
                            labelInfo.labelName,
                            "admin",
                            new Timestamp(System.currentTimeMillis()),
                            normalizePath(sampleFilePath)
                    );

                    allRecords.add(record);
                }

                totalSampleCount += fileSampleCount;

                jdbcTemplate.update(
                        "UPDATE t4_data_file " +
                                "SET parse_status = ?, sample_count = ?, error_msg = NULL, update_time = NOW() " +
                                "WHERE file_id = ?",
                        "成功",
                        fileSampleCount,
                        fileId
                );
            }

            Map<String, Object> data = new HashMap<>();
            data.put("taskCode", taskCode);
            data.put("fileCount", fileList.size());
            data.put("sampleCount", totalSampleCount);
            data.put("records", allRecords);

            return AjaxResult.success("数据预处理完成", data);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return AjaxResult.error("数据预处理失败：" + e.getMessage());
        }
    }


    /**
     * 从 raw_desc 中提取 key=value 格式字段
     *
     * 例如：
     * raw_desc = sourceFile=108.mat, signalKey=X108_DE_time, filePath=D:/xxx.mat
     */
    private String extractRawDescValue(String rawDesc, String key)
    {
        if (isBlank(rawDesc) || isBlank(key))
        {
            return "";
        }

        String[] parts = rawDesc.split(",");

        for (String part : parts)
        {
            String item = part.trim();

            if (item.startsWith(key + "="))
            {
                return item.substring((key + "=").length()).trim();
            }
        }

        return "";
    }

    /**
     * 加载需要预处理的数据文件
     */
    private List<Map<String, Object>> loadPreprocessFiles(PreprocessRunDto dto)
    {
        Long[] fileIds = readFileIds(dto);

        if (fileIds != null && fileIds.length > 0)
        {
            String placeholders = String.join(",", Arrays.stream(fileIds).map(id -> "?").toArray(String[]::new));

            String sql = "SELECT file_id, dataset_id, file_code, original_file_name, storage_path, source_type " +
                    "FROM t4_data_file " +
                    "WHERE del_flag = '0' " +
                    "AND file_id IN (" + placeholders + ") " +
                    "ORDER BY file_id";

            Object[] args = Arrays.stream(fileIds).toArray();

            return jdbcTemplate.queryForList(sql, args);
        }

        return jdbcTemplate.queryForList(
                "SELECT file_id, dataset_id, file_code, original_file_name, storage_path, source_type " +
                        "FROM t4_data_file " +
                        "WHERE del_flag = '0' " +
                        "AND source_type = 'CWRU' " +
                        "AND file_suffix = 'mat' " +
                        "ORDER BY file_id"
        );
    }

    /**
     * 读取前端传来的文件ID
     */
    private Long[] readFileIds(PreprocessRunDto dto)
    {
        Object value = readValue(dto, "getFileIds");

        if (value == null)
        {
            value = readValue(dto, "getFileIdList");
        }

        if (value == null)
        {
            value = readValue(dto, "getIds");
        }

        if (value instanceof Long[])
        {
            return (Long[]) value;
        }

        if (value instanceof List)
        {
            List<?> list = (List<?>) value;
            List<Long> ids = new ArrayList<>();

            for (Object item : list)
            {
                Long id = longValue(item);
                if (id != null)
                {
                    ids.add(id);
                }
            }

            return ids.toArray(new Long[0]);
        }

        Object singleFileId = readValue(dto, "getFileId");
        Long one = longValue(singleFileId);

        if (one != null)
        {
            return new Long[]{one};
        }

        return null;
    }

    /**
     * 兼容不同 DTO 字段名，避免因为字段名不一致导致编译错误
     */
    private Object readValue(Object target, String methodName)
    {
        if (target == null || isBlank(methodName))
        {
            return null;
        }

        try
        {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    /**
     * 从 Python 输出 JSON 中提取 records
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRecords(Map<String, Object> output)
    {
        if (output == null)
        {
            return new ArrayList<>();
        }

        Object records = output.get("records");
        if (records instanceof List)
        {
            return (List<Map<String, Object>>) records;
        }

        Object result = output.get("result");
        if (result instanceof Map)
        {
            Object innerRecords = ((Map<String, Object>) result).get("records");
            if (innerRecords instanceof List)
            {
                return (List<Map<String, Object>>) innerRecords;
            }
        }

        Object data = output.get("data");
        if (data instanceof Map)
        {
            Map<String, Object> dataMap = (Map<String, Object>) data;

            Object dataRecords = dataMap.get("records");
            if (dataRecords instanceof List)
            {
                return (List<Map<String, Object>>) dataRecords;
            }

            Object dataResult = dataMap.get("result");
            if (dataResult instanceof Map)
            {
                Object innerRecords = ((Map<String, Object>) dataResult).get("records");
                if (innerRecords instanceof List)
                {
                    return (List<Map<String, Object>>) innerRecords;
                }
            }
        }

        return new ArrayList<>();
    }

    /**
     * 更新文件解析失败状态
     */
    private void updateFileParseFailed(Long fileId, String errorMsg)
    {
        if (fileId == null)
        {
            return;
        }

        jdbcTemplate.update(
                "UPDATE t4_data_file " +
                        "SET parse_status = ?, error_msg = ?, update_time = NOW() " +
                        "WHERE file_id = ?",
                "失败",
                errorMsg,
                fileId
        );
    }

    /**
     * 根据文件名推断故障标签
     */
    private LabelInfo inferLabelInfo(String originalFileName, String storagePath)
    {
        String text = ((originalFileName == null ? "" : originalFileName) + " " + (storagePath == null ? "" : storagePath)).toLowerCase();

        if (text.contains("normal") || text.contains("normal_0hp"))
        {
            return new LabelInfo("NORMAL", "正常");
        }

        if (text.contains("inner") || text.contains("ir"))
        {
            if (text.contains("014"))
            {
                return new LabelInfo("IR014", "内圈故障");
            }
            return new LabelInfo("IR007", "内圈故障");
        }

        if (text.contains("ball") || text.contains("b007"))
        {
            return new LabelInfo("B007", "滚动体故障");
        }

        if (text.contains("outer") || text.contains("or"))
        {
            if (text.contains("021"))
            {
                return new LabelInfo("OR021", "外圈故障");
            }
            return new LabelInfo("OR007", "外圈故障");
        }

        return new LabelInfo("UNKNOWN", "未知故障");
    }

    /**
     * 根据 CWRU 文件名推断 mat 文件中的变量编号。
     *
     * CWRU 文件常见变量名为 X097_DE_time、X108_DE_time、X121_DE_time 等。
     * 因此最可靠的做法是优先从文件名中提取数字：
     * 97_0.mat -> 97
     * 108.mat  -> 108
     * 121.mat  -> 121
     * 130_6.mat -> 130
     * 174.mat  -> 174
     * 226.mat  -> 226
     */
    private Integer inferKeyNum(String originalFileName, String storagePath)
    {
        Integer keyFromFileName = extractCwruKeyNum(originalFileName);
        if (keyFromFileName != null)
        {
            return keyFromFileName;
        }

        Integer keyFromPath = extractCwruKeyNum(storagePath);
        if (keyFromPath != null)
        {
            return keyFromPath;
        }

        return 108;
    }

    /**
     * 从文本中提取 .mat 文件名前面的数字作为 CWRU keyNum。
     */
    private Integer extractCwruKeyNum(String text)
    {
        if (isBlank(text))
        {
            return null;
        }

        String normalized = text.replace("\\", "/");

        Pattern pattern = Pattern.compile("([0-9]{2,3})(?:_[0-9]+)?\\.mat", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(normalized);

        Integer lastMatchedNumber = null;
        while (matcher.find())
        {
            lastMatchedNumber = intValue(matcher.group(1), null);
        }

        return lastMatchedNumber;
    }

    /**
     * 生成不会重复的原始样本编号。
     */
    private String buildUniqueSampleCode(Long fileId, String taskCode, int index)
    {
        String taskNo = isBlank(taskCode) ? String.valueOf(System.currentTimeMillis()) : taskCode.replace("PRE-", "");
        return String.format("RAW-F%s-%s-%03d", fileId == null ? "X" : fileId, taskNo, index);
    }

    private String joinPath(String root, String child)
    {
        if (root == null)
        {
            return child;
        }

        if (root.endsWith("/") || root.endsWith("\\"))
        {
            return root + child;
        }

        return root + File.separator + child;
    }

    private String normalizePath(String path)
    {
        if (path == null)
        {
            return null;
        }

        return path.replace("\\", "/");
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private String str(Object value)
    {
        if (value == null)
        {
            return "";
        }

        return String.valueOf(value);
    }

    private Long longValue(Object value)
    {
        if (value == null)
        {
            return null;
        }

        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }

        try
        {
            return Long.parseLong(String.valueOf(value));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private Integer intValue(Object value, Integer defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }

        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }

        try
        {
            return Integer.parseInt(String.valueOf(value));
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    private static class LabelInfo
    {
        private final String labelCode;
        private final String labelName;

        private LabelInfo(String labelCode, String labelName)
        {
            this.labelCode = labelCode;
            this.labelName = labelName;
        }
    }
}
