package com.ruoyi.designtask1.mapper;

import java.util.List;
import com.ruoyi.designtask1.domain.DesignTemplate;

public interface DesignTemplateMapper {

    List<DesignTemplate> selectTemplateList(DesignTemplate template);

    DesignTemplate selectTemplateById(Long templateId);

    List<DesignTemplate> selectTemplateAll();

    DesignTemplate selectDefaultTemplate();

    int insertTemplate(DesignTemplate template);

    int updateTemplate(DesignTemplate template);

    int deleteTemplateById(Long templateId);

    int deleteTemplateByIds(Long[] templateIds);

    DesignTemplate checkTemplateNameUnique(String templateName);
}