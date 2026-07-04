package com.ruoyi.qms.api.factory;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.qms.api.RemoteQualityProblemService;
import com.ruoyi.qms.api.domain.QualityProblemDto;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 质量问题远程服务降级处理
 */
@Component
public class RemoteQualityProblemFallbackFactory implements FallbackFactory<RemoteQualityProblemService>
{
    @Override
    public RemoteQualityProblemService create(Throwable throwable)
    {
        return new RemoteQualityProblemService()
        {
            @Override
            public R<List<QualityProblemDto>> listForModule(String moduleCode, String source)
            {
                return R.fail("调用质量问题服务失败：" + throwable.getMessage());
            }
        };
    }
}