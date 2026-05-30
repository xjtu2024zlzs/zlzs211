package com.ruoyi.topic5.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 课题五-诊断证据对象 t5_diagnosis_evidence
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class T5DiagnosisEvidence extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 诊断证据ID */
    private Long evidenceId;

    /** 诊断结果ID */
    @Excel(name = "诊断结果ID")
    private Long diagnosisId;

    /** 追溯任务ID */
    @Excel(name = "追溯任务ID")
    private Long taskId;

    /** 零部件实例ID */
    @Excel(name = "零部件实例ID")
    private Long partId;

    /** 生命周期事件ID */
    @Excel(name = "生命周期事件ID")
    private Long eventId;

    /** 证据类型：lifecycle/kg/rule/model/history */
    @Excel(name = "证据类型：lifecycle/kg/rule/model/history")
    private String evidenceType;

    /** 证据名称 */
    @Excel(name = "证据名称")
    private String evidenceName;

    /** 证据描述 */
    @Excel(name = "证据描述")
    private String evidenceDesc;

    /** 证据取值 */
    @Excel(name = "证据取值")
    private String evidenceValue;

    /** 证据贡献度 */
    @Excel(name = "证据贡献度")
    private BigDecimal contribution;

    /** 证据置信度 */
    @Excel(name = "证据置信度")
    private BigDecimal confidence;

    /** 知识图谱路径 */
    @Excel(name = "知识图谱路径")
    private String kgPath;

    /** 来源表 */
    @Excel(name = "来源表")
    private String sourceTable;

    /** 来源记录ID */
    @Excel(name = "来源记录ID")
    private String sourceRecordId;

    public void setEvidenceId(Long evidenceId) 
    {
        this.evidenceId = evidenceId;
    }

    public Long getEvidenceId() 
    {
        return evidenceId;
    }

    public void setDiagnosisId(Long diagnosisId) 
    {
        this.diagnosisId = diagnosisId;
    }

    public Long getDiagnosisId() 
    {
        return diagnosisId;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setPartId(Long partId) 
    {
        this.partId = partId;
    }

    public Long getPartId() 
    {
        return partId;
    }

    public void setEventId(Long eventId) 
    {
        this.eventId = eventId;
    }

    public Long getEventId() 
    {
        return eventId;
    }

    public void setEvidenceType(String evidenceType) 
    {
        this.evidenceType = evidenceType;
    }

    public String getEvidenceType() 
    {
        return evidenceType;
    }

    public void setEvidenceName(String evidenceName) 
    {
        this.evidenceName = evidenceName;
    }

    public String getEvidenceName() 
    {
        return evidenceName;
    }

    public void setEvidenceDesc(String evidenceDesc) 
    {
        this.evidenceDesc = evidenceDesc;
    }

    public String getEvidenceDesc() 
    {
        return evidenceDesc;
    }

    public void setEvidenceValue(String evidenceValue) 
    {
        this.evidenceValue = evidenceValue;
    }

    public String getEvidenceValue() 
    {
        return evidenceValue;
    }

    public void setContribution(BigDecimal contribution) 
    {
        this.contribution = contribution;
    }

    public BigDecimal getContribution() 
    {
        return contribution;
    }

    public void setConfidence(BigDecimal confidence) 
    {
        this.confidence = confidence;
    }

    public BigDecimal getConfidence() 
    {
        return confidence;
    }

    public void setKgPath(String kgPath) 
    {
        this.kgPath = kgPath;
    }

    public String getKgPath() 
    {
        return kgPath;
    }

    public void setSourceTable(String sourceTable) 
    {
        this.sourceTable = sourceTable;
    }

    public String getSourceTable() 
    {
        return sourceTable;
    }

    public void setSourceRecordId(String sourceRecordId) 
    {
        this.sourceRecordId = sourceRecordId;
    }

    public String getSourceRecordId() 
    {
        return sourceRecordId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("evidenceId", getEvidenceId())
            .append("diagnosisId", getDiagnosisId())
            .append("taskId", getTaskId())
            .append("partId", getPartId())
            .append("eventId", getEventId())
            .append("evidenceType", getEvidenceType())
            .append("evidenceName", getEvidenceName())
            .append("evidenceDesc", getEvidenceDesc())
            .append("evidenceValue", getEvidenceValue())
            .append("contribution", getContribution())
            .append("confidence", getConfidence())
            .append("kgPath", getKgPath())
            .append("sourceTable", getSourceTable())
            .append("sourceRecordId", getSourceRecordId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
