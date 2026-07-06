package com.ruoyi.project4.service;

import com.ruoyi.project4.domain.dto.AugmentReqDTO;
import com.ruoyi.project4.domain.entity.T4BearingAugment;

public interface BearingAugmentService {

    /**
     * 执行数据增强
     * @param sourcePreId 预处理记录id t4_bearing_preprocess.id
     * @param reqDTO 增强入参（内含preprocessResult）
     * @return 增强入库记录
     */
    T4BearingAugment execAugment(Long sourcePreId, AugmentReqDTO reqDTO);
}