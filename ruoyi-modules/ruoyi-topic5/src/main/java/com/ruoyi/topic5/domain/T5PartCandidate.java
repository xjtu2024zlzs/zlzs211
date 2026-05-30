package com.ruoyi.topic5.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 课题五-候选零部件定位结果对象 t5_part_candidate
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class T5PartCandidate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 候选结果ID */
    private Long candidateId;

    /** 追溯任务ID */
    @Excel(name = "追溯任务ID")
    private Long taskId;

    /** 算法运行ID */
    @Excel(name = "算法运行ID")
    private Long runId;

    /** 零部件实例ID */
    @Excel(name = "零部件实例ID")
    private Long partId;

    /** 零件编号 */
    @Excel(name = "零件编号")
    private String partNo;

    /** 零件名称 */
    @Excel(name = "零件名称")
    private String partName;

    /** 零件序列号 */
    @Excel(name = "零件序列号")
    private String serialNo;

    /** 批次号 */
    @Excel(name = "批次号")
    private String batchNo;

    /** 定位方式：rule/kg/transe/gnn/hybrid */
    @Excel(name = "定位方式：rule/kg/transe/gnn/hybrid")
    private String locateMethod;

    /** 匹配分数 */
    @Excel(name = "匹配分数")
    private BigDecimal matchScore;

    /** 置信度 */
    @Excel(name = "置信度")
    private BigDecimal confidence;

    /** 排序号 */
    @Excel(name = "排序号")
    private Long rankNo;

    /** 是否选定为目标零件：0否，1是 */
    @Excel(name = "是否选定为目标零件：0否，1是")
    private String isSelected;

    /** 定位原因 */
    @Excel(name = "定位原因")
    private String locateReason;

    /** 关联路径/知识图谱路径 */
    @Excel(name = "关联路径/知识图谱路径")
    private String relationPath;

    public void setCandidateId(Long candidateId) 
    {
        this.candidateId = candidateId;
    }

    public Long getCandidateId() 
    {
        return candidateId;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setRunId(Long runId) 
    {
        this.runId = runId;
    }

    public Long getRunId() 
    {
        return runId;
    }

    public void setPartId(Long partId) 
    {
        this.partId = partId;
    }

    public Long getPartId() 
    {
        return partId;
    }

    public void setPartNo(String partNo) 
    {
        this.partNo = partNo;
    }

    public String getPartNo() 
    {
        return partNo;
    }

    public void setPartName(String partName) 
    {
        this.partName = partName;
    }

    public String getPartName() 
    {
        return partName;
    }

    public void setSerialNo(String serialNo) 
    {
        this.serialNo = serialNo;
    }

    public String getSerialNo() 
    {
        return serialNo;
    }

    public void setBatchNo(String batchNo) 
    {
        this.batchNo = batchNo;
    }

    public String getBatchNo() 
    {
        return batchNo;
    }

    public void setLocateMethod(String locateMethod) 
    {
        this.locateMethod = locateMethod;
    }

    public String getLocateMethod() 
    {
        return locateMethod;
    }

    public void setMatchScore(BigDecimal matchScore) 
    {
        this.matchScore = matchScore;
    }

    public BigDecimal getMatchScore() 
    {
        return matchScore;
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

    public void setIsSelected(String isSelected) 
    {
        this.isSelected = isSelected;
    }

    public String getIsSelected() 
    {
        return isSelected;
    }

    public void setLocateReason(String locateReason) 
    {
        this.locateReason = locateReason;
    }

    public String getLocateReason() 
    {
        return locateReason;
    }

    public void setRelationPath(String relationPath) 
    {
        this.relationPath = relationPath;
    }

    public String getRelationPath() 
    {
        return relationPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("candidateId", getCandidateId())
            .append("taskId", getTaskId())
            .append("runId", getRunId())
            .append("partId", getPartId())
            .append("partNo", getPartNo())
            .append("partName", getPartName())
            .append("serialNo", getSerialNo())
            .append("batchNo", getBatchNo())
            .append("locateMethod", getLocateMethod())
            .append("matchScore", getMatchScore())
            .append("confidence", getConfidence())
            .append("rankNo", getRankNo())
            .append("isSelected", getIsSelected())
            .append("locateReason", getLocateReason())
            .append("relationPath", getRelationPath())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
