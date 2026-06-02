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
import com.ruoyi.topic5.domain.T5LifecycleNode;
import com.ruoyi.topic5.service.IT5LifecycleNodeService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 全生命周期图节点Controller
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/lifecycleNode")
public class T5LifecycleNodeController extends BaseController
{
    @Autowired
    private IT5LifecycleNodeService t5LifecycleNodeService;

    /**
     * 查询全生命周期图节点列表
     */
    @RequiresPermissions("topic5:lifecycleNode:list")
    @GetMapping("/list")
    public TableDataInfo list(T5LifecycleNode t5LifecycleNode)
    {
        startPage();
        List<T5LifecycleNode> list = t5LifecycleNodeService.selectT5LifecycleNodeList(t5LifecycleNode);
        return getDataTable(list);
    }

    /**
     * 导出全生命周期图节点列表
     */
    @RequiresPermissions("topic5:lifecycleNode:export")
    @Log(title = "全生命周期图节点", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5LifecycleNode t5LifecycleNode)
    {
        List<T5LifecycleNode> list = t5LifecycleNodeService.selectT5LifecycleNodeList(t5LifecycleNode);
        ExcelUtil<T5LifecycleNode> util = new ExcelUtil<T5LifecycleNode>(T5LifecycleNode.class);
        util.exportExcel(response, list, "全生命周期图节点数据");
    }

    /**
     * 获取全生命周期图节点详细信息
     */
    @RequiresPermissions("topic5:lifecycleNode:query")
    @GetMapping(value = "/{nodeId}")
    public AjaxResult getInfo(@PathVariable("nodeId") Long nodeId)
    {
        return success(t5LifecycleNodeService.selectT5LifecycleNodeByNodeId(nodeId));
    }

    /**
     * 新增全生命周期图节点
     */
    @RequiresPermissions("topic5:lifecycleNode:add")
    @Log(title = "全生命周期图节点", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5LifecycleNode t5LifecycleNode)
    {
        return toAjax(t5LifecycleNodeService.insertT5LifecycleNode(t5LifecycleNode));
    }

    /**
     * 修改全生命周期图节点
     */
    @RequiresPermissions("topic5:lifecycleNode:edit")
    @Log(title = "全生命周期图节点", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5LifecycleNode t5LifecycleNode)
    {
        return toAjax(t5LifecycleNodeService.updateT5LifecycleNode(t5LifecycleNode));
    }

    /**
     * 删除全生命周期图节点
     */
    @RequiresPermissions("topic5:lifecycleNode:remove")
    @Log(title = "全生命周期图节点", businessType = BusinessType.DELETE)
	@DeleteMapping("/{nodeIds}")
    public AjaxResult remove(@PathVariable Long[] nodeIds)
    {
        return toAjax(t5LifecycleNodeService.deleteT5LifecycleNodeByNodeIds(nodeIds));
    }
}
