package com.ruoyi.project4.controller;

import com.ruoyi.project4.domain.dto.PreprocessReqDTO;
import com.ruoyi.project4.domain.entity.T4BearingPreprocess;
import com.ruoyi.project4.service.BearingPreprocessService;
import com.ruoyi.common.core.domain.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;
import com.ruoyi.project4.domain.entity.CwruFileMeta;
import com.ruoyi.project4.domain.mapper.CwruFileMetaMapper;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
@RestController
@RequestMapping("/bearing/preprocess")
public class BearingPreprocessController {

    @Resource
    private BearingPreprocessService bearingPreprocessService;

    @Resource
    private CwruFileMetaMapper cwruFileMetaMapper;

    @GetMapping("/cwru-files")
    public R<List<CwruFileMeta>> listCwruFiles() {
        return R.ok(cwruFileMetaMapper.selectCwruFileMetaList());
    }

    @PostMapping("/run")
    public R<T4BearingPreprocess> preprocess(
            @RequestBody PreprocessReqDTO reqDTO
    ) {
        T4BearingPreprocess result = bearingPreprocessService.execPreprocess(reqDTO);
        if ("fail".equals(result.getStatus())) {
            return R.fail("预处理执行失败：" + result.getBizResult());
        }
        return R.ok(result, "预处理执行成功");
    }
}