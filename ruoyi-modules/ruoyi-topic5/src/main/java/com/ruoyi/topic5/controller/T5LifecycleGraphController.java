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
import com.ruoyi.topic5.domain.T5LifecycleGraph;
import com.ruoyi.topic5.service.IT5LifecycleGraphService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 全生命周期关联模型Controller
 *
 * @author ruoyi
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/lifecycleGraph")
public class T5LifecycleGraphController extends BaseController
{
    @Autowired
    private IT5LifecycleGraphService t5LifecycleGraphService;

    /**
     * 查询全生命周期关联模型列表
     */
    @RequiresPermissions("topic5:lifecycleGraph:list")
    @GetMapping("/list")
    public TableDataInfo list(T5LifecycleGraph t5LifecycleGraph)
    {
        startPage();
        List<T5LifecycleGraph> list = t5LifecycleGraphService.selectT5LifecycleGraphList(t5LifecycleGraph);
        return getDataTable(list);
    }

    /**
     * 导出全生命周期关联模型列表
     */
    @RequiresPermissions("topic5:lifecycleGraph:export")
    @Log(title = "全生命周期关联模型", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5LifecycleGraph t5LifecycleGraph)
    {
        List<T5LifecycleGraph> list = t5LifecycleGraphService.selectT5LifecycleGraphList(t5LifecycleGraph);
        ExcelUtil<T5LifecycleGraph> util = new ExcelUtil<T5LifecycleGraph>(T5LifecycleGraph.class);
        util.exportExcel(response, list, "全生命周期关联模型数据");
    }

    /**
     * 获取全生命周期关联模型详细信息
     */
    @RequiresPermissions("topic5:lifecycleGraph:query")
    @GetMapping(value = "/{graphId}")
    public AjaxResult getInfo(@PathVariable("graphId") Long graphId)
    {
        return success(t5LifecycleGraphService.selectT5LifecycleGraphByGraphId(graphId));
    }

    /**
     * 新增全生命周期关联模型
     */
    @RequiresPermissions("topic5:lifecycleGraph:add")
    @Log(title = "全生命周期关联模型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5LifecycleGraph t5LifecycleGraph)
    {
        return toAjax(t5LifecycleGraphService.insertT5LifecycleGraph(t5LifecycleGraph));
    }

    /**
     * 修改全生命周期关联模型
     */
    @RequiresPermissions("topic5:lifecycleGraph:edit")
    @Log(title = "全生命周期关联模型", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5LifecycleGraph t5LifecycleGraph)
    {
        return toAjax(t5LifecycleGraphService.updateT5LifecycleGraph(t5LifecycleGraph));
    }

    /**
     * 删除全生命周期关联模型
     */
    @RequiresPermissions("topic5:lifecycleGraph:remove")
    @Log(title = "全生命周期关联模型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{graphIds}")
    public AjaxResult remove(@PathVariable Long[] graphIds)
    {
        return toAjax(t5LifecycleGraphService.deleteT5LifecycleGraphByGraphIds(graphIds));
    }

    /**
     * 根据追溯案例构建全生命周期关联图模型
     */
    @PostMapping("/build/{caseId}")
    public AjaxResult buildLifecycleGraph(@PathVariable("caseId") Long caseId, Long algorithmId)
    {
        return t5LifecycleGraphService.buildLifecycleGraph(caseId, algorithmId);
    }

    /**
     * 根据追溯案例查询最新图模型
     */
    @GetMapping("/latestByCase/{caseId}")
    public AjaxResult latestByCase(@PathVariable("caseId") Long caseId)
    {
        return success(t5LifecycleGraphService.selectLatestGraphByCaseId(caseId));
    }

    /**
     * 获取前端图网络展示数据
     */
    @GetMapping("/graphData/{graphId}")
    public AjaxResult graphData(@PathVariable("graphId") Long graphId)
    {
        return success(t5LifecycleGraphService.getGraphData(graphId));
    }
}