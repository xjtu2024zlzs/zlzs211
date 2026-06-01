package com.ruoyi.project3.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project3.service.FaultIdenCatalogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/quality/fault-iden")
public class FaultIdenCatalogController
{
    @Resource
    private FaultIdenCatalogService faultIdenCatalogService;

    @PostMapping("/catalog")
    public AjaxResult catalog(@RequestBody(required = false) Map<String, Object> req)
    {
        String rootPath = text(req, "rootPath");
        String start = text(req, "defaultStartTime");
        Boolean verify = req == null ? false : Boolean.valueOf(String.valueOf(req.getOrDefault("verifyLineCount", "false")));
        return AjaxResult.success(faultIdenCatalogService.catalog(rootPath, start, verify));
    }

    @PostMapping("/catalog/import-numeric-data")
    public AjaxResult importNumericData(@RequestBody(required = false) Map<String, Object> req)
    {
        return AjaxResult.success("导入完成", faultIdenCatalogService.importNumericData(req));
    }

    @PostMapping("/catalog/upload-numeric-data")
    public AjaxResult uploadNumericData(
            @RequestParam(value = "purpose", required = false) String purpose,
            @RequestParam(value = "executionObject", required = false) String executionObject,
            @RequestParam(value = "conditionLabel", required = false) String conditionLabel,
            @RequestParam(value = "bearingNo", required = false) Integer bearingNo,
            @RequestParam(value = "aircraftId", required = false) String airId,
            @RequestParam(value = "subsystemId", required = false) String subId,
            @RequestParam(value = "equipmentId", required = false) String eqpId,
            @RequestParam(value = "componentId", required = false) String cmpId,
            @RequestParam(value = "partId", required = false) String ptId,
            @RequestParam("files") MultipartFile[] files
    )
    {
        return AjaxResult.success("导入完成", faultIdenCatalogService.uploadNumericData(
                purpose,
                executionObject,
                conditionLabel,
                bearingNo,
                airId,
                subId,
                eqpId,
                cmpId,
                ptId,
                files
        ));
    }

    @PostMapping("/catalog/upload-numeric-file")
    public AjaxResult uploadNumericFile(
            @RequestParam(value = "purpose", required = false) String purpose,
            @RequestParam(value = "executionObject", required = false) String executionObject,
            @RequestParam(value = "conditionLabel", required = false) String conditionLabel,
            @RequestParam(value = "bearingNo", required = false) Integer bearingNo,
            @RequestParam(value = "aircraftId", required = false) String airId,
            @RequestParam(value = "subsystemId", required = false) String subId,
            @RequestParam(value = "equipmentId", required = false) String eqpId,
            @RequestParam(value = "componentId", required = false) String cmpId,
            @RequestParam(value = "partId", required = false) String ptId,
            @RequestParam(value = "uploadBatchId", required = false) String uploadBatchId,
            @RequestParam(value = "fileIndex", required = false) Integer fileIndex,
            @RequestParam(value = "totalFiles", required = false) Integer totalFiles,
            @RequestParam("file") MultipartFile file
    )
    {
        return AjaxResult.success("上传完成", faultIdenCatalogService.uploadNumericFile(
                purpose,
                executionObject,
                conditionLabel,
                bearingNo,
                airId,
                subId,
                eqpId,
                cmpId,
                ptId,
                uploadBatchId,
                fileIndex,
                totalFiles,
                file
        ));
    }

    @PostMapping("/catalog/upload-numeric-chunk")
    public AjaxResult uploadNumericChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("chunkCount") Integer chunkCount,
            @RequestParam("fileName") String fileName,
            @RequestParam("chunk") MultipartFile chunk
    )
    {
        return AjaxResult.success("分片上传完成", faultIdenCatalogService.uploadNumericChunk(uploadId, chunkIndex, chunkCount, fileName, chunk));
    }

    @PostMapping("/catalog/merge-numeric-chunks")
    public AjaxResult mergeNumericChunks(
            @RequestParam(value = "purpose", required = false) String purpose,
            @RequestParam(value = "executionObject", required = false) String executionObject,
            @RequestParam(value = "conditionLabel", required = false) String conditionLabel,
            @RequestParam(value = "bearingNo", required = false) Integer bearingNo,
            @RequestParam(value = "aircraftId", required = false) String airId,
            @RequestParam(value = "subsystemId", required = false) String subId,
            @RequestParam(value = "equipmentId", required = false) String eqpId,
            @RequestParam(value = "componentId", required = false) String cmpId,
            @RequestParam(value = "partId", required = false) String ptId,
            @RequestParam(value = "uploadBatchId", required = false) String uploadBatchId,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "totalFiles", required = false) Integer totalFiles
    )
    {
        return AjaxResult.success("合并完成", faultIdenCatalogService.mergeNumericChunks(
                purpose, executionObject, conditionLabel, bearingNo,
                airId, subId, eqpId, cmpId, ptId, uploadBatchId, uploadId, fileName, totalFiles
        ));
    }

    @GetMapping("/catalog/numeric-chunk-status")
    public AjaxResult numericChunkStatus(@RequestParam("uploadId") String uploadId)
    {
        return AjaxResult.success(faultIdenCatalogService.numericChunkStatus(uploadId));
    }

    @GetMapping("/conditions")
    public AjaxResult conditions()
    {
        return AjaxResult.success(faultIdenCatalogService.conditions().get("data"));
    }

    @GetMapping("/bearings")
    public AjaxResult bearings(@RequestParam("conditionLabel") String conditionLabel)
    {
        return AjaxResult.success(faultIdenCatalogService.bearings(conditionLabel).get("data"));
    }

    @GetMapping("/samples")
    public AjaxResult samples(
            @RequestParam(value = "conditionLabel", required = false) String conditionLabel,
            @RequestParam(value = "bearingCode", required = false) String bearingCode,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "aircraftId", required = false) String airId,
            @RequestParam(value = "subsystemId", required = false) String subId,
            @RequestParam(value = "equipmentId", required = false) String eqpId,
            @RequestParam(value = "componentId", required = false) String cmpId,
            @RequestParam(value = "dataUsage", required = false) String dataUsage,
            @RequestParam(value = "uploadBatchId", required = false) String uploadBatchId,
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize
    )
    {
        Map<String, Object> page = faultIdenCatalogService.samples(
                conditionLabel,
                bearingCode,
                keyword,
                airId,
                subId,
                eqpId,
                cmpId,
                dataUsage,
                uploadBatchId,
                pageNum,
                pageSize
        );
        AjaxResult ret = AjaxResult.success();
        ret.put("rows", page.get("rows"));
        ret.put("total", page.get("total"));
        return ret;
    }

    @DeleteMapping("/samples/{sampleId}")
    public AjaxResult deleteSample(@PathVariable("sampleId") Long sampleId)
    {
        return AjaxResult.success("删除完成", faultIdenCatalogService.deleteSample(sampleId));
    }

    private String text(Map<String, Object> req, String key)
    {
        Object value = req == null ? null : req.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }
}
