package com.ruoyi.project4.service;

import com.ruoyi.project4.domain.dto.FeatureFusionReqDTO;
import com.ruoyi.project4.domain.entity.T4BearingFeatureFusion;

public interface BearingFeatureFusionService {

    /**
     * 执行特征融合
     * @param sourcePreId 预处理记录id
     * @param reqDTO 融合入参
     * @return 融合入库记录
     */
    T4BearingFeatureFusion execFusion(Long sourcePreId, FeatureFusionReqDTO reqDTO);
}