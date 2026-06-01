package com.ruoyi.project3.mapper;

import com.ruoyi.project3.domain.partquality.DesignQuality;
import com.ruoyi.project3.domain.partquality.ManufacturingQuality;
import com.ruoyi.project3.domain.partquality.ServiceQuality;
import org.apache.ibatis.annotations.Param;

public interface PartQualityMapper
{
    int countDesignById(@Param("id") String id);

    int countManufacturingById(@Param("id") String id);

    int countServiceById(@Param("id") String id);

    int countPartTemplateById(@Param("id") String id);

    int countPartInstanceById(@Param("id") String id);

    int countEquipmentById(@Param("id") String id);

    int countComponentById(@Param("id") String id);

    int upsertDesign(DesignQuality row);

    int upsertManufacturing(ManufacturingQuality row);

    int upsertService(ServiceQuality row);
}
