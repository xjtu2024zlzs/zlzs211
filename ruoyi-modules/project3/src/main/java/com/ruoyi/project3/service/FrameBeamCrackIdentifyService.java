package com.ruoyi.project3.service;

import com.ruoyi.project3.domain.PageRows;
import com.ruoyi.project3.domain.framebeam.FrameBeamIdentifyRequest;
import com.ruoyi.project3.domain.framebeam.FrameBeamIdentifyResultVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FrameBeamCrackIdentifyService
{
    FrameBeamIdentifyResultVo startTask(FrameBeamIdentifyRequest request);

    FrameBeamIdentifyResultVo importAndStart(MultipartFile file, String objectInfoJson, String paramsJson, String requestId);

    Map<String, Object> uploadDataFile(MultipartFile file, String uploadBatchId, Integer fileIndex, Integer totalFiles, String relativePath);

    Map<String, Object> uploadDataChunk(String uploadId, Integer chunkIndex, Integer chunkCount, String fileName, MultipartFile chunk);

    Map<String, Object> mergeDataChunks(String uploadBatchId, String uploadId, String fileName, Integer totalFiles, String relativePath);

    FrameBeamIdentifyResultVo getTaskResult(String taskId);

    PageRows getHistory(String keyword, String status, String result, Integer pageNum, Integer pageSize);

    FrameBeamIdentifyResultVo getHistoryDetail(String taskId);

    int deleteHistory(String taskId);
}
