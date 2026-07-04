package com.ruoyi.quality.controller.remote;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.qms.api.domain.QualityProblemDto;
import com.ruoyi.quality.service.IQmsQualityProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 质量问题远程调用接口
 */
@RestController
@RequestMapping("/remote/qualityProblem")
public class RemoteQualityProblemController
{
    @Autowired
    private IQmsQualityProblemService qmsQualityProblemService;

    /**
     * 查询需要同步到指定模块的质量问题
     */
    @InnerAuth
    @GetMapping("/listForModule")
    public R<List<QualityProblemDto>> listForModule(@RequestParam("moduleCode") String moduleCode)
    {
        List<QualityProblemDto> list = qmsQualityProblemService.selectProblemDtoListForModule(moduleCode);
        return R.ok(list);
    }
}