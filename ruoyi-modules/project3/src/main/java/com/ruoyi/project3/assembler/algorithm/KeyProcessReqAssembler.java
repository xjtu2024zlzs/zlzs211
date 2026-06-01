package com.ruoyi.project3.assembler.algorithm;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.config.PythonAlgorithmProperties;
import com.ruoyi.project3.domain.algorithm.FaultIdentifyStartRequest;
import com.ruoyi.project3.domain.faultiden.FaultIdenFilePackage;
import com.ruoyi.project3.mapper.MonitorMapper;
import com.ruoyi.project3.service.FaultIdenFileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class KeyProcessReqAssembler
{
    private static final String USAGE_FEATURE = "FEATURE_ANALYSIS";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String SELECTED_FILES = "SELECTED_FILES";
    private static final String RAW_SINGLE = "RAW_SINGLE";

    @Resource
    private MonitorMapper monitorMapper;

    @Resource
    private PythonAlgorithmProperties pythonAlgorithmProperties;

    @Resource
    private FaultIdenFileService faultIdenFileService;

    public Map<String, Object> assemble(FaultIdentifyStartRequest source, String taskId, String requestId)
    {
        Map<String, Object> params = params(source);
        String nodeId = targetId(source, params);
        Map<String, Object> target = target(nodeId);

        Map<String, Object> node = monitorMapper.sel_node_by_id(nodeId);
        if (node == null || node.isEmpty())
        {
            throw new ServiceException("关键工序识别对象不存在或已失效");
        }
        target.put("targetName", text(node.get("name")));

        List<Map<String, Object>> path = monitorMapper.sel_bread_nodes(nodeId);
        Map<String, Object> ctx = ctx(path == null ? Collections.emptyList() : path, node);
        if (ctx.get("aircraftId") == null)
        {
            throw new ServiceException("关键工序识别对象缺少飞机层级上下文");
        }

        String mode = dataMode(source, params);
        if (!SELECTED_FILES.equals(mode))
        {
            throw new ServiceException("当前关键工序识别仅支持 SELECTED_FILES 原始文件选择模式");
        }

        List<Long> ids = sampleIds(source, params);
        if (ids.isEmpty())
        {
            throw new ServiceException("请选择至少一个原始 CSV/TXT 文件");
        }

        FaultIdenFilePackage pack = faultIdenFileService.prepare(taskId, ids, dataUsage(params));

        Map<String, Object> req = new LinkedHashMap<>();
        req.put("authCode", pythonAlgorithmProperties.getKeyProcessAuthCode());
        req.put("requestId", requestId);
        req.put("taskId", taskId);
        req.put("algorithmVersion", text(first(params, "algorithmVersion", "algorithm_version")) == null ? "v1.0" : text(first(params, "algorithmVersion", "algorithm_version")));
        req.put("requestTime", now());
        req.put("targetType", target.get("targetType"));
        req.put("targetId", target.get("targetId"));
        req.put("targetName", target.get("targetName"));
        req.put("targetNodeId", nodeId);
        req.put("hierarchyContext", ctx);
        req.put("fileInfo", fileInfo(pack));
        req.put("dbExportInfo", dbInfo(pack));
        req.put("params", algoParams(params, pack));
        return req;
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
            // selected_object 兼容 warning.vue 关键工序识别参数结构。
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
            throw new ServiceException("关键工序识别目标对象不能为空");
        }
        return nodeId;
    }

    private Map<String, Object> target(String nodeId)
    {
        String type = nodeType(nodeId);
        if ("aircraft".equals(type))
        {
            throw new ServiceException("飞机不能直接作为关键工序识别目标，请选择分系统、设备、组件或零件");
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
        else if ("part_template".equals(type) || "part".equals(type))
        {
            targetType = "part";
        }
        else
        {
            throw new ServiceException("不支持的关键工序识别目标类型：" + type);
        }

        Map<String, Object> target = new LinkedHashMap<>();
        target.put("targetType", targetType);
        target.put("targetId", rawId(nodeId));
        return target;
    }

    private Map<String, Object> ctx(List<Map<String, Object>> path, Map<String, Object> node)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        List<String> breadcrumb = new ArrayList<>();
        for (Map<String, Object> item : path)
        {
            applyNode(out, breadcrumb, item);
        }
        applyNode(out, breadcrumb, node);
        out.put("breadcrumb", breadcrumb);
        return out;
    }

    private void applyNode(Map<String, Object> context, List<String> breadcrumb, Map<String, Object> node)
    {
        if (context == null || node == null)
        {
            return;
        }
        String nodeId = text(node.get("id"));
        String type = text(node.get("node_type"));
        String name = text(node.get("name"));
        String id = rawId(nodeId);
        if (name != null && !breadcrumb.contains(name))
        {
            breadcrumb.add(name);
        }

        if ("aircraft".equals(type))
        {
            context.put("aircraftId", id);
            context.put("aircraftName", name);
        }
        else if ("subsystem".equals(type))
        {
            context.put("subsystemId", id);
            context.put("subsystemName", name);
        }
        else if ("equipment".equals(type))
        {
            context.put("deviceId", id);
            context.put("deviceName", name);
            context.put("equipmentId", id);
            context.put("equipmentName", name);
        }
        else if ("component".equals(type))
        {
            context.put("componentId", id);
            context.put("componentName", name);
        }
        else if ("part_template".equals(type) || "part".equals(type))
        {
            context.put("partId", id);
            context.put("partName", name);
        }
    }

    private Map<String, Object> dbInfo(FaultIdenFilePackage pack)
    {
        List<Long> ids = packIds(pack);
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("dataSelectionMode", SELECTED_FILES);
        query.put("sampleIds", ids);
        query.put("sourceFileCount", ids.size());
        query.put("fileMode", pack.getFileMode());

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("exportTime", now());
        info.put("exportFileFormat", pack.getFileType());
        info.put("queryCondition", query);
        return info;
    }

    private Map<String, Object> fileInfo(FaultIdenFilePackage pack)
    {
        if (pack == null || text(pack.getFileUrl()) == null)
        {
            throw new ServiceException("关键工序识别数据文件缺少 fileUrl");
        }

        boolean single = RAW_SINGLE.equals(pack.getFileMode());
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("fileId", pack.getId());
        info.put("fileName", pack.getFileName());
        info.put("fileType", single ? pack.getFileType() : "zip");
        info.put("fileUrl", pack.getFileUrl());
        info.put("fileMode", pack.getFileMode());
        info.put("dataUsage", pack.getDataUsage());
        info.put("encoding", single ? "UTF-8" : null);
        info.put("delimiter", single ? "," : null);
        return info;
    }

    private Map<String, Object> algoParams(Map<String, Object> params, FaultIdenFilePackage pack)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("dataSelectionMode", SELECTED_FILES);
        out.put("sampleIds", packIds(pack));
        out.put("fileMode", pack.getFileMode());
        out.put("topN", number(params, 3D, "topN", "top_n"));
        out.put("threshold", number(params, 0.7D, "threshold"));
        out.put("channel", text(first(params, "channel", "collectChannel")));
        out.put("samplingRate", number(params, 25600D, "samplingRate", "sampling_rate"));
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

    private String dataUsage(Map<String, Object> params)
    {
        String usage = text(first(params, "dataUsage", "data_usage"));
        return usage == null ? USAGE_FEATURE : usage;
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
}
