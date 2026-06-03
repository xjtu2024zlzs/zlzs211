package com.ruoyi.project3.assembler.algorithm;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.config.PythonAlgorithmProperties;
import com.ruoyi.project3.config.faultiden.FaultIdenFileProps;
import com.ruoyi.project3.domain.faultiden.FaultIdenFilePackage;
import com.ruoyi.project3.domain.algorithm.predict.PredictReq;
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
public class PredictReqAssembler
{
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String USAGE_PREDICT = "FAULT_PREDICT";
    private static final String RAW_SINGLE = "RAW_SINGLE";

    @Resource
    private MonitorMapper monitorMapper;

    @Resource
    private FaultIdenFileService faultIdenFileService;

    @Resource
    private PythonAlgorithmProperties pythonAlgorithmProperties;

    @Resource
    private FaultIdenFileProps faultIdenFileProps;

    public PredictReq assemble(Map<String, Object> reqMap, String taskId, String requestId)
    {
        Map<String, Object> params = params(reqMap);
        String nodeId = nodeId(reqMap, params);
        Map<String, Object> node = monitorMapper.sel_node_by_id(nodeId);
        if (node == null || node.isEmpty())
        {
            throw new ServiceException("故障预测对象不存在或已失效");
        }

        String nodeType = text(node.get("node_type"));
        if ("aircraft".equals(nodeType))
        {
            throw new ServiceException("飞机不能直接作为故障预测目标，请选择分系统、设备或组件");
        }

        List<Map<String, Object>> path = monitorMapper.sel_bread_nodes(nodeId);
        Map<String, Object> ctx = ctx(path == null ? Collections.emptyList() : path, node);
        if (text(ctx.get("aircraftId")) == null)
        {
            throw new ServiceException("故障预测对象缺少飞机层级上下文");
        }

        FaultIdenFilePackage pack = faultIdenFileService.prepare(taskId, sampleIds(reqMap, params), USAGE_PREDICT);
        Map<String, Object> fileInfo = fileInfo(pack);

        PredictReq req = new PredictReq();
        req.setAuthCode(pythonAlgorithmProperties.getPredictAuthCode());
        req.setRequestId(requestId);
        req.setTaskId(taskId);
        req.setRequestTime(new SimpleDateFormat(DATE_TIME_PATTERN).format(new Date()));
        req.setAircraftId(text(ctx.get("aircraftId")));
        req.setTargetType(targetType(nodeType));
        req.setTargetId(rawId(nodeId));
        req.setTargetName(text(node.get("name")));
        req.setHierarchyContext(ctx);
        req.setFileInfo(fileInfo);
        return req;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> params(Map<String, Object> req)
    {
        Object params = first(req, "params");
        return params instanceof Map ? (Map<String, Object>) params : Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private String nodeId(Map<String, Object> req, Map<String, Object> params)
    {
        // target_node_id 兼容旧接口字段。
        Object id = first(req, "targetNodeId", "target_node_id");
        if (id == null)
        {
            id = first(params, "targetNodeId", "target_node_id");
        }
        Object selected = first(req, "selected_object", "selectedObject");
        if (id == null && selected instanceof Map)
        {
            id = ((Map<String, Object>) selected).get("id");
        }
        if (id == null)
        {
            // 兼容早期按 targetType + targetId 提交的调用方；当前前端不再使用。
            Object targetId = first(req, "targetId", "target_id");
            Object targetType = first(req, "targetType", "target_type", "targetLevel", "target_level");
            if (targetId != null && targetType != null)
            {
                id = ("device".equals(String.valueOf(targetType)) ? "equipment" : String.valueOf(targetType)) + ":" + targetId;
            }
        }

        String nodeId = text(id);
        if (nodeId == null)
        {
            throw new ServiceException("故障预测目标对象不能为空");
        }
        if (!nodeId.contains(":"))
        {
            throw new ServiceException("故障预测目标对象格式不正确");
        }
        return nodeId;
    }

    private Map<String, Object> fileInfo(FaultIdenFilePackage pack)
    {
        if (pack == null || text(pack.getFileUrl()) == null)
        {
            throw new ServiceException("预测数据文件准备失败");
        }
        List<Long> ids = sampleIds(pack.getSelectedSampleIds());
        Long sampleId = RAW_SINGLE.equals(pack.getFileMode()) && !ids.isEmpty() ? ids.get(0) : null;

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("fileId", sampleId == null ? pack.getId() : sampleId);
        if (sampleId != null)
        {
            out.put("sampleId", sampleId);
        }
        out.put("fileName", pack.getFileName());
        out.put("fileType", pack.getFileType());
        out.put("fileMode", fileMode(pack.getFileMode()));
        out.put("fileUrl", sampleId == null ? pack.getFileUrl() : predictSourceUrl(sampleId));
        out.put("selectedSampleIds", ids);
        out.put("dataUsage", pack.getDataUsage());
        return out;
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

    private String predictSourceUrl(Long sampleId)
    {
        String prefix = text(faultIdenFileProps.getPredictSourceFileUrlPrefix());
        if (prefix == null)
        {
            throw new ServiceException("缺少预测数据文件URL前缀配置");
        }
        return (prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix) + "/" + sampleId;
    }

    private List<Long> sampleIds(Map<String, Object> req, Map<String, Object> params)
    {
        Object value = first(req, "sampleIds", "sample_ids");
        if (value == null)
        {
            value = first(params, "sampleIds", "sample_ids");
        }
        List<Long> ids = sampleIds(value);
        if (ids.isEmpty())
        {
            throw new ServiceException("请选择至少一个CSV/TXT文件");
        }
        return ids;
    }

    private List<Long> sampleIds(Object value)
    {
        return AlgorithmParamReader.requiredLongList(value, "sampleIds");
    }

    private Map<String, Object> ctx(List<Map<String, Object>> path, Map<String, Object> node)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        List<String> bread = new ArrayList<>();
        for (Map<String, Object> item : path)
        {
            apply(out, item);
            Object name = item.get("name");
            if (name != null)
            {
                bread.add(String.valueOf(name));
            }
        }
        apply(out, node);
        out.put("nodeId", text(node.get("id")));
        out.put("nodeType", text(node.get("node_type")));
        out.put("breadcrumb", bread);
        return out;
    }

    private void apply(Map<String, Object> ctx, Map<String, Object> node)
    {
        String type = text(node.get("node_type"));
        String id = rawId(text(node.get("id")));
        String name = text(node.get("name"));
        if ("aircraft".equals(type))
        {
            ctx.put("aircraftId", id);
            ctx.put("aircraftName", name);
        }
        else if ("subsystem".equals(type))
        {
            ctx.put("subsystemId", id);
            ctx.put("subsystemName", name);
        }
        else if ("equipment".equals(type))
        {
            ctx.put("deviceId", id);
            ctx.put("deviceName", name);
        }
        else if ("component".equals(type))
        {
            ctx.put("componentId", id);
            ctx.put("componentName", name);
        }
    }

    private String targetType(String nodeType)
    {
        return "equipment".equals(nodeType) ? "device" : nodeType;
    }

    private Object first(Map<String, Object> source, String... names)
    {
        return AlgorithmParamReader.first(source, names);
    }

    private String rawId(String nodeId)
    {
        return AlgorithmParamReader.rawId(nodeId);
    }

    private String text(Object value)
    {
        return AlgorithmParamReader.text(value);
    }

}
