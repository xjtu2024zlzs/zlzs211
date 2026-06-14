package com.ruoyi.quality.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import com.ruoyi.quality.domain.QmsQualityProblem;
import com.ruoyi.quality.domain.QmsQualityTask;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TableWidthType;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class QualityProblemReportWordUtil {

    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter BEIJING_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒");

    private QualityProblemReportWordUtil() {
    }

    public static XWPFDocument buildReport(QmsQualityProblem problem, List<QmsQualityTask> taskList) {
        XWPFDocument document = new XWPFDocument();

        addTitle(document, "质量问题处理报告");
        addSubTitle(document, "报告编号：" + value(problem.getProblemCode()));
        addSubTitle(document, "生成时间：" + formatBeijingTime(new Date()));

        addSectionTitle(document, "一、质量问题基本信息");
        addProblemBaseInfoTable(document, problem);

        addSectionTitle(document, "二、问题描述与影响范围");
        addNormalParagraph(document, "1. 问题描述：" + value(problem.getDescription()));
        addNormalParagraph(document, "2. 影响范围：" + value(problem.getInfluenceScope()));

        addSectionTitle(document, "三、模块分派与处理结果");
        addTaskResultSections(document, taskList);

        addSectionTitle(document, "四、处理结论");
        addConclusion(document, problem, taskList);

        addSectionTitle(document, "五、报告说明");
        addNormalParagraph(document,
                "本报告由质量问题管理中心根据质量问题填报信息、模块分派记录及各模块反馈结果自动生成，" +
                        "可作为后续问题闭环、质量复盘和追溯分析的过程依据。");

        return document;
    }

    private static void addTitle(XWPFDocument document, String title) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setVerticalAlignment(TextAlignment.CENTER);

        XWPFRun run = paragraph.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(20);
        run.setFontFamily("宋体");
    }

    private static void addSubTitle(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = paragraph.createRun();
        run.setText(value(text));
        run.setFontSize(11);
        run.setFontFamily("宋体");
    }

    private static void addSectionTitle(XWPFDocument document, String title) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(240);
        paragraph.setSpacingAfter(120);

        XWPFRun run = paragraph.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(14);
        run.setFontFamily("宋体");
    }

    private static void addTaskTitle(XWPFDocument document, String title) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(180);
        paragraph.setSpacingAfter(80);

        XWPFRun run = paragraph.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(12);
        run.setFontFamily("宋体");
    }

    private static void addNormalParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setFirstLineIndent(420);
        paragraph.setSpacingAfter(80);

        XWPFRun run = paragraph.createRun();
        run.setText(value(text));
        run.setFontSize(11);
        run.setFontFamily("宋体");
    }

    private static void addIndentedParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setIndentationLeft(420);
        paragraph.setSpacingAfter(60);

        XWPFRun run = paragraph.createRun();
        run.setText(value(text));
        run.setFontSize(11);
        run.setFontFamily("宋体");
    }

    private static void addProblemBaseInfoTable(XWPFDocument document, QmsQualityProblem problem) {
        XWPFTable table = document.createTable(7, 4);
        table.setWidth("100%");
        table.setWidthType(TableWidthType.PCT);

        setCellText(table.getRow(0), 0, "问题编号", true);
        setCellText(table.getRow(0), 1, value(problem.getProblemCode()), false);
        setCellText(table.getRow(0), 2, "问题标题", true);
        setCellText(table.getRow(0), 3, value(problem.getTitle()), false);

        setCellText(table.getRow(1), 0, "发生时间", true);
        setCellText(table.getRow(1), 1, formatBeijingTime(problem.getOccurTime()), false);
        setCellText(table.getRow(1), 2, "问题状态", true);
        setCellText(table.getRow(1), 3, getProblemStatusText(problem.getStatus()), false);

        setCellText(table.getRow(2), 0, "产品型号", true);
        setCellText(table.getRow(2), 1, value(problem.getProductModel()), false);
        setCellText(table.getRow(2), 2, "涉及系统", true);
        setCellText(table.getRow(2), 3, value(problem.getInvolvedSystem()), false);

        setCellText(table.getRow(3), 0, "发生部位", true);
        setCellText(table.getRow(3), 1, value(problem.getOccurPart()), false);
        setCellText(table.getRow(3), 2, "部件编号", true);
        setCellText(table.getRow(3), 3, value(problem.getComponentCode()), false);

        setCellText(table.getRow(4), 0, "严重程度", true);
        setCellText(table.getRow(4), 1, value(problem.getSeverity()), false);
        setCellText(table.getRow(4), 2, "问题来源", true);
        setCellText(table.getRow(4), 3, value(problem.getSource()), false);

        setCellText(table.getRow(5), 0, "填报人员", true);
        setCellText(table.getRow(5), 1, value(problem.getReporter()), false);
        setCellText(table.getRow(5), 2, "当前模块", true);
        setCellText(table.getRow(5), 3, getProblemModuleDisplayName(problem), false);

        setCellText(table.getRow(6), 0, "创建时间", true);
        setCellText(table.getRow(6), 1, formatBeijingTime(problem.getCreateTime()), false);
        setCellText(table.getRow(6), 2, "更新时间", true);
        setCellText(table.getRow(6), 3, formatBeijingTime(problem.getUpdateTime()), false);
    }

    /**
     * 模块分派与处理结果：不再用表格，改为按模块分点展示。
     */
    private static void addTaskResultSections(XWPFDocument document, List<QmsQualityTask> taskList) {
        if (taskList == null || taskList.isEmpty()) {
            addNormalParagraph(document, "当前质量问题暂未形成模块分派记录。");
            return;
        }

        for (int i = 0; i < taskList.size(); i++) {
            QmsQualityTask task = taskList.get(i);

            addTaskTitle(document, "（" + (i + 1) + "）" + getTaskModuleDisplayName(task));

            addIndentedParagraph(document, "1. 任务状态：" + getTaskStatusText(task.getTaskStatus()));
            addIndentedParagraph(document, "2. 分派说明：" + value(task.getDispatchOpinion()));

            /*
             * 如果你的 QmsQualityTask 实体中有 getDispatchTime()，可以打开下面这一行：
             * addIndentedParagraph(document, "3. 分派时间：" + formatBeijingTime(task.getDispatchTime()));
             *
             * 如果没有 getDispatchTime()，这里用 createTime 作为分派记录生成时间。
             */
            addIndentedParagraph(document, "3. 分派时间：" + formatBeijingTime(task.getCreateTime()));

            /*
             * 如果你的 QmsQualityTask 实体中有 getSubmitTime()，可以打开下面这一行：
             * addIndentedParagraph(document, "4. 反馈时间：" + formatBeijingTime(task.getSubmitTime()));
             *
             * 如果没有 getSubmitTime()，可以先显示为暂无。
             */
            addIndentedParagraph(document, "4. 反馈时间：-");

            addIndentedParagraph(document, "5. 模块处理结果：" + value(task.getProcessResult()));
            addIndentedParagraph(document, "6. 管理确认时间：" + formatBeijingTime(task.getConfirmTime()));
            addIndentedParagraph(document, "7. 管理确认意见：" + value(task.getConfirmOpinion()));
        }
    }

    private static void addConclusion(XWPFDocument document, QmsQualityProblem problem, List<QmsQualityTask> taskList) {
        int taskCount = taskList == null ? 0 : taskList.size();
        long confirmedCount = taskList == null ? 0 : taskList.stream()
                .filter(task -> "CONFIRMED".equals(task.getTaskStatus()))
                .count();

        addNormalParagraph(document, "1. 当前质量问题状态为：" + getProblemStatusText(problem.getStatus()) + "。");
        addNormalParagraph(document, "2. 该问题共形成 " + taskCount + " 条模块处理任务，其中已确认任务 " + confirmedCount + " 条。");

        if ("FINISHED".equals(problem.getStatus())) {
            addNormalParagraph(document, "3. 该质量问题已完成闭环处理，相关处理结果可作为后续质量复盘、知识追溯和改进措施制定依据。");
        } else {
            addNormalParagraph(document, "3. 该质量问题尚未最终结束，建议继续跟踪模块处理结果，并在管理员确认后完成问题闭环。");
        }
    }

    private static void setCellText(XWPFTableRow row, int index, String text, boolean bold) {
        row.getCell(index).removeParagraph(0);

        XWPFParagraph paragraph = row.getCell(index).addParagraph();
        paragraph.setSpacingAfter(0);

        XWPFRun run = paragraph.createRun();
        run.setText(value(text));
        run.setFontSize(10);
        run.setFontFamily("宋体");
        run.setBold(bold);
    }

    private static String value(Object value) {
        if (value == null) {
            return "-";
        }

        String str = String.valueOf(value);
        if (str.trim().isEmpty()) {
            return "-";
        }

        return str;
    }

    /**
     * 统一将时间格式化为北京时间：
     * yyyy年MM月dd日 HH时mm分ss秒
     */
    private static String formatBeijingTime(Object time) {
        if (time == null) {
            return "-";
        }

        try {
            if (time instanceof Date) {
                return ((Date) time).toInstant()
                        .atZone(BEIJING_ZONE)
                        .format(BEIJING_FORMATTER);
            }

            if (time instanceof LocalDateTime) {
                return ((LocalDateTime) time)
                        .atZone(BEIJING_ZONE)
                        .format(BEIJING_FORMATTER);
            }

            if (time instanceof OffsetDateTime) {
                return ((OffsetDateTime) time)
                        .atZoneSameInstant(BEIJING_ZONE)
                        .format(BEIJING_FORMATTER);
            }

            if (time instanceof Instant) {
                return ((Instant) time)
                        .atZone(BEIJING_ZONE)
                        .format(BEIJING_FORMATTER);
            }

            String str = String.valueOf(time).trim();
            if (str.isEmpty()) {
                return "-";
            }

            /*
             * 兼容常见字符串：
             * 2026-06-14 10:20:30
             * 2026-06-14T10:20:30
             */
            str = str.replace("T", " ");
            if (str.length() >= 19) {
                str = str.substring(0, 19);
                LocalDateTime localDateTime = LocalDateTime.parse(
                        str,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                return localDateTime.atZone(BEIJING_ZONE).format(BEIJING_FORMATTER);
            }

            return str;
        } catch (Exception e) {
            return String.valueOf(time);
        }
    }

    private static String getStandardModuleName(String moduleCodeOrName) {
        if (moduleCodeOrName == null || moduleCodeOrName.trim().isEmpty()) {
            return "-";
        }

        String value = moduleCodeOrName.trim();

        if ("PROJECT_1".equals(value) || "课题一".equals(value)) {
            return "全域异构信息集成系统";
        }
        if ("PROJECT_2".equals(value) || "课题二".equals(value)) {
            return "复杂产品设计制造协同优化平台";
        }
        if ("PROJECT_3".equals(value) || "课题三".equals(value)) {
            return "复杂产品质量监管与故障预防";
        }
        if ("PROJECT_4".equals(value) || "课题四".equals(value)) {
            return "智能故障诊断与根源性分析技术";
        }
        if ("PROJECT_5".equals(value) || "课题五".equals(value)) {
            return "质量自反馈追溯系统";
        }

        return value;
    }

    private static String getTaskModuleDisplayName(QmsQualityTask task) {
        if (task == null) {
            return "-";
        }

        // 优先使用模块编码转换，例如 PROJECT_1、PROJECT_2
        if (task.getModuleCode() != null && !task.getModuleCode().trim().isEmpty()) {
            return getStandardModuleName(task.getModuleCode());
        }

        // 如果没有模块编码，则使用模块名称兜底转换
        return getStandardModuleName(task.getModuleName());
    }

    private static String getProblemModuleDisplayName(QmsQualityProblem problem) {
        if (problem == null) {
            return "-";
        }

        // 优先使用当前模块编码
        if (problem.getCurrentModuleCode() != null && !problem.getCurrentModuleCode().trim().isEmpty()) {
            return getStandardModuleName(problem.getCurrentModuleCode());
        }

        // 如果没有编码，则用当前模块名称兜底
        return getStandardModuleName(problem.getCurrentModuleName());
    }

    private static String getProblemStatusText(String status) {
        if ("CREATED".equals(status)) {
            return "已填报";
        }
        if ("DISPATCHING".equals(status)) {
            return "待处理";
        }
        if ("PROCESSING".equals(status)) {
            return "处理中";
        }
        if ("WAIT_CONFIRM".equals(status)) {
            return "待确认";
        }
        if ("FINISHED".equals(status)) {
            return "已结束";
        }
        return value(status);
    }

    private static String getTaskStatusText(String status) {
        if ("PROCESSING".equals(status)) {
            return "处理中";
        }
        if ("SUBMITTED".equals(status)) {
            return "待确认";
        }
        if ("CONFIRMED".equals(status)) {
            return "已确认";
        }
        return value(status);
    }
}