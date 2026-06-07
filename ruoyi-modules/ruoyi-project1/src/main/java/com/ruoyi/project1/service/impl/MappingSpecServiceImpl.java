package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.MappingSpecMapper;
import com.ruoyi.project1.domain.MappingSpec;
import com.ruoyi.project1.service.IMappingSpecService;

/**
 * 最终接入字段规则Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class MappingSpecServiceImpl implements IMappingSpecService 
{
    @Autowired
    private MappingSpecMapper mappingSpecMapper;

    /**
     * 查询最终接入字段规则
     * 
     * @param mappingId 最终接入字段规则主键
     * @return 最终接入字段规则
     */
    @Override
    public MappingSpec selectMappingSpecByMappingId(Long mappingId)
    {
        return mappingSpecMapper.selectMappingSpecByMappingId(mappingId);
    }

    /**
     * 查询最终接入字段规则列表
     * 
     * @param mappingSpec 最终接入字段规则
     * @return 最终接入字段规则
     */
    @Override
    public List<MappingSpec> selectMappingSpecList(MappingSpec mappingSpec)
    {
        return mappingSpecMapper.selectMappingSpecList(mappingSpec);
    }

    /**
     * 新增最终接入字段规则
     * 
     * @param mappingSpec 最终接入字段规则
     * @return 结果
     */
    @Override
    public int insertMappingSpec(MappingSpec mappingSpec)
    {
        mappingSpec.setCreateTime(DateUtils.getNowDate());
        return mappingSpecMapper.insertMappingSpec(mappingSpec);
    }

    /**
     * 修改最终接入字段规则
     * 
     * @param mappingSpec 最终接入字段规则
     * @return 结果
     */
    @Override
    public int updateMappingSpec(MappingSpec mappingSpec)
    {
        mappingSpec.setUpdateTime(DateUtils.getNowDate());
        return mappingSpecMapper.updateMappingSpec(mappingSpec);
    }

    /**
     * 批量删除最终接入字段规则
     * 
     * @param mappingIds 需要删除的最终接入字段规则主键
     * @return 结果
     */
    @Override
    public int deleteMappingSpecByMappingIds(Long[] mappingIds)
    {
        return mappingSpecMapper.deleteMappingSpecByMappingIds(mappingIds);
    }

    /**
     * 删除最终接入字段规则信息
     * 
     * @param mappingId 最终接入字段规则主键
     * @return 结果
     */
    @Override
    public int deleteMappingSpecByMappingId(Long mappingId)
    {
        return mappingSpecMapper.deleteMappingSpecByMappingId(mappingId);
    }
}
