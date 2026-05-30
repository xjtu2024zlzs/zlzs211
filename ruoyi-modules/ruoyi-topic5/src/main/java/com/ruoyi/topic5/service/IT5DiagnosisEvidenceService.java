package com.ruoyi.topic5.service;

import java.util.List;
import com.ruoyi.topic5.domain.T5DiagnosisEvidence;

/**
 * 课题五-诊断证据Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface IT5DiagnosisEvidenceService 
{
    /**
     * 查询课题五-诊断证据
     * 
     * @param evidenceId 课题五-诊断证据主键
     * @return 课题五-诊断证据
     */
    public T5DiagnosisEvidence selectT5DiagnosisEvidenceByEvidenceId(Long evidenceId);

    /**
     * 查询课题五-诊断证据列表
     * 
     * @param t5DiagnosisEvidence 课题五-诊断证据
     * @return 课题五-诊断证据集合
     */
    public List<T5DiagnosisEvidence> selectT5DiagnosisEvidenceList(T5DiagnosisEvidence t5DiagnosisEvidence);

    /**
     * 新增课题五-诊断证据
     * 
     * @param t5DiagnosisEvidence 课题五-诊断证据
     * @return 结果
     */
    public int insertT5DiagnosisEvidence(T5DiagnosisEvidence t5DiagnosisEvidence);

    /**
     * 修改课题五-诊断证据
     * 
     * @param t5DiagnosisEvidence 课题五-诊断证据
     * @return 结果
     */
    public int updateT5DiagnosisEvidence(T5DiagnosisEvidence t5DiagnosisEvidence);

    /**
     * 批量删除课题五-诊断证据
     * 
     * @param evidenceIds 需要删除的课题五-诊断证据主键集合
     * @return 结果
     */
    public int deleteT5DiagnosisEvidenceByEvidenceIds(Long[] evidenceIds);

    /**
     * 删除课题五-诊断证据信息
     * 
     * @param evidenceId 课题五-诊断证据主键
     * @return 结果
     */
    public int deleteT5DiagnosisEvidenceByEvidenceId(Long evidenceId);
}
