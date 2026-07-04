package com.ruoyi.qms.api.factory;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.qms.api.RemoteQualityTaskService;
import com.ruoyi.qms.api.domain.QualityTaskDto;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.qms.api.domain.QualityTaskSubmitDto;
import java.util.List;

/**
 * 质量任务远程服务降级处理
 */
@Component
public class RemoteQualityTaskFallbackFactory implements FallbackFactory<RemoteQualityTaskService>
{
    @Override
    public RemoteQualityTaskService create(Throwable throwable)
    {
        return new RemoteQualityTaskService()
        {
            @Override
            public R<List<QualityTaskDto>> listTaskForModule(String moduleCode, String source)
            {
                return R.fail("调用质量任务服务失败：" + throwable.getMessage());
            }
            @Override
            public R<Boolean> submitTaskResult(QualityTaskSubmitDto submitDto, String source)
            {
                return R.fail("提交质量任务处理结果失败：" + throwable.getMessage());
            }
        };
    }
}