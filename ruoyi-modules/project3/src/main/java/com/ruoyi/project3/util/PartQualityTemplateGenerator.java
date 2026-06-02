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

public class PartQualityTemplateGenerator
{
    public void write(OutputStream outputStream) throws IOException
    {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            designSheet(workbook, headerStyle);
            manufacturingSheet(workbook, headerStyle);
            serviceSheet(workbook, headerStyle);
            workbook.write(outputStream);
        }
    }

    private void designSheet(Workbook workbook, CellStyle headerStyle)
    {
        Sheet sheet = workbook.createSheet("设计质量信息");
        String[] headers = {"设计质量ID", "零件模板ID", "设计版本", "图纸版本", "设计要求", "功能要求", "公差要求", "关键质量特性", "设计评审结果", "验证结果"};
        writeHeader(sheet, headers, headerStyle);
        writeRow(sheet, 1, "DQ001", "PT1", "V1.0", "DWG-001", "满足设计规范", "满足功能要求", "±0.01", "尺寸精度、材料强度", "通过", "通过");
        autoSize(sheet, headers.length);
    }

    private void manufacturingSheet(Workbook workbook, CellStyle headerStyle)
    {
        Sheet sheet = workbook.createSheet("制造质量信息");
        String[] headers = {"制造质量ID", "零件实例ID", "生产工单ID", "车间ID", "产线ID", "开始时间", "结束时间", "工艺状态", "终检结果", "缺陷数", "返工数"};
        writeHeader(sheet, headers, headerStyle);
        writeRow(sheet, 1, "MQ001", "PI1", "WO001", "WS01", "PL01", "2026-05-20 08:00:00", "2026-05-20 12:00:00", "已完成", "合格", "0", "0");
        autoSize(sheet, headers.length);
    }

    private void serviceSheet(Workbook workbook, CellStyle headerStyle)
    {
        Sheet sheet = workbook.createSheet("服役质量信息");
        String[] headers = {"服役质量ID", "零件实例id", "设备ID", "组件ID", "装机飞机ID", "安装位置", "安装日期", "服役状态", "累计运行小时", "循环次数", "健康评分", "上次检修日期", "下次检修日期"};
        writeHeader(sheet, headers, headerStyle);
        writeRow(sheet, 1, "SQ001", "PI1", "E1", "C1", "A1", "左侧液压舱", "2026-05-20", "在役", "120.5", "30", "98.5", "2026-04-20", "2026-06-20");
        autoSize(sheet, headers.length);
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
