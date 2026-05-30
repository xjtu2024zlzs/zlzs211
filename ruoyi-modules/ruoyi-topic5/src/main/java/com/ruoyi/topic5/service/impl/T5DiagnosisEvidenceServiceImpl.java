package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5DiagnosisEvidenceMapper;
import com.ruoyi.topic5.domain.T5DiagnosisEvidence;
import com.ruoyi.topic5.service.IT5DiagnosisEvidenceService;

/**
 * 课题五-诊断证据Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class T5DiagnosisEvidenceServiceImpl implements IT5DiagnosisEvidenceService 
{
    @Autowired
    private T5DiagnosisEvidenceMapper t5DiagnosisEvidenceMapper;

    /**
     * 查询课题五-诊断证据
     * 
     * @param evidenceId 课题五-诊断证据主键
     * @return 课题五-诊断证据
     */
    @Override
    public T5DiagnosisEvidence selectT5DiagnosisEvidenceByEvidenceId(Long evidenceId)
    {
        return t5DiagnosisEvidenceMapper.selectT5DiagnosisEvidenceByEvidenceId(evidenceId);
    }

    /**
     * 查询课题五-诊断证据列表
     * 
     * @param t5DiagnosisEvidence 课题五-诊断证据
     * @return 课题五-诊断证据
     */
    @Override
    public List<T5DiagnosisEvidence> selectT5DiagnosisEvidenceList(T5DiagnosisEvidence t5DiagnosisEvidence)
    {
        return t5DiagnosisEvidenceMapper.selectT5DiagnosisEvidenceList(t5DiagnosisEvidence);
    }

    /**
     * 新增课题五-诊断证据
     * 
     * @param t5DiagnosisEvidence 课题五-诊断证据
     * @return 结果
     */
    @Override
    public int insertT5DiagnosisEvidence(T5DiagnosisEvidence t5DiagnosisEvidence)
    {
        t5DiagnosisEvidence.setCreateTime(DateUtils.getNowDate());
        return t5DiagnosisEvidenceMapper.insertT5DiagnosisEvidence(t5DiagnosisEvidence);
    }

    /**
     * 修改课题五-诊断证据
     * 
     * @param t5DiagnosisEvidence 课题五-诊断证据
     * @return 结果
     */
    @Override
    public int updateT5DiagnosisEvidence(T5DiagnosisEvidence t5DiagnosisEvidence)
    {
        t5DiagnosisEvidence.setUpdateTime(DateUtils.getNowDate());
        return t5DiagnosisEvidenceMapper.updateT5DiagnosisEvidence(t5DiagnosisEvidence);
    }

    /**
     * 批量删除课题五-诊断证据
     * 
     * @param evidenceIds 需要删除的课题五-诊断证据主键
     * @return 结果
     */
    @Override
    public int deleteT5DiagnosisEvidenceByEvidenceIds(Long[] evidenceIds)
    {
        return t5DiagnosisEvidenceMapper.deleteT5DiagnosisEvidenceByEvidenceIds(evidenceIds);
    }

    /**
     * 删除课题五-诊断证据信息
     * 
     * @param evidenceId 课题五-诊断证据主键
     * @return 结果
     */
    @Override
    public int deleteT5DiagnosisEvidenceByEvidenceId(Long evidenceId)
    {
        return t5DiagnosisEvidenceMapper.deleteT5DiagnosisEvidenceByEvidenceId(evidenceId);
    }
}
