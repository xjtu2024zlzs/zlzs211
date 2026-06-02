package com.ruoyi.project3.util;

import com.ruoyi.project3.domain.partquality.DesignQuality;
import com.ruoyi.project3.domain.partquality.ManufacturingQuality;
import com.ruoyi.project3.domain.partquality.PartQualityImportData;
import com.ruoyi.project3.domain.partquality.PartQualityImportError;
import com.ruoyi.project3.domain.partquality.ServiceQuality;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PartQualityExcelParser
{
    private static final String DESIGN_SHEET = "设计质量信息";
    private static final String MANUFACTURING_SHEET = "制造质量信息";
    private static final String SERVICE_SHEET = "服役质量信息";

    private final DataFormatter formatter = new DataFormatter();
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public PartQualityExcelParser()
    {
        dateTimeFormat.setLenient(false);
        dateFormat.setLenient(false);
    }

    public PartQualityImportData parse(MultipartFile file)
    {
        PartQualityImportData data = new PartQualityImportData();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            parseDesign(workbook.getSheet(DESIGN_SHEET), data);
            parseManufacturing(workbook.getSheet(MANUFACTURING_SHEET), data);
            parseService(workbook.getSheet(SERVICE_SHEET), data);
        } catch (Exception e) {
            data.getErrors().add(new PartQualityImportError(null, null, "Excel文件", "解析Excel失败：" + e.getMessage()));
        }
        return data;
    }

    private void parseDesign(Sheet sheet, PartQualityImportData data)
    {
        if (!ensureSheet(sheet, DESIGN_SHEET, data)) return;
        Map<String, Integer> header = header(sheet, data, DESIGN_SHEET);
        String[] requiredHeaders = {"设计质量ID", "零件模板ID", "设计版本", "图纸版本", "设计要求", "功能要求", "公差要求", "关键质量特性", "设计评审结果", "验证结果"};
        if (!ensureHeaders(header, requiredHeaders, DESIGN_SHEET, data)) return;

        Set<String> ids = new HashSet<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (emptyRow(row)) continue;
            int rowIndex = i + 1;
            String id = text(row, header, "设计质量ID");
            if (blank(id)) continue;
            if (!ids.add(id)) {
                data.getErrors().add(err(DESIGN_SHEET, rowIndex, "设计质量ID", "设计质量ID重复：" + id));
                continue;
            }
            DesignQuality item = new DesignQuality();
            item.setDesignQualityId(id);
            item.setPartTemplateId(text(row, header, "零件模板ID"));
            item.setDesignVersion(text(row, header, "设计版本"));
            item.setDrawingVersion(text(row, header, "图纸版本"));
            item.setDesignRequirements(text(row, header, "设计要求"));
            item.setFunctionalRequirements(text(row, header, "功能要求"));
            item.setToleranceRequirements(text(row, header, "公差要求"));
            item.setKeyQualityCharacteristics(text(row, header, "关键质量特性"));
            item.setDesignReviewResult(text(row, header, "设计评审结果"));
            item.setVerificationResult(text(row, header, "验证结果"));

            req(data, DESIGN_SHEET, rowIndex, "零件模板ID", item.getPartTemplateId());
            data.getDesignRows().add(item);
        }
    }

    private void parseManufacturing(Sheet sheet, PartQualityImportData data)
    {
        if (!ensureSheet(sheet, MANUFACTURING_SHEET, data)) return;
        Map<String, Integer> header = header(sheet, data, MANUFACTURING_SHEET);
        String[] requiredHeaders = {"制造质量ID", "零件实例ID", "生产工单ID", "车间ID", "产线ID", "开始时间", "结束时间", "工艺状态", "终检结果", "缺陷数", "返工数"};
        if (!ensureHeaders(header, requiredHeaders, MANUFACTURING_SHEET, data)) return;

        Set<String> ids = new HashSet<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (emptyRow(row)) continue;
            int rowIndex = i + 1;
            String id = text(row, header, "制造质量ID");
            if (blank(id)) continue;
            if (!ids.add(id)) {
                data.getErrors().add(err(MANUFACTURING_SHEET, rowIndex, "制造质量ID", "制造质量ID重复：" + id));
                continue;
            }
            ManufacturingQuality item = new ManufacturingQuality();
            item.setManufacturingQualityId(id);
            item.setPartInstanceId(text(row, header, "零件实例ID"));
            item.setProductionOrderId(text(row, header, "生产工单ID"));
            item.setWorkshopId(text(row, header, "车间ID"));
            item.setProductionLineId(text(row, header, "产线ID"));
            item.setStartTime(dateTime(row, header, "开始时间", MANUFACTURING_SHEET, rowIndex, data));
            item.setEndTime(dateTime(row, header, "结束时间", MANUFACTURING_SHEET, rowIndex, data));
            item.setProcessStatus(text(row, header, "工艺状态"));
            item.setFinalInspectionResult(text(row, header, "终检结果"));
            item.setDefectCount(nonNegativeInt(row, header, "缺陷数", MANUFACTURING_SHEET, rowIndex, data));
            item.setReworkCount(nonNegativeInt(row, header, "返工数", MANUFACTURING_SHEET, rowIndex, data));

            req(data, MANUFACTURING_SHEET, rowIndex, "零件实例ID", item.getPartInstanceId());
            data.getManufacturingRows().add(item);
        }
    }

    private void parseService(Sheet sheet, PartQualityImportData data)
    {
        if (!ensureSheet(sheet, SERVICE_SHEET, data)) return;
        Map<String, Integer> header = header(sheet, data, SERVICE_SHEET);
        String[] requiredHeaders = {"服役质量ID", "零件实例id", "设备ID", "组件ID", "装机飞机ID", "安装位置", "安装日期", "服役状态", "累计运行小时", "循环次数", "健康评分", "上次检修日期", "下次检修日期"};
        if (!ensureHeaders(header, requiredHeaders, SERVICE_SHEET, data)) return;

        Set<String> ids = new HashSet<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (emptyRow(row)) continue;
            int rowIndex = i + 1;
            String id = text(row, header, "服役质量ID");
            if (blank(id)) continue;
            if (!ids.add(id)) {
                data.getErrors().add(err(SERVICE_SHEET, rowIndex, "服役质量ID", "服役质量ID重复：" + id));
                continue;
            }
            ServiceQuality item = new ServiceQuality();
            item.setServiceQualityId(id);
            item.setPartInstanceId(text(row, header, "零件实例id"));
            item.setEquipmentId(text(row, header, "设备ID"));
            item.setComponentId(text(row, header, "组件ID"));
            item.setInstalledAircraftId(text(row, header, "装机飞机ID"));
            item.setInstallationPosition(text(row, header, "安装位置"));
            item.setInstallationDate(date(row, header, "安装日期", SERVICE_SHEET, rowIndex, data));
            item.setServiceStatus(text(row, header, "服役状态"));
            item.setTotalRunningHours(nonNegativeDecimal(row, header, "累计运行小时", SERVICE_SHEET, rowIndex, data));
            item.setCycleCount(nonNegativeInt(row, header, "循环次数", SERVICE_SHEET, rowIndex, data));
            item.setHealthScore(score(row, header, "健康评分", SERVICE_SHEET, rowIndex, data));
            item.setLastInspectionDate(date(row, header, "上次检修日期", SERVICE_SHEET, rowIndex, data));
            item.setNextInspectionDate(date(row, header, "下次检修日期", SERVICE_SHEET, rowIndex, data));

            req(data, SERVICE_SHEET, rowIndex, "零件实例id", item.getPartInstanceId());
            req(data, SERVICE_SHEET, rowIndex, "设备ID", item.getEquipmentId());
            req(data, SERVICE_SHEET, rowIndex, "组件ID", item.getComponentId());
            data.getServiceRows().add(item);
        }
    }

    private boolean ensureSheet(Sheet sheet, String sheetName, PartQualityImportData data)
    {
        if (sheet != null) return true;
        data.getErrors().add(err(sheetName, null, "Sheet", "缺少Sheet：" + sheetName));
        return false;
    }

    private Map<String, Integer> header(Sheet sheet, PartQualityImportData data, String sheetName)
    {
        Map<String, Integer> map = new HashMap<>();
        Row row = sheet.getRow(0);
        if (row == null) {
            data.getErrors().add(err(sheetName, 1, "表头", "表头不能为空"));
            return map;
        }
        if (row.getFirstCellNum() < 0 || row.getLastCellNum() < 0) {
            data.getErrors().add(err(sheetName, 1, "表头", "表头不能为空"));
            return map;
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            String name = clean(formatter.formatCellValue(row.getCell(i)));
            if (!blank(name)) map.put(name, i);
        }
        return map;
    }

    private boolean ensureHeaders(Map<String, Integer> header, String[] fields, String sheetName, PartQualityImportData data)
    {
        boolean ok = true;
        for (String field : fields) {
            if (!header.containsKey(field)) {
                data.getErrors().add(err(sheetName, 1, field, "缺少表头：" + field));
                ok = false;
            }
        }
        return ok;
    }

    private String text(Row row, Map<String, Integer> header, String field)
    {
        Integer index = header.get(field);
        if (index == null) return null;
        return clean(formatter.formatCellValue(row.getCell(index)));
    }

    private Date dateTime(Row row, Map<String, Integer> header, String field, String sheet, int rowIndex, PartQualityImportData data)
    {
        Cell cell = cell(row, header, field);
        if (emptyCell(cell)) return null;
        try {
            if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue();
            return dateTimeFormat.parse(clean(formatter.formatCellValue(cell)));
        } catch (Exception e) {
            data.getErrors().add(err(sheet, rowIndex, field, field + "格式必须为yyyy-MM-dd HH:mm:ss"));
            return null;
        }
    }

    private Date date(Row row, Map<String, Integer> header, String field, String sheet, int rowIndex, PartQualityImportData data)
    {
        Cell cell = cell(row, header, field);
        if (emptyCell(cell)) return null;
        try {
            if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue();
            return dateFormat.parse(clean(formatter.formatCellValue(cell)));
        } catch (Exception e) {
            data.getErrors().add(err(sheet, rowIndex, field, field + "格式必须为yyyy-MM-dd"));
            return null;
        }
    }

    private Integer nonNegativeInt(Row row, Map<String, Integer> header, String field, String sheet, int rowIndex, PartQualityImportData data)
    {
        String value = text(row, header, field);
        if (blank(value)) return null;
        try {
            BigDecimal decimal = new BigDecimal(value);
            if (decimal.compareTo(BigDecimal.ZERO) < 0 || decimal.stripTrailingZeros().scale() > 0) throw new IllegalArgumentException();
            return decimal.intValueExact();
        } catch (Exception e) {
            data.getErrors().add(err(sheet, rowIndex, field, field + "必须为大于等于0的整数"));
            return null;
        }
    }

    private BigDecimal nonNegativeDecimal(Row row, Map<String, Integer> header, String field, String sheet, int rowIndex, PartQualityImportData data)
    {
        String value = text(row, header, field);
        if (blank(value)) return null;
        try {
            BigDecimal decimal = new BigDecimal(value);
            if (decimal.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException();
            return decimal;
        } catch (Exception e) {
            data.getErrors().add(err(sheet, rowIndex, field, field + "必须大于等于0"));
            return null;
        }
    }

    private BigDecimal score(Row row, Map<String, Integer> header, String field, String sheet, int rowIndex, PartQualityImportData data)
    {
        BigDecimal value = nonNegativeDecimal(row, header, field, sheet, rowIndex, data);
        if (value != null && value.compareTo(new BigDecimal("100")) > 0) {
            data.getErrors().add(err(sheet, rowIndex, field, field + "必须在0到100之间"));
        }
        return value;
    }

    private void req(PartQualityImportData data, String sheet, int rowIndex, String field, String value)
    {
        if (blank(value)) data.getErrors().add(err(sheet, rowIndex, field, field + "不能为空"));
    }

    private boolean emptyRow(Row row)
    {
        if (row == null) return true;
        if (row.getFirstCellNum() < 0 || row.getLastCellNum() < 0) return true;
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            if (!blank(clean(formatter.formatCellValue(row.getCell(i))))) return false;
        }
        return true;
    }

    private Cell cell(Row row, Map<String, Integer> header, String field)
    {
        Integer index = header.get(field);
        return index == null || row == null ? null : row.getCell(index);
    }

    private boolean emptyCell(Cell cell)
    {
        return cell == null || blank(clean(formatter.formatCellValue(cell)));
    }

    private PartQualityImportError err(String sheet, Integer row, String field, String message)
    {
        return new PartQualityImportError(sheet, row, field, message);
    }

    private String clean(String value)
    {
        return value == null ? null : value.trim();
    }

    private boolean blank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
