package com.ruoyi.project3.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface FaultIdenCatalogService
{
    Map<String, Object> catalog(String rootPath, String defaultStartTime, Boolean verifyLineCount);
    Map<String, Object> importNumericData(Map<String, Object> req);
    Map<String, Object> uploadNumericData(String purpose, String executionObject, String conditionLabel, Integer bearingNo,
                                          String airId, String subId, String eqpId, String cmpId,
                                          String ptId,
                                          MultipartFile[] files);
    Map<String, Object> uploadNumericFile(String purpose, String executionObject, String conditionLabel, Integer bearingNo,
                                          String airId, String subId, String eqpId, String cmpId,
                                          String ptId, String uploadBatchId, Integer fileIndex, Integer totalFiles,
                                          MultipartFile file, String relativePath);
    Map<String, Object> uploadNumericApi(Map<String, Object> req);
    Map<String, Object> uploadNumericChunk(String uploadId, Integer chunkIndex, Integer chunkCount, String fileName, MultipartFile chunk);
    Map<String, Object> mergeNumericChunks(String purpose, String executionObject, String conditionLabel, Integer bearingNo,
                                           String airId, String subId, String eqpId, String cmpId,
                                           String ptId, String uploadBatchId, String uploadId, String fileName,
                                           Integer totalFiles, String relativePath);
    Map<String, Object> numericChunkStatus(String uploadId);
    Map<String, Object> conditions();
    Map<String, Object> bearings(String conditionLabel);
    Map<String, Object> samples(String conditionLabel, String bearingCode, String keyword,
                                String airId, String subId, String eqpId, String cmpId,
                                String partId,
                                String dataUsage, String uploadBatchId, Integer pageNum, Integer pageSize);
    Map<String, Object> deleteSample(Long sampleId);
    Map<String, Object> updateSampleDataUsage(Long sampleId, String dataUsage);
}
