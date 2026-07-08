package com.ruoyi.project4.service;

import com.ruoyi.project4.domain.dto.PreprocessReqDTO;
import com.ruoyi.project4.domain.entity.T4BearingPreprocess;

public interface BearingPreprocessService {

    /**
     * 执行预处理，调用Python并入库
     * @param sourceRawId 原始数据表id t4_raw_bearing_data.id
     * @param reqDTO 预处理入参
     * @return 入库后的预处理记录
     */
    T4BearingPreprocess execPreprocess(PreprocessReqDTO reqDTO);
}