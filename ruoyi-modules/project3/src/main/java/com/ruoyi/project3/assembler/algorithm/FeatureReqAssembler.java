package com.ruoyi.project3.assembler.algorithm;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.config.PythonAlgorithmProperties;
import com.ruoyi.project3.config.faultiden.FaultIdenFileProps;
import com.ruoyi.project3.config.faultiden.FaultIdenParaProperties;
import com.ruoyi.project3.domain.algorithm.FaultIdentifyStartRequest;
import com.ruoyi.project3.domain.algorithm.feature.DbExportInfo;
import com.ruoyi.project3.domain.algorithm.feature.FeatureFileInfo;
import com.ruoyi.project3.domain.algorithm.feature.FeatureParams;
import com.ruoyi.project3.domain.algorithm.feature.FeatureReq;
import com.ruoyi.project3.domain.algorithm.feature.FeatureTarget;
import com.ruoyi.project3.domain.algorithm.feature.HierarchyContext;
import com.ruoyi.project3.domain.faultiden.FaultIdenFilePackage;
import com.ruoyi.project3.domain.faultiden.FaultIdenSampleFile;
import com.ruoyi.project3.mapper.FaultIdenSampleMapper;
import com.ruoyi.project3.mapper.MonitorMapper;
import com.ruoyi.project3.service.FaultIdenFileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class FeatureReqAssembler
{
    private static final String USAGE_FEATURE = "FEATURE_ANALYSIS";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String TASK_TIME_PATTERN = "yyyyMMddHHmmssSSS";
    private static final String SELECTED_FILES = "SELECTED_FILES";
    private static final String RAW_SINGLE = "RAW_SINGLE";

    @Resource
    private MonitorMapper monitorMapper;

    @Resource
    private PythonAlgorithmProperties pythonAlgorithmProperties;

    @Resource
    private FaultIdenParaProperties faultIdenParaProperties;

    @Resource
    private FaultIdenFileProps faultIdenFileProps;

    @Resource
    private FaultIdenFileService faultIdenFileService;

    @Resource
    private FaultIdenSampleMapper faultIdenSampleMapper;

    public FeatureReq assemble(FaultIdentifyStartRequest source, String taskId)
    {
        Map<String, Object> params = params(source);
        String nodeId = targetId(source, params);
        FeatureTarget target = parseTarget(nodeId);

        Map<String, Object> node = monitorMapper.sel_node_by_id(nodeId);
        if (node == null || node.isEmpty())
        {
            throw new ServiceException("特征分析对象不存在或已失效");
        }
        target.setTargetName(text(node.get("name")));

        List<Map<String, Object>> path = monitorMapper.sel_bread_nodes(nodeId);
        HierarchyContext ctx = ctx(path == null ? Collections.emptyList() : path, node);
        if (ctx.getAircraftId() == null)
        {
            throw new ServiceException("特征分析对象缺少飞机层级上下文");
        }
        target.setAircraftId(ctx.getAircraftId());

        String mode = dataMode(source, params);
        if (!SELECTED_FILES.equals(mode))
        {
            throw new ServiceException("当前特征分析仅支持 SELECTED_FILES 原始文件选择模式");
        }

        List<Long> ids = sampleIds(source, params);
        if (ids.isEmpty())
        {
            throw new ServiceException("请选择至少一个原始CSV文件");
        }

        String sampleUsage = sampleUsage(params);
        String channel = AlgorithmParamReader.optionalText(params, "channel", "collectChannel");
        if (channel == null)
        {
            channel = faultIdenParaProperties.getDefaultChannel();
        }
        String collectStartTime = AlgorithmParamReader.optionalText(params, "collectStartTime", "collect_start_time");
        String collectEndTime = AlgorithmParamReader.optionalText(params, "collectEndTime", "collect_end_time");
        target.setChannel(channel);
        target.setCollectStartTime(collectStartTime);
        target.setCollectEndTime(collectEndTime);

        FaultIdenFilePackage pack = faultIdenFileService.prepare(taskId, ids, sampleUsage);

        FeatureReq req = new FeatureReq();
        req.setAuthCode(pythonAlgorithmProperties.getFeatureAnalysisAuthCode());
        req.setRequestId("REQ" + new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date()));
        req.setTaskId(taskId);
        req.setAlgorithmVersion(faultIdenParaProperties.getAlgorithmVersion());
        req.setRequestTime(now());
        req.setAircraftId(ctx.getAircraftId());
        req.setAircraftCode(ctx.getAircraftCode());
        req.setTargetType(target.getTargetType());
        req.setTargetId(target.getTargetId());
        req.setTargetName(target.getTargetName());
        req.setHierarchyContext(ctx);
        req.setSignalType(faultIdenParaProperties.getSignalType());
        req.setChannel(channel);
        req.setSourceType(faultIdenParaProperties.getSourceType());
        req.setSamplingRate(number(params, faultIdenParaProperties.getDefaultSamplingRate(), "samplingRate", "sampling_rate"));
        req.setCollectStartTime(collectStartTime);
        req.setCollectEndTime(collectEndTime);
        req.setAnalysisStartTime(number(params, 0D, "analysisStartTime", "analysis_start_time"));
        req.setAnalysisEndTime(number(params, null, "analysisEndTime", "analysis_end_time"));
        req.setDbExportInfo(dbInfo(pack));
        req.setFileInfo(fileInfo(pack));
        req.setParams(algoParams(params, pack));
        req.setFileMode(algorithmFileMode(pack));
        req.setSampleIds(ids);
        req.setBatchPath(pack.getFilePath());
        req.setDatasetId(taskId);
        applyFlatAlgorithmParams(req, params, pack, ids, channel, sampleUsage);
        return req;
    }

    private void applyFlatAlgorithmParams(FeatureReq req, Map<String, Object> params, FaultIdenFilePackage pack, List<Long> ids, String channel, String sampleUsage)
    {
        Double samplingFrequency = number(params, faultIdenParaProperties.getDefaultSamplingRate(), "samplingFrequency", "sampling_frequency", "samplingRate", "sampling_rate");
        Double windowSize = number(params, faultIdenParaProperties.getDefaultWindowSize(), "windowSize", "window_size");
        Double overlapPercent = number(params, faultIdenParaProperties.getDefaultOverlapRate(), "overlapPercent", "overlap_percent", "overlapRate", "overlap_rate");

        req.setSamplingFrequency(samplingFrequency);
        req.setColumnIndex(columnIndex(params, channel));
        req.setWindowSize(windowSize);
        req.setOverlapPercent(overlapPercent);
        req.setRemoveOutliers(bool(params, true, "removeOutliers", "remove_outliers"));
        req.setMaxSeconds(number(params, 5.0D, "maxSeconds", "max_seconds"));

        List<FaultIdenSampleFile> samples = faultIdenSampleMapper.selectSamplesByIds(ids, sampleUsage);
        samples = samples == null ? Collections.emptyList() : new ArrayList<>(samples);
        samples.sort(Comparator.comparing(item -> text(item.getFileName()) == null ? "" : text(item.getFileName())));

        List<String> filePaths = new ArrayList<>();
        List<String> fileUrls = new ArrayList<>();
        for (FaultIdenSampleFile sample : samples)
        {
            String path = text(sample.getSourceFile());
            if (path != null)
            {
                filePaths.add(path);
            }
            if (sample.getId() != null)
            {
                String prefix = prefix(faultIdenFileProps.getSourceFileUrlPrefix());
                if (prefix != null)
                {
                    fileUrls.add(prefix + "/" + sample.getId());
                }
            }
        }

        if (filePaths.size() == 1)
        {
            req.setFilePath(filePaths.get(0));
        }
        else if (!filePaths.isEmpty())
        {
            req.setFilePaths(filePaths);
        }

        if (fileUrls.size() == 1)
        {
            req.setFileUrl(fileUrls.get(0));
        }
        else if (!fileUrls.isEmpty())
        {
            req.setFileUrls(fileUrls);
        }

        if ((req.getFilePath() == null && req.getFileUrl() == null && req.getFilePaths() == null && req.getFileUrls() == null) && pack != null)
        {
            req.setFilePath(text(pack.getFilePath()));
            req.setFileUrl(text(pack.getFileUrl()));
        }
    }

    private Map<String, Object> params(FaultIdentifyStartRequest source)
    {
        if (source == null)
        {
            return Collections.emptyMap();
        }
        Map<String, Object> params = source.getAlgoParams();
        if (params == null)
        {
            params = source.getFeatureParams();
        }
        if (params == null)
        {
            params = source.getParams();
        }
        return params == null ? Collections.emptyMap() : params;
    }

    private String targetId(FaultIdentifyStartRequest source, Map<String, Object> params)
    {
        // target_node_id 兼容旧接口字段；当前前端发送 targetNodeId。
        Object id = first(params, "targetNodeId", "target_node_id");
        if (id == null)
        {
            // selected_object 兼容当前 service.js 对 feature_params 的透传结构。
            id = nested(params, "selected_object", "id");
        }
        if (id == null)
        {
            // selectedObject 兼容早期 camelCase 嵌套对象。
            id = nested(params, "selectedObject", "id");
        }
        if (id == null && source != null && source.getSelected() != null)
        {
            id = source.getSelected().get("id");
        }
        if (id == null && source != null)
        {
            id = source.getBizId();
        }
        if (id == null && source != null)
        {
            id = source.getImportId();
        }
        if (id == null && source != null)
        {
            id = source.getObject_id();
        }

        String nodeId = text(id);
        if (nodeId == null)
        {
            throw new ServiceException("特征分析目标对象不能为空");
        }
        return nodeId;
    }

    private FeatureTarget parseTarget(String nodeId)
    {
        String type = nodeType(nodeId);
        if ("aircraft".equals(type))
        {
            throw new ServiceException("飞机不能直接作为特征分析目标，请选择分系统、设备或组件");
        }

        String targetType;
        if ("subsystem".equals(type))
        {
            targetType = "subsystem";
        }
        else if ("equipment".equals(type))
        {
            targetType = "device";
        }
        else if ("component".equals(type))
        {
            targetType = "component";
        }
        else
        {
            throw new ServiceException("不支持的特征分析目标类型：" + type);
        }

        FeatureTarget target = new FeatureTarget();
        target.setNodeId(nodeId);
        target.setNodeType(type);
        target.setTargetType(targetType);
        target.setTargetId(rawId(nodeId));
        return target;
    }

    private HierarchyContext ctx(List<Map<String, Object>> path, Map<String, Object> node)
    {
        HierarchyContext ret = new HierarchyContext();
        for (Map<String, Object> item : path)
        {
            applyNode(ret, item);
        }
        applyNode(ret, node);
        return ret;
    }

    private void applyNode(HierarchyContext context, Map<String, Object> node)
    {
        if (context == null || node == null)
        {
            return;
        }
        String nodeId = text(node.get("id"));
        String type = text(node.get("node_type"));
        String name = text(node.get("name"));
        String rawId = rawId(nodeId);

        if ("aircraft".equals(type))
        {
            context.setAircraftId(rawId);
            context.setAircraftName(name);
        }
        else if ("subsystem".equals(type))
        {
            context.setSubsystemId(rawId);
            context.setSubsystemName(name);
        }
        else if ("equipment".equals(type))
        {
            context.setDeviceId(rawId);
            context.setDeviceName(name);
        }
        else if ("component".equals(type))
        {
            context.setComponentId(rawId);
            context.setComponentName(name);
        }
    }

    private DbExportInfo dbInfo(FaultIdenFilePackage pack)
    {
        List<Long> ids = packIds(pack);
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("dataSelectionMode", SELECTED_FILES);
        query.put("sampleIds", ids);
        query.put("sourceFileCount", ids.size());
        query.put("fileMode", pack.getFileMode());
        query.put("rawFormat", faultIdenParaProperties.getRawFormat());
        query.put("note", faultIdenParaProperties.getExportNote());

        DbExportInfo info = new DbExportInfo();
        info.setTableName(faultIdenParaProperties.getExportTableName());
        info.setExportTime(now());
        info.setRecordCount(null);
        info.setExportStatus(faultIdenParaProperties.getExportStatus());
        info.setExportFileFormat(pack.getFileType());
        info.setQueryCondition(query);
        return info;
    }

    private FeatureFileInfo fileInfo(FaultIdenFilePackage pack)
    {
        if (pack == null)
        {
            throw new ServiceException("原始文件包不存在");
        }

        boolean single = RAW_SINGLE.equals(pack.getFileMode());
        FeatureFileInfo info = new FeatureFileInfo();
        info.setFileId(pack.getId());
        info.setFileName(pack.getFileName());
        info.setFileType(single ? pack.getFileType() : pack.getFileType());
        info.setFilePath(pack.getFilePath());
        info.setFileUrl(pack.getFileUrl());
        info.setFileSource(single ? faultIdenParaProperties.getSingleFileSource() : "fault_iden_raw_multi");
        info.setFileSize(null);
        info.setEncoding(single ? "UTF-8" : null);
        info.setDelimiter(single ? "," : null);
        info.setHasHeader(false);
        info.setTimeColumn(null);
        info.setIndexColumn(null);
        info.setValueColumn(null);
        info.setUnit(faultIdenParaProperties.getUnit());
        return info;
    }

    private String algorithmFileMode(FaultIdenFilePackage pack)
    {
        if (pack == null)
        {
            return "SINGLE_FILE";
        }
        return RAW_SINGLE.equals(pack.getFileMode()) ? "SINGLE_FILE" : "MULTI_FILE";
    }

    private FeatureParams algoParams(Map<String, Object> params, FaultIdenFilePackage pack)
    {
        String channel = AlgorithmParamReader.optionalText(params, "channel", "collectChannel");
        if (channel == null)
        {
            channel = faultIdenParaProperties.getDefaultChannel();
        }

        FeatureParams out = new FeatureParams();
        out.setWindowSize(number(params, faultIdenParaProperties.getDefaultWindowSize(), "windowSize", "window_size"));
        out.setOverlapRate(number(params, faultIdenParaProperties.getDefaultOverlapRate(), "overlapRate", "overlap_rate"));
        out.setFftWindowSize(number(params, null, "fftWindowSize", "fft_window_size"));
        out.setFftOverlapRate(number(params, null, "fftOverlapRate", "fft_overlap_rate"));
        out.setRawFormat(faultIdenParaProperties.getRawFormat());
        out.setChannel(channel);
        out.setSamplingRate(number(params, faultIdenParaProperties.getDefaultSamplingRate(), "samplingRate", "sampling_rate"));
        out.setSampleIntervalSeconds(faultIdenParaProperties.getSampleIntervalSeconds());
        out.setPointsPerSample(faultIdenParaProperties.getPointsPerSample());
        out.setChannelColumns(faultIdenParaProperties.getChannelColumns());
        out.setFileMode(pack.getFileMode());
        out.setSelectedSampleIds(packIds(pack));
        return out;
    }

    private String dataMode(FaultIdentifyStartRequest source, Map<String, Object> params)
    {
        String mode = text(source == null ? null : source.getDataSelectionMode());
        if (mode == null)
        {
            mode = text(first(params, "dataSelectionMode", "data_selection_mode"));
        }
        return mode;
    }

    private List<Long> packIds(FaultIdenFilePackage pack)
    {
        if (pack == null || pack.getSelectedSampleIds() == null)
        {
            return Collections.emptyList();
        }
        try
        {
            return JSON.parseArray(pack.getSelectedSampleIds(), Long.class);
        }
        catch (Exception e)
        {
            return Collections.emptyList();
        }
    }

    private List<Long> sampleIds(FaultIdentifyStartRequest source, Map<String, Object> params)
    {
        if (source != null && source.getSampleIds() != null && !source.getSampleIds().isEmpty())
        {
            return source.getSampleIds();
        }

        Object value = first(params, "sampleIds", "sample_ids");
        List<Long> ids = new ArrayList<>();
        if (value instanceof List)
        {
            for (Object item : (List<?>) value)
            {
                Long id = longVal(item);
                if (id != null)
                {
                    ids.add(id);
                }
            }
        }
        else if (value instanceof String)
        {
            ids.addAll(sampleIds((String) value));
        }
        return ids;
    }

    private String sampleUsage(Map<String, Object> params)
    {
        String usage = text(first(params, "dataUsage", "data_usage", "sampleUsage", "sample_usage"));
        return usage == null ? USAGE_FEATURE : usage;
    }

    private List<Long> sampleIds(String value)
    {
        return AlgorithmParamReader.requiredLongList(value, "sampleIds");
    }

    private Object first(Map<String, Object> source, String... names)
    {
        return AlgorithmParamReader.first(source, names);
    }

    @SuppressWarnings("unchecked")
    private Object nested(Map<String, Object> source, String objectName, String fieldName)
    {
        return AlgorithmParamReader.nested(source, objectName, fieldName);
    }

    private Double number(Map<String, Object> source, Double fallback, String... names)
    {
        return AlgorithmParamReader.optionalNumber(source, fallback, names);
    }

    private Integer columnIndex(Map<String, Object> source, String channel)
    {
        Double explicit = number(source, null, "columnIndex", "column_index");
        if (explicit != null)
        {
            return explicit.intValue();
        }
        return 0;
    }

    private Boolean bool(Map<String, Object> source, Boolean fallback, String... names)
    {
        Object value = first(source, names);
        if (value == null)
        {
            return fallback;
        }
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        String text = text(value);
        if (text == null)
        {
            return fallback;
        }
        return Boolean.parseBoolean(text);
    }

    private Long longVal(Object value)
    {
        return AlgorithmParamReader.longVal(value, "sampleIds");
    }

    private String nodeType(String nodeId)
    {
        return AlgorithmParamReader.nodeType(nodeId);
    }

    private String rawId(String nodeId)
    {
        return AlgorithmParamReader.rawId(nodeId);
    }

    private String text(Object value)
    {
        return AlgorithmParamReader.text(value);
    }

    private String now()
    {
        return new SimpleDateFormat(DATE_TIME_PATTERN).format(new Date());
    }

    private String prefix(String value)
    {
        String text = text(value);
        if (text == null)
        {
            return null;
        }
        while (text.endsWith("/"))
        {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
