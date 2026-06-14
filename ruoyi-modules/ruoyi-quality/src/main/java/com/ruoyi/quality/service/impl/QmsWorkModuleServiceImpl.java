package com.ruoyi.quality.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.quality.mapper.QmsWorkModuleMapper;
import com.ruoyi.quality.domain.QmsWorkModule;
import com.ruoyi.quality.service.IQmsWorkModuleService;

/**
 * 质量问题工作模块配置Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
@Service
public class QmsWorkModuleServiceImpl implements IQmsWorkModuleService 
{
    @Autowired
    private QmsWorkModuleMapper qmsWorkModuleMapper;

    /**
     * 查询质量问题工作模块配置
     * 
     * @param moduleId 质量问题工作模块配置主键
     * @return 质量问题工作模块配置
     */
    @Override
    public QmsWorkModule selectQmsWorkModuleByModuleId(Long moduleId)
    {
        return qmsWorkModuleMapper.selectQmsWorkModuleByModuleId(moduleId);
    }

    /**
     * 查询质量问题工作模块配置列表
     * 
     * @param qmsWorkModule 质量问题工作模块配置
     * @return 质量问题工作模块配置
     */
    @Override
    public List<QmsWorkModule> selectQmsWorkModuleList(QmsWorkModule qmsWorkModule)
    {
        return qmsWorkModuleMapper.selectQmsWorkModuleList(qmsWorkModule);
    }

    /**
     * 新增质量问题工作模块配置
     * 
     * @param qmsWorkModule 质量问题工作模块配置
     * @return 结果
     */
    @Override
    public int insertQmsWorkModule(QmsWorkModule qmsWorkModule)
    {
        qmsWorkModule.setCreateTime(DateUtils.getNowDate());
        return qmsWorkModuleMapper.insertQmsWorkModule(qmsWorkModule);
    }

    /**
     * 修改质量问题工作模块配置
     * 
     * @param qmsWorkModule 质量问题工作模块配置
     * @return 结果
     */
    @Override
    public int updateQmsWorkModule(QmsWorkModule qmsWorkModule)
    {
        qmsWorkModule.setUpdateTime(DateUtils.getNowDate());
        return qmsWorkModuleMapper.updateQmsWorkModule(qmsWorkModule);
    }

    /**
     * 批量删除质量问题工作模块配置
     * 
     * @param moduleIds 需要删除的质量问题工作模块配置主键
     * @return 结果
     */
    @Override
    public int deleteQmsWorkModuleByModuleIds(Long[] moduleIds)
    {
        return qmsWorkModuleMapper.deleteQmsWorkModuleByModuleIds(moduleIds);
    }

    /**
     * 删除质量问题工作模块配置信息
     * 
     * @param moduleId 质量问题工作模块配置主键
     * @return 结果
     */
    @Override
    public int deleteQmsWorkModuleByModuleId(Long moduleId)
    {
        return qmsWorkModuleMapper.deleteQmsWorkModuleByModuleId(moduleId);
    }
}
