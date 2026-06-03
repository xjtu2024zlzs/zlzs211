package com.ruoyi.project3.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PartActualManufacturingProcessTemplateGenerator
{
    private static final String PART_INSTANCE_SHEET = "零件实例";
    private static final String WORK_ORDER_SHEET = "生产工单";
    private static final String PROCESS_EXECUTION_SHEET = "工序执行记录";
    private static final String MASTER_DATA_SHEET = "基础数据";
    private static final String PART_TEMPLATE_ID_LIST = "actual_part_template_id_list";
    private static final String ROUTE_ID_LIST = "actual_route_id_list";
    private static final String PROCESS_DEF_ID_LIST = "actual_process_def_id_list";
    private static final int DATA_FIRST_ROW = 1;
    private static final int DATA_LAST_ROW = 1000;

    public void write(OutputStream outputStream) throws IOException
    {
        write(outputStream, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public void write(OutputStream outputStream, List<Map<String, Object>> partTemplateRows,
                      List<Map<String, Object>> routeRows, List<Map<String, Object>> processDefRows) throws IOException
    {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle requiredStyle = headerStyle(workbook, IndexedColors.LIGHT_YELLOW);
            CellStyle optionalStyle = headerStyle(workbook, IndexedColors.PALE_BLUE);
            CellStyle lockedStyle = lockedStyle(workbook);

            Sheet masterDataSheet = workbook.createSheet(MASTER_DATA_SHEET);
            Sheet partInstanceSheet = workbook.createSheet(PART_INSTANCE_SHEET);
            Sheet workOrderSheet = workbook.createSheet(WORK_ORDER_SHEET);
            Sheet processExecutionSheet = workbook.createSheet(PROCESS_EXECUTION_SHEET);

            writeMasterData(masterDataSheet, partTemplateRows, routeRows, processDefRows, requiredStyle, lockedStyle);
            writePartInstances(partInstanceSheet, requiredStyle, optionalStyle);
            writeWorkOrders(workOrderSheet, requiredStyle, optionalStyle);
            writeProcessExecutions(processExecutionSheet, requiredStyle, optionalStyle);
            defineNames(workbook, size(partTemplateRows), size(routeRows), size(processDefRows));

            addDropdown(partInstanceSheet, DATA_FIRST_ROW, DATA_LAST_ROW, 1, PART_TEMPLATE_ID_LIST);
            addDropdown(workOrderSheet, DATA_FIRST_ROW, DATA_LAST_ROW, 2, ROUTE_ID_LIST);
            addDropdown(processExecutionSheet, DATA_FIRST_ROW, DATA_LAST_ROW, 2, PROCESS_DEF_ID_LIST);

            protect(masterDataSheet);
            workbook.write(outputStream);
        }
    }

    private void writePartInstances(Sheet sheet, CellStyle requiredStyle, CellStyle optionalStyle)
    {
        String[] labels = {"零件实例ID", "零件模板ID", "零件序列号", "批次号", "制造商", "生产日期(yyyy-MM-dd)", "当前状态", "质量等级", "关键度", "零件图片地址"};
        writeHeaders(sheet, labels, requiredStyle, optionalStyle, 3);
        addListValidation(sheet, DATA_FIRST_ROW, DATA_LAST_ROW, 6, "未生产", "生产中", "已完成", "已报废", "已返工");
        addListValidation(sheet, DATA_FIRST_ROW, DATA_LAST_ROW, 7, "A", "B", "C", "D");
        addListValidation(sheet, DATA_FIRST_ROW, DATA_LAST_ROW, 8, "低", "中", "高", "关键");
        autoSize(sheet, labels.length);
    }

    private void writeWorkOrders(Sheet sheet, CellStyle requiredStyle, CellStyle optionalStyle)
    {
        String[] labels = {"生产工单ID", "零件实例ID", "工艺路线ID", "制造质量记录ID", "生产线", "工单开始时间(yyyy-MM-dd HH:mm:ss)", "工单结束时间(yyyy-MM-dd HH:mm:ss)", "操作人员", "工单状态"};
        writeHeaders(sheet, labels, requiredStyle, optionalStyle, 3);
        addListValidation(sheet, DATA_FIRST_ROW, DATA_LAST_ROW, 8, "待开始", "执行中", "已完成", "已暂停", "已取消", "异常");
        autoSize(sheet, labels.length);
    }

    private void writeProcessExecutions(Sheet sheet, CellStyle requiredStyle, CellStyle optionalStyle)
    {
        String[] labels = {"工序执行记录ID", "生产工单ID", "详细工序ID", "制造设备ID", "工序开始时间(yyyy-MM-dd HH:mm:ss)", "工序结束时间(yyyy-MM-dd HH:mm:ss)", "操作人员", "工序执行状态", "备注"};
        writeHeaders(sheet, labels, requiredStyle, optionalStyle, 3);
        addListValidation(sheet, DATA_FIRST_ROW, DATA_LAST_ROW, 7, "待执行", "执行中", "已完成", "异常", "跳过", "返工");
        autoSize(sheet, labels.length);
    }

    private void writeMasterData(Sheet sheet, List<Map<String, Object>> partTemplateRows,
                                 List<Map<String, Object>> routeRows, List<Map<String, Object>> processDefRows,
                                 CellStyle headerStyle, CellStyle lockedStyle)
    {
        String[] headers = {"零件模板ID", "工艺路线ID", "详细工序ID"};
        writeHeaders(sheet, headers, headerStyle, headerStyle, headers.length);
        writeColumn(sheet, partTemplateRows, "part_template_id", 0, lockedStyle);
        writeColumn(sheet, routeRows, "route_id", 1, lockedStyle);
        writeColumn(sheet, processDefRows, "process_def_id", 2, lockedStyle);
        autoSize(sheet, headers.length);
    }

    private void writeColumn(Sheet sheet, List<Map<String, Object>> rows, String field, int column, CellStyle lockedStyle)
    {
        if (rows == null) return;
        for (int i = 0; i < rows.size(); i++) {
            Row row = sheet.getRow(i + 1);
            if (row == null) row = sheet.createRow(i + 1);
            Cell cell = row.createCell(column);
            cell.setCellValue(text(rows.get(i), field));
            cell.setCellStyle(lockedStyle);
        }
    }

    private void writeHeaders(Sheet sheet, String[] labels, CellStyle requiredStyle, CellStyle optionalStyle, int requiredCount)
    {
        Row row = sheet.createRow(0);
        for (int i = 0; i < labels.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(labels[i]);
            cell.setCellStyle(i < requiredCount ? requiredStyle : optionalStyle);
        }
        sheet.createFreezePane(0, 1);
    }

    private void defineNames(Workbook workbook, int partTemplateCount, int routeCount, int processDefCount)
    {
        defineName(workbook, PART_TEMPLATE_ID_LIST, "A", partTemplateCount);
        defineName(workbook, ROUTE_ID_LIST, "B", routeCount);
        defineName(workbook, PROCESS_DEF_ID_LIST, "C", processDefCount);
    }

    private void defineName(Workbook workbook, String name, String column, int count)
    {
        Name workbookName = workbook.createName();
        workbookName.setNameName(name);
        workbookName.setRefersToFormula("'" + MASTER_DATA_SHEET + "'!$" + column + "$2:$" + column + "$" + (Math.max(1, count) + 1));
    }

    private void addDropdown(Sheet sheet, int firstRow, int lastRow, int col, String name)
    {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint(name);
        DataValidation validation = helper.createValidation(constraint, new CellRangeAddressList(firstRow, lastRow, col, col));
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("输入无效", "请从下拉列表中选择基础数据中已有的ID");
        sheet.addValidationData(validation);
    }

    private void addListValidation(Sheet sheet, int firstRow, int lastRow, int col, String... values)
    {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(values);
        DataValidation validation = helper.createValidation(constraint, new CellRangeAddressList(firstRow, lastRow, col, col));
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        sheet.addValidationData(validation);
    }

    private void protect(Sheet sheet)
    {
        sheet.protectSheet("project3");
    }

    private void autoSize(Sheet sheet, int columns)
    {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
            int width = Math.max(sheet.getColumnWidth(i), 16 * 256);
            sheet.setColumnWidth(i, Math.min(width + 2 * 256, 45 * 256));
        }
    }

    private CellStyle headerStyle(Workbook workbook, IndexedColors color)
    {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setLocked(true);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle lockedStyle(Workbook workbook)
    {
        CellStyle style = workbook.createCellStyle();
        style.setLocked(true);
        return style;
    }

    private int size(List<Map<String, Object>> rows)
    {
        return rows == null ? 0 : rows.size();
    }

    private String text(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? "" : String.valueOf(value);
    }
}
