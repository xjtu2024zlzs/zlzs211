package com.ruoyi.project4.service;

import com.ruoyi.project4.domain.FdDiagnosisResult;

import java.util.List;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project4.domain.dto.DiagnosisRunDto;
/**
 * 鏁呴殰璇婃柇-璇婃柇缁撴灉Service鎺ュ彛
 *
 * @author ruoyi
 * @date 2026-05-29
 */
public interface IFdDiagnosisResultService
{
    AjaxResult runDiagnosis(DiagnosisRunDto dto);
    /**
     * 鏌ヨ鏁呴殰璇婃柇-璇婃柇缁撴灉
     *
     * @param diagnosisId 璇婃柇ID
     * @return 鏁呴殰璇婃柇-璇婃柇缁撴灉
     */
    FdDiagnosisResult selectFdDiagnosisResultByDiagnosisId(Long diagnosisId);

    /**
     * 鏌ヨ鏁呴殰璇婃柇-璇婃柇缁撴灉鍒楄〃
     *
     * @param fdDiagnosisResult 鏁呴殰璇婃柇-璇婃柇缁撴灉
     * @return 鏁呴殰璇婃柇-璇婃柇缁撴灉闆嗗悎
     */
    List<FdDiagnosisResult> selectFdDiagnosisResultList(FdDiagnosisResult fdDiagnosisResult);

    /**
     * 鏂板鏁呴殰璇婃柇-璇婃柇缁撴灉
     *
     * @param fdDiagnosisResult 鏁呴殰璇婃柇-璇婃柇缁撴灉
     * @return 缁撴灉
     */
    int insertFdDiagnosisResult(FdDiagnosisResult fdDiagnosisResult);

    /**
     * 淇敼鏁呴殰璇婃柇-璇婃柇缁撴灉
     *
     * @param fdDiagnosisResult 鏁呴殰璇婃柇-璇婃柇缁撴灉
     * @return 缁撴灉
     */
    int updateFdDiagnosisResult(FdDiagnosisResult fdDiagnosisResult);

    /**
     * 鎵归噺鍒犻櫎鏁呴殰璇婃柇-璇婃柇缁撴灉
     *
     * @param diagnosisIds 闇€瑕佸垹闄ょ殑璇婃柇ID
     * @return 缁撴灉
     */
    int deleteFdDiagnosisResultByDiagnosisIds(Long[] diagnosisIds);

    /**
     * 鍒犻櫎鏁呴殰璇婃柇-璇婃柇缁撴灉淇℃伅
     *
     * @param diagnosisId 璇婃柇ID
     * @return 缁撴灉
     */
    int deleteFdDiagnosisResultByDiagnosisId(Long diagnosisId);
}
