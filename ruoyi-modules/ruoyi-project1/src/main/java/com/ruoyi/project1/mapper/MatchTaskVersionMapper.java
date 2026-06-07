package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.MatchTaskVersion;

/**
 * 模式映射任务版本Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface MatchTaskVersionMapper 
{
    /**
     * 查询模式映射任务版本
     * 
     * @param versionId 模式映射任务版本主键
     * @return 模式映射任务版本
     */
    public MatchTaskVersion selectMatchTaskVersionByVersionId(Long versionId);

    /**
     * 查询模式映射任务版本列表
     * 
     * @param matchTaskVersion 模式映射任务版本
     * @return 模式映射任务版本集合
     */
    public List<MatchTaskVersion> selectMatchTaskVersionList(MatchTaskVersion matchTaskVersion);

    /**
     * 新增模式映射任务版本
     * 
     * @param matchTaskVersion 模式映射任务版本
     * @return 结果
     */
    public int insertMatchTaskVersion(MatchTaskVersion matchTaskVersion);

    /**
     * 修改模式映射任务版本
     * 
     * @param matchTaskVersion 模式映射任务版本
     * @return 结果
     */
    public int updateMatchTaskVersion(MatchTaskVersion matchTaskVersion);

    /**
     * 删除模式映射任务版本
     * 
     * @param versionId 模式映射任务版本主键
     * @return 结果
     */
    public int deleteMatchTaskVersionByVersionId(Long versionId);

    /**
     * 批量删除模式映射任务版本
     * 
     * @param versionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMatchTaskVersionByVersionIds(Long[] versionIds);
}
