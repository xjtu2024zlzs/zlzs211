package com.ruoyi.qms.api;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.qms.api.domain.QualityProblemDto;
import com.ruoyi.qms.api.factory.RemoteQualityProblemFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 质量问题远程服务
 */
@FeignClient(
        contextId = "remoteQualityProblemService",
        value = ServiceNameConstants.QMS_SERVICE,
        fallbackFactory = RemoteQualityProblemFallbackFactory.class
)
public interface RemoteQualityProblemService
{
    /**
     * 查询需要同步到指定模块的质量问题
     *
     * @param moduleCode 模块编码，例如 PROJECT_5
     * @param source 内部调用标识
     * @return 质量问题列表
     */
    @GetMapping("/remote/qualityProblem/listForModule")
    public R<List<QualityProblemDto>> listForModule(
            @RequestParam("moduleCode") String moduleCode,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source
    );
}