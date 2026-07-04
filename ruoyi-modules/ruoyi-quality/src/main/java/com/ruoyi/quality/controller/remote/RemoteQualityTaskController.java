package com.ruoyi.quality.controller.remote;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.qms.api.domain.QualityTaskDto;
import com.ruoyi.quality.service.IQmsQualityTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.qms.api.domain.QualityTaskSubmitDto;
import java.util.List;

/**
 * 质量任务远程接口
 */
@RestController
@RequestMapping("/remote/qualityTask")
public class RemoteQualityTaskController
{
    /**
     * 模块提交质量任务处理结果
     */
    @InnerAuth
    @PostMapping("/submitResult")
    public R<Boolean> submitResult(@RequestBody QualityTaskSubmitDto submitDto)
    {
        qmsQualityTaskService.submitQualityTaskResult(submitDto);
        return R.ok(true);
    }
    @Autowired
    private IQmsQualityTaskService qmsQualityTaskService;

    /**
     * 查询分派给指定模块的任务
     */
    @InnerAuth
    @GetMapping("/listForModule")
    public R<List<QualityTaskDto>> listForModule(@RequestParam("moduleCode") String moduleCode)
    {
        List<QualityTaskDto> list = qmsQualityTaskService.selectQualityTaskDtoListForModule(moduleCode);
        return R.ok(list);
    }
}