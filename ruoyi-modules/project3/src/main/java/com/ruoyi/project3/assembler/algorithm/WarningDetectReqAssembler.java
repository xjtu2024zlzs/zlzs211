package com.ruoyi.project3.assembler.algorithm;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.config.PythonAlgorithmProperties;
import com.ruoyi.project3.config.faultiden.FaultIdenFileProps;
import com.ruoyi.project3.domain.algorithm.warning.WarningDetectReq;
import com.ruoyi.project3.domain.faultiden.FaultIdenFilePackage;
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

        FaultIdenFilePackage pack = faultIdenFileService.prepare(taskId, sampleIds(reqMap), USAGE_WARNING);

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
        req.setHierarchyContext(ctx(reqMap, partId, partName, processId, processName));
        req.setFileInfo(fileInfo(pack));
        return req;
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

    private List<Long> sampleIds(Object value)
    {
        return AlgorithmParamReader.requiredLongList(value, "sampleIds");
    }

    private Object first(Map<String, Object> source, String... names)
    {
        return AlgorithmParamReader.first(source, names);
    }

    private String text(Object value)
    {
        return AlgorithmParamReader.text(value);
    }
}
