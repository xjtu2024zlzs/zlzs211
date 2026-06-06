package com.ruoyi.project3.mapper;

import com.ruoyi.project3.domain.algorithm.AlgTaskResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlgTaskMapper
{
    AlgTaskResult getByTaskId(@Param("taskId") String taskId);

    AlgTaskResult getRunningTaskByRemark(
            @Param("taskType") String taskType,
            @Param("remark") String remark
    );

    List<AlgTaskResult> getTasksByFlowTaskId(@Param("flowTaskId") String flowTaskId);

    int deleteByFlowTaskId(@Param("flowTaskId") String flowTaskId);

    AlgTaskResult getLatestFeatureProcessingByFlowTaskId(@Param("flowTaskId") String flowTaskId);

    int insertTask(AlgTaskResult record);

    int updateTask(AlgTaskResult record);

    AlgTaskResult getLatestFeatureDataTask(
            @Param("bizId") String bizId,
            @Param("bizLevel") String bizLevel
    );

    List<AlgTaskResult> getWarningResult(
            @Param("status") String status,
            @Param("targetType") String targetType,
            @Param("targetId") String targetId,
            @Param("beginTime") String beginTime,
            @Param("endTime") String endTime,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    Long countWarningResult(
            @Param("status") String status,
            @Param("targetType") String targetType,
            @Param("targetId") String targetId,
            @Param("beginTime") String beginTime,
            @Param("endTime") String endTime
    );

    List<AlgTaskResult> getKqcMiningResult(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    Long countKqcMiningResult(
            @Param("keyword") String keyword,
            @Param("status") String status
    );

    List<AlgTaskResult> getIdentifyResult(
            @Param("keyword") String keyword,
            @Param("taskType") String taskType,
            @Param("status") String status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    Long countIdentifyResult(
            @Param("keyword") String keyword,
            @Param("taskType") String taskType,
            @Param("status") String status
    );

    List<AlgTaskResult> getKeyProcessResult(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("targetType") String targetType,
            @Param("targetId") String targetId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    Long countKeyProcessResult(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("targetType") String targetType,
            @Param("targetId") String targetId
    );
}
