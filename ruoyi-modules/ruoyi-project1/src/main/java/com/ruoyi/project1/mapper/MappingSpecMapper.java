package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.MappingSpec;

/**
 * 最终接入字段规则Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface MappingSpecMapper 
{
    /**
     * 查询最终接入字段规则
     * 
     * @param mappingId 最终接入字段规则主键
     * @return 最终接入字段规则
     */
    public MappingSpec selectMappingSpecByMappingId(Long mappingId);

    /**
     * 查询最终接入字段规则列表
     * 
     * @param mappingSpec 最终接入字段规则
     * @return 最终接入字段规则集合
     */
    public List<MappingSpec> selectMappingSpecList(MappingSpec mappingSpec);

    /**
     * 新增最终接入字段规则
     * 
     * @param mappingSpec 最终接入字段规则
     * @return 结果
     */
    public int insertMappingSpec(MappingSpec mappingSpec);

    /**
     * 修改最终接入字段规则
     * 
     * @param mappingSpec 最终接入字段规则
     * @return 结果
     */
    public int updateMappingSpec(MappingSpec mappingSpec);

    /**
     * 删除最终接入字段规则
     * 
     * @param mappingId 最终接入字段规则主键
     * @return 结果
     */
    public int deleteMappingSpecByMappingId(Long mappingId);

    /**
     * 批量删除最终接入字段规则
     * 
     * @param mappingIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMappingSpecByMappingIds(Long[] mappingIds);

    /**
     * Delete final access field rules by rule set id.
     *
     * @param specSetId rule set id
     * @return result
     */
    public int deleteMappingSpecBySpecSetId(Long specSetId);
}
