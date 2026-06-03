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
                    new String[]{"飞机ID", "飞机名称", "飞机型号", "序列号", "状态", "备注"});
            sheet(workbook, headerStyle, "subsystems",
                    new String[]{"分系统ID", "分系统名称", "飞机ID", "备注"});
            sheet(workbook, headerStyle, "equipments",
                    new String[]{"设备ID", "设备名称", "分系统ID", "备注"});
            sheet(workbook, headerStyle, "components",
                    new String[]{"组件ID", "组件名称", "设备ID", "规格型号", "备注"});
            sheet(workbook, headerStyle, "part_templates",
                    new String[]{"零件模板ID", "零件编号", "零件名称", "组件ID", "材料", "规格型号", "设计版本"});
            sheet(workbook, headerStyle, "part_instances",
                    new String[]{"零件实例ID", "零件模板ID", "序列号", "批次号", "制造商", "生产日期", "当前状态", "质量等级", "关键程度", "图片地址"});
            workbook.write(outputStream);
        }
    }

    private void sheet(Workbook workbook, CellStyle headerStyle, String sheetName, String[] headers)
    {
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
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
