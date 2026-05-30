package com.ruoyi.topic5.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 课题五-故障阶段诊断结果对象 t5_stage_diagnosis
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class T5StageDiagnosis extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 诊断结果ID */
    private Long diagnosisId;

    /** 追溯任务ID */
    @Excel(name = "追溯任务ID")
    private Long taskId;

    /** 零部件实例ID */
    @Excel(name = "零部件实例ID")
    private Long partId;

    /** 算法运行ID */
    @Excel(name = "算法运行ID")
    private Long runId;

    /** 故障阶段编码 */
    @Excel(name = "故障阶段编码")
    private String stageCode;

    /** 故障阶段名称 */
    @Excel(name = "故障阶段名称")
    private String stageName;

    /** 风险分数 */
    @Excel(name = "风险分数")
    private BigDecimal riskScore;

    /** 置信度 */
    @Excel(name = "置信度")
    private BigDecimal confidence;

    /** 排序号 */
    @Excel(name = "排序号")
    private Long rankNo;

    /** 是否最终结论：0否，1是 */
    @Excel(name = "是否最终结论：0否，1是")
    private String isFinal;

    /** 故障模式 */
    @Excel(name = "故障模式")
    private String faultMode;

    /** 原因描述 */
    @Excel(name = "原因描述")
    private String causeDesc;

    /** 诊断结论 */
    @Excel(name = "诊断结论")
    private String diagnosisResult;

    /** 算法名称 */
    @Excel(name = "算法名称")
    private String algorithmName;

    /** 模型版本 */
    @Excel(name = "模型版本")
    private String modelVersion;

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

    public void setRunId(Long runId) 
    {
        this.runId = runId;
    }

    public Long getRunId() 
    {
        return runId;
    }

    public void setStageCode(String stageCode) 
    {
        this.stageCode = stageCode;
    }

    public String getStageCode() 
    {
        return stageCode;
    }

    public void setStageName(String stageName) 
    {
        this.stageName = stageName;
    }

    public String getStageName() 
    {
        return stageName;
    }

    public void setRiskScore(BigDecimal riskScore) 
    {
        this.riskScore = riskScore;
    }

    public BigDecimal getRiskScore() 
    {
        return riskScore;
    }

    public void setConfidence(BigDecimal confidence) 
    {
        this.confidence = confidence;
    }

    public BigDecimal getConfidence() 
    {
        return confidence;
    }

    public void setRankNo(Long rankNo) 
    {
        this.rankNo = rankNo;
    }

    public Long getRankNo() 
    {
        return rankNo;
    }

    public void setIsFinal(String isFinal) 
    {
        this.isFinal = isFinal;
    }

    public String getIsFinal() 
    {
        return isFinal;
    }

    public void setFaultMode(String faultMode) 
    {
        this.faultMode = faultMode;
    }

    public String getFaultMode() 
    {
        return faultMode;
    }

    public void setCauseDesc(String causeDesc) 
    {
        this.causeDesc = causeDesc;
    }

    public String getCauseDesc() 
    {
        return causeDesc;
    }

    public void setDiagnosisResult(String diagnosisResult) 
    {
        this.diagnosisResult = diagnosisResult;
    }

    public String getDiagnosisResult() 
    {
        return diagnosisResult;
    }

    public void setAlgorithmName(String algorithmName) 
    {
        this.algorithmName = algorithmName;
    }

    public String getAlgorithmName() 
    {
        return algorithmName;
    }

    public void setModelVersion(String modelVersion) 
    {
        this.modelVersion = modelVersion;
    }

    public String getModelVersion() 
    {
        return modelVersion;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("diagnosisId", getDiagnosisId())
            .append("taskId", getTaskId())
            .append("partId", getPartId())
            .append("runId", getRunId())
            .append("stageCode", getStageCode())
            .append("stageName", getStageName())
            .append("riskScore", getRiskScore())
            .append("confidence", getConfidence())
            .append("rankNo", getRankNo())
            .append("isFinal", getIsFinal())
            .append("faultMode", getFaultMode())
            .append("causeDesc", getCauseDesc())
            .append("diagnosisResult", getDiagnosisResult())
            .append("algorithmName", getAlgorithmName())
            .append("modelVersion", getModelVersion())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
