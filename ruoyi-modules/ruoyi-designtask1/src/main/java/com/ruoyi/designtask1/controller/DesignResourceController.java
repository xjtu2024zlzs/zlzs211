package com.ruoyi.designtask1.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"resource", "/designtask/resource"})
public class DesignResourceController {

    @RequiresPermissions("designtask:resource:list")
    @GetMapping("/list")
    public AjaxResult list(String keyword, String category) {
        return AjaxResult.success(List.of(
            resource(1L, "起落架舱门结构边界条件", "结构资源", "v1.0", "PDF", "用于结构目标约束选择"),
            resource(2L, "舱门管线路径禁布区域", "布局资源", "v1.1", "DWG", "用于线缆管路联合布局"),
            resource(3L, "液压弯管冲击载荷谱", "液压资源", "v2.0", "XLSX", "用于代理模型求解"),
            resource(4L, "气动外形包络约束", "气动资源", "v1.0", "PNG", "用于气动约束确认"),
            resource(5L, "仿真验证指标规范", "验证资源", "v1.0", "DOCX", "用于仿真结果判定")
        ));
    }

    @RequiresPermissions("designtask:resource:add")
    @PostMapping
    public AjaxResult add(@RequestBody Map<String, Object> body) {
        return AjaxResult.success("资源已记录", body);
    }

    @RequiresPermissions("designtask:resource:list")
    @GetMapping("/{id}/preview")
    public AjaxResult preview(@PathVariable Long id) {
        return AjaxResult.success(resource(id, "演示资源预览", "标准资源", "v1.0", "PDF", "第一版展示文件元信息，后续接入真实文件预览"));
    }

    private Map<String, Object> resource(Long id, String name, String category, String version, String fileType, String description) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("resourceId", id);
        item.put("resourceName", name);
        item.put("category", category);
        item.put("version", version);
        item.put("fileType", fileType);
        item.put("description", description);
        item.put("status", "0");
        return item;
    }
}
