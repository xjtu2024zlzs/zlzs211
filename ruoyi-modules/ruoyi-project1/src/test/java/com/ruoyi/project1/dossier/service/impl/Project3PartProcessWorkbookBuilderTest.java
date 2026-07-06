package com.ruoyi.project1.dossier.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

class Project3PartProcessWorkbookBuilderTest
{
    private static final String PART_SHEET = "\u96f6\u4ef6\u6a21\u677f";
    private static final String ROUTE_SHEET = "\u5de5\u5e8f\u8def\u7ebf";
    private static final String PROCESS_SHEET = "\u8be6\u7ec6\u5de5\u5e8f";

    @Test
    void writesProject3StandardProcessWorkbook() throws Exception
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        new Project3PartProcessWorkbookBuilder().write(outputStream,
                rows(row("part_template_id", "HYD-TUBE-MLG-32A",
                        "part_number", "HYD-TUBE-MLG-32A",
                        "part_name", "MLG hydraulic supply bend tube",
                        "component_id", "f1000005-0005-4005-8005-000000000005",
                        "material", "stainless steel",
                        "specification", "OD9.53 WT0.9",
                        "design_version", "Rev.C",
                        "create_time", "2026-06-01 08:00:00",
                        "update_time", "2026-06-02 09:00:00")),
                rows(row("route_id", "R-HYD-TUBE-MLG-32A-REV-C",
                        "part_template_id", "HYD-TUBE-MLG-32A",
                        "route_name", "standard route",
                        "version", "Rev.C",
                        "effective_date", "2026-06-01",
                        "is_active", "1")),
                rows(row("process_def_id", "PD-HYD-TUBE-MLG-32A-010",
                        "route_id", "R-HYD-TUBE-MLG-32A-REV-C",
                        "process_number", 10,
                        "process_name", "blank cutting",
                        "equipment_type", "tube cutting machine",
                        "standard_duration", "30",
                        "is_key_process", "0",
                        "is_high_risk", "0"),
                     row("process_def_id", "PD-HYD-TUBE-MLG-32A-020",
                        "route_id", "R-HYD-TUBE-MLG-32A-REV-C",
                        "process_number", 20,
                        "process_name", "CNC bending",
                        "equipment_type", "CNC tube bender",
                        "standard_duration", "45",
                        "is_key_process", "1",
                        "is_high_risk", "1")));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(outputStream.toByteArray()))) {
            assertHeaders(workbook, PART_SHEET, new String[] {
                    "\u96f6\u4ef6id", "\u96f6\u4ef6\u7f16\u53f7", "\u96f6\u4ef6\u540d\u79f0",
                    "\u7ec4\u4ef6id", "\u6750\u6599", "\u89c4\u683c\u578b\u53f7",
                    "\u8bbe\u8ba1\u7248\u672c", "\u521b\u5efa\u65f6\u95f4", "\u66f4\u65b0\u65f6\u95f4" });
            assertHeaders(workbook, ROUTE_SHEET, new String[] {
                    "\u5de5\u5e8f\u8def\u7ebfid", "\u96f6\u4ef6id",
                    "\u5de5\u5e8f\u8def\u7ebf\u540d\u79f0", "\u7248\u672c",
                    "\u751f\u6548\u65e5\u671f", "\u662f\u5426\u542f\u7528" });
            assertHeaders(workbook, PROCESS_SHEET, new String[] {
                    "\u5de5\u5e8fid", "\u5de5\u5e8f\u8def\u7ebfid", "\u5de5\u5e8f\u5e8f\u53f7",
                    "\u5de5\u5e8f\u540d\u79f0", "\u8bbe\u5907\u7c7b\u578b", "\u6807\u51c6\u5de5\u65f6",
                    "\u662f\u5426\u5173\u952e\u5de5\u5e8f", "\u662f\u5426\u9ad8\u98ce\u9669" });

            DataFormatter formatter = new DataFormatter();
            assertEquals("HYD-TUBE-MLG-32A", cell(workbook, PART_SHEET, 1, 0, formatter));
            assertEquals("R-HYD-TUBE-MLG-32A-REV-C", cell(workbook, ROUTE_SHEET, 1, 0, formatter));
            assertEquals("PD-HYD-TUBE-MLG-32A-020", cell(workbook, PROCESS_SHEET, 2, 0, formatter));
            assertEquals("CNC tube bender", cell(workbook, PROCESS_SHEET, 2, 4, formatter));
        }
    }

    private static void assertHeaders(Workbook workbook, String sheetName, String[] expected)
    {
        Sheet sheet = workbook.getSheet(sheetName);
        assertNotNull(sheet, "Missing sheet: " + sheetName);
        Row row = sheet.getRow(0);
        DataFormatter formatter = new DataFormatter();
        String[] actual = new String[expected.length];
        for (int i = 0; i < expected.length; i++)
        {
            actual[i] = formatter.formatCellValue(row.getCell(i));
        }
        assertArrayEquals(expected, actual);
    }

    private static String cell(Workbook workbook, String sheetName, int rowIndex, int cellIndex,
            DataFormatter formatter)
    {
        return formatter.formatCellValue(workbook.getSheet(sheetName).getRow(rowIndex).getCell(cellIndex));
    }

    @SafeVarargs
    private static List<Map<String, Object>> rows(Map<String, Object>... rows)
    {
        return Arrays.asList(rows);
    }

    private static Map<String, Object> row(Object... values)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2)
        {
            row.put(String.valueOf(values[i]), values[i + 1]);
        }
        return row;
    }
}
