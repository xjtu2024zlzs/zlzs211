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
import com.ruoyi.topic5.domain.T5LifecycleEdge;
import com.ruoyi.topic5.service.IT5LifecycleEdgeService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 全生命周期图关系边Controller
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/lifecycleEdge")
public class T5LifecycleEdgeController extends BaseController
{
    @Autowired
    private IT5LifecycleEdgeService t5LifecycleEdgeService;

    /**
     * 查询全生命周期图关系边列表
     */
    @RequiresPermissions("topic5:lifecycleEdge:list")
    @GetMapping("/list")
    public TableDataInfo list(T5LifecycleEdge t5LifecycleEdge)
    {
        startPage();
        List<T5LifecycleEdge> list = t5LifecycleEdgeService.selectT5LifecycleEdgeList(t5LifecycleEdge);
        return getDataTable(list);
    }

    /**
     * 导出全生命周期图关系边列表
     */
    @RequiresPermissions("topic5:lifecycleEdge:export")
    @Log(title = "全生命周期图关系边", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5LifecycleEdge t5LifecycleEdge)
    {
        List<T5LifecycleEdge> list = t5LifecycleEdgeService.selectT5LifecycleEdgeList(t5LifecycleEdge);
        ExcelUtil<T5LifecycleEdge> util = new ExcelUtil<T5LifecycleEdge>(T5LifecycleEdge.class);
        util.exportExcel(response, list, "全生命周期图关系边数据");
    }

    /**
     * 获取全生命周期图关系边详细信息
     */
    @RequiresPermissions("topic5:lifecycleEdge:query")
    @GetMapping(value = "/{edgeId}")
    public AjaxResult getInfo(@PathVariable("edgeId") Long edgeId)
    {
        return success(t5LifecycleEdgeService.selectT5LifecycleEdgeByEdgeId(edgeId));
    }

    /**
     * 新增全生命周期图关系边
     */
    @RequiresPermissions("topic5:lifecycleEdge:add")
    @Log(title = "全生命周期图关系边", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5LifecycleEdge t5LifecycleEdge)
    {
        return toAjax(t5LifecycleEdgeService.insertT5LifecycleEdge(t5LifecycleEdge));
    }

    /**
     * 修改全生命周期图关系边
     */
    @RequiresPermissions("topic5:lifecycleEdge:edit")
    @Log(title = "全生命周期图关系边", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5LifecycleEdge t5LifecycleEdge)
    {
        return toAjax(t5LifecycleEdgeService.updateT5LifecycleEdge(t5LifecycleEdge));
    }

    /**
     * 删除全生命周期图关系边
     */
    @RequiresPermissions("topic5:lifecycleEdge:remove")
    @Log(title = "全生命周期图关系边", businessType = BusinessType.DELETE)
	@DeleteMapping("/{edgeIds}")
    public AjaxResult remove(@PathVariable Long[] edgeIds)
    {
        return toAjax(t5LifecycleEdgeService.deleteT5LifecycleEdgeByEdgeIds(edgeIds));
    }
}
