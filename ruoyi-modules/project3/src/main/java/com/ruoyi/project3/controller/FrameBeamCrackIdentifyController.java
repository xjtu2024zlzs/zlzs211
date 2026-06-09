package com.ruoyi.project3.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project3.domain.framebeam.FrameBeamIdentifyRequest;
import com.ruoyi.project3.service.FrameBeamCrackIdentifyService;
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
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/service/frame-beam-crack")
public class FrameBeamCrackIdentifyController
{
    @Resource
    private FrameBeamCrackIdentifyService frameBeamCrackIdentifyService;

    @PostMapping("/tasks")
    public AjaxResult startTask(@RequestBody(required = false) FrameBeamIdentifyRequest request)
    {
        return AjaxResult.success(frameBeamCrackIdentifyService.startTask(request));
    }

    @PostMapping(value = "/import-and-start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult importAndStart(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "objectInfo", required = false) String objectInfo,
            @RequestParam(value = "params", required = false) String params,
            @RequestParam(value = "requestId", required = false) String requestId
    )
    {
        return AjaxResult.success(frameBeamCrackIdentifyService.importAndStart(file, objectInfo, params, requestId));
    }

    @PostMapping(value = "/uploads/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult uploadDataFile(
            @RequestParam(value = "uploadBatchId", required = false) String uploadBatchId,
            @RequestParam(value = "fileIndex", required = false) Integer fileIndex,
            @RequestParam(value = "totalFiles", required = false) Integer totalFiles,
            @RequestParam(value = "relativePath", required = false) String relativePath,
            @RequestParam("file") MultipartFile file
    )
    {
        return AjaxResult.success("上传完成", frameBeamCrackIdentifyService.uploadDataFile(file, uploadBatchId, fileIndex, totalFiles, relativePath));
    }

    @PostMapping(value = "/uploads/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult uploadDataChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("chunkCount") Integer chunkCount,
            @RequestParam("fileName") String fileName,
            @RequestParam("chunk") MultipartFile chunk
    )
    {
        return AjaxResult.success("分片上传完成", frameBeamCrackIdentifyService.uploadDataChunk(uploadId, chunkIndex, chunkCount, fileName, chunk));
    }

    @PostMapping(value = "/uploads/merge", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult mergeDataChunks(
            @RequestParam(value = "uploadBatchId", required = false) String uploadBatchId,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "totalFiles", required = false) Integer totalFiles,
            @RequestParam(value = "relativePath", required = false) String relativePath
    )
    {
        return AjaxResult.success("合并完成", frameBeamCrackIdentifyService.mergeDataChunks(uploadBatchId, uploadId, fileName, totalFiles, relativePath));
    }

    @GetMapping("/tasks/{taskId}")
    public AjaxResult getTaskResult(@PathVariable("taskId") String taskId)
    {
        return AjaxResult.success(frameBeamCrackIdentifyService.getTaskResult(taskId));
    }

    @GetMapping("/history")
    public AjaxResult history(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "result", required = false) String result,
            @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize
    )
    {
        return AjaxResult.success(frameBeamCrackIdentifyService.getHistory(keyword, status, result, pageNum, pageSize));
    }

    @GetMapping("/history/{taskId}")
    public AjaxResult historyDetail(@PathVariable("taskId") String taskId)
    {
        return AjaxResult.success(frameBeamCrackIdentifyService.getHistoryDetail(taskId));
    }

    @DeleteMapping("/history/{taskId}")
    public AjaxResult deleteHistory(@PathVariable("taskId") String taskId)
    {
        return AjaxResult.success(frameBeamCrackIdentifyService.deleteHistory(taskId));
    }
}
