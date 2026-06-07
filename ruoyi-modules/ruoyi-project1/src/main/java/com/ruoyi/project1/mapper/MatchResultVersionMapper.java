package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.MatchResultVersion;

/**
 * 模式映射结果版本Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface MatchResultVersionMapper 
{
    /**
     * 查询模式映射结果版本
     * 
     * @param resultVersionId 模式映射结果版本主键
     * @return 模式映射结果版本
     */
    public MatchResultVersion selectMatchResultVersionByResultVersionId(Long resultVersionId);

    /**
     * 查询模式映射结果版本列表
     * 
     * @param matchResultVersion 模式映射结果版本
     * @return 模式映射结果版本集合
     */
    public List<MatchResultVersion> selectMatchResultVersionList(MatchResultVersion matchResultVersion);

    /**
     * 新增模式映射结果版本
     * 
     * @param matchResultVersion 模式映射结果版本
     * @return 结果
     */
    public int insertMatchResultVersion(MatchResultVersion matchResultVersion);

    /**
     * 修改模式映射结果版本
     * 
     * @param matchResultVersion 模式映射结果版本
     * @return 结果
     */
    public int updateMatchResultVersion(MatchResultVersion matchResultVersion);

    /**
     * 删除模式映射结果版本
     * 
     * @param resultVersionId 模式映射结果版本主键
     * @return 结果
     */
    public int deleteMatchResultVersionByResultVersionId(Long resultVersionId);

    /**
     * 批量删除模式映射结果版本
     * 
     * @param resultVersionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMatchResultVersionByResultVersionIds(Long[] resultVersionIds);
}
