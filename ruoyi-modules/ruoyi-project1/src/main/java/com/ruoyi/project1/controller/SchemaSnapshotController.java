package com.ruoyi.project1.controller;

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
import com.ruoyi.project1.domain.SchemaSnapshot;
import com.ruoyi.project1.service.ISchemaSnapshotService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 数据源模式快照Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/schemaSnapshot")
public class SchemaSnapshotController extends BaseController
{
    @Autowired
    private ISchemaSnapshotService schemaSnapshotService;

    /**
     * 查询数据源模式快照列表
     */
    @RequiresPermissions("project1:schemaSnapshot:list")
    @GetMapping("/list")
    public TableDataInfo list(SchemaSnapshot schemaSnapshot)
    {
        startPage();
        List<SchemaSnapshot> list = schemaSnapshotService.selectSchemaSnapshotList(schemaSnapshot);
        return getDataTable(list);
    }

    /**
     * 导出数据源模式快照列表
     */
    @RequiresPermissions("project1:schemaSnapshot:export")
    @Log(title = "数据源模式快照", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SchemaSnapshot schemaSnapshot)
    {
        List<SchemaSnapshot> list = schemaSnapshotService.selectSchemaSnapshotList(schemaSnapshot);
        ExcelUtil<SchemaSnapshot> util = new ExcelUtil<SchemaSnapshot>(SchemaSnapshot.class);
        util.exportExcel(response, list, "数据源模式快照数据");
    }

    /**
     * 获取数据源模式快照详细信息
     */
    @RequiresPermissions("project1:schemaSnapshot:query")
    @GetMapping(value = "/{snapshotId}")
    public AjaxResult getInfo(@PathVariable("snapshotId") Long snapshotId)
    {
        return success(schemaSnapshotService.selectSchemaSnapshotBySnapshotId(snapshotId));
    }

    /**
     * 新增数据源模式快照
     */
    @RequiresPermissions("project1:schemaSnapshot:add")
    @Log(title = "数据源模式快照", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SchemaSnapshot schemaSnapshot)
    {
        return toAjax(schemaSnapshotService.insertSchemaSnapshot(schemaSnapshot));
    }

    /**
     * 修改数据源模式快照
     */
    @RequiresPermissions("project1:schemaSnapshot:edit")
    @Log(title = "数据源模式快照", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SchemaSnapshot schemaSnapshot)
    {
        return toAjax(schemaSnapshotService.updateSchemaSnapshot(schemaSnapshot));
    }

    /**
     * 删除数据源模式快照
     */
    @RequiresPermissions("project1:schemaSnapshot:remove")
    @Log(title = "数据源模式快照", businessType = BusinessType.DELETE)
	@DeleteMapping("/{snapshotIds}")
    public AjaxResult remove(@PathVariable Long[] snapshotIds)
    {
        return toAjax(schemaSnapshotService.deleteSchemaSnapshotBySnapshotIds(snapshotIds));
    }
}
