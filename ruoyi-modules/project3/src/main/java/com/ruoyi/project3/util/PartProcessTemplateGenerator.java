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
import java.util.Locale;
import java.util.Map;

public class PartProcessTemplateGenerator
{
    private static final String PART_SHEET = "零件模板";
    private static final String ROUTE_SHEET = "工序路线";
    private static final String PROCESS_SHEET = "详细工序";
    private static final String LIST_SHEET = "_下拉数据";
    private static final String PART_ID_LIST = "part_id_list";
    private static final String ROUTE_ID_LIST = "route_id_list";
    private static final int DATA_FIRST_ROW = 1;
    private static final int DATA_LAST_ROW = 1000;

    public void write(OutputStream outputStream) throws IOException
    {
        write(outputStream, Collections.emptyList());
    }

    public void write(OutputStream outputStream, List<Map<String, Object>> partRows) throws IOException
    {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle lockedStyle = lockedStyle(workbook);
            CellStyle unlockedStyle = unlockedStyle(workbook);

            Sheet partSheet = workbook.createSheet(PART_SHEET);
            Sheet routeSheet = workbook.createSheet(ROUTE_SHEET);
            Sheet processSheet = workbook.createSheet(PROCESS_SHEET);
            Sheet listSheet = workbook.createSheet(LIST_SHEET);

            writePartSheet(partSheet, partRows, headerStyle, lockedStyle);
            writeRouteSheet(routeSheet, headerStyle, unlockedStyle);
            writeProcessSheet(processSheet, headerStyle, unlockedStyle);
            writeListSheet(listSheet, partRows);
            defineNames(workbook, partRows == null ? 0 : partRows.size());
            addDropdown(routeSheet, DATA_FIRST_ROW, DATA_LAST_ROW, 1, PART_ID_LIST);
            addDropdown(processSheet, DATA_FIRST_ROW, DATA_LAST_ROW, 1, ROUTE_ID_LIST);

            protect(partSheet);
            protect(routeSheet);
            protect(processSheet);
            workbook.setSheetHidden(workbook.getSheetIndex(LIST_SHEET), true);
            workbook.write(outputStream);
        }
    }

    private void writePartSheet(Sheet sheet, List<Map<String, Object>> partRows, CellStyle headerStyle, CellStyle lockedStyle)
    {
        String[] headers = {"零件id", "零件编号", "零件名称", "组件id", "材料", "规格型号", "设计版本", "创建时间", "更新时间"};
        String[] fields = {"part_template_id", "part_number", "part_name", "component_id", "material", "specification", "design_version", "create_time", "update_time"};
        writeHeader(sheet, headers, headerStyle);
        if (partRows != null) {
            for (int i = 0; i < partRows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < fields.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(text(partRows.get(i), fields[j]));
                    cell.setCellStyle(lockedStyle);
                }
            }
        }
        autoSize(sheet, headers.length);
    }

    private void writeRouteSheet(Sheet sheet, CellStyle headerStyle, CellStyle unlockedStyle)
    {
        String[] headers = {"工序路线id", "零件id", "工序路线名称", "版本", "生效日期", "是否启用"};
        writeHeader(sheet, headers, headerStyle);
        unlockDataCells(sheet, headers.length, unlockedStyle);
        autoSize(sheet, headers.length);
    }

    private void writeProcessSheet(Sheet sheet, CellStyle headerStyle, CellStyle unlockedStyle)
    {
        String[] headers = {"工序id", "工序路线id", "工序序号", "工序名称", "设备类型", "标准工时", "是否关键工序", "是否高风险"};
        writeHeader(sheet, headers, headerStyle);
        unlockDataCells(sheet, headers.length, unlockedStyle);
        autoSize(sheet, headers.length);
    }

    private void writeListSheet(Sheet sheet, List<Map<String, Object>> partRows)
    {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("零件id");
        if (partRows == null) return;
        for (int i = 0; i < partRows.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(text(partRows.get(i), "part_template_id"));
        }
    }

    private void writeHeader(Sheet sheet, String[] headers, CellStyle headerStyle)
    {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.createFreezePane(0, 1);
    }

    private void unlockDataCells(Sheet sheet, int columns, CellStyle unlockedStyle)
    {
        for (int r = DATA_FIRST_ROW; r <= DATA_LAST_ROW; r++) {
            Row row = sheet.getRow(r);
            if (row == null) row = sheet.createRow(r);
            for (int c = 0; c < columns; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) cell = row.createCell(c);
                cell.setCellStyle(unlockedStyle);
            }
        }
    }

    private void defineNames(Workbook workbook, int partCount)
    {
        int partRows = Math.max(1, partCount);
        Name partName = workbook.createName();
        partName.setNameName(PART_ID_LIST);
        partName.setRefersToFormula("'" + LIST_SHEET + "'!$A$2:$A$" + (partRows + 1));

        Name routeName = workbook.createName();
        routeName.setNameName(ROUTE_ID_LIST);
        routeName.setRefersToFormula("OFFSET('" + ROUTE_SHEET + "'!$A$2,0,0,MAX(1,COUNTA('" + ROUTE_SHEET + "'!$A:$A)-1),1)");
    }

    private void addDropdown(Sheet sheet, int firstRow, int lastRow, int col, String name)
    {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint(name);
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, lastRow, col, col);
        DataValidation validation = helper.createValidation(constraint, regions);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("输入无效", "请从下拉列表中选择已存在的数据");
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
            int width = Math.max(sheet.getColumnWidth(i), 14 * 256);
            sheet.setColumnWidth(i, Math.min(width + 2 * 256, 40 * 256));
        }
    }

    private CellStyle headerStyle(Workbook workbook)
    {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
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

    private CellStyle unlockedStyle(Workbook workbook)
    {
        CellStyle style = workbook.createCellStyle();
        style.setLocked(false);
        return style;
    }

    private String text(Map<String, Object> row, String key)
    {
        Object value = get(row, key);
        return value == null ? "" : String.valueOf(value);
    }

    private Object get(Map<String, Object> row, String key)
    {
        if (row == null || key == null) return null;
        if (row.containsKey(key)) return row.get(key);
        String upper = key.toUpperCase(Locale.ROOT);
        if (row.containsKey(upper)) return row.get(upper);
        String lower = key.toLowerCase(Locale.ROOT);
        if (row.containsKey(lower)) return row.get(lower);
        return null;
    }
}
