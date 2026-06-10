package com.ruoyi.designtask1.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.designtask1.domain.DesignTemplate;
import com.ruoyi.designtask1.domain.DesignTemplateNode;
import com.ruoyi.designtask1.domain.DesignTaskNode;
import com.ruoyi.designtask1.mapper.DesignTemplateNodeMapper;
import com.ruoyi.designtask1.mapper.DesignTaskNodeMapper;
import com.ruoyi.designtask1.service.IDesignTemplateService;

/**
 * Process node management (template nodes + task nodes)
 * Route prefix: /flow or /designtask/flow
 */
@RestController
@RequestMapping({"flow", "/designtask/flow"})
public class DesignFlowController extends BaseController {

    @Autowired
    private IDesignTemplateService templateService;

    @Autowired
    private DesignTemplateNodeMapper templateNodeMapper;

    @Autowired
    private DesignTaskNodeMapper taskNodeMapper;

    // ==================== Template Nodes ====================

    /**
     * Query flow template list
     */
    @RequiresPermissions("designtask:flow:list")
    @GetMapping("/template/list")
    public TableDataInfo listFlowTemplates(DesignTemplate template) {
        startPage();
        List<DesignTemplate> list = templateService.selectTemplateList(template);
        return getDataTable(list);
    }

    /**
     * Query flow template node list
     */
    @RequiresPermissions("designtask:flow:list")
    @GetMapping("/node/list/{templateId}")
    public AjaxResult listTemplateNodes(@PathVariable Long templateId) {
        List<DesignTemplateNode> list = templateNodeMapper.selectNodesByTemplateId(templateId);
        return success(list);
    }

    /**
     * Query template node detail
     */
    @RequiresPermissions("designtask:flow:query")
    @GetMapping("/node/{nodeId}")
    public AjaxResult getTemplateNode(@PathVariable Long nodeId) {
        return success(templateNodeMapper.selectNodeById(nodeId));
    }

    /**
     * Add template node
     */
    @RequiresPermissions("designtask:flow:add")
    @Log(title = "Template Node", businessType = BusinessType.INSERT)
    @PostMapping("/node")
    public AjaxResult addTemplateNode(@RequestBody DesignTemplateNode node) {
        node.setCreateBy(SecurityUtils.getUsername());
        return toAjax(templateNodeMapper.insertNode(node));
    }

    /**
     * Update template node
     */
    @RequiresPermissions("designtask:flow:edit")
    @Log(title = "Template Node", businessType = BusinessType.UPDATE)
    @PutMapping("/node/{nodeId}")
    public AjaxResult editTemplateNode(@PathVariable Long nodeId, @RequestBody DesignTemplateNode node) {
        node.setNodeId(nodeId);
        node.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(templateNodeMapper.updateNode(node));
    }

    /**
     * Delete template node
     */
    @RequiresPermissions("designtask:flow:remove")
    @Log(title = "Template Node", businessType = BusinessType.DELETE)
    @DeleteMapping("/node/{nodeId}")
    public AjaxResult removeTemplateNode(@PathVariable Long nodeId) {
        return toAjax(templateNodeMapper.deleteNodeById(nodeId));
    }

    // ==================== Task Nodes ====================

    /**
     * Query task node list
     */
    @RequiresPermissions("designtask:task:query")
    @GetMapping("/task/node/list/{taskId}")
    public AjaxResult listTaskNodes(@PathVariable Long taskId) {
        List<DesignTaskNode> list = taskNodeMapper.selectNodesByTaskId(taskId);
        return success(list);
    }

    /**
     * Query task node detail
     */
    @RequiresPermissions("designtask:task:query")
    @GetMapping("/task/node/{nodeId}")
    public AjaxResult getTaskNode(@PathVariable Long nodeId) {
        return success(taskNodeMapper.selectTaskNodeById(nodeId));
    }

    /**
     * Update task node status
     */
    @RequiresPermissions("designtask:task:edit")
    @Log(title = "Task Node", businessType = BusinessType.UPDATE)
    @PutMapping("/task/node/{nodeId}")
    public AjaxResult updateTaskNode(@PathVariable Long nodeId, @RequestBody DesignTaskNode node) {
        node.setId(nodeId);
        node.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(taskNodeMapper.updateTaskNode(node));
    }

    /**
     * Query node collaboration records
     */
    @RequiresPermissions("designtask:task:query")
    @GetMapping("/task/node/{nodeId}/collaborations")
    public AjaxResult getNodeCollaborations(@PathVariable Long nodeId) {
        return success(List.of());
    }

    /**
     * Submit task node result
     */
    @RequiresPermissions("designtask:task:edit")
    @Log(title = "Task Node", businessType = BusinessType.UPDATE)
    @PostMapping("/task/node/{nodeId}/submit")
    public AjaxResult submitTaskNode(@PathVariable Long nodeId, @RequestBody(required = false) DesignTaskNode node) {
        if (node == null) {
            node = new DesignTaskNode();
        }
        node.setId(nodeId);
        node.setStatus("COMPLETED");
        node.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(taskNodeMapper.updateTaskNode(node));
    }

    // ==================== Template Preview ====================

    /**
     * Preview flow template (returns parsed node list by template ID)
     */
    @RequiresPermissions("designtask:flow:query")
    @GetMapping("/template/{templateId}/preview")
    public AjaxResult previewTemplate(@PathVariable Long templateId) {
        DesignTemplate template = templateService.selectTemplateById(templateId);
        if (StringUtils.isNull(template)) {
            return error("Template not found");
        }
        List<DesignTemplateNode> nodes = templateNodeMapper.selectNodesByTemplateId(templateId);
        return success().put("template", template).put("nodes", nodes);
    }
}
