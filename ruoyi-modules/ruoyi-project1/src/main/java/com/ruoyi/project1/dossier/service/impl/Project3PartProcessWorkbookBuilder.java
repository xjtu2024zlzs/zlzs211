package com.ruoyi.project1.dossier.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class Project3PartProcessWorkbookBuilder
{
    private static final String PART_SHEET = "\u96f6\u4ef6\u6a21\u677f";
    private static final String ROUTE_SHEET = "\u5de5\u5e8f\u8def\u7ebf";
    private static final String PROCESS_SHEET = "\u8be6\u7ec6\u5de5\u5e8f";

    private static final String[] PART_HEADERS = {
            "\u96f6\u4ef6id", "\u96f6\u4ef6\u7f16\u53f7", "\u96f6\u4ef6\u540d\u79f0",
            "\u7ec4\u4ef6id", "\u6750\u6599", "\u89c4\u683c\u578b\u53f7",
            "\u8bbe\u8ba1\u7248\u672c", "\u521b\u5efa\u65f6\u95f4", "\u66f4\u65b0\u65f6\u95f4" };
    private static final String[] PART_FIELDS = {
            "part_template_id", "part_number", "part_name", "component_id", "material", "specification",
            "design_version", "create_time", "update_time" };

    private static final String[] ROUTE_HEADERS = {
            "\u5de5\u5e8f\u8def\u7ebfid", "\u96f6\u4ef6id",
            "\u5de5\u5e8f\u8def\u7ebf\u540d\u79f0", "\u7248\u672c",
            "\u751f\u6548\u65e5\u671f", "\u662f\u5426\u542f\u7528" };
    private static final String[] ROUTE_FIELDS = {
            "route_id", "part_template_id", "route_name", "version", "effective_date", "is_active" };

    private static final String[] PROCESS_HEADERS = {
            "\u5de5\u5e8fid", "\u5de5\u5e8f\u8def\u7ebfid", "\u5de5\u5e8f\u5e8f\u53f7",
            "\u5de5\u5e8f\u540d\u79f0", "\u8bbe\u5907\u7c7b\u578b", "\u6807\u51c6\u5de5\u65f6",
            "\u662f\u5426\u5173\u952e\u5de5\u5e8f", "\u662f\u5426\u9ad8\u98ce\u9669" };
    private static final String[] PROCESS_FIELDS = {
            "process_def_id", "route_id", "process_number", "process_name", "equipment_type", "standard_duration",
            "is_key_process", "is_high_risk" };

    public void write(OutputStream outputStream, List<Map<String, Object>> parts, List<Map<String, Object>> routes,
            List<Map<String, Object>> processes) throws IOException
    {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            writeSheet(workbook, PART_SHEET, PART_HEADERS, PART_FIELDS, parts, headerStyle);
            writeSheet(workbook, ROUTE_SHEET, ROUTE_HEADERS, ROUTE_FIELDS, routes, headerStyle);
            writeSheet(workbook, PROCESS_SHEET, PROCESS_HEADERS, PROCESS_FIELDS, processes, headerStyle);
            workbook.write(outputStream);
        }
    }

    private void writeSheet(Workbook workbook, String sheetName, String[] headers, String[] fields,
            List<Map<String, Object>> rows, CellStyle headerStyle)
    {
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++)
        {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        if (rows != null)
        {
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++)
            {
                Row row = sheet.createRow(rowIndex + 1);
                for (int columnIndex = 0; columnIndex < fields.length; columnIndex++)
                {
                    row.createCell(columnIndex).setCellValue(text(rows.get(rowIndex), fields[columnIndex]));
                }
            }
        }
        sheet.createFreezePane(0, 1);
        autoSize(sheet, headers.length);
    }

    private CellStyle headerStyle(Workbook workbook)
    {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void autoSize(Sheet sheet, int columns)
    {
        for (int i = 0; i < columns; i++)
        {
            sheet.autoSizeColumn(i);
            int width = Math.max(sheet.getColumnWidth(i), 14 * 256);
            sheet.setColumnWidth(i, Math.min(width + 2 * 256, 48 * 256));
        }
    }

    private String text(Map<String, Object> row, String key)
    {
        Object value = get(row, key);
        return value == null ? "" : String.valueOf(value);
    }

    private Object get(Map<String, Object> row, String key)
    {
        if (row == null || key == null)
        {
            return null;
        }
        if (row.containsKey(key))
        {
            return row.get(key);
        }
        String upper = key.toUpperCase(Locale.ROOT);
        if (row.containsKey(upper))
        {
            return row.get(upper);
        }
        String lower = key.toLowerCase(Locale.ROOT);
        if (row.containsKey(lower))
        {
            return row.get(lower);
        }
        return null;
    }
}
