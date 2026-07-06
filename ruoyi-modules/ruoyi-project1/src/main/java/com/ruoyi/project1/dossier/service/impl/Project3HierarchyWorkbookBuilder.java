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

class Project3HierarchyWorkbookBuilder
{
    private static final String[] AIRCRAFT_HEADERS = {
            "\u98de\u673aID", "\u98de\u673a\u540d\u79f0", "\u98de\u673a\u578b\u53f7",
            "\u5e8f\u5217\u53f7", "\u72b6\u6001", "\u5907\u6ce8" };
    private static final String[] AIRCRAFT_FIELDS = {
            "aircraft_id", "aircraft_name", "aircraft_model", "serial_number", "status", "remarks" };

    private static final String[] SUBSYSTEM_HEADERS = {
            "\u5206\u7cfb\u7edfID", "\u5206\u7cfb\u7edf\u540d\u79f0", "\u98de\u673aID", "\u5907\u6ce8" };
    private static final String[] SUBSYSTEM_FIELDS = {
            "subsystem_id", "subsystem_name", "aircraft_id", "remarks" };

    private static final String[] EQUIPMENT_HEADERS = {
            "\u8bbe\u5907ID", "\u8bbe\u5907\u540d\u79f0", "\u5206\u7cfb\u7edfID", "\u5907\u6ce8" };
    private static final String[] EQUIPMENT_FIELDS = {
            "equipment_id", "equipment_name", "subsystem_id", "remarks" };

    private static final String[] COMPONENT_HEADERS = {
            "\u7ec4\u4ef6ID", "\u7ec4\u4ef6\u540d\u79f0", "\u8bbe\u5907ID",
            "\u89c4\u683c\u578b\u53f7", "\u5907\u6ce8" };
    private static final String[] COMPONENT_FIELDS = {
            "component_id", "component_name", "equipment_id", "specification", "remarks" };

    private static final String[] PART_TEMPLATE_HEADERS = {
            "\u96f6\u4ef6\u6a21\u677fID", "\u96f6\u4ef6\u7f16\u53f7",
            "\u96f6\u4ef6\u540d\u79f0", "\u7ec4\u4ef6ID",
            "\u6750\u6599", "\u89c4\u683c\u578b\u53f7", "\u8bbe\u8ba1\u7248\u672c" };
    private static final String[] PART_TEMPLATE_FIELDS = {
            "part_template_id", "part_number", "part_name", "component_id", "material", "specification",
            "design_version" };

    private static final String[] PART_INSTANCE_HEADERS = {
            "\u96f6\u4ef6\u5b9e\u4f8bID", "\u96f6\u4ef6\u6a21\u677fID",
            "\u5e8f\u5217\u53f7", "\u6279\u6b21\u53f7", "\u5236\u9020\u5546",
            "\u751f\u4ea7\u65e5\u671f", "\u5f53\u524d\u72b6\u6001",
            "\u8d28\u91cf\u7b49\u7ea7", "\u5173\u952e\u7a0b\u5ea6", "\u56fe\u7247\u5730\u5740" };
    private static final String[] PART_INSTANCE_FIELDS = {
            "part_instance_id", "part_template_id", "serial_number", "batch_number", "manufacturer",
            "production_date", "current_status", "quality_level", "key_degree", "image_url" };

    public void write(OutputStream outputStream, List<Map<String, Object>> aircraft,
            List<Map<String, Object>> subsystems, List<Map<String, Object>> equipments,
            List<Map<String, Object>> components, List<Map<String, Object>> partTemplates,
            List<Map<String, Object>> partInstances) throws IOException
    {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            writeSheet(workbook, "aircraft", AIRCRAFT_HEADERS, AIRCRAFT_FIELDS, aircraft, headerStyle);
            writeSheet(workbook, "subsystems", SUBSYSTEM_HEADERS, SUBSYSTEM_FIELDS, subsystems, headerStyle);
            writeSheet(workbook, "equipments", EQUIPMENT_HEADERS, EQUIPMENT_FIELDS, equipments, headerStyle);
            writeSheet(workbook, "components", COMPONENT_HEADERS, COMPONENT_FIELDS, components, headerStyle);
            writeSheet(workbook, "part_templates", PART_TEMPLATE_HEADERS, PART_TEMPLATE_FIELDS, partTemplates,
                    headerStyle);
            writeSheet(workbook, "part_instances", PART_INSTANCE_HEADERS, PART_INSTANCE_FIELDS, partInstances,
                    headerStyle);
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
