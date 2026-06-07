package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.AccessFieldResultMapper;
import com.ruoyi.project1.domain.AccessFieldResult;
import com.ruoyi.project1.service.IAccessFieldResultService;

/**
 * 目标字段接入结果Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class AccessFieldResultServiceImpl implements IAccessFieldResultService 
{
    @Autowired
    private AccessFieldResultMapper accessFieldResultMapper;

    /**
     * 查询目标字段接入结果
     * 
     * @param fieldResultId 目标字段接入结果主键
     * @return 目标字段接入结果
     */
    @Override
    public AccessFieldResult selectAccessFieldResultByFieldResultId(Long fieldResultId)
    {
        return accessFieldResultMapper.selectAccessFieldResultByFieldResultId(fieldResultId);
    }

    /**
     * 查询目标字段接入结果列表
     * 
     * @param accessFieldResult 目标字段接入结果
     * @return 目标字段接入结果
     */
    @Override
    public List<AccessFieldResult> selectAccessFieldResultList(AccessFieldResult accessFieldResult)
    {
        return accessFieldResultMapper.selectAccessFieldResultList(accessFieldResult);
    }

    /**
     * 新增目标字段接入结果
     * 
     * @param accessFieldResult 目标字段接入结果
     * @return 结果
     */
    @Override
    public int insertAccessFieldResult(AccessFieldResult accessFieldResult)
    {
        accessFieldResult.setCreateTime(DateUtils.getNowDate());
        return accessFieldResultMapper.insertAccessFieldResult(accessFieldResult);
    }

    /**
     * 修改目标字段接入结果
     * 
     * @param accessFieldResult 目标字段接入结果
     * @return 结果
     */
    @Override
    public int updateAccessFieldResult(AccessFieldResult accessFieldResult)
    {
        accessFieldResult.setUpdateTime(DateUtils.getNowDate());
        return accessFieldResultMapper.updateAccessFieldResult(accessFieldResult);
    }

    /**
     * 批量删除目标字段接入结果
     * 
     * @param fieldResultIds 需要删除的目标字段接入结果主键
     * @return 结果
     */
    @Override
    public int deleteAccessFieldResultByFieldResultIds(Long[] fieldResultIds)
    {
        return accessFieldResultMapper.deleteAccessFieldResultByFieldResultIds(fieldResultIds);
    }

    /**
     * 删除目标字段接入结果信息
     * 
     * @param fieldResultId 目标字段接入结果主键
     * @return 结果
     */
    @Override
    public int deleteAccessFieldResultByFieldResultId(Long fieldResultId)
    {
        return accessFieldResultMapper.deleteAccessFieldResultByFieldResultId(fieldResultId);
    }
}
