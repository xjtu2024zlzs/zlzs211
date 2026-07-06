package com.ruoyi.project4.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.project4.domain.dto.AugmentReqDTO;
import com.ruoyi.project4.domain.dto.PythonApiResult;
import com.ruoyi.project4.domain.entity.T4BearingAugment;
import com.ruoyi.project4.domain.mapper.T4BearingAugmentMapper;
import com.ruoyi.project4.feign.BearingPythonFeignClient;
import com.ruoyi.project4.service.BearingAugmentService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import com.ruoyi.project4.domain.entity.T4BearingPreprocess;
import com.ruoyi.project4.domain.mapper.T4BearingPreprocessMapper;

@Service
public class BearingAugmentServiceImpl implements BearingAugmentService {

    @Resource
    private T4BearingAugmentMapper augmentMapper;

    @Resource
    private BearingPythonFeignClient pythonFeignClient;

    @Resource
    private T4BearingPreprocessMapper preprocessMapper;

    @Override
    public T4BearingAugment execAugment(Long sourcePreId, AugmentReqDTO reqDTO) {
        T4BearingAugment entity = new T4BearingAugment();
        entity.setSourceId(sourcePreId);
        entity.setAugModel(reqDTO.getAugModel());
        entity.setAugScale(reqDTO.getAugScale());
        entity.setBizParams(JSON.toJSONString(reqDTO));
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        try {
            T4BearingPreprocess preprocess = preprocessMapper.selectById(sourcePreId);
            if (preprocess == null) {
                throw new RuntimeException("未找到预处理记录，preprocessId=" + sourcePreId);
            }

            if (!"success".equals(preprocess.getStatus())) {
                throw new RuntimeException("预处理记录不是成功状态，preprocessId=" + sourcePreId);
            }

            Map<String, Object> preprocessResult = JSON.parseObject(preprocess.getBizResult(), Map.class);
            reqDTO.setPreprocessResult(preprocessResult);
            PythonApiResult<Map<String, Object>> resp = pythonFeignClient.augment(reqDTO);
            if (!Integer.valueOf(200).equals(resp.getCode())) {
                throw new RuntimeException("Python数据增强异常：" + resp.getMsg());
            }
            Map<String, Object> pythonResult = resp.getData();
            entity.setBizResult(JSON.toJSONString(pythonResult));
            entity.setStatus("success");
        } catch (Exception e) {
            entity.setBizResult("调用失败：" + e.getMessage());
            entity.setStatus("fail");
        }
        augmentMapper.insert(entity);
        return entity;
    }
}