package com.ruoyi.project4.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.project4.domain.dto.DiagnoseReqDTO;
import com.ruoyi.project4.domain.dto.PythonApiResult;
import com.ruoyi.project4.domain.entity.T4BearingDiagnose;
import com.ruoyi.project4.domain.mapper.T4BearingDiagnoseMapper;
import com.ruoyi.project4.feign.BearingPythonFeignClient;
import com.ruoyi.project4.service.BearingDiagnoseService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class BearingDiagnoseServiceImpl implements BearingDiagnoseService {

    @Resource
    private T4BearingDiagnoseMapper diagnoseMapper;

    @Resource
    private BearingPythonFeignClient pythonFeignClient;

    @Override
    public T4BearingDiagnose execDiagnose(Long sourceRawId, DiagnoseReqDTO reqDTO) {
        T4BearingDiagnose entity = new T4BearingDiagnose();
        entity.setSourceId(sourceRawId);
        entity.setWX1(reqDTO.getWx1());
        entity.setWX2(reqDTO.getWx2());
        entity.setBizParams(JSON.toJSONString(reqDTO));
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        try {
            PythonApiResult<Map<String, Object>> resp = pythonFeignClient.diagnose(reqDTO);
            if (!Integer.valueOf(200).equals(resp.getCode())) {
                throw new RuntimeException("Python故障诊断异常：" + resp.getMsg());
            }
            Map<String, Object> pythonResult = resp.getData();
            entity.setBizResult(JSON.toJSONString(pythonResult));
            entity.setStatus("success");
        } catch (Exception e) {
            entity.setBizResult("调用失败：" + e.getMessage());
            entity.setStatus("fail");
        }
        diagnoseMapper.insert(entity);
        return entity;
    }
}