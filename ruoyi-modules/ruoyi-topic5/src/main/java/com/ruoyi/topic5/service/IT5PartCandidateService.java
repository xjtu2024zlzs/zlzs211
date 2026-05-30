package com.ruoyi.topic5.service;

import java.util.List;
import com.ruoyi.topic5.domain.T5PartCandidate;

/**
 * 课题五-候选零部件定位结果Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface IT5PartCandidateService 
{
    /**
     * 查询课题五-候选零部件定位结果
     * 
     * @param candidateId 课题五-候选零部件定位结果主键
     * @return 课题五-候选零部件定位结果
     */
    public T5PartCandidate selectT5PartCandidateByCandidateId(Long candidateId);

    /**
     * 查询课题五-候选零部件定位结果列表
     * 
     * @param t5PartCandidate 课题五-候选零部件定位结果
     * @return 课题五-候选零部件定位结果集合
     */
    public List<T5PartCandidate> selectT5PartCandidateList(T5PartCandidate t5PartCandidate);

    /**
     * 新增课题五-候选零部件定位结果
     * 
     * @param t5PartCandidate 课题五-候选零部件定位结果
     * @return 结果
     */
    public int insertT5PartCandidate(T5PartCandidate t5PartCandidate);

    /**
     * 修改课题五-候选零部件定位结果
     * 
     * @param t5PartCandidate 课题五-候选零部件定位结果
     * @return 结果
     */
    public int updateT5PartCandidate(T5PartCandidate t5PartCandidate);

    /**
     * 批量删除课题五-候选零部件定位结果
     * 
     * @param candidateIds 需要删除的课题五-候选零部件定位结果主键集合
     * @return 结果
     */
    public int deleteT5PartCandidateByCandidateIds(Long[] candidateIds);

    /**
     * 删除课题五-候选零部件定位结果信息
     * 
     * @param candidateId 课题五-候选零部件定位结果主键
     * @return 结果
     */
    public int deleteT5PartCandidateByCandidateId(Long candidateId);
}
