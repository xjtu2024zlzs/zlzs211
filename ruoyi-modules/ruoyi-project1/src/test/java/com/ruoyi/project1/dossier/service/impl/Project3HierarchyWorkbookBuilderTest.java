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

class Project3HierarchyWorkbookBuilderTest
{
    @Test
    void writesProject3HierarchyWorkbook() throws Exception
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        new Project3HierarchyWorkbookBuilder().write(outputStream,
                rows(row("aircraft_id", "aircraft-1",
                        "aircraft_name", "C919 B-1234",
                        "aircraft_model", "C919",
                        "serial_number", "B1234B1234",
                        "status", "IN_TEST",
                        "remarks", "demo aircraft")),
                rows(row("subsystem_id", "subsystem-1",
                        "subsystem_name", "landing gear hydraulic subsystem",
                        "aircraft_id", "aircraft-1",
                        "remarks", "hydraulic system")),
                rows(row("equipment_id", "equipment-1",
                        "equipment_name", "main landing gear hydraulic supply equipment",
                        "subsystem_id", "subsystem-1",
                        "remarks", "")),
                rows(row("component_id", "component-1",
                        "component_name", "hydraulic supply pipe assembly",
                        "equipment_id", "equipment-1",
                        "specification", "assembly",
                        "remarks", "")),
                rows(row("part_template_id", "HYD-TUBE-MLG-32A",
                        "part_number", "HYD-TUBE-MLG-32A",
                        "part_name", "MLG hydraulic supply bend tube",
                        "component_id", "component-1",
                        "material", "stainless steel",
                        "specification", "OD9.53 WT0.9",
                        "design_version", "Rev.C")),
                rows(row("part_instance_id", "part-instance-1",
                        "part_template_id", "HYD-TUBE-MLG-32A",
                        "serial_number", "SN-001",
                        "batch_number", "BATCH-001",
                        "manufacturer", "demo manufacturer",
                        "production_date", "2026-06-01",
                        "current_status", "INSTALLED",
                        "quality_level", "A",
                        "key_degree", "important",
                        "image_url", "")));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(outputStream.toByteArray()))) {
            assertHeaders(workbook, "aircraft", new String[] {
                    "\u98de\u673aID", "\u98de\u673a\u540d\u79f0", "\u98de\u673a\u578b\u53f7",
                    "\u5e8f\u5217\u53f7", "\u72b6\u6001", "\u5907\u6ce8" });
            assertHeaders(workbook, "subsystems", new String[] {
                    "\u5206\u7cfb\u7edfID", "\u5206\u7cfb\u7edf\u540d\u79f0",
                    "\u98de\u673aID", "\u5907\u6ce8" });
            assertHeaders(workbook, "equipments", new String[] {
                    "\u8bbe\u5907ID", "\u8bbe\u5907\u540d\u79f0",
                    "\u5206\u7cfb\u7edfID", "\u5907\u6ce8" });
            assertHeaders(workbook, "components", new String[] {
                    "\u7ec4\u4ef6ID", "\u7ec4\u4ef6\u540d\u79f0",
                    "\u8bbe\u5907ID", "\u89c4\u683c\u578b\u53f7", "\u5907\u6ce8" });
            assertHeaders(workbook, "part_templates", new String[] {
                    "\u96f6\u4ef6\u6a21\u677fID", "\u96f6\u4ef6\u7f16\u53f7",
                    "\u96f6\u4ef6\u540d\u79f0", "\u7ec4\u4ef6ID",
                    "\u6750\u6599", "\u89c4\u683c\u578b\u53f7", "\u8bbe\u8ba1\u7248\u672c" });
            assertHeaders(workbook, "part_instances", new String[] {
                    "\u96f6\u4ef6\u5b9e\u4f8bID", "\u96f6\u4ef6\u6a21\u677fID",
                    "\u5e8f\u5217\u53f7", "\u6279\u6b21\u53f7", "\u5236\u9020\u5546",
                    "\u751f\u4ea7\u65e5\u671f", "\u5f53\u524d\u72b6\u6001",
                    "\u8d28\u91cf\u7b49\u7ea7", "\u5173\u952e\u7a0b\u5ea6", "\u56fe\u7247\u5730\u5740" });

            DataFormatter formatter = new DataFormatter();
            assertEquals("C919 B-1234", cell(workbook, "aircraft", 1, 1, formatter));
            assertEquals("HYD-TUBE-MLG-32A", cell(workbook, "part_templates", 1, 0, formatter));
            assertEquals("part-instance-1", cell(workbook, "part_instances", 1, 0, formatter));
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
