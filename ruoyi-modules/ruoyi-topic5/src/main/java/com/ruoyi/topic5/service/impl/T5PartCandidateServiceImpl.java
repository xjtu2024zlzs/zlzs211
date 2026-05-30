package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5PartCandidateMapper;
import com.ruoyi.topic5.domain.T5PartCandidate;
import com.ruoyi.topic5.service.IT5PartCandidateService;

/**
 * 课题五-候选零部件定位结果Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class T5PartCandidateServiceImpl implements IT5PartCandidateService 
{
    @Autowired
    private T5PartCandidateMapper t5PartCandidateMapper;

    /**
     * 查询课题五-候选零部件定位结果
     * 
     * @param candidateId 课题五-候选零部件定位结果主键
     * @return 课题五-候选零部件定位结果
     */
    @Override
    public T5PartCandidate selectT5PartCandidateByCandidateId(Long candidateId)
    {
        return t5PartCandidateMapper.selectT5PartCandidateByCandidateId(candidateId);
    }

    /**
     * 查询课题五-候选零部件定位结果列表
     * 
     * @param t5PartCandidate 课题五-候选零部件定位结果
     * @return 课题五-候选零部件定位结果
     */
    @Override
    public List<T5PartCandidate> selectT5PartCandidateList(T5PartCandidate t5PartCandidate)
    {
        return t5PartCandidateMapper.selectT5PartCandidateList(t5PartCandidate);
    }

    /**
     * 新增课题五-候选零部件定位结果
     * 
     * @param t5PartCandidate 课题五-候选零部件定位结果
     * @return 结果
     */
    @Override
    public int insertT5PartCandidate(T5PartCandidate t5PartCandidate)
    {
        t5PartCandidate.setCreateTime(DateUtils.getNowDate());
        return t5PartCandidateMapper.insertT5PartCandidate(t5PartCandidate);
    }

    /**
     * 修改课题五-候选零部件定位结果
     * 
     * @param t5PartCandidate 课题五-候选零部件定位结果
     * @return 结果
     */
    @Override
    public int updateT5PartCandidate(T5PartCandidate t5PartCandidate)
    {
        t5PartCandidate.setUpdateTime(DateUtils.getNowDate());
        return t5PartCandidateMapper.updateT5PartCandidate(t5PartCandidate);
    }

    /**
     * 批量删除课题五-候选零部件定位结果
     * 
     * @param candidateIds 需要删除的课题五-候选零部件定位结果主键
     * @return 结果
     */
    @Override
    public int deleteT5PartCandidateByCandidateIds(Long[] candidateIds)
    {
        return t5PartCandidateMapper.deleteT5PartCandidateByCandidateIds(candidateIds);
    }

    /**
     * 删除课题五-候选零部件定位结果信息
     * 
     * @param candidateId 课题五-候选零部件定位结果主键
     * @return 结果
     */
    @Override
    public int deleteT5PartCandidateByCandidateId(Long candidateId)
    {
        return t5PartCandidateMapper.deleteT5PartCandidateByCandidateId(candidateId);
    }
}
