package com.ruoyi.designtask1.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.designtask1.mapper.DesignTemplateMapper;
import com.ruoyi.designtask1.domain.DesignTemplate;
import com.ruoyi.designtask1.service.IDesignTemplateService;

@Service
public class DesignTemplateServiceImpl implements IDesignTemplateService {

    @Autowired
    private DesignTemplateMapper templateMapper;

    @Override
    public List<DesignTemplate> selectTemplateList(DesignTemplate template) {
        return templateMapper.selectTemplateList(template);
    }

    @Override
    public DesignTemplate selectTemplateById(Long templateId) {
        return templateMapper.selectTemplateById(templateId);
    }

    @Override
    public List<DesignTemplate> selectTemplateAll() {
        return templateMapper.selectTemplateAll();
    }

    @Override
    public DesignTemplate selectDefaultTemplate() {
        return templateMapper.selectDefaultTemplate();
    }

    @Override
    @Transactional
    public int insertTemplate(DesignTemplate template) {
        return templateMapper.insertTemplate(template);
    }

    @Override
    @Transactional
    public int updateTemplate(DesignTemplate template) {
        return templateMapper.updateTemplate(template);
    }

    @Override
    @Transactional
    public int deleteTemplateById(Long templateId) {
        return templateMapper.deleteTemplateById(templateId);
    }

    @Override
    @Transactional
    public int deleteTemplateByIds(Long[] templateIds) {
        return templateMapper.deleteTemplateByIds(templateIds);
    }
}