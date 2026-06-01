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

public class PartProcessTemplateGenerator
{
    public void write(OutputStream outputStream) throws IOException
    {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("零件工序");
            String[] headers = {"零件编号", "零件名称", "工序序号", "工序编号", "工序名称"};
            writeHeader(sheet, headers, headerStyle(workbook));
            writeRow(sheet, 1, "LYB-Z-001", "液压泵轴", "5", "OP005", "粗车两端面");
            autoSize(sheet, headers.length);
            workbook.write(outputStream);
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

    private void writeRow(Sheet sheet, int rowIndex, String... values)
    {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
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
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
