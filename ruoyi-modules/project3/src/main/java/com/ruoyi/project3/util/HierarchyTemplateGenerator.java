package com.ruoyi.project3.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;

public class HierarchyTemplateGenerator
{
    public void write(OutputStream outputStream) throws IOException
    {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            sheet(workbook, headerStyle, "aircraft",
                    new String[]{"aircraft_id", "aircraft_name", "aircraft_model", "serial_number", "status", "remarks"},
                    new String[]{"A001", "某型飞机", "MODEL-A", "SN-A001", "在役", ""});
            sheet(workbook, headerStyle, "subsystems",
                    new String[]{"subsystem_id", "subsystem_name", "aircraft_id", "remarks"},
                    new String[]{"S001", "液压系统", "A001", ""});
            sheet(workbook, headerStyle, "equipments",
                    new String[]{"equipment_id", "equipment_name", "subsystem_id", "remarks"},
                    new String[]{"E001", "液压泵", "S001", ""});
            sheet(workbook, headerStyle, "components",
                    new String[]{"component_id", "component_name", "equipment_id", "specification", "remarks"},
                    new String[]{"C001", "泵体组件", "E001", "SPEC-001", ""});
            sheet(workbook, headerStyle, "part_templates",
                    new String[]{"part_template_id", "part_number", "part_name", "component_id", "material", "specification", "design_version"},
                    new String[]{"PT001", "LYB-Z-001", "液压泵轴", "C001", "40Cr", "D20x180", "V1.0"});
            sheet(workbook, headerStyle, "part_instances",
                    new String[]{"part_instance_id", "part_template_id", "serial_number", "batch_number", "manufacturer", "production_date", "current_status", "quality_level", "key_degree", "image_url"},
                    new String[]{"PI001", "PT001", "SN-001", "BATCH-001", "一车间", "2026-05-20", "在库", "A", "关键", ""});
            workbook.write(outputStream);
        }
    }

    private void sheet(Workbook workbook, CellStyle headerStyle, String sheetName, String[] headers, String[] example)
    {
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        Row row = sheet.createRow(1);
        for (int i = 0; i < example.length; i++) {
            row.createCell(i).setCellValue(example[i]);
        }
        sheet.createFreezePane(0, 1);
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            int width = Math.max(sheet.getColumnWidth(i), 16 * 256);
            sheet.setColumnWidth(i, Math.min(width + 2 * 256, 40 * 256));
        }
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
}
