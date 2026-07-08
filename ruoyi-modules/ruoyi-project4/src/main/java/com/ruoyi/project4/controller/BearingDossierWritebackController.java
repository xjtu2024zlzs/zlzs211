package com.ruoyi.project4.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project4.service.BearingDossierWritebackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bearing/dossier")
public class BearingDossierWritebackController {

    @Autowired
    private BearingDossierWritebackService bearingDossierWritebackService;

    @PostMapping("/writeback/{diagnoseId}")
    public AjaxResult writebackDiagnoseResult(@PathVariable Long diagnoseId) {
        return bearingDossierWritebackService.writebackDiagnoseResult(diagnoseId);
    }
}