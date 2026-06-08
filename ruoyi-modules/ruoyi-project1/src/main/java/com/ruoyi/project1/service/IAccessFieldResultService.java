package com.ruoyi.project1.service;

import java.util.List;
import com.ruoyi.project1.domain.AccessFieldResult;

/**
 * 目标字段接入结果Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IAccessFieldResultService 
{
    /**
     * 查询目标字段接入结果
     * 
     * @param fieldResultId 目标字段接入结果主键
     * @return 目标字段接入结果
     */
    public AccessFieldResult selectAccessFieldResultByFieldResultId(Long fieldResultId);

    /**
     * 查询目标字段接入结果列表
     * 
     * @param accessFieldResult 目标字段接入结果
     * @return 目标字段接入结果集合
     */
    public List<AccessFieldResult> selectAccessFieldResultList(AccessFieldResult accessFieldResult);

    /**
     * 新增目标字段接入结果
     * 
     * @param accessFieldResult 目标字段接入结果
     * @return 结果
     */
    public int insertAccessFieldResult(AccessFieldResult accessFieldResult);

    /**
     * 修改目标字段接入结果
     * 
     * @param accessFieldResult 目标字段接入结果
     * @return 结果
     */
    public int updateAccessFieldResult(AccessFieldResult accessFieldResult);

    /**
     * 批量删除目标字段接入结果
     * 
     * @param fieldResultIds 需要删除的目标字段接入结果主键集合
     * @return 结果
     */
    public int deleteAccessFieldResultByFieldResultIds(Long[] fieldResultIds);

    /**
     * 删除目标字段接入结果信息
     * 
     * @param fieldResultId 目标字段接入结果主键
     * @return 结果
     */
    public int deleteAccessFieldResultByFieldResultId(Long fieldResultId);
}
