package com.ruoyi.project4.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.project4.domain.dto.PreprocessReqDTO;
import com.ruoyi.project4.domain.dto.PythonApiResult;
import com.ruoyi.project4.domain.entity.T4BearingPreprocess;
import com.ruoyi.project4.domain.mapper.T4BearingPreprocessMapper;
import com.ruoyi.project4.feign.BearingPythonFeignClient;
import com.ruoyi.project4.service.BearingPreprocessService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import com.ruoyi.project4.domain.entity.CwruFileMeta;
import com.ruoyi.project4.domain.mapper.CwruFileMetaMapper;

@Service
public class BearingPreprocessServiceImpl implements BearingPreprocessService {

    @Resource
    private T4BearingPreprocessMapper preprocessMapper;

    @Resource
    private BearingPythonFeignClient pythonFeignClient;

    @Resource
    private CwruFileMetaMapper cwruFileMetaMapper;

    @Override
    public T4BearingPreprocess execPreprocess(PreprocessReqDTO reqDTO) {
        Integer keyNum = reqDTO.getKeyNum();

        T4BearingPreprocess entity = new T4BearingPreprocess();
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        try {
            if (keyNum == null) {
                throw new RuntimeException("keyNum不能为空");
            }

            CwruFileMeta meta = cwruFileMetaMapper.selectByFileName(String.valueOf(keyNum));
            if (meta == null) {
                throw new RuntimeException("未找到CWRU数据文件，keyNum=" + keyNum);
            }

            String absPath = meta.getFileAbsPath();
            if (absPath == null || absPath.isEmpty()) {
                throw new RuntimeException("file_abs_path为空，keyNum=" + keyNum);
            }

            String dataRootPath;
            int idx1 = absPath.lastIndexOf("\\");
            int idx2 = absPath.lastIndexOf("/");
            int idx = Math.max(idx1, idx2);

            if (idx > 0) {
                dataRootPath = absPath.substring(0, idx);
            } else {
                throw new RuntimeException("file_abs_path格式不正确：" + absPath);
            }

            dataRootPath = dataRootPath.replace("\\", "/");

            reqDTO.setDataRootPath(dataRootPath);
            reqDTO.setKeyNum(keyNum);

            entity.setSourceId(meta.getId());
            entity.setBizParams(JSON.toJSONString(reqDTO));

            PythonApiResult<Map<String, Object>> resp = pythonFeignClient.preprocess(reqDTO);
            if (!Integer.valueOf(200).equals(resp.getCode())) {
                throw new RuntimeException("Python预处理异常：" + resp.getMsg());
            }

            Map<String, Object> pythonResult = resp.getData();
            entity.setBizResult(JSON.toJSONString(pythonResult));
            entity.setStatus("success");
        } catch (Exception e) {
            entity.setBizResult("调用失败：" + e.getMessage());
            entity.setStatus("fail");
        }

        preprocessMapper.insert(entity);
        return entity;
    }
}