package com.ruoyi.project3.service;

import com.ruoyi.project3.domain.faultiden.FaultIdenFilePackage;
import com.ruoyi.project3.domain.faultiden.FaultIdenSampleFile;

import java.nio.file.Path;
import java.util.List;

public interface FaultIdenFileService
{
    FaultIdenFilePackage prepare(String taskId, List<Long> sampleIds);
    FaultIdenFilePackage prepare(String taskId, List<Long> sampleIds, String dataUsage);
    FaultIdenSampleFile sample(Long sampleId);
    Path sourceFile(Long sampleId);
    Path exportFile(String fileName);
}
