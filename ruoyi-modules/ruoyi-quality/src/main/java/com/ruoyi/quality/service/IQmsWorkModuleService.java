package com.ruoyi.quality.service;

import java.util.List;
import com.ruoyi.quality.domain.QmsWorkModule;

/**
 * 质量问题工作模块配置Service接口
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
public interface IQmsWorkModuleService 
{
    /**
     * 查询质量问题工作模块配置
     * 
     * @param moduleId 质量问题工作模块配置主键
     * @return 质量问题工作模块配置
     */
    public QmsWorkModule selectQmsWorkModuleByModuleId(Long moduleId);

    /**
     * 查询质量问题工作模块配置列表
     * 
     * @param qmsWorkModule 质量问题工作模块配置
     * @return 质量问题工作模块配置集合
     */
    public List<QmsWorkModule> selectQmsWorkModuleList(QmsWorkModule qmsWorkModule);

    /**
     * 新增质量问题工作模块配置
     * 
     * @param qmsWorkModule 质量问题工作模块配置
     * @return 结果
     */
    public int insertQmsWorkModule(QmsWorkModule qmsWorkModule);

    /**
     * 修改质量问题工作模块配置
     * 
     * @param qmsWorkModule 质量问题工作模块配置
     * @return 结果
     */
    public int updateQmsWorkModule(QmsWorkModule qmsWorkModule);

    /**
     * 批量删除质量问题工作模块配置
     * 
     * @param moduleIds 需要删除的质量问题工作模块配置主键集合
     * @return 结果
     */
    public int deleteQmsWorkModuleByModuleIds(Long[] moduleIds);

    /**
     * 删除质量问题工作模块配置信息
     * 
     * @param moduleId 质量问题工作模块配置主键
     * @return 结果
     */
    public int deleteQmsWorkModuleByModuleId(Long moduleId);
}
