package com.ruoyi.project3.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.domain.partquality.DesignQuality;
import com.ruoyi.project3.domain.partquality.ManufacturingQuality;
import com.ruoyi.project3.domain.partquality.PartQualityImportData;
import com.ruoyi.project3.domain.partquality.PartQualityImportError;
import com.ruoyi.project3.domain.partquality.PartQualityImportResult;
import com.ruoyi.project3.domain.partquality.ServiceQuality;
import com.ruoyi.project3.mapper.PartQualityMapper;
import com.ruoyi.project3.service.PartQualityService;
import com.ruoyi.project3.util.PartQualityExcelParser;
import com.ruoyi.project3.util.PartQualityTemplateGenerator;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

@Service
public class PartQualityServiceImpl implements PartQualityService
{
    private static final Logger log = LoggerFactory.getLogger(PartQualityServiceImpl.class);

    @Resource
    private PartQualityMapper partQualityMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartQualityImportResult importPartQuality(MultipartFile file)
    {
        PartQualityImportResult result = new PartQualityImportResult();
        if (file == null || file.isEmpty()) {
            result.getErrors().add(new PartQualityImportError(null, null, "Excel文件", "Excel文件不能为空"));
            return result;
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            result.getErrors().add(new PartQualityImportError(null, null, "Excel文件", "只支持.xlsx文件"));
            return result;
        }

        PartQualityImportData data = new PartQualityExcelParser().parse(file);
        result.setDesignCount(data.getDesignRows().size());
        result.setManufacturingCount(data.getManufacturingRows().size());
        result.setServiceCount(data.getServiceRows().size());
        validateReferences(data);
        if (!data.getErrors().isEmpty()) {
            result.setErrors(data.getErrors());
            result.setSuccess(false);
            return result;
        }

        int insertCount = 0;
        int updateCount = 0;
        try {
            for (DesignQuality row : data.getDesignRows()) {
                if (partQualityMapper.countDesignById(row.getDesignQualityId()) > 0) {
                    updateCount++;
                } else {
                    insertCount++;
                }
                partQualityMapper.upsertDesign(row);
            }
            for (ManufacturingQuality row : data.getManufacturingRows()) {
                if (partQualityMapper.countManufacturingById(row.getManufacturingQualityId()) > 0) {
                    updateCount++;
                } else {
                    insertCount++;
                }
                partQualityMapper.upsertManufacturing(row);
            }
            for (ServiceQuality row : data.getServiceRows()) {
                if (partQualityMapper.countServiceById(row.getServiceQualityId()) > 0) {
                    updateCount++;
                } else {
                    insertCount++;
                }
                partQualityMapper.upsertService(row);
            }
        } catch (Exception e) {
            log.error("零件质量信息导入写库失败，文件名={}", fileName, e);
            throw new ServiceException("数据库写入失败，请检查引用关系和唯一键约束");
        }
        result.setInsertCount(insertCount);
        result.setUpdateCount(updateCount);
        result.setSuccess(true);
        return result;
    }

    @Override
    public void writeTemplate(OutputStream outputStream) throws IOException
    {
        new PartQualityTemplateGenerator().write(outputStream);
    }

    private void validateReferences(PartQualityImportData data)
    {
        Set<String> partTemplateIds = new HashSet<>();
        Set<String> partInstanceIds = new HashSet<>();
        Set<String> equipmentIds = new HashSet<>();
        Set<String> componentIds = new HashSet<>();

        for (DesignQuality row : data.getDesignRows()) {
            partTemplateIds.add(row.getPartTemplateId());
        }
        for (ManufacturingQuality row : data.getManufacturingRows()) {
            partInstanceIds.add(row.getPartInstanceId());
        }
        for (ServiceQuality row : data.getServiceRows()) {
            partInstanceIds.add(row.getPartInstanceId());
            equipmentIds.add(row.getEquipmentId());
            componentIds.add(row.getComponentId());
        }

        for (String id : partTemplateIds) {
            if (notBlank(id) && partQualityMapper.countPartTemplateById(id) == 0) {
                data.getErrors().add(new PartQualityImportError("设计质量信息", null, "零件模板ID", "零件模板不存在：" + id));
            }
        }
        for (String id : partInstanceIds) {
            if (notBlank(id) && partQualityMapper.countPartInstanceById(id) == 0) {
                data.getErrors().add(new PartQualityImportError("制造质量信息", null, "零件实例ID", "零件实例不存在：" + id));
            }
        }
        for (String id : equipmentIds) {
            if (notBlank(id) && partQualityMapper.countEquipmentById(id) == 0) {
                data.getErrors().add(new PartQualityImportError("服役质量信息", null, "设备ID", "设备不存在：" + id));
            }
        }
        for (String id : componentIds) {
            if (notBlank(id) && partQualityMapper.countComponentById(id) == 0) {
                data.getErrors().add(new PartQualityImportError("服役质量信息", null, "组件ID", "组件不存在：" + id));
            }
        }
    }

    private boolean notBlank(String value)
    {
        return value != null && !value.trim().isEmpty();
    }
}
