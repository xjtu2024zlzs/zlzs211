package com.ruoyi.project3.mapper;

import com.ruoyi.project3.domain.faultiden.FaultIdenFilePackage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FaultIdenFilePackageMapper
{
    int inFaultIdenFilePackage(FaultIdenFilePackage filePackage);

    FaultIdenFilePackage seFaultIdenFilePackageById(@Param("id") Long id);

    FaultIdenFilePackage seFaultIdenFilePackageByTaskId(@Param("taskId") String taskId);

    List<FaultIdenFilePackage> selectByTaskIds(@Param("taskIds") List<String> taskIds);

    int deleteByIds(@Param("ids") List<Long> ids);
}
