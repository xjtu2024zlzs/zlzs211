package com.ruoyi.topic5.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.topic5.domain.Topic5TraceAttachment;
import com.ruoyi.topic5.service.ITopic5TraceAttachmentService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 追溯附件Controller
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
@RestController
@RequestMapping("/traceattachment")
public class Topic5TraceAttachmentController extends BaseController
{
    @Autowired
    private ITopic5TraceAttachmentService topic5TraceAttachmentService;

    /**
     * 查询追溯附件列表
     */
    @RequiresPermissions("topic5:traceattachment:list")
    @GetMapping("/list")
    public TableDataInfo list(Topic5TraceAttachment topic5TraceAttachment)
    {
        startPage();
        List<Topic5TraceAttachment> list = topic5TraceAttachmentService.selectTopic5TraceAttachmentList(topic5TraceAttachment);
        return getDataTable(list);
    }

    /**
     * 导出追溯附件列表
     */
    @RequiresPermissions("topic5:traceattachment:export")
    @Log(title = "追溯附件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Topic5TraceAttachment topic5TraceAttachment)
    {
        List<Topic5TraceAttachment> list = topic5TraceAttachmentService.selectTopic5TraceAttachmentList(topic5TraceAttachment);
        ExcelUtil<Topic5TraceAttachment> util = new ExcelUtil<Topic5TraceAttachment>(Topic5TraceAttachment.class);
        util.exportExcel(response, list, "追溯附件数据");
    }

    /**
     * 获取追溯附件详细信息
     */
    @RequiresPermissions("topic5:traceattachment:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(topic5TraceAttachmentService.selectTopic5TraceAttachmentById(id));
    }

    /**
     * 新增追溯附件
     */
    @RequiresPermissions("topic5:traceattachment:add")
    @Log(title = "追溯附件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Topic5TraceAttachment topic5TraceAttachment)
    {
        return toAjax(topic5TraceAttachmentService.insertTopic5TraceAttachment(topic5TraceAttachment));
    }

    /**
     * 修改追溯附件
     */
    @RequiresPermissions("topic5:traceattachment:edit")
    @Log(title = "追溯附件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Topic5TraceAttachment topic5TraceAttachment)
    {
        return toAjax(topic5TraceAttachmentService.updateTopic5TraceAttachment(topic5TraceAttachment));
    }

    /**
     * 删除追溯附件
     */
    @RequiresPermissions("topic5:traceattachment:remove")
    @Log(title = "追溯附件", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(topic5TraceAttachmentService.deleteTopic5TraceAttachmentByIds(ids));
    }
}
