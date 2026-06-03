package com.ruoyi.topic5.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.core.utils.poi.ExcelUtil;

import com.ruoyi.topic5.domain.Topic5TraceProblem;
import com.ruoyi.topic5.domain.dto.Topic4CallbackDTO;
import com.ruoyi.topic5.service.ITopic5TraceProblemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.topic5.domain.dto.AttachmentSavePathDTO;
/**
 * 课题五追溯问题Controller
 */
@RestController
@RequestMapping("trace")
public class Topic5TraceProblemController extends BaseController
{
    @Autowired
    private ITopic5TraceProblemService traceProblemService;

    /**
     * 查询追溯问题列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Topic5TraceProblem topic5TraceProblem)
    {
        startPage();
        List<Topic5TraceProblem> list = traceProblemService.selectTopic5TraceProblemList(topic5TraceProblem);
        return getDataTable(list);
    }

    /**
     * 导出追溯问题列表
     */
    @Log(title = "追溯问题", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Topic5TraceProblem topic5TraceProblem)
    {
        List<Topic5TraceProblem> list = traceProblemService.selectTopic5TraceProblemList(topic5TraceProblem);
        ExcelUtil<Topic5TraceProblem> util = new ExcelUtil<Topic5TraceProblem>(Topic5TraceProblem.class);
        util.exportExcel(response, list, "追溯问题数据");
    }

    /**
     * 获取追溯问题详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(traceProblemService.selectTopic5TraceProblemById(id));
    }

    /**
     * 新增追溯问题
     */
    @Log(title = "追溯问题", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Topic5TraceProblem topic5TraceProblem)
    {
        return toAjax(traceProblemService.insertTopic5TraceProblem(topic5TraceProblem));
    }

    /**
     * 修改追溯问题
     */
    @Log(title = "追溯问题", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Topic5TraceProblem topic5TraceProblem)
    {
        return toAjax(traceProblemService.updateTopic5TraceProblem(topic5TraceProblem));
    }

    /**
     * 删除追溯问题
     */
    @Log(title = "追溯问题", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(traceProblemService.deleteTopic5TraceProblemByIds(ids));
    }

    /**
     * 获取追溯任务下拉框
     */
    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(traceProblemService.selectTraceOptions());
    }

    /**
     * 模拟从课题一数字卷宗调用传感器文件
     */
    @PostMapping("/{id}/dossier/import")
    public AjaxResult importDossierFiles(@PathVariable Long id)
    {
        return success(traceProblemService.importDossierFiles(id));
    }

    /**
     * 保存数字卷宗附件
     */
    @PostMapping("/{id}/attachment/save")
    public AjaxResult saveAttachments(@PathVariable Long id)
    {
        traceProblemService.saveDossierAttachments(id);
        return success("附件保存成功");
    }

    /**
     * 推送给课题四
     */
    @PostMapping("/{id}/push-topic4")
    public AjaxResult pushToTopic4(@PathVariable Long id)
    {
        traceProblemService.pushToTopic4(id);
        return success("已推送给课题四处理");
    }

    /**
     * 接收课题四回调
     */
    @PostMapping("/topic4/callback")
    public AjaxResult topic4Callback(@RequestBody Topic4CallbackDTO dto)
    {
        traceProblemService.receiveTopic4Callback(dto);
        return success("课题四结果接收成功");
    }

    /**
     * 将课题四结果回填到追溯问题表
     */
    @PostMapping("/{id}/fill-topic4-result")
    public AjaxResult fillTopic4Result(@PathVariable Long id)
    {
        traceProblemService.fillTopic4Result(id);
        return success("课题四结果已回填");
    }

    /**
     * 查询当前流程阶段
     */
    @GetMapping("/{id}/workflow")
    public AjaxResult workflow(@PathVariable Long id)
    {
        return success(traceProblemService.getWorkflowStage(id));
    }

    /**
     * 模拟运行 Python 追溯算法
     */
    @PostMapping("/{id}/run-algorithm")
    public AjaxResult runAlgorithm(@PathVariable Long id)
    {
        return success(traceProblemService.runAlgorithmMock(id));
    }

    /**
     * 保存附件并同步附件保存位置
     */
    @PostMapping("/{id}/attachment/save-with-path")
    public AjaxResult saveAttachmentsWithPath(@PathVariable Long id, @RequestBody AttachmentSavePathDTO dto)
    {
        traceProblemService.saveDossierAttachmentsWithPath(id, dto.getAttachmentSavePath());
        return success("附件保存成功，保存位置已同步到追溯问题表");
    }
}