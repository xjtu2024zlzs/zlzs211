package com.ruoyi.quality.controller;

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
import com.ruoyi.quality.domain.QmsQualityProblem;
import com.ruoyi.quality.service.IQmsQualityProblemService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import com.ruoyi.quality.service.IQmsQualityTaskService;
import com.ruoyi.quality.domain.QmsQualityTask;
import com.ruoyi.quality.utils.QualityProblemReportWordUtil;

/**
 * 质量问题Controller
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
@RestController
@RequestMapping("/qms/problem")
public class QmsQualityProblemController extends BaseController
{
    @Autowired
    private IQmsQualityProblemService qmsQualityProblemService;

    @Autowired
    private IQmsQualityTaskService qmsQualityTaskService;

    @GetMapping("/report/{problemId}")
    public void exportProblemReport(@PathVariable("problemId") Long problemId, HttpServletResponse response) throws IOException {
        QmsQualityProblem problem = qmsQualityProblemService.selectQmsQualityProblemByProblemId(problemId);

        if (problem == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"msg\":\"质量问题不存在\"}");
            return;
        }

        QmsQualityTask queryTask = new QmsQualityTask();
        queryTask.setProblemId(problemId);
        List<QmsQualityTask> taskList = qmsQualityTaskService.selectQmsQualityTaskList(queryTask);

        XWPFDocument document = QualityProblemReportWordUtil.buildReport(problem, taskList);

        String fileName = "质量问题报告_" + safeFileName(problem.getProblemCode()) + ".docx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        document.write(response.getOutputStream());
        document.close();
    }

    private String safeFileName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return String.valueOf(System.currentTimeMillis());
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * 查询质量问题列表
     */
    //@RequiresPermissions("quality:problem:list")
    @GetMapping("/list")
    public TableDataInfo list(QmsQualityProblem qmsQualityProblem)
    {
        startPage();
        List<QmsQualityProblem> list = qmsQualityProblemService.selectQmsQualityProblemList(qmsQualityProblem);
        return getDataTable(list);
    }

    /**
     * 导出质量问题列表
     */
    //@RequiresPermissions("quality:problem:export")
    @Log(title = "质量问题", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsQualityProblem qmsQualityProblem)
    {
        List<QmsQualityProblem> list = qmsQualityProblemService.selectQmsQualityProblemList(qmsQualityProblem);
        ExcelUtil<QmsQualityProblem> util = new ExcelUtil<QmsQualityProblem>(QmsQualityProblem.class);
        util.exportExcel(response, list, "质量问题数据");
    }

    /**
     * 获取质量问题详细信息
     */
    //@RequiresPermissions("quality:problem:query")
    @GetMapping(value = "/{problemId}")
    public AjaxResult getInfo(@PathVariable("problemId") Long problemId)
    {
        return success(qmsQualityProblemService.selectQmsQualityProblemByProblemId(problemId));
    }

    /**
     * 新增质量问题
     */
    //@RequiresPermissions("quality:problem:add")
    @Log(title = "质量问题", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QmsQualityProblem qmsQualityProblem)
    {
        return toAjax(qmsQualityProblemService.insertQmsQualityProblem(qmsQualityProblem));
    }

    /**
     * 修改质量问题
     */
    //@RequiresPermissions("quality:problem:edit")
    @Log(title = "质量问题", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QmsQualityProblem qmsQualityProblem)
    {
        return toAjax(qmsQualityProblemService.updateQmsQualityProblem(qmsQualityProblem));
    }

    /**
     * 删除质量问题
     */
    //@RequiresPermissions("quality:problem:remove")
    @Log(title = "质量问题", businessType = BusinessType.DELETE)
	@DeleteMapping("/{problemIds}")
    public AjaxResult remove(@PathVariable Long[] problemIds)
    {
        return toAjax(qmsQualityProblemService.deleteQmsQualityProblemByProblemIds(problemIds));
    }
}
