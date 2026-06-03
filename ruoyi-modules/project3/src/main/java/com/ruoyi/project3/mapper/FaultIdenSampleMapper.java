package com.ruoyi.project3.mapper;

import com.ruoyi.project3.domain.faultiden.FaultIdenSampleFile;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FaultIdenSampleMapper
{
    int inFaultIdenSample(FaultIdenSampleFile sample);

    FaultIdenSampleFile seFaultIdenSampleById(@Param("id") Long id);

    FaultIdenSampleFile selectByUniqueKey(
            @Param("conditionLabel") String conditionLabel,
            @Param("bearingCode") String bearingCode,
            @Param("fileName") String fileName,
            @Param("dataUsage") String dataUsage
    );

    List<Map<String, Object>> selectConditions();

    List<Map<String, Object>> selectBearingsByConditionLabel(@Param("conditionLabel") String conditionLabel);

    List<FaultIdenSampleFile> selectSamplesByConditionAndBearing(
            @Param("conditionLabel") String conditionLabel,
            @Param("bearingCode") String bearingCode,
            @Param("dataUsage") String dataUsage,
            @Param("uploadBatchId") String uploadBatchId,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );
    Long countSamplesByConditionAndBearing(
            @Param("conditionLabel") String conditionLabel,
            @Param("bearingCode") String bearingCode,
            @Param("dataUsage") String dataUsage,
            @Param("uploadBatchId") String uploadBatchId,
            @Param("keyword") String keyword
    );

    List<FaultIdenSampleFile> selectSamplesByObject(
            @Param("targetLevel") String tgtLv,
            @Param("targetId") String tgtId,
            @Param("aircraftId") String airId,
            @Param("subsystemId") String subId,
            @Param("equipmentId") String eqpId,
            @Param("componentId") String cmpId,
            @Param("dataUsage") String dataUsage,
            @Param("uploadBatchId") String uploadBatchId,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    Long countSamplesByObject(
            @Param("targetLevel") String tgtLv,
            @Param("targetId") String tgtId,
            @Param("aircraftId") String airId,
            @Param("subsystemId") String subId,
            @Param("equipmentId") String eqpId,
            @Param("componentId") String cmpId,
            @Param("dataUsage") String dataUsage,
            @Param("uploadBatchId") String uploadBatchId,
            @Param("keyword") String keyword
    );

    List<FaultIdenSampleFile> selectSamplesByIds(@Param("ids") List<Long> ids, @Param("dataUsage") String dataUsage);

    List<FaultIdenSampleFile> selectAllSamples(
            @Param("dataUsage") String dataUsage,
            @Param("uploadBatchId") String uploadBatchId,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    Long countAllSamples(@Param("dataUsage") String dataUsage, @Param("uploadBatchId") String uploadBatchId, @Param("keyword") String keyword);

    int deleteSampleById(@Param("id") Long id);

    int deleteSamplesByIds(@Param("ids") List<Long> ids);

    Long countByConditionBearingSampleNo(
            @Param("conditionLabel") String conditionLabel,
            @Param("bearingCode") String bearingCode,
            @Param("sampleNo") Integer sampleNo,
            @Param("dataUsage") String dataUsage
    );
}
