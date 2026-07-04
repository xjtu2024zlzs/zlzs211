package com.ruoyi.project4.service;

import com.ruoyi.project4.domain.dto.DiagnoseReqDTO;
import com.ruoyi.project4.domain.entity.T4BearingDiagnose;

public interface BearingDiagnoseService {

    /**
     * 执行故障诊断
     * @param sourceRawId 原始数据id
     * @param reqDTO 诊断入参
     * @return 诊断入库记录
     */
    T4BearingDiagnose execDiagnose(Long sourceRawId, DiagnoseReqDTO reqDTO);
}