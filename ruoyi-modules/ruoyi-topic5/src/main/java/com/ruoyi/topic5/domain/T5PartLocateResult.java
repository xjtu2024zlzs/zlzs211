package com.ruoyi.topic5.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 算法配置对象 t5_part_locate_result
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
public class T5PartLocateResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 零件定位结果ID */
    private Long locateId;

    /** 追溯案例ID */
    @Excel(name = "追溯案例ID")
    private Long caseId;

    /** 算法ID */
    @Excel(name = "算法ID")
    private Long algorithmId;

    /** 算法名称 */
    @Excel(name = "算法名称")
    private String algorithmName;

    /** 候选零部件ID */
    @Excel(name = "候选零部件ID")
    private Long partId;

    /** 候选零件编号 */
    @Excel(name = "候选零件编号")
    private String partNo;

    /** 候选零件名称 */
    @Excel(name = "候选零件名称")
    private String partName;

    /** 匹配分数 */
    @Excel(name = "匹配分数")
    private BigDecimal matchScore;

    /** 置信度 */
    @Excel(name = "置信度")
    private BigDecimal confidence;

    /** 排序号 */
    @Excel(name = "排序号")
    private Long rankNo;

    /** 是否最终定位零件：0否，1是 */
    @Excel(name = "是否最终定位零件：0否，1是")
    private String isFinal;

    /** 定位原因 */
    @Excel(name = "定位原因")
    private String locateReason;

    /** 定位证据摘要 */
    @Excel(name = "定位证据摘要")
    private String evidenceSummary;

    public void setLocateId(Long locateId) 
    {
        this.locateId = locateId;
    }

    public Long getLocateId() 
    {
        return locateId;
    }

    public void setCaseId(Long caseId) 
    {
        this.caseId = caseId;
    }

    public Long getCaseId() 
    {
        return caseId;
    }

    public void setAlgorithmId(Long algorithmId) 
    {
        this.algorithmId = algorithmId;
    }

    public Long getAlgorithmId() 
    {
        return algorithmId;
    }

    public void setAlgorithmName(String algorithmName) 
    {
        this.algorithmName = algorithmName;
    }

    public String getAlgorithmName() 
    {
        return algorithmName;
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

    public void setIsFinal(String isFinal) 
    {
        this.isFinal = isFinal;
    }

    public String getIsFinal() 
    {
        return isFinal;
    }

    public void setLocateReason(String locateReason) 
    {
        this.locateReason = locateReason;
    }

    public String getLocateReason() 
    {
        return locateReason;
    }

    public void setEvidenceSummary(String evidenceSummary) 
    {
        this.evidenceSummary = evidenceSummary;
    }

    public String getEvidenceSummary() 
    {
        return evidenceSummary;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("locateId", getLocateId())
            .append("caseId", getCaseId())
            .append("algorithmId", getAlgorithmId())
            .append("algorithmName", getAlgorithmName())
            .append("partId", getPartId())
            .append("partNo", getPartNo())
            .append("partName", getPartName())
            .append("matchScore", getMatchScore())
            .append("confidence", getConfidence())
            .append("rankNo", getRankNo())
            .append("isFinal", getIsFinal())
            .append("locateReason", getLocateReason())
            .append("evidenceSummary", getEvidenceSummary())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
