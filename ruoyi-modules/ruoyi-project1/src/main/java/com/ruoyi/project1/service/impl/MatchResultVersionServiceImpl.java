package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.MatchResultVersionMapper;
import com.ruoyi.project1.domain.MatchResultVersion;
import com.ruoyi.project1.service.IMatchResultVersionService;

/**
 * 模式映射结果版本Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class MatchResultVersionServiceImpl implements IMatchResultVersionService 
{
    @Autowired
    private MatchResultVersionMapper matchResultVersionMapper;

    /**
     * 查询模式映射结果版本
     * 
     * @param resultVersionId 模式映射结果版本主键
     * @return 模式映射结果版本
     */
    @Override
    public MatchResultVersion selectMatchResultVersionByResultVersionId(Long resultVersionId)
    {
        return matchResultVersionMapper.selectMatchResultVersionByResultVersionId(resultVersionId);
    }

    /**
     * 查询模式映射结果版本列表
     * 
     * @param matchResultVersion 模式映射结果版本
     * @return 模式映射结果版本
     */
    @Override
    public List<MatchResultVersion> selectMatchResultVersionList(MatchResultVersion matchResultVersion)
    {
        return matchResultVersionMapper.selectMatchResultVersionList(matchResultVersion);
    }

    /**
     * 新增模式映射结果版本
     * 
     * @param matchResultVersion 模式映射结果版本
     * @return 结果
     */
    @Override
    public int insertMatchResultVersion(MatchResultVersion matchResultVersion)
    {
        matchResultVersion.setCreateTime(DateUtils.getNowDate());
        return matchResultVersionMapper.insertMatchResultVersion(matchResultVersion);
    }

    /**
     * 修改模式映射结果版本
     * 
     * @param matchResultVersion 模式映射结果版本
     * @return 结果
     */
    @Override
    public int updateMatchResultVersion(MatchResultVersion matchResultVersion)
    {
        return matchResultVersionMapper.updateMatchResultVersion(matchResultVersion);
    }

    /**
     * 批量删除模式映射结果版本
     * 
     * @param resultVersionIds 需要删除的模式映射结果版本主键
     * @return 结果
     */
    @Override
    public int deleteMatchResultVersionByResultVersionIds(Long[] resultVersionIds)
    {
        return matchResultVersionMapper.deleteMatchResultVersionByResultVersionIds(resultVersionIds);
    }

    /**
     * 删除模式映射结果版本信息
     * 
     * @param resultVersionId 模式映射结果版本主键
     * @return 结果
     */
    @Override
    public int deleteMatchResultVersionByResultVersionId(Long resultVersionId)
    {
        return matchResultVersionMapper.deleteMatchResultVersionByResultVersionId(resultVersionId);
    }
}
