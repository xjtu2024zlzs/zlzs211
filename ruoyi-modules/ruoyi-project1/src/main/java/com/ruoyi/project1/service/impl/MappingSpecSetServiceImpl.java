package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.MappingSpecSetMapper;
import com.ruoyi.project1.domain.MappingSpecSet;
import com.ruoyi.project1.service.IMappingSpecSetService;

/**
 * 最终接入规则集Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class MappingSpecSetServiceImpl implements IMappingSpecSetService 
{
    @Autowired
    private MappingSpecSetMapper mappingSpecSetMapper;

    /**
     * 查询最终接入规则集
     * 
     * @param specSetId 最终接入规则集主键
     * @return 最终接入规则集
     */
    @Override
    public MappingSpecSet selectMappingSpecSetBySpecSetId(Long specSetId)
    {
        return mappingSpecSetMapper.selectMappingSpecSetBySpecSetId(specSetId);
    }

    /**
     * 查询最终接入规则集列表
     * 
     * @param mappingSpecSet 最终接入规则集
     * @return 最终接入规则集
     */
    @Override
    public List<MappingSpecSet> selectMappingSpecSetList(MappingSpecSet mappingSpecSet)
    {
        return mappingSpecSetMapper.selectMappingSpecSetList(mappingSpecSet);
    }

    /**
     * 新增最终接入规则集
     * 
     * @param mappingSpecSet 最终接入规则集
     * @return 结果
     */
    @Override
    public int insertMappingSpecSet(MappingSpecSet mappingSpecSet)
    {
        mappingSpecSet.setCreateTime(DateUtils.getNowDate());
        return mappingSpecSetMapper.insertMappingSpecSet(mappingSpecSet);
    }

    /**
     * 修改最终接入规则集
     * 
     * @param mappingSpecSet 最终接入规则集
     * @return 结果
     */
    @Override
    public int updateMappingSpecSet(MappingSpecSet mappingSpecSet)
    {
        mappingSpecSet.setUpdateTime(DateUtils.getNowDate());
        return mappingSpecSetMapper.updateMappingSpecSet(mappingSpecSet);
    }

    /**
     * 批量删除最终接入规则集
     * 
     * @param specSetIds 需要删除的最终接入规则集主键
     * @return 结果
     */
    @Override
    public int deleteMappingSpecSetBySpecSetIds(Long[] specSetIds)
    {
        return mappingSpecSetMapper.deleteMappingSpecSetBySpecSetIds(specSetIds);
    }

    /**
     * 删除最终接入规则集信息
     * 
     * @param specSetId 最终接入规则集主键
     * @return 结果
     */
    @Override
    public int deleteMappingSpecSetBySpecSetId(Long specSetId)
    {
        return mappingSpecSetMapper.deleteMappingSpecSetBySpecSetId(specSetId);
    }
}
