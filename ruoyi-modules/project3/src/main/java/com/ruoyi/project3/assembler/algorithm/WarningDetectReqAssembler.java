package com.ruoyi.project3.assembler.algorithm;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.config.PythonAlgorithmProperties;
import com.ruoyi.project3.config.faultiden.FaultIdenFileProps;
import com.ruoyi.project3.domain.algorithm.warning.WarningDetectReq;
import com.ruoyi.project3.domain.faultiden.FaultIdenFilePackage;
import com.ruoyi.project3.service.FaultIdenFileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class WarningDetectReqAssembler
{
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String USAGE_WARNING = "PROCESS_ANOMALY";
    private static final String RAW_SINGLE = "RAW_SINGLE";

    @Resource
    private PythonAlgorithmProperties pythonAlgorithmProperties;

    @Resource
    private FaultIdenFileProps faultIdenFileProps;

    @Resource
    private FaultIdenFileService faultIdenFileService;

    public WarningDetectReq assemble(Map<String, Object> reqMap, String taskId, String requestId)
    {
        String partId = AlgorithmParamReader.requiredText(reqMap, "partId", "partId", "part_id");
        String partName = AlgorithmParamReader.optionalText(reqMap, "partName", "part_name");
        // snake_case/processExecId 兼容后端列表返回字段，warning.vue 会透传 process_exec_id。
        String processId = AlgorithmParamReader.requiredText(reqMap, "processId", "processId", "process_id", "processExecId", "process_exec_id");
        String processName = AlgorithmParamReader.optionalText(reqMap, "processName", "process_name");
        Integer featureCol = featureCol(reqMap);
        String boschPath = text(first(reqMap, "boschTrainNumericPath", "bosch_train_numeric_path", "trainNumericPath", "train_numeric_path"));

        List<Long> trainIds = sampleIds(reqMap, "trainSampleIds", "train_sample_ids");
        List<Long> detectIds = sampleIds(reqMap, "detectSampleIds", "detect_sample_ids");
        FaultIdenFilePackage legacyPack = null;
        FaultIdenFilePackage trainPack = null;
        FaultIdenFilePackage detectPack = null;
        if (boschPath != null)
        {
            // Bosch算法直接读取 train_numeric.csv，不需要从系统样本表准备训练/检测文件。
        }
        else if (!trainIds.isEmpty() || !detectIds.isEmpty())
        {
            if (trainIds.isEmpty())
            {
                throw new ServiceException("请选择训练和测试数据文件");
            }
            if (detectIds.isEmpty())
            {
                throw new ServiceException("请选择检测数据文件");
            }
            trainPack = faultIdenFileService.prepare(taskId + "_TRAIN", trainIds, USAGE_WARNING);
            detectPack = faultIdenFileService.prepare(taskId + "_DETECT", detectIds, USAGE_WARNING);
        }
        else
        {
            legacyPack = faultIdenFileService.prepare(taskId, sampleIds(reqMap), USAGE_WARNING);
        }

        WarningDetectReq req = new WarningDetectReq();
        req.setAuthCode(pythonAlgorithmProperties.getWarningAuthCode());
        req.setRequestId(requestId);
        req.setTaskId(taskId);
        req.setRequestTime(new SimpleDateFormat(DATE_TIME_PATTERN).format(new Date()));
        req.setTargetType("process");
        req.setTargetId(processId);
        req.setTargetName(processName == null ? partName : processName);
        req.setPartId(partId);
        req.setProcessId(processId);
        req.setProcessName(processName);
        req.setFeatureCol(featureCol);
        req.setHierarchyContext(ctx(reqMap, partId, partName, processId, processName));
        applyBoschParams(req, reqMap, boschPath);
        if (boschPath != null)
        {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("fileName", "train_numeric.csv");
            info.put("fileUrl", boschPath);
            info.put("fileType", "csv");
            info.put("fileMode", "bosch_train_numeric");
            info.put("dataUsage", "BOSCH_PROCESS_ANOMALY");
            req.setFileInfo(info);
        }
        else if (legacyPack == null)
        {
            req.setTrainFileInfo(fileInfo(trainPack));
            Map<String, Object> detectFileInfo = fileInfo(detectPack, featureCol, true, taskId);
            req.setDetectFileInfo(detectFileInfo);
            req.setFileInfo(detectFileInfo);
        }
        else
        {
            req.setFileInfo(fileInfo(legacyPack, featureCol, true, taskId));
        }
        return req;
    }

    private void applyBoschParams(WarningDetectReq req, Map<String, Object> source, String boschPath)
    {
        if (boschPath == null)
        {
            return;
        }
        req.setBoschTrainNumericPath(boschPath);
        req.setStation(text(first(source, "station")));
        req.setFeatureColName(text(first(source, "featureColName", "feature_col_name", "feature_col")));
        String selectionMode = text(first(source, "selectionMode", "selection_mode"));
        req.setSelectionMode(selectionMode == null ? "response_assoc" : selectionMode);
        req.setMaxRows(intVal(source, 200000, "maxRows", "max_rows"));
        req.setMinFailedObserved(intVal(source, 10, "minFailedObserved", "min_failed_observed"));
        req.setMinObserved(intVal(source, 1000, "minObserved", "min_observed"));
        req.setEpochs(intVal(source, 60, "epochs"));
        req.setAlphaSample(numVal(source, 0.01D, "alphaSample", "alpha_sample"));
        req.setEwmaLambda(numVal(source, 0.30D, "ewmaLambda", "ewma_lambda"));
        req.setSeed(intVal(source, 0, "seed"));
        String device = text(first(source, "device"));
        req.setDevice(device == null ? "auto" : device);
    }

    private Map<String, Object> ctx(Map<String, Object> req, String partId, String partName, String processId, String processName)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("partId", partId);
        out.put("partName", partName);
        out.put("partCode", text(first(req, "partCode", "part_code")));
        out.put("processId", processId);
        out.put("processName", processName);
        out.put("processOrder", first(req, "processOrder", "process_order"));
        out.put("deviceId", text(first(req, "deviceId", "device_id")));
        out.put("deviceName", text(first(req, "deviceName", "device_name")));
        out.put("breadcrumb", breadcrumb(text(first(req, "pathText", "path_text")), partName, processName));
        return out;
    }

    private List<String> breadcrumb(String path, String partName, String processName)
    {
        List<String> list = new ArrayList<>();
        if (path != null)
        {
            for (String item : path.split("/"))
            {
                String text = text(item);
                if (text != null)
                {
                    list.add(text);
                }
            }
        }
        if (list.isEmpty() && partName != null)
        {
            list.add(partName);
        }
        if (processName != null)
        {
            list.add(processName);
        }
        return list;
    }

    private Map<String, Object> fileInfo(FaultIdenFilePackage pack)
    {
        return fileInfo(pack, null, false, null);
    }

    private Map<String, Object> fileInfo(FaultIdenFilePackage pack, Integer featureCol, boolean extract, String taskId)
    {
        if (pack == null || text(pack.getFileUrl()) == null)
        {
            throw new ServiceException("异常检测数据文件准备失败");
        }
        List<Long> ids = sampleIds(pack.getSelectedSampleIds());
        Long sampleId = RAW_SINGLE.equals(pack.getFileMode()) && !ids.isEmpty() ? ids.get(0) : null;

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("fileId", sampleId == null ? pack.getId() : sampleId);
        out.put("sampleId", sampleId);
        out.put("fileName", pack.getFileName());
        out.put("fileType", pack.getFileType());
        out.put("fileMode", fileMode(pack.getFileMode()));
        out.put("fileUrl", sampleId == null ? pack.getFileUrl() : warningSourceUrl(sampleId));
        out.put("selectedSampleIds", ids);
        out.put("dataUsage", pack.getDataUsage());
        if (extract && sampleId != null)
        {
            applyFeatureColumn(out, sampleId, featureCol, taskId);
        }
        else if (featureCol != null)
        {
            out.put("featureCol", featureCol);
        }
        return out;
    }

    private void applyFeatureColumn(Map<String, Object> fileInfo, Long sampleId, Integer requestedCol, String taskId)
    {
        try
        {
            Path source = faultIdenFileService.sourceFile(sampleId);
            List<String> lines = Files.readAllLines(source, StandardCharsets.UTF_8);
            String first = firstDataLine(lines);
            if (first == null)
            {
                throw new ServiceException("检测数据文件为空");
            }
            char delimiter = delimiter(first);
            int columnCount = splitRow(first, delimiter).size();
            fileInfo.put("sourceColumnCount", columnCount);
            if (columnCount <= 1)
            {
                fileInfo.put("featureCol", 1);
                return;
            }

            int col = requestedCol == null ? 0 : requestedCol;
            if (col < 1)
            {
                throw new ServiceException("多列检测数据缺少 featureCol");
            }
            if (col > columnCount)
            {
                throw new ServiceException("检测数据列数小于工序 number，请选择需要使用的数据列");
            }

            Path exportDir = exportRoot();
            Files.createDirectories(exportDir);
            String name = safeName(taskId) + "_feature_col_" + col + ".csv";
            Path target = exportDir.resolve(name).normalize();
            if (!target.startsWith(exportDir))
            {
                throw new ServiceException("特征列文件路径不合法");
            }

            List<String> extracted = new ArrayList<>();
            for (String line : lines)
            {
                if (line == null || line.trim().isEmpty())
                {
                    continue;
                }
                List<String> values = splitRow(line, delimiter);
                extracted.add(csvCell(col <= values.size() ? values.get(col - 1) : ""));
            }
            Files.write(target, extracted, StandardCharsets.UTF_8);

            fileInfo.put("featureCol", col);
            fileInfo.put("fileId", null);
            fileInfo.put("sampleId", sampleId);
            fileInfo.put("fileName", name);
            fileInfo.put("fileType", "csv");
            fileInfo.put("fileMode", "single");
            fileInfo.put("fileUrl", exportFileUrl(name));
            fileInfo.put("extractedFromSampleId", sampleId);
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("检测数据特征列提取失败：" + e.getMessage());
        }
    }

    private String firstDataLine(List<String> lines)
    {
        if (lines == null)
        {
            return null;
        }
        for (String line : lines)
        {
            if (line != null && !line.trim().isEmpty())
            {
                return line;
            }
        }
        return null;
    }

    private char delimiter(String line)
    {
        if (line != null && line.indexOf('\t') >= 0 && line.indexOf(',') < 0)
        {
            return '\t';
        }
        if (line != null && line.indexOf(',') < 0 && line.trim().contains(" "))
        {
            return ' ';
        }
        return ',';
    }

    private List<String> splitRow(String line, char delimiter)
    {
        if (delimiter == ' ')
        {
            List<String> values = new ArrayList<>();
            for (String value : line.trim().split("\\s+"))
            {
                values.add(value);
            }
            return values;
        }
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++)
        {
            char ch = line.charAt(i);
            if (ch == '"')
            {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"')
                {
                    current.append('"');
                    i++;
                }
                else
                {
                    quoted = !quoted;
                }
            }
            else if (ch == delimiter && !quoted)
            {
                values.add(current.toString());
                current.setLength(0);
            }
            else
            {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
    }

    private String csvCell(String value)
    {
        String text = value == null ? "" : value;
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private Path exportRoot()
    {
        String dir = text(faultIdenFileProps.getExportDir());
        if (dir == null)
        {
            throw new ServiceException("缺少数据 export-dir 配置");
        }
        return Paths.get(dir).toAbsolutePath().normalize();
    }

    private String exportFileUrl(String fileName)
    {
        String prefix = text(faultIdenFileProps.getExportFileUrlPrefix());
        if (prefix == null)
        {
            throw new ServiceException("缺少导出文件 URL 前缀配置");
        }
        return (prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix) + "/" + fileName;
    }

    private String safeName(String value)
    {
        String text = value == null ? "warning_detect" : value;
        return text.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String fileMode(String mode)
    {
        if (RAW_SINGLE.equals(mode))
        {
            return "single";
        }
        if ("RAW_ZIP".equals(mode))
        {
            return "zip";
        }
        return mode;
    }

    private String warningSourceUrl(Long sampleId)
    {
        String prefix = text(faultIdenFileProps.getWarningSourceFileUrlPrefix());
        if (prefix == null)
        {
            throw new ServiceException("缺少异常检测数据文件URL前缀配置");
        }
        return (prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix) + "/" + sampleId;
    }

    private List<Long> sampleIds(Map<String, Object> req)
    {
        Object value = first(req, "sampleIds", "sample_ids");
        List<Long> ids = sampleIds(value);
        if (ids.isEmpty())
        {
            throw new ServiceException("请选择至少一个 CSV/TXT 文件");
        }
        return ids;
    }

    private List<Long> sampleIds(Map<String, Object> req, String... names)
    {
        Object value = first(req, names);
        if (value == null)
        {
            return Collections.emptyList();
        }
        return AlgorithmParamReader.sampleIds(value, "sampleIds");
    }

    private List<Long> sampleIds(Object value)
    {
        return AlgorithmParamReader.requiredLongList(value, "sampleIds");
    }

    private Integer featureCol(Map<String, Object> req)
    {
        Object value = first(req, "featureCol", "feature_col", "selectedFeatureCol", "selected_feature_col", "processOrder", "process_order");
        if (value == null)
        {
            return null;
        }
        try
        {
            int col = Integer.parseInt(String.valueOf(value).trim());
            return col > 0 ? col : null;
        }
        catch (Exception e)
        {
            throw new ServiceException("featureCol 必须是正整数");
        }
    }

    private Object first(Map<String, Object> source, String... names)
    {
        return AlgorithmParamReader.first(source, names);
    }

    private Integer intVal(Map<String, Object> source, Integer fallback, String... names)
    {
        Double value = AlgorithmParamReader.optionalNumber(source, fallback == null ? null : fallback.doubleValue(), names);
        return value == null ? fallback : value.intValue();
    }

    private Double numVal(Map<String, Object> source, Double fallback, String... names)
    {
        return AlgorithmParamReader.optionalNumber(source, fallback, names);
    }

    private String text(Object value)
    {
        return AlgorithmParamReader.text(value);
    }
}
