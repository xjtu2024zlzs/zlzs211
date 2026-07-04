package com.ruoyi.qms.api;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.qms.api.domain.QualityTaskDto;
import com.ruoyi.qms.api.factory.RemoteQualityTaskFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.ruoyi.qms.api.domain.QualityTaskSubmitDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

/**
 * 质量任务远程服务
 */
@FeignClient(
        contextId = "remoteQualityTaskService",
        value = ServiceNameConstants.QMS_SERVICE,
        fallbackFactory = RemoteQualityTaskFallbackFactory.class
)
public interface RemoteQualityTaskService
{
    /**
     * 模块提交质量任务处理结果
     *
     * @param submitDto 提交结果
     * @param source 内部调用标识
     * @return 结果
     */
    @PostMapping("/remote/qualityTask/submitResult")
    public R<Boolean> submitTaskResult(
            @RequestBody QualityTaskSubmitDto submitDto,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source
    );
    /**
     * 查询分派给指定模块的质量任务
     *
     * @param moduleCode 模块编码
     * @param source 内部调用标识
     * @return 质量任务列表
     */
    @GetMapping("/remote/qualityTask/listForModule")
    public R<List<QualityTaskDto>> listTaskForModule(
            @RequestParam("moduleCode") String moduleCode,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source
    );
}