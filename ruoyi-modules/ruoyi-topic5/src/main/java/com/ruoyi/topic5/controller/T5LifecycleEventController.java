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
import com.ruoyi.topic5.domain.T5LifecycleEvent;
import com.ruoyi.topic5.service.IT5LifecycleEventService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 课题五-零部件生命周期事件Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/event")
public class T5LifecycleEventController extends BaseController
{
    @Autowired
    private IT5LifecycleEventService t5LifecycleEventService;

    /**
     * 查询课题五-零部件生命周期事件列表
     */
    @RequiresPermissions("topic5:event:list")
    @GetMapping("/list")
    public TableDataInfo list(T5LifecycleEvent t5LifecycleEvent)
    {
        startPage();
        List<T5LifecycleEvent> list = t5LifecycleEventService.selectT5LifecycleEventList(t5LifecycleEvent);
        return getDataTable(list);
    }

    /**
     * 导出课题五-零部件生命周期事件列表
     */
    @RequiresPermissions("topic5:event:export")
    @Log(title = "课题五-零部件生命周期事件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5LifecycleEvent t5LifecycleEvent)
    {
        List<T5LifecycleEvent> list = t5LifecycleEventService.selectT5LifecycleEventList(t5LifecycleEvent);
        ExcelUtil<T5LifecycleEvent> util = new ExcelUtil<T5LifecycleEvent>(T5LifecycleEvent.class);
        util.exportExcel(response, list, "课题五-零部件生命周期事件数据");
    }

    /**
     * 获取课题五-零部件生命周期事件详细信息
     */
    @RequiresPermissions("topic5:event:query")
    @GetMapping(value = "/{eventId}")
    public AjaxResult getInfo(@PathVariable("eventId") Long eventId)
    {
        return success(t5LifecycleEventService.selectT5LifecycleEventByEventId(eventId));
    }

    /**
     * 新增课题五-零部件生命周期事件
     */
    @RequiresPermissions("topic5:event:add")
    @Log(title = "课题五-零部件生命周期事件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5LifecycleEvent t5LifecycleEvent)
    {
        return toAjax(t5LifecycleEventService.insertT5LifecycleEvent(t5LifecycleEvent));
    }

    /**
     * 修改课题五-零部件生命周期事件
     */
    @RequiresPermissions("topic5:event:edit")
    @Log(title = "课题五-零部件生命周期事件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5LifecycleEvent t5LifecycleEvent)
    {
        return toAjax(t5LifecycleEventService.updateT5LifecycleEvent(t5LifecycleEvent));
    }

    /**
     * 删除课题五-零部件生命周期事件
     */
    @RequiresPermissions("topic5:event:remove")
    @Log(title = "课题五-零部件生命周期事件", businessType = BusinessType.DELETE)
	@DeleteMapping("/{eventIds}")
    public AjaxResult remove(@PathVariable Long[] eventIds)
    {
        return toAjax(t5LifecycleEventService.deleteT5LifecycleEventByEventIds(eventIds));
    }
}
