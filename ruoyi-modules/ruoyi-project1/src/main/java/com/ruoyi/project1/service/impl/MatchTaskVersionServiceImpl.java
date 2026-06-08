package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.MatchTaskVersionMapper;
import com.ruoyi.project1.domain.MatchTaskVersion;
import com.ruoyi.project1.service.IMatchTaskVersionService;

/**
 * 模式映射任务版本Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class MatchTaskVersionServiceImpl implements IMatchTaskVersionService 
{
    @Autowired
    private MatchTaskVersionMapper matchTaskVersionMapper;

    /**
     * 查询模式映射任务版本
     * 
     * @param versionId 模式映射任务版本主键
     * @return 模式映射任务版本
     */
    @Override
    public MatchTaskVersion selectMatchTaskVersionByVersionId(Long versionId)
    {
        return matchTaskVersionMapper.selectMatchTaskVersionByVersionId(versionId);
    }

    /**
     * 查询模式映射任务版本列表
     * 
     * @param matchTaskVersion 模式映射任务版本
     * @return 模式映射任务版本
     */
    @Override
    public List<MatchTaskVersion> selectMatchTaskVersionList(MatchTaskVersion matchTaskVersion)
    {
        return matchTaskVersionMapper.selectMatchTaskVersionList(matchTaskVersion);
    }

    /**
     * 新增模式映射任务版本
     * 
     * @param matchTaskVersion 模式映射任务版本
     * @return 结果
     */
    @Override
    public int insertMatchTaskVersion(MatchTaskVersion matchTaskVersion)
    {
        matchTaskVersion.setCreateTime(DateUtils.getNowDate());
        return matchTaskVersionMapper.insertMatchTaskVersion(matchTaskVersion);
    }

    /**
     * 修改模式映射任务版本
     * 
     * @param matchTaskVersion 模式映射任务版本
     * @return 结果
     */
    @Override
    public int updateMatchTaskVersion(MatchTaskVersion matchTaskVersion)
    {
        matchTaskVersion.setUpdateTime(DateUtils.getNowDate());
        return matchTaskVersionMapper.updateMatchTaskVersion(matchTaskVersion);
    }

    /**
     * 批量删除模式映射任务版本
     * 
     * @param versionIds 需要删除的模式映射任务版本主键
     * @return 结果
     */
    @Override
    public int deleteMatchTaskVersionByVersionIds(Long[] versionIds)
    {
        return matchTaskVersionMapper.deleteMatchTaskVersionByVersionIds(versionIds);
    }

    /**
     * 删除模式映射任务版本信息
     * 
     * @param versionId 模式映射任务版本主键
     * @return 结果
     */
    @Override
    public int deleteMatchTaskVersionByVersionId(Long versionId)
    {
        return matchTaskVersionMapper.deleteMatchTaskVersionByVersionId(versionId);
    }
}
