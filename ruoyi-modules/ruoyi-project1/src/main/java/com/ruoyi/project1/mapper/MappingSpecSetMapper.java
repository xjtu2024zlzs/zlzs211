package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.MappingSpecSet;

/**
 * 最终接入规则集Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface MappingSpecSetMapper 
{
    /**
     * 查询最终接入规则集
     * 
     * @param specSetId 最终接入规则集主键
     * @return 最终接入规则集
     */
    public MappingSpecSet selectMappingSpecSetBySpecSetId(Long specSetId);

    /**
     * 查询最终接入规则集列表
     * 
     * @param mappingSpecSet 最终接入规则集
     * @return 最终接入规则集集合
     */
    public List<MappingSpecSet> selectMappingSpecSetList(MappingSpecSet mappingSpecSet);

    /**
     * 新增最终接入规则集
     * 
     * @param mappingSpecSet 最终接入规则集
     * @return 结果
     */
    public int insertMappingSpecSet(MappingSpecSet mappingSpecSet);

    /**
     * 修改最终接入规则集
     * 
     * @param mappingSpecSet 最终接入规则集
     * @return 结果
     */
    public int updateMappingSpecSet(MappingSpecSet mappingSpecSet);

    /**
     * 删除最终接入规则集
     * 
     * @param specSetId 最终接入规则集主键
     * @return 结果
     */
    public int deleteMappingSpecSetBySpecSetId(Long specSetId);

    /**
     * 批量删除最终接入规则集
     * 
     * @param specSetIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMappingSpecSetBySpecSetIds(Long[] specSetIds);
}
