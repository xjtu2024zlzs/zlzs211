package com.ruoyi.project4.feign;

import com.ruoyi.project4.domain.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

/**
 * 调用Python FastAPI算法服务
 */
@FeignClient(
        name = "bearing-python-service",
        url = "${python.api.url:http://127.0.0.1:8000}"
)
public interface BearingPythonFeignClient {

    /**
     * 1. 数据预处理接口
     */
    @PostMapping("/api/preprocess")
    PythonApiResult<Map<String, Object>> preprocess(@RequestBody PreprocessReqDTO req);

    /**
     * 2. 数据增强接口
     */
    @PostMapping("/api/augment")
    PythonApiResult<Map<String, Object>> augment(@RequestBody AugmentReqDTO req);

    /**
     * 3. 特征融合接口
     */
    @PostMapping("/api/fusion")
    PythonApiResult<Map<String, Object>> featureFusion(@RequestBody FeatureFusionReqDTO req);

    /**
     * 4. 故障诊断接口
     */
    @PostMapping("/api/diagnose")
    PythonApiResult<Map<String, Object>> diagnose(@RequestBody DiagnoseReqDTO req);

    /**
     * 5. 一站式流水线接口
     */
    @PostMapping("/api/pipeline")
    PythonApiResult<Map<String, Object>> pipeline(
            @RequestBody DiagnoseReqDTO req,
            @RequestParam(value = "aug_model", defaultValue = "scale") String augModel,
            @RequestParam(value = "aug_scale", defaultValue = "2") Integer augScale
    );

    /**
     * 健康检测接口
     */
    @GetMapping("/health")
    PythonApiResult<Map<String, Object>> health();
}