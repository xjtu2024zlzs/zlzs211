package com.ruoyi.project3.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.domain.ModuleNode;
import com.ruoyi.project3.domain.ModuleNodeView;
import com.ruoyi.project3.domain.PageRows;
import com.ruoyi.project3.mapper.MonitorMapper;
import com.ruoyi.project3.service.MonitorService;
import com.ruoyi.project3.util.HierarchyTemplateGenerator;
import com.ruoyi.project3.util.PartActualManufacturingProcessTemplateGenerator;
import com.ruoyi.project3.util.PartProcessTemplateGenerator;
import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Service
public class MonitorServiceImpl implements MonitorService {

    private static final String TASK_PART_ACTUAL_MANUFACTURING_PROCESS = "PART_ACTUAL_MANUFACTURING_PROCESS";

    @Resource
    private MonitorMapper monitorMapper;

    @Override
    public List<ModuleNode> getTree() {
        // TODO: 当前为全量层级树查询，后续可按机型维度引入缓存，降低高并发查询成本。
        List<Map<String, Object>> rawNodeRows = monitorMapper.sel_all_nodes();
        if (rawNodeRows == null || rawNodeRows.isEmpty()) return Collections.emptyList();

        List<ModuleNode> flatNodes = new ArrayList<>();
        Map<String, ModuleNode> nodesById = new LinkedHashMap<>();
        for (Map<String, Object> rawNode : rawNodeRows) {
            ModuleNode node = cvt_to_mod_node(rawNode);
            node.set_children(new ArrayList<>());
            flatNodes.add(node);
            nodesById.put(node.get_id(), node);
        }

        List<ModuleNode> rootNodes = new ArrayList<>();
        for (ModuleNode node : flatNodes) {
            String parentId = node.get_parent_id();
            if (parentId == null || parentId.trim().isEmpty()) {
                rootNodes.add(node);
                continue;
            }
            ModuleNode parent = nodesById.get(parentId);
            if (parent == null) {
                rootNodes.add(node);
                continue;
            }
            if (parent.get_children() == null) parent.set_children(new ArrayList<>());
            parent.get_children().add(node);
        }
        return rootNodes;
    }

    @Override
    public ModuleNodeView getNodeView(String nodeId) {
        ModuleNodeView view = new ModuleNodeView();
        if (nodeId == null || nodeId.trim().isEmpty()) {
            view.set_cur_node(null);
            view.set_bread_list(Collections.emptyList());
            view.set_children(Collections.emptyList());
            return view;
        }

        Map<String, Object> rawCurrentNode = monitorMapper.sel_node_by_id(nodeId);
        if (rawCurrentNode == null || rawCurrentNode.isEmpty()) {
            view.set_cur_node(null);
            view.set_bread_list(Collections.emptyList());
            view.set_children(Collections.emptyList());
            return view;
        }

        view.set_cur_node(cvt_to_mod_node(rawCurrentNode));
        view.set_bread_list(cvt_to_mod_node_list(monitorMapper.sel_bread_nodes(nodeId)));
        view.set_children(cvt_to_mod_node_list(monitorMapper.sel_child_nodes(nodeId)));
        return view;
    }

    @Override
    public PageRows getPartInstances(String moduleId, String keyword, String material, String status, Integer pageNum, Integer pageSize) {
        PageRows pageRows = new PageRows();
        Integer normalizedPageNum = norm_page_num(pageNum);
        Integer normalizedPageSize = norm_page_size(pageSize);
        List<Map<String, Object>> partRows = monitorMapper.sel_part_list(moduleId, keyword, material, status, normalizedPageNum, normalizedPageSize);
        Long totalCount = monitorMapper.cnt_part_list(moduleId, keyword, material, status);
        pageRows.set_rows(partRows == null ? Collections.emptyList() : partRows);
        pageRows.set_tot(totalCount == null ? 0L : totalCount);
        return pageRows;
    }

    @Override
    public Map<String, Object> getPartQualityTables(String partInstanceId) {
        if (partInstanceId == null || partInstanceId.trim().isEmpty()) return Collections.emptyMap();

        List<Map<String, Object>> designRows = monitorMapper.sel_dsn_rows(partInstanceId);
        List<Map<String, Object>> manufacturingRows = monitorMapper.sel_manu_rows(partInstanceId);
        List<Map<String, Object>> serviceRows = monitorMapper.sel_srv_rows(partInstanceId);

        List<Map<String, Object>> stageTables = new ArrayList<>();
        stageTables.add(bld_qlt_tbl("design_quality", "设计质量信息", designRows));
        stageTables.add(bld_qlt_tbl("manufacturing_quality", "制造质量信息", manufacturingRows));
        stageTables.add(bld_qlt_tbl("service_quality", "服役质量信息", serviceRows));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tables", stageTables);
        return payload;
    }

    @Override
    public Map<String, Object> createModule(String currentNodeId, String moduleName) {
        ParsedNode current = prs_node_id(currentNodeId);
        if (current == null) throw new ServiceException("当前节点不合法");
        if (moduleName == null || moduleName.trim().isEmpty()) throw new ServiceException("模块名称不能为空");

        Map<String, Object> rawCurrentNode = monitorMapper.sel_node_by_id(currentNodeId);
        if (rawCurrentNode == null || rawCurrentNode.isEmpty()) throw new ServiceException("当前节点不存在");
        ParsedNode parent = prs_node_id(get_str(rawCurrentNode, "parent_id"));

        String cleanModuleName = moduleName.trim();
        String newNodeType = current.type;
        String newNodeId;
        switch (current.type) {
            case "aircraft":
                newNodeId = "A" + sh_uuid();
                monitorMapper.ins_aircraft(newNodeId, cleanModuleName);
                break;
            case "subsystem":
                newNodeId = "S" + sh_uuid();
                if (parent == null || !"aircraft".equals(parent.type)) throw new ServiceException("当前分系统父级异常");
                monitorMapper.ins_subsystem(newNodeId, cleanModuleName, parent.raw_id);
                break;
            case "equipment":
                newNodeId = "E" + sh_uuid();
                if (parent == null || !"subsystem".equals(parent.type)) throw new ServiceException("当前设备父级异常");
                monitorMapper.ins_equipment(newNodeId, cleanModuleName, parent.raw_id);
                break;
            case "component":
                newNodeId = "C" + sh_uuid();
                if (parent == null || !"equipment".equals(parent.type)) throw new ServiceException("当前组件父级异常");
                monitorMapper.ins_component(newNodeId, cleanModuleName, parent.raw_id);
                break;
            case "part_template":
                newNodeId = "PT" + sh_uuid();
                if (parent == null || !"component".equals(parent.type)) throw new ServiceException("当前零件模板父级异常");
                monitorMapper.ins_part_tpl(newNodeId, cleanModuleName, parent.raw_id, newNodeId, null, null);
                break;
            default:
                throw new ServiceException("当前节点不支持新增同级模块");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("node_id", newNodeType + ":" + newNodeId);
        payload.put("node_type", newNodeType);
        payload.put("name", cleanModuleName);
        return payload;
    }

    @Override
    public void deleteModule(String nodeId) {
        ParsedNode node = prs_node_id(nodeId);
        if (node == null) throw new ServiceException("节点不合法");

        switch (node.type) {
            case "aircraft":
                ens_zero(monitorMapper.cnt_subs_by_air_id(node.raw_id), "请先删除该飞机下的分系统");
                monitorMapper.del_aircraft(node.raw_id);
                break;
            case "subsystem":
                ens_zero(monitorMapper.cnt_equip_by_sub_id(node.raw_id), "请先删除该分系统下的设备");
                monitorMapper.del_subsystem(node.raw_id);
                break;
            case "equipment":
                ens_zero(monitorMapper.cnt_comp_by_equ_id(node.raw_id), "请先删除该设备下的组件");
                monitorMapper.del_equipment(node.raw_id);
                break;
            case "component":
                ens_zero(monitorMapper.cnt_tpl_by_comp_id(node.raw_id), "请先删除该组件下的零件模板");
                monitorMapper.del_component(node.raw_id);
                break;
            case "part_template":
                ens_zero(monitorMapper.cnt_ins_by_tpl_id(node.raw_id), "该零件模板下存在零件实例，不能删除");
                monitorMapper.del_part_tpl(node.raw_id);
                break;
            default:
                throw new ServiceException("不支持删除该节点");
        }
    }

    @Override
    public void updateModuleName(String nodeId, String moduleName) {
        ParsedNode node = prs_node_id(nodeId);
        if (node == null) throw new ServiceException("节点不合法");
        if (moduleName == null || moduleName.trim().isEmpty()) throw new ServiceException("对象名称不能为空");

        String cleanName = moduleName.trim();
        int rows;
        switch (node.type) {
            case "aircraft":
                rows = monitorMapper.upd_aircraft_name(node.raw_id, cleanName);
                break;
            case "subsystem":
                rows = monitorMapper.upd_subsystem_name(node.raw_id, cleanName);
                break;
            case "equipment":
                rows = monitorMapper.upd_equipment_name(node.raw_id, cleanName);
                break;
            case "component":
                rows = monitorMapper.upd_component_name(node.raw_id, cleanName);
                break;
            case "part_template":
                rows = monitorMapper.upd_part_tpl_name(node.raw_id, cleanName);
                break;
            default:
                throw new ServiceException("不支持修改该对象名称");
        }
        if (rows <= 0) throw new ServiceException("对象不存在或未更新");
    }

    @Override
    public Map<String, Object> createPartInstance(
            String moduleId,
            String partCode,
            String partName,
            String material,
            String specModel,
            String serialNumber,
            String batchNumber,
            String manufacturer,
            String productionDate,
            String status,
            String qualityLevel,
            String keyDegree
    ) {
        ParsedNode parsed = prs_node_id(moduleId);
        if (parsed == null || !"part_template".equals(parsed.type)) {
            throw new ServiceException("仅支持在零件模板节点下新增零件实例");
        }
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new ServiceException("序列号不能为空");
        }
        if (batchNumber == null || batchNumber.trim().isEmpty()) {
            throw new ServiceException("批次号不能为空");
        }

        Map<String, Object> template = monitorMapper.sel_part_tpl_basic(parsed.raw_id);
        if (template == null || template.isEmpty()) {
            throw new ServiceException("当前零件模板不存在");
        }

        String partInstanceId = "PI" + sh_uuid();
        monitorMapper.ins_part_instance(
                partInstanceId,
                parsed.raw_id,
                serialNumber.trim(),
                trm_to_nil(batchNumber),
                trm_to_nil(manufacturer),
                trm_to_nil(productionDate),
                trm_to_nil(status),
                trm_to_nil(qualityLevel),
                trm_to_nil(keyDegree)
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("part_template_id", parsed.raw_id);
        payload.put("part_instance_id", partInstanceId);
        return payload;
    }

    @Override
    public Map<String, Object> createPartTemplate(String moduleId, String partCode, String partName, String material, String specModel) {
        ParsedNode parsed = prs_node_id(moduleId);
        if (parsed == null) throw new ServiceException("节点不合法");
        if (partCode == null || partCode.trim().isEmpty()) throw new ServiceException("编号不能为空");
        if (partName == null || partName.trim().isEmpty()) throw new ServiceException("零件名称不能为空");

        String componentId;
        if ("component".equals(parsed.type)) {
            componentId = parsed.raw_id;
        } else if ("part_template".equals(parsed.type)) {
            Map<String, Object> template = monitorMapper.sel_part_tpl_basic(parsed.raw_id);
            if (template == null || template.isEmpty()) throw new ServiceException("当前零件模板不存在");
            componentId = get_str(template, "component_id");
        } else {
            throw new ServiceException("请在组件或零件模板节点下新增零件类型");
        }

        String partTemplateId = "PT" + sh_uuid();
        monitorMapper.ins_part_tpl(
                partTemplateId,
                partName.trim(),
                componentId,
                partCode.trim(),
                trm_to_nil(material),
                trm_to_nil(specModel)
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("part_template_id", partTemplateId);
        return payload;
    }

    @Override
    public Map<String, Object> getPartTemplate(String partTemplateId) {
        if (partTemplateId == null || partTemplateId.trim().isEmpty()) {
            throw new ServiceException("零件模板ID不能为空");
        }
        Map<String, Object> template = monitorMapper.sel_part_tpl_basic(partTemplateId.trim());
        if (template == null || template.isEmpty()) {
            throw new ServiceException("零件模板不存在");
        }
        return template;
    }

    @Override
    public void updatePartTemplate(String partTemplateId, String partNumber, String partName, String material, String specModel) {
        if (partTemplateId == null || partTemplateId.trim().isEmpty()) {
            throw new ServiceException("零件模板ID不能为空");
        }
        if (partNumber == null || partNumber.trim().isEmpty()) {
            throw new ServiceException("零件编号不能为空");
        }
        if (partName == null || partName.trim().isEmpty()) {
            throw new ServiceException("零件名称不能为空");
        }
        int rows = monitorMapper.upd_part_tpl_basic(
                partTemplateId.trim(),
                partNumber.trim(),
                partName.trim(),
                trm_to_nil(material),
                trm_to_nil(specModel)
        );
        if (rows <= 0) {
            throw new ServiceException("零件模板不存在或未更新");
        }
    }

    @Override
    public void deletePartInstance(String partInstanceId) {
        if (partInstanceId == null || partInstanceId.trim().isEmpty()) {
            throw new ServiceException("零件实例ID不能为空");
        }
        monitorMapper.del_part_ins(partInstanceId);


    }

    @Override
    public void updatePartInstance(
            String partInstanceId,
            String serialNumber,
            String batchNumber,
            String manufacturer,
            String productionDate,
            String status,
            String qualityLevel,
            String keyDegree
    ) {
        if (partInstanceId == null || partInstanceId.trim().isEmpty()) {
            throw new ServiceException("零件实例ID不能为空");
        }
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new ServiceException("序列号不能为空");
        }
        if (batchNumber == null || batchNumber.trim().isEmpty()) {
            throw new ServiceException("批次号不能为空");
        }
        int rows = monitorMapper.upd_part_instance(
                partInstanceId.trim(),
                serialNumber.trim(),
                batchNumber.trim(),
                trm_to_nil(manufacturer),
                trm_to_nil(productionDate),
                trm_to_nil(status),
                trm_to_nil(qualityLevel),
                trm_to_nil(keyDegree)
        );
        if (rows <= 0) {
            throw new ServiceException("零件实例不存在或未更新");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importProcessText(String componentId, MultipartFile file) {
        return importProcessText(componentId, file, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importProcessText(String componentId, MultipartFile file, String taskType) {
        if (TASK_PART_ACTUAL_MANUFACTURING_PROCESS.equals(taskType)) {
            return import_actual_process_text(file);
        }
        if (file == null || file.isEmpty()) {
            throw new ServiceException("导入文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx") || fileName.toLowerCase(Locale.ROOT).endsWith(".xls"))) {
            throw new ServiceException("请导入 Excel 文件");
        }

        ProcessImportData processData = parse_process_template_workbook(file);
        if (processData != null) {
            int routeCount = upsert_rows(processData.routeRows, monitorMapper::upsert_process_route);
            int processCount = upsert_rows(processData.processRows, monitorMapper::upsert_process_def);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("component_id", "");
            payload.put("part_count", processData.partIds.size());
            payload.put("route_count", routeCount);
            payload.put("process_count", processCount);
            return payload;
        }

        String component = raw_component_id(componentId);
        if (component == null) {
            throw new ServiceException("旧版零件工序导入格式需要选择组件作为导入对象，请使用零件标准制作过程导入模板或选择组件后再导入");
        }
        Map<String, Object> componentRow = monitorMapper.sel_component_basic(component);
        if (componentRow == null || componentRow.isEmpty()) {
            throw new ServiceException("组件不存在，无法导入");
        }

        List<ImportedPart> parts = parse_process_workbook(file);
        if (parts.isEmpty()) {
            throw new ServiceException("Excel 中没有可导入的零件工序数据");
        }

        int partCount = 0;
        int processCount = 0;
        int instanceCount = 0;
        for (ImportedPart part : parts) {
            Map<String, Object> existing = monitorMapper.sel_part_tpl_by_comp_num(component, part.partCode);
            String partTemplateId;
            if (existing == null || existing.isEmpty()) {
                partTemplateId = "PT" + sh_uuid();
                monitorMapper.ins_part_tpl(partTemplateId, part.partName, component, part.partCode, null, null);
            } else {
                partTemplateId = get_str(existing, "part_template_id");
                monitorMapper.upd_part_tpl_basic(partTemplateId, part.partCode, part.partName, get_str(existing, "material"), get_str(existing, "specification"));
            }

            Long instanceTotal = monitorMapper.cnt_ins_by_tpl_id(partTemplateId);
            if (instanceTotal == null || instanceTotal == 0) {
                monitorMapper.ins_part_instance(
                        "PI" + sh_uuid(),
                        partTemplateId,
                        part.partCode,
                        "TEXT_IMPORT",
                        "文本导入",
                        null,
                        "在库",
                        null,
                        null
                );
                instanceCount++;
            }

            String routeId = stable_id("PR", partTemplateId);
            monitorMapper.ins_process_route(routeId, partTemplateId, part.partName + "工艺路线");
            for (ImportedProcess process : part.processes.values()) {
                String processKey = process.processCode == null ? String.valueOf(process.processNumber) : process.processCode;
                monitorMapper.ins_process_def(
                        stable_id("PD", partTemplateId + "_" + processKey),
                        routeId,
                        process.processNumber,
                        process.processName,
                        "文本导入"
                );
                processCount++;
            }
            partCount++;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("component_id", component);
        payload.put("part_count", partCount);
        payload.put("instance_count", instanceCount);
        payload.put("process_count", processCount);
        return payload;
    }

    @Override
    public void writeProcessTemplate(OutputStream outputStream) throws IOException {
        writeProcessTemplate(outputStream, null);
    }

    @Override
    public void writeProcessTemplate(OutputStream outputStream, String taskType) throws IOException {
        if (TASK_PART_ACTUAL_MANUFACTURING_PROCESS.equals(taskType)) {
            new PartActualManufacturingProcessTemplateGenerator().write(
                    outputStream,
                    monitorMapper.sel_all_part_template_ids(),
                    monitorMapper.sel_all_process_route_ids(),
                    monitorMapper.sel_all_process_def_ids()
            );
            return;
        }
        new PartProcessTemplateGenerator().write(outputStream, monitorMapper.sel_all_part_tpl());
    }

    private Map<String, Object> import_actual_process_text(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("导入文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new ServiceException("请选择 .xlsx Excel 文件");
        }

        ActualProcessImportData data = parse_actual_process_workbook(file);
        validate_actual_process_data(data);

        int partInstanceCount = upsert_rows(data.partInstanceRows, monitorMapper::upsert_part_instance);
        int workOrderCount = upsert_rows(data.workOrderRows, monitorMapper::upsert_work_order);
        int processExecutionCount = upsert_rows(data.processExecutionRows, monitorMapper::upsert_process_execution);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("part_instance_count", partInstanceCount);
        payload.put("work_order_count", workOrderCount);
        payload.put("process_execution_count", processExecutionCount);
        return payload;
    }

    private ActualProcessImportData parse_actual_process_workbook(MultipartFile file) {
        ActualProcessImportData data = new ActualProcessImportData();
        DataFormatter formatter = new DataFormatter();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            data.partInstanceRows.addAll(parse_actual_sheet(
                    workbook, formatter, "零件实例",
                    new String[]{"part_instance_id", "part_template_id", "serial_number"},
                    new String[]{"part_instance_id", "part_template_id", "serial_number", "batch_number", "manufacturer", "production_date", "current_status", "quality_level", "key_degree", "image_url"},
                    actual_part_instance_labels()));
            data.workOrderRows.addAll(parse_actual_sheet(
                    workbook, formatter, "生产工单",
                    new String[]{"work_order_id", "part_instance_id", "route_id"},
                    new String[]{"work_order_id", "part_instance_id", "route_id", "manufacturing_quality_id", "production_line", "start_time", "end_time", "operator", "status"},
                    actual_work_order_labels()));
            data.processExecutionRows.addAll(parse_actual_sheet(
                    workbook, formatter, "工序执行记录",
                    new String[]{"process_exec_id", "work_order_id", "process_def_id"},
                    new String[]{"process_exec_id", "work_order_id", "process_def_id", "device_id", "start_time", "end_time", "operator", "status", "remark"},
                    actual_process_execution_labels()));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("解析零件实际制作过程Excel失败：" + e.getMessage());
        }
        return data;
    }

    private List<Map<String, Object>> parse_actual_sheet(Workbook workbook, DataFormatter formatter, String sheetName,
                                                         String[] requiredFields, String[] fields,
                                                         Map<String, String[]> fieldLabels) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) throw new ServiceException("缺少Sheet：" + sheetName);
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) throw new ServiceException("Sheet " + sheetName + " 缺少中文表头");

        Map<String, Integer> header = new LinkedHashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String label = normalize_actual_header(formatter.formatCellValue(headerRow.getCell(i)));
            if (label != null) header.put(label, i);
        }

        Map<String, Integer> columns = new LinkedHashMap<>();
        for (String field : fields) {
            for (String label : fieldLabels.get(field)) {
                Integer column = header.get(normalize_actual_header(label));
                if (column != null) {
                    columns.put(field, column);
                    break;
                }
            }
        }
        for (String field : fields) {
            if (!columns.containsKey(field)) {
                throw new ServiceException("Sheet " + sheetName + " 缺少字段列：" + actual_field_label(fieldLabels, field));
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (actual_row_empty(row, formatter, columns.values())) continue;
            int rowNum = i + 1;
            Map<String, Object> values = new LinkedHashMap<>();
            for (String field : fields) {
                values.put(field, trm_to_nil(formatter.formatCellValue(row.getCell(columns.get(field)))));
            }
            values.put("_row_num", rowNum);
            for (String field : requiredFields) {
                if (values.get(field) == null) {
                    throw new ServiceException("Sheet " + sheetName + " 第" + rowNum + "行字段 " + actual_field_label(fieldLabels, field) + " 不能为空");
                }
            }
            validate_actual_date(values, fieldLabels, sheetName, rowNum, "production_date", "yyyy-MM-dd");
            validate_actual_date(values, fieldLabels, sheetName, rowNum, "start_time", "yyyy-MM-dd HH:mm:ss");
            validate_actual_date(values, fieldLabels, sheetName, rowNum, "end_time", "yyyy-MM-dd HH:mm:ss");
            rows.add(values);
        }
        return rows;
    }

    private boolean actual_row_empty(Row row, DataFormatter formatter, Collection<Integer> columns) {
        if (row == null) return true;
        for (Integer column : columns) {
            if (trm_to_nil(formatter.formatCellValue(row.getCell(column))) != null) return false;
        }
        return true;
    }

    private void validate_actual_date(Map<String, Object> values, Map<String, String[]> fieldLabels, String sheetName, int rowNum, String field, String pattern) {
        Object value = values.get(field);
        if (value == null) return;
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            format.setLenient(false);
            format.parse(String.valueOf(value));
        } catch (Exception e) {
            throw new ServiceException("Sheet " + sheetName + " 第" + rowNum + "行字段 " + actual_field_label(fieldLabels, field) + " 格式应为 " + pattern);
        }
    }

    private Map<String, String[]> actual_part_instance_labels() {
        Map<String, String[]> labels = new LinkedHashMap<>();
        labels.put("part_instance_id", new String[]{"零件实例ID", "part_instance_id"});
        labels.put("part_template_id", new String[]{"零件模板ID", "part_template_id"});
        labels.put("serial_number", new String[]{"零件序列号", "serial_number"});
        labels.put("batch_number", new String[]{"批次号", "batch_number"});
        labels.put("manufacturer", new String[]{"制造商", "manufacturer"});
        labels.put("production_date", new String[]{"生产日期(yyyy-MM-dd)", "生产日期", "production_date"});
        labels.put("current_status", new String[]{"当前状态", "current_status"});
        labels.put("quality_level", new String[]{"质量等级", "quality_level"});
        labels.put("key_degree", new String[]{"关键度", "key_degree"});
        labels.put("image_url", new String[]{"零件图片地址", "image_url"});
        return labels;
    }

    private Map<String, String[]> actual_work_order_labels() {
        Map<String, String[]> labels = new LinkedHashMap<>();
        labels.put("work_order_id", new String[]{"生产工单ID", "work_order_id"});
        labels.put("part_instance_id", new String[]{"零件实例ID", "part_instance_id"});
        labels.put("route_id", new String[]{"工艺路线ID", "route_id"});
        labels.put("manufacturing_quality_id", new String[]{"制造质量记录ID", "manufacturing_quality_id"});
        labels.put("production_line", new String[]{"生产线", "production_line"});
        labels.put("start_time", new String[]{"工单开始时间(yyyy-MM-dd HH:mm:ss)", "工单开始时间", "start_time"});
        labels.put("end_time", new String[]{"工单结束时间(yyyy-MM-dd HH:mm:ss)", "工单结束时间", "end_time"});
        labels.put("operator", new String[]{"操作人员", "operator"});
        labels.put("status", new String[]{"工单状态", "status"});
        return labels;
    }

    private Map<String, String[]> actual_process_execution_labels() {
        Map<String, String[]> labels = new LinkedHashMap<>();
        labels.put("process_exec_id", new String[]{"工序执行记录ID", "process_exec_id"});
        labels.put("work_order_id", new String[]{"生产工单ID", "work_order_id"});
        labels.put("process_def_id", new String[]{"详细工序ID", "process_def_id"});
        labels.put("device_id", new String[]{"制造设备ID", "device_id"});
        labels.put("start_time", new String[]{"工序开始时间(yyyy-MM-dd HH:mm:ss)", "工序开始时间", "start_time"});
        labels.put("end_time", new String[]{"工序结束时间(yyyy-MM-dd HH:mm:ss)", "工序结束时间", "end_time"});
        labels.put("operator", new String[]{"操作人员", "operator"});
        labels.put("status", new String[]{"工序执行状态", "status"});
        labels.put("remark", new String[]{"备注", "remark"});
        return labels;
    }

    private String normalize_actual_header(String value) {
        String text = trm_to_nil(value);
        if (text == null) return null;
        return text.replace("*", "").replace("：", ":").trim();
    }

    private String actual_field_label(Map<String, String[]> fieldLabels, String field) {
        String[] labels = fieldLabels.get(field);
        return labels == null || labels.length == 0 ? field : labels[0];
    }

    private int actual_row_num(Map<String, Object> row, int defaultRowNum) {
        Object rowNum = row.get("_row_num");
        return rowNum instanceof Integer ? (Integer) rowNum : defaultRowNum;
    }

    private void validate_actual_process_data(ActualProcessImportData data) {
        if (data.partInstanceRows.isEmpty() && data.workOrderRows.isEmpty() && data.processExecutionRows.isEmpty()) {
            throw new ServiceException("Excel 中没有可导入的零件实际制作过程数据");
        }

        Map<String, String> excelPartTemplates = new LinkedHashMap<>();
        Map<String, String> excelWorkOrderRoutes = new LinkedHashMap<>();
        Set<String> partInstanceIds = new LinkedHashSet<>();
        Set<String> workOrderIds = new LinkedHashSet<>();
        Set<String> processExecIds = new LinkedHashSet<>();

        for (int i = 0; i < data.partInstanceRows.size(); i++) {
            Map<String, Object> row = data.partInstanceRows.get(i);
            int rowNum = actual_row_num(row, i + 2);
            String partInstanceId = get_str(row, "part_instance_id");
            String partTemplateId = get_str(row, "part_template_id");
            if (!partInstanceIds.add(partInstanceId)) {
                throw new ServiceException("Sheet 零件实例 第" + rowNum + "行字段 零件实例ID 重复");
            }
            if (monitorMapper.count_part_tpl_by_id(partTemplateId) == 0) {
                throw new ServiceException("Sheet 零件实例 第" + rowNum + "行字段 零件模板ID 不存在于 part_templates");
            }
            excelPartTemplates.put(partInstanceId, partTemplateId);
        }

        for (int i = 0; i < data.workOrderRows.size(); i++) {
            Map<String, Object> row = data.workOrderRows.get(i);
            int rowNum = actual_row_num(row, i + 2);
            String workOrderId = get_str(row, "work_order_id");
            String partInstanceId = get_str(row, "part_instance_id");
            String routeId = get_str(row, "route_id");
            String manufacturingQualityId = get_str(row, "manufacturing_quality_id");
            if (!workOrderIds.add(workOrderId)) {
                throw new ServiceException("Sheet 生产工单 第" + rowNum + "行字段 生产工单ID 重复");
            }
            if (manufacturingQualityId != null && monitorMapper.count_manufacturing_quality_by_id(manufacturingQualityId) == 0) {
                throw new ServiceException("Sheet 生产工单 第" + rowNum + "行字段 制造质量记录ID 不存在于 manufacturing_quality");
            }
            String partTemplateId = excelPartTemplates.get(partInstanceId);
            if (partTemplateId == null) {
                Map<String, Object> dbPart = monitorMapper.sel_part_instance_ref(partInstanceId);
                if (dbPart == null || dbPart.isEmpty()) {
                    throw new ServiceException("Sheet 生产工单 第" + rowNum + "行字段 零件实例ID 不存在于本次Excel或数据库 part_instances");
                }
                partTemplateId = get_str(dbPart, "part_template_id");
            }
            Map<String, Object> route = monitorMapper.sel_process_route_ref(routeId);
            if (route == null || route.isEmpty()) {
                throw new ServiceException("Sheet 生产工单 第" + rowNum + "行字段 工艺路线ID 不存在于 process_routes");
            }
            if (!Objects.equals(partTemplateId, get_str(route, "part_template_id"))) {
                throw new ServiceException("Sheet 生产工单 第" + rowNum + "行错误：工单使用了不属于该零件模板的工艺路线");
            }
            excelWorkOrderRoutes.put(workOrderId, routeId);
        }

        for (int i = 0; i < data.processExecutionRows.size(); i++) {
            Map<String, Object> row = data.processExecutionRows.get(i);
            int rowNum = actual_row_num(row, i + 2);
            String processExecId = get_str(row, "process_exec_id");
            String workOrderId = get_str(row, "work_order_id");
            String processDefId = get_str(row, "process_def_id");
            String deviceId = get_str(row, "device_id");
            if (!processExecIds.add(processExecId)) {
                throw new ServiceException("Sheet 工序执行记录 第" + rowNum + "行字段 工序执行记录ID 重复");
            }
            if (deviceId != null && monitorMapper.count_manufacturing_device_by_id(deviceId) == 0) {
                throw new ServiceException("Sheet 工序执行记录 第" + rowNum + "行字段 制造设备ID 不存在于 manufacturing_devices");
            }
            String routeId = excelWorkOrderRoutes.get(workOrderId);
            if (routeId == null) {
                Map<String, Object> dbWorkOrder = monitorMapper.sel_work_order_ref(workOrderId);
                if (dbWorkOrder == null || dbWorkOrder.isEmpty()) {
                    throw new ServiceException("Sheet 工序执行记录 第" + rowNum + "行字段 生产工单ID 不存在于本次Excel或数据库 work_orders");
                }
                routeId = get_str(dbWorkOrder, "route_id");
            }
            Map<String, Object> processDef = monitorMapper.sel_process_def_ref(processDefId);
            if (processDef == null || processDef.isEmpty()) {
                throw new ServiceException("Sheet 工序执行记录 第" + rowNum + "行字段 详细工序ID 不存在于 process_definitions");
            }
            if (!Objects.equals(routeId, get_str(processDef, "route_id"))) {
                throw new ServiceException("Sheet 工序执行记录 第" + rowNum + "行错误：工单执行了不属于该工艺路线的工序");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importHierarchy(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("导入文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new ServiceException("请选择 .xlsx Excel 文件");
        }

        Map<String, List<Map<String, Object>>> rows = parse_hierarchy_workbook(file);
        int aircraftCount = upsert_rows(rows.get("aircraft"), monitorMapper::upsert_aircraft);
        int subsystemCount = upsert_rows(rows.get("subsystems"), monitorMapper::upsert_subsystem);
        int equipmentCount = upsert_rows(rows.get("equipments"), monitorMapper::upsert_equipment);
        int componentCount = upsert_rows(rows.get("components"), monitorMapper::upsert_component);
        int partTemplateCount = upsert_rows(rows.get("part_templates"), monitorMapper::upsert_part_template);
        int partInstanceCount = upsert_rows(rows.get("part_instances"), monitorMapper::upsert_part_instance);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("aircraft_count", aircraftCount);
        payload.put("subsystem_count", subsystemCount);
        payload.put("equipment_count", equipmentCount);
        payload.put("component_count", componentCount);
        payload.put("part_template_count", partTemplateCount);
        payload.put("part_instance_count", partInstanceCount);
        payload.put("total_count", aircraftCount + subsystemCount + equipmentCount + componentCount + partTemplateCount + partInstanceCount);
        return payload;
    }

    @Override
    public void writeHierarchyTemplate(OutputStream outputStream) throws IOException {
        new HierarchyTemplateGenerator().write(outputStream);
    }

    private Map<String, List<Map<String, Object>>> parse_hierarchy_workbook(MultipartFile file) {
        Map<String, String[]> requiredColumns = new LinkedHashMap<>();
        requiredColumns.put("aircraft", new String[]{"aircraft_id", "aircraft_name"});
        requiredColumns.put("subsystems", new String[]{"subsystem_id", "subsystem_name", "aircraft_id"});
        requiredColumns.put("equipments", new String[]{"equipment_id", "equipment_name", "subsystem_id"});
        requiredColumns.put("components", new String[]{"component_id", "component_name", "equipment_id"});
        requiredColumns.put("part_templates", new String[]{"part_template_id", "part_name", "component_id"});
        requiredColumns.put("part_instances", new String[]{"part_instance_id", "part_template_id", "serial_number"});

        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        DataFormatter formatter = new DataFormatter();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            for (Map.Entry<String, String[]> entry : requiredColumns.entrySet()) {
                String sheetName = entry.getKey();
                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new ServiceException("缺少Sheet：" + sheetName);
                }
                result.put(sheetName, parse_hierarchy_sheet(sheet, formatter, entry.getValue()));
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("解析层级对象Excel失败：" + e.getMessage());
        }
        return result;
    }

    private List<Map<String, Object>> parse_hierarchy_sheet(Sheet sheet, DataFormatter formatter, String[] requiredColumns) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new ServiceException("Sheet " + sheet.getSheetName() + " 缺少表头");
        }
        Map<String, Integer> headerMap = new LinkedHashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String header = trm_to_nil(formatter.formatCellValue(headerRow.getCell(i)));
            if (header != null) headerMap.put(hierarchy_column_key(header), i);
        }
        for (String column : requiredColumns) {
            if (!headerMap.containsKey(column)) {
                throw new ServiceException("Sheet " + sheet.getSheetName() + " 缺少必填列：" + hierarchy_column_label(column));
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || hierarchy_row_empty(row, formatter, headerMap.values())) continue;
            Map<String, Object> values = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> header : headerMap.entrySet()) {
                values.put(header.getKey(), trm_to_nil(formatter.formatCellValue(row.getCell(header.getValue()))));
            }
            for (String column : requiredColumns) {
                if (values.get(column) == null) {
                    throw new ServiceException("Sheet " + sheet.getSheetName() + " 第" + (i + 1) + "行缺少必填字段：" + hierarchy_column_label(column));
                }
            }
            if (values.containsKey("production_date") && values.get("production_date") != null) {
                values.put("production_date", parse_date_text(String.valueOf(values.get("production_date")), sheet.getSheetName(), i + 1, "production_date"));
            }
            rows.add(values);
        }
        return rows;
    }
    private boolean hierarchy_row_empty(Row row, DataFormatter formatter, Collection<Integer> indexes) {
        for (Integer index : indexes) {
            if (trm_to_nil(formatter.formatCellValue(row.getCell(index))) != null) return false;
        }
        return true;
    }

    private String hierarchy_column_key(String header) {
        if (header == null) return null;
        return HIERARCHY_COLUMN_MAP.getOrDefault(header, header);
    }

    private String hierarchy_column_label(String column) {
        if (column == null) return "";
        for (Map.Entry<String, String> entry : HIERARCHY_COLUMN_MAP.entrySet()) {
            if (column.equals(entry.getValue())) return entry.getKey();
        }
        return column;
    }

    private String parse_date_text(String value, String sheetName, int rowNum, String fieldName) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setLenient(false);
            format.parse(value);
            return value;
        } catch (Exception e) {
            throw new ServiceException("Sheet " + sheetName + " 第" + rowNum + "行" + fieldName + "格式应为 yyyy-MM-dd");
        }
    }

    private int upsert_rows(List<Map<String, Object>> rows, Function<Map<String, Object>, Integer> mapper) {
        if (rows == null || rows.isEmpty()) return 0;
        int count = 0;
        for (Map<String, Object> row : rows) {
            mapper.apply(row);
            count++;
        }
        return count;
    }



    private Map<String, Object> bld_qlt_tbl(String table_name, String table_label, List<Map<String, Object>> row_list) {
        // FIXME: 这里按请求实时查 information_schema，后续应缓存列定义，减少元数据查询开销。
        List<Map<String, Object>> raw_column_list = monitorMapper.sel_table_cols(table_name);
        List<Map<String, Object>> column_list = new ArrayList<>();
        for (Map<String, Object> raw_column : raw_column_list) {
            String column_name = get_str(raw_column, "column_name");
            if (column_name == null || column_name.trim().isEmpty()) continue;
            Map<String, Object> column = new LinkedHashMap<>();
            column.put("prop", column_name);
            column.put("label", map_col_lbl(column_name));
            column_list.add(column);
        }
        Map<String, Object> table = new LinkedHashMap<>();
        table.put("table_name", table_name);
        table.put("table_label", table_label);
        table.put("columns", column_list);
        table.put("rows", row_list == null ? Collections.emptyList() : row_list);
        return table;

    }


    private String trm_to_nil(String value) {
        if (value == null) return null;
        String s = value.trim();
        return s.isEmpty() ? null : s;
    }

    private ProcessImportData parse_process_template_workbook(MultipartFile file) {
        DataFormatter formatter = new DataFormatter();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet partSheet = workbook.getSheet("零件模板");
            Sheet routeSheet = workbook.getSheet("工序路线");
            Sheet processSheet = workbook.getSheet("详细工序");
            if (partSheet == null && routeSheet == null && processSheet == null) {
                return null;
            }
            if (partSheet == null) throw new ServiceException("缺少Sheet：零件模板");
            if (routeSheet == null) throw new ServiceException("缺少Sheet：工序路线");
            if (processSheet == null) throw new ServiceException("缺少Sheet：详细工序");

            ProcessImportData data = new ProcessImportData();
            data.partIds.addAll(parse_process_part_ids(partSheet, formatter));
            data.routeRows.addAll(parse_process_routes(routeSheet, formatter, data.partIds));
            Set<String> routeIds = new LinkedHashSet<>();
            for (Map<String, Object> row : data.routeRows) {
                String routeId = trm_to_nil(String.valueOf(row.get("route_id")));
                if (routeId != null) routeIds.add(routeId);
            }
            data.processRows.addAll(parse_process_defs(processSheet, formatter, routeIds));
            if (data.routeRows.isEmpty() && data.processRows.isEmpty()) {
                throw new ServiceException("Excel 中没有可导入的工序路线或详细工序数据");
            }
            return data;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("解析零件标准制作过程Excel失败：" + e.getMessage());
        }
    }

    private Set<String> parse_process_part_ids(Sheet sheet, DataFormatter formatter) {
        Map<String, Integer> header = process_header(sheet, formatter, new String[]{"零件id"});
        Set<String> partIds = new LinkedHashSet<>();
        Integer partIdCol = header.get("零件id");
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String partId = trm_to_nil(formatter.formatCellValue(row.getCell(partIdCol)));
            if (partId != null) partIds.add(partId);
        }
        if (partIds.isEmpty()) {
            throw new ServiceException("Sheet 零件模板 缺少可用于关联校验的零件id数据");
        }
        return partIds;
    }

    private List<Map<String, Object>> parse_process_routes(Sheet sheet, DataFormatter formatter, Set<String> partIds) {
        String[] headers = {"工序路线id", "零件id", "工序路线名称", "版本", "生效日期", "是否启用"};
        Map<String, Integer> header = process_header(sheet, formatter, headers);
        List<Map<String, Object>> rows = new ArrayList<>();
        Set<String> routeIds = new LinkedHashSet<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (process_row_empty(row, formatter, header.values())) continue;
            int rowNum = i + 1;
            String routeId = process_cell(row, formatter, header, "工序路线id");
            String partId = process_cell(row, formatter, header, "零件id");
            String routeName = process_cell(row, formatter, header, "工序路线名称");
            require_process_value(routeId, "工序路线", rowNum, "工序路线id");
            require_process_value(partId, "工序路线", rowNum, "零件id");
            require_process_value(routeName, "工序路线", rowNum, "工序路线名称");
            if (!partIds.contains(partId)) {
                throw new ServiceException("Sheet 工序路线 第" + rowNum + "行 零件id 错误：零件id不在零件模板工作表中");
            }
            if (!routeIds.add(routeId)) {
                throw new ServiceException("Sheet 工序路线 第" + rowNum + "行 工序路线id 错误：工序路线id重复");
            }

            Map<String, Object> values = new LinkedHashMap<>();
            values.put("route_id", routeId);
            values.put("part_template_id", partId);
            values.put("route_name", routeName);
            values.put("version", process_cell(row, formatter, header, "版本"));
            values.put("effective_date", process_cell(row, formatter, header, "生效日期"));
            values.put("is_active", process_default(process_cell(row, formatter, header, "是否启用"), "1"));
            rows.add(values);
        }
        return rows;
    }

    private List<Map<String, Object>> parse_process_defs(Sheet sheet, DataFormatter formatter, Set<String> routeIds) {
        String[] headers = {"工序id", "工序路线id", "工序序号", "工序名称", "设备类型", "标准工时", "是否关键工序", "是否高风险"};
        Map<String, Integer> header = process_header(sheet, formatter, headers);
        List<Map<String, Object>> rows = new ArrayList<>();
        Set<String> processIds = new LinkedHashSet<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (process_row_empty(row, formatter, header.values())) continue;
            int rowNum = i + 1;
            String processId = process_cell(row, formatter, header, "工序id");
            String routeId = process_cell(row, formatter, header, "工序路线id");
            String processNumber = process_cell(row, formatter, header, "工序序号");
            String processName = process_cell(row, formatter, header, "工序名称");
            String equipmentType = process_cell(row, formatter, header, "设备类型");
            require_process_value(processId, "详细工序", rowNum, "工序id");
            require_process_value(routeId, "详细工序", rowNum, "工序路线id");
            require_process_value(processNumber, "详细工序", rowNum, "工序序号");
            require_process_value(processName, "详细工序", rowNum, "工序名称");
            require_process_value(equipmentType, "详细工序", rowNum, "设备类型");
            if (!routeIds.contains(routeId)) {
                throw new ServiceException("Sheet 详细工序 第" + rowNum + "行 工序路线id 错误：工序路线id不在工序路线工作表中");
            }
            if (!processIds.add(processId)) {
                throw new ServiceException("Sheet 详细工序 第" + rowNum + "行 工序id 错误：工序id重复");
            }

            Map<String, Object> values = new LinkedHashMap<>();
            values.put("process_def_id", processId);
            values.put("route_id", routeId);
            values.put("process_number", parse_process_int(processNumber, "详细工序", rowNum, "工序序号"));
            values.put("process_name", processName);
            values.put("equipment_type", equipmentType);
            values.put("standard_duration", process_cell(row, formatter, header, "标准工时"));
            values.put("is_key_process", process_cell(row, formatter, header, "是否关键工序"));
            values.put("is_high_risk", process_cell(row, formatter, header, "是否高风险"));
            rows.add(values);
        }
        return rows;
    }

    private Map<String, Integer> process_header(Sheet sheet, DataFormatter formatter, String[] requiredHeaders) {
        Row row = sheet.getRow(0);
        if (row == null) throw new ServiceException("Sheet " + sheet.getSheetName() + " 缺少表头");
        Map<String, Integer> header = new LinkedHashMap<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            String value = trm_to_nil(formatter.formatCellValue(row.getCell(i)));
            if (value != null) header.put(value, i);
        }
        for (String required : requiredHeaders) {
            if (!header.containsKey(required)) {
                throw new ServiceException("Sheet " + sheet.getSheetName() + " 缺少表头：" + required);
            }
        }
        return header;
    }

    private boolean process_row_empty(Row row, DataFormatter formatter, Collection<Integer> columns) {
        if (row == null) return true;
        for (Integer column : columns) {
            if (trm_to_nil(formatter.formatCellValue(row.getCell(column))) != null) return false;
        }
        return true;
    }

    private String process_cell(Row row, DataFormatter formatter, Map<String, Integer> header, String column) {
        Integer index = header.get(column);
        return index == null ? null : trm_to_nil(formatter.formatCellValue(row.getCell(index)));
    }

    private void require_process_value(String value, String sheetName, int rowNum, String column) {
        if (value == null) {
            throw new ServiceException("Sheet " + sheetName + " 第" + rowNum + "行 " + column + " 错误：" + column + "不能为空");
        }
    }

    private Object process_default(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Integer parse_process_int(String value, String sheetName, int rowNum, String column) {
        try {
            return new BigDecimal(value.trim()).intValueExact();
        } catch (Exception e) {
            throw new ServiceException("Sheet " + sheetName + " 第" + rowNum + "行 " + column + " 错误：" + column + "必须是整数");
        }
    }

    private List<ImportedPart> parse_process_workbook(MultipartFile file) {
        Map<String, ImportedPart> partMap = new LinkedHashMap<>();
        DataFormatter formatter = new DataFormatter();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return Collections.emptyList();
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String partCode = trm_to_nil(formatter.formatCellValue(row.getCell(0)));
                String partName = trm_to_nil(formatter.formatCellValue(row.getCell(1)));
                String processNoText = trm_to_nil(formatter.formatCellValue(row.getCell(2)));
                String processCode = trm_to_nil(formatter.formatCellValue(row.getCell(3)));
                String processName = trm_to_nil(formatter.formatCellValue(row.getCell(4)));
                if (partCode == null && partName == null && processNoText == null && processCode == null && processName == null) {
                    continue;
                }
                if (partCode == null || partName == null || processNoText == null || processName == null) {
                    throw new ServiceException("Excel第" + (i + 1) + "行缺少必填字段");
                }
                ImportedPart part = partMap.computeIfAbsent(partCode, key -> new ImportedPart(partCode, partName));
                part.partName = partName;
                ImportedProcess process = new ImportedProcess(parse_int(processNoText, i + 1), processCode, processName);
                String processKey = processCode == null ? String.valueOf(process.processNumber) : processCode;
                part.processes.put(processKey, process);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("解析Excel文件失败：" + e.getMessage());
        }
        return new ArrayList<>(partMap.values());
    }

    private Integer parse_int(String value, int rowNum) {
        try {
            return new BigDecimal(value.trim()).intValue();
        } catch (Exception e) {
            throw new ServiceException("Excel第" + rowNum + "行工序序号不是有效数字");
        }
    }

    private String raw_component_id(String componentId) {
        if (componentId == null || componentId.trim().isEmpty()) return null;
        String value = componentId.trim();
        ParsedNode node = prs_node_id(value);
        if (node == null) return value;
        return "component".equals(node.type) ? node.raw_id : null;
    }

    private String stable_id(String prefix, String value) {
        String source = value == null ? "" : value;
        String uuid = UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8))
                .toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase(Locale.ROOT);
        return prefix + "_" + uuid;
    }

    private String map_col_lbl(String column_name) {
        if (column_name == null) return "";
        return COLUMN_LABEL_MAP.getOrDefault(column_name, column_name);
    }
    private ParsedNode prs_node_id(String node_id) {
        if (node_id == null) return null;
        int idx = node_id.indexOf(':');
        if (idx <= 0 || idx >= node_id.length() - 1) return null;
        return new ParsedNode(node_id.substring(0, idx), node_id.substring(idx + 1));
    }



    private void ens_zero(Long count, String error_message) {
        if (count != null && count > 0) throw new ServiceException(error_message);
    }
    private String sh_uuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private List<ModuleNode> cvt_to_mod_node_list(List<Map<String, Object>> raw_node_list) {
        if (raw_node_list == null || raw_node_list.isEmpty()) return Collections.emptyList();
        List<ModuleNode> node_list = new ArrayList<>();
        for (Map<String, Object> raw_node : raw_node_list) node_list.add(cvt_to_mod_node(raw_node));
        return node_list;
    }



    private ModuleNode cvt_to_mod_node(Map<String, Object> raw_node) {
        ModuleNode node = new ModuleNode();
        node.set_id(get_str(raw_node, "id"));
        node.set_parent_id(get_str(raw_node, "parent_id"));
        node.set_name(get_str(raw_node, "name"));
        node.set_level(get_int(raw_node, "level"));
        node.set_terminal(get_bool(raw_node, "terminal"));
        node.set_child_count(get_int(raw_node, "child_count"));
        node.set_part_cnt(get_int(raw_node, "part_count"));
        node.set_children(new ArrayList<>());
        return node;
    }

    private Integer norm_page_num(Integer page_num) {
        if (page_num == null || page_num < 1) return 1;
        return page_num;
    }



    private Integer norm_page_size(Integer page_size) {
        if (page_size == null || page_size < 1) return 10;
        if (page_size > 200) return 200;
        return page_size;


    }

    private String get_str(Map<String, Object> map, String key) {
        Object value = get_val(map, key);
        return value == null ? null : String.valueOf(value);

    }

    private Integer get_int(Map<String, Object> map, String key) {
        Object value = get_val(map, key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof BigInteger) return ((BigInteger) value).intValue();
        if (value instanceof BigDecimal) return ((BigDecimal) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();

        return Integer.valueOf(String.valueOf(value));

    }

    private Boolean get_bool(Map<String, Object> map, String key) {
        Object value = get_val(map, key);
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() == 1;
        String text = String.valueOf(value);

        return "true".equalsIgnoreCase(text) || "1".equals(text) || "yes".equalsIgnoreCase(text);

    }

    private Object get_val(Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        if (map.containsKey(key)) return map.get(key);
        String upper_key = key.toUpperCase(Locale.ROOT);
        if (map.containsKey(upper_key)) return map.get(upper_key);
        String lower_key = key.toLowerCase(Locale.ROOT);
        if (map.containsKey(lower_key)) return map.get(lower_key);
        return null;

    }

    private static class ParsedNode {
        private final String type;
        private final String raw_id;


        private ParsedNode(String type, String raw_id) {
            this.type = type;
            this.raw_id = raw_id;
        }

    }

    private static class ImportedPart {
        private final String partCode;
        private String partName;
        private final Map<String, ImportedProcess> processes = new LinkedHashMap<>();

        private ImportedPart(String partCode, String partName) {
            this.partCode = partCode;
            this.partName = partName;
        }
    }

    private static class ImportedProcess {
        private final Integer processNumber;
        private final String processCode;
        private final String processName;

        private ImportedProcess(Integer processNumber, String processCode, String processName) {
            this.processNumber = processNumber;
            this.processCode = processCode;
            this.processName = processName;
        }
    }

    private static class ProcessImportData {
        private final Set<String> partIds = new LinkedHashSet<>();
        private final List<Map<String, Object>> routeRows = new ArrayList<>();
        private final List<Map<String, Object>> processRows = new ArrayList<>();
    }

    private static class ActualProcessImportData {
        private final List<Map<String, Object>> partInstanceRows = new ArrayList<>();
        private final List<Map<String, Object>> workOrderRows = new ArrayList<>();
        private final List<Map<String, Object>> processExecutionRows = new ArrayList<>();
    }

    private static final Map<String, String> HIERARCHY_COLUMN_MAP = new LinkedHashMap<>();
    static {
        HIERARCHY_COLUMN_MAP.put("飞机ID", "aircraft_id");
        HIERARCHY_COLUMN_MAP.put("飞机名称", "aircraft_name");
        HIERARCHY_COLUMN_MAP.put("飞机型号", "aircraft_model");
        HIERARCHY_COLUMN_MAP.put("序列号", "serial_number");
        HIERARCHY_COLUMN_MAP.put("状态", "status");
        HIERARCHY_COLUMN_MAP.put("备注", "remarks");
        HIERARCHY_COLUMN_MAP.put("分系统ID", "subsystem_id");
        HIERARCHY_COLUMN_MAP.put("分系统名称", "subsystem_name");
        HIERARCHY_COLUMN_MAP.put("设备ID", "equipment_id");
        HIERARCHY_COLUMN_MAP.put("设备名称", "equipment_name");
        HIERARCHY_COLUMN_MAP.put("组件ID", "component_id");
        HIERARCHY_COLUMN_MAP.put("组件名称", "component_name");
        HIERARCHY_COLUMN_MAP.put("规格型号", "specification");
        HIERARCHY_COLUMN_MAP.put("零件模板ID", "part_template_id");
        HIERARCHY_COLUMN_MAP.put("零件编号", "part_number");
        HIERARCHY_COLUMN_MAP.put("零件名称", "part_name");
        HIERARCHY_COLUMN_MAP.put("材料", "material");
        HIERARCHY_COLUMN_MAP.put("设计版本", "design_version");
        HIERARCHY_COLUMN_MAP.put("零件实例ID", "part_instance_id");
        HIERARCHY_COLUMN_MAP.put("批次号", "batch_number");
        HIERARCHY_COLUMN_MAP.put("制造商", "manufacturer");
        HIERARCHY_COLUMN_MAP.put("生产日期", "production_date");
        HIERARCHY_COLUMN_MAP.put("当前状态", "current_status");
        HIERARCHY_COLUMN_MAP.put("质量等级", "quality_level");
        HIERARCHY_COLUMN_MAP.put("关键程度", "key_degree");
        HIERARCHY_COLUMN_MAP.put("图片地址", "image_url");
    }

    private static final Map<String, String> COLUMN_LABEL_MAP = new LinkedHashMap<>();
    static {
        COLUMN_LABEL_MAP.put("design_quality_id", "设计质量ID");
        COLUMN_LABEL_MAP.put("part_template_id", "零件模板ID");
        COLUMN_LABEL_MAP.put("design_version", "设计版本");
        COLUMN_LABEL_MAP.put("drawing_version", "图纸版本");
        COLUMN_LABEL_MAP.put("design_requirements", "设计要求");
        COLUMN_LABEL_MAP.put("functional_requirements", "功能要求");
        COLUMN_LABEL_MAP.put("tolerance_requirements", "公差要求");
        COLUMN_LABEL_MAP.put("key_quality_characteristics", "关键质量特性");
        COLUMN_LABEL_MAP.put("design_review_result", "设计评审结果");
        COLUMN_LABEL_MAP.put("verification_result", "验证结果");
        COLUMN_LABEL_MAP.put("create_time", "创建时间");
        COLUMN_LABEL_MAP.put("manufacturing_quality_id", "制造质量ID");
        COLUMN_LABEL_MAP.put("part_instance_id", "零件实例ID");
        COLUMN_LABEL_MAP.put("production_order_id", "生产工单ID");
        COLUMN_LABEL_MAP.put("workshop_id", "车间ID");
        COLUMN_LABEL_MAP.put("production_line_id", "产线ID");
        COLUMN_LABEL_MAP.put("start_time", "开始时间");
        COLUMN_LABEL_MAP.put("end_time", "结束时间");
        COLUMN_LABEL_MAP.put("process_status", "工艺状态");
        COLUMN_LABEL_MAP.put("final_inspection_result", "终检结果");
        COLUMN_LABEL_MAP.put("defect_count", "缺陷数");
        COLUMN_LABEL_MAP.put("rework_count", "返工数");
        COLUMN_LABEL_MAP.put("service_quality_id", "服役质量ID");
        COLUMN_LABEL_MAP.put("equipment_id", "设备ID");
        COLUMN_LABEL_MAP.put("component_id", "组件ID");
        COLUMN_LABEL_MAP.put("installed_aircraft_id", "装机飞机ID");
        COLUMN_LABEL_MAP.put("installation_position", "安装位置");
        COLUMN_LABEL_MAP.put("installation_date", "安装日期");
        COLUMN_LABEL_MAP.put("service_status", "服役状态");
        COLUMN_LABEL_MAP.put("total_running_hours", "累计运行小时");
        COLUMN_LABEL_MAP.put("cycle_count", "循环次数");
        COLUMN_LABEL_MAP.put("health_score", "健康评分");
        COLUMN_LABEL_MAP.put("last_inspection_date", "上次检修日期");
        COLUMN_LABEL_MAP.put("next_inspection_date", "下次检修日期");
        COLUMN_LABEL_MAP.put("update_time", "更新时间");

    }
}



