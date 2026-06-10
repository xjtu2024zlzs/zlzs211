package com.ruoyi.designtask1.service;

import java.util.List;
import com.ruoyi.designtask1.domain.DesignTemplate;

public interface IDesignTemplateService {

    List<DesignTemplate> selectTemplateList(DesignTemplate template);

    DesignTemplate selectTemplateById(Long templateId);

    List<DesignTemplate> selectTemplateAll();

    DesignTemplate selectDefaultTemplate();

    int insertTemplate(DesignTemplate template);

    int updateTemplate(DesignTemplate template);

    int deleteTemplateById(Long templateId);

    int deleteTemplateByIds(Long[] templateIds);
}