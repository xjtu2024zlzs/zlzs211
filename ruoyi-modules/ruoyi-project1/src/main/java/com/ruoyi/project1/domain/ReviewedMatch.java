package com.ruoyi.project1.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 审核后的字段匹配结果对象 p1p_reviewed_match
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class ReviewedMatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 审核记录主键 */
    private Long reviewId;

    /** 任务 ID */
    @Excel(name = "任务 ID")
    private Long taskId;

    /** 任务版本 ID */
    @Excel(name = "任务版本 ID")
    private Long versionId;

    /** 运行记录 ID */
    @Excel(name = "运行记录 ID")
    private Long recordId;

    /** 结果集 ID */
    @Excel(name = "结果集 ID")
    private Long resultSetId;

    /** 结果明细 ID */
    @Excel(name = "结果明细 ID")
    private Long resultRowId;

    /** 来源数据源 ID */
    @Excel(name = "来源数据源 ID")
    private Long sourceDatasourceId;

    /** 源数据库名 */
    @Excel(name = "源数据库名")
    private String sourceDatabase;

    /** 源表名 */
    @Excel(name = "源表名")
    private String sourceTable;

    /** 源字段名 */
    @Excel(name = "源字段名")
    private String sourceColumn;

    /** 推荐目标表 */
    @Excel(name = "推荐目标表")
    private String proposedTargetTable;

    /** 推荐目标字段 */
    @Excel(name = "推荐目标字段")
    private String proposedTargetColumn;

    /** 审核确认目标表 */
    @Excel(name = "审核确认目标表")
    private String targetTable;

    /** 审核确认目标字段 */
    @Excel(name = "审核确认目标字段")
    private String targetColumn;

    /** 映射类型，如 direct 直接写入、rename_cast 重命名转换、lookup_normalize 字典归一 */
    @Excel(name = "映射类型，如 direct 直接写入、rename_cast 重命名转换、lookup_normalize 字典归一")
    private String mappingType;

    /** 算法匹配分数 */
    @Excel(name = "算法匹配分数")
    private BigDecimal algorithmScore;

    /** 审核状态：pending 待审核、auto_approved 自动通过、approved 审核通过、rejected 审核未通过 */
    @Excel(name = "审核状态：pending 待审核、auto_approved 自动通过、approved 审核通过、rejected 审核未通过")
    private String reviewStatus;

    /** 审核人 */
    @Excel(name = "审核人")
    private String reviewedBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date reviewedAt;

    public void setReviewId(Long reviewId) 
    {
        this.reviewId = reviewId;
    }

    public Long getReviewId() 
    {
        return reviewId;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setVersionId(Long versionId) 
    {
        this.versionId = versionId;
    }

    public Long getVersionId() 
    {
        return versionId;
    }

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }

    public void setResultSetId(Long resultSetId) 
    {
        this.resultSetId = resultSetId;
    }

    public Long getResultSetId() 
    {
        return resultSetId;
    }

    public void setResultRowId(Long resultRowId) 
    {
        this.resultRowId = resultRowId;
    }

    public Long getResultRowId() 
    {
        return resultRowId;
    }

    public void setSourceDatasourceId(Long sourceDatasourceId) 
    {
        this.sourceDatasourceId = sourceDatasourceId;
    }

    public Long getSourceDatasourceId() 
    {
        return sourceDatasourceId;
    }

    public void setSourceDatabase(String sourceDatabase) 
    {
        this.sourceDatabase = sourceDatabase;
    }

    public String getSourceDatabase() 
    {
        return sourceDatabase;
    }

    public void setSourceTable(String sourceTable) 
    {
        this.sourceTable = sourceTable;
    }

    public String getSourceTable() 
    {
        return sourceTable;
    }

    public void setSourceColumn(String sourceColumn) 
    {
        this.sourceColumn = sourceColumn;
    }

    public String getSourceColumn() 
    {
        return sourceColumn;
    }

    public void setProposedTargetTable(String proposedTargetTable) 
    {
        this.proposedTargetTable = proposedTargetTable;
    }

    public String getProposedTargetTable() 
    {
        return proposedTargetTable;
    }

    public void setProposedTargetColumn(String proposedTargetColumn) 
    {
        this.proposedTargetColumn = proposedTargetColumn;
    }

    public String getProposedTargetColumn() 
    {
        return proposedTargetColumn;
    }

    public void setTargetTable(String targetTable) 
    {
        this.targetTable = targetTable;
    }

    public String getTargetTable() 
    {
        return targetTable;
    }

    public void setTargetColumn(String targetColumn) 
    {
        this.targetColumn = targetColumn;
    }

    public String getTargetColumn() 
    {
        return targetColumn;
    }

    public void setMappingType(String mappingType) 
    {
        this.mappingType = mappingType;
    }

    public String getMappingType() 
    {
        return mappingType;
    }

    public void setAlgorithmScore(BigDecimal algorithmScore) 
    {
        this.algorithmScore = algorithmScore;
    }

    public BigDecimal getAlgorithmScore() 
    {
        return algorithmScore;
    }

    public void setReviewStatus(String reviewStatus) 
    {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewStatus() 
    {
        return reviewStatus;
    }

    public void setReviewedBy(String reviewedBy) 
    {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewedBy() 
    {
        return reviewedBy;
    }

    public void setReviewedAt(Date reviewedAt) 
    {
        this.reviewedAt = reviewedAt;
    }

    public Date getReviewedAt() 
    {
        return reviewedAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("reviewId", getReviewId())
            .append("taskId", getTaskId())
            .append("versionId", getVersionId())
            .append("recordId", getRecordId())
            .append("resultSetId", getResultSetId())
            .append("resultRowId", getResultRowId())
            .append("sourceDatasourceId", getSourceDatasourceId())
            .append("sourceDatabase", getSourceDatabase())
            .append("sourceTable", getSourceTable())
            .append("sourceColumn", getSourceColumn())
            .append("proposedTargetTable", getProposedTargetTable())
            .append("proposedTargetColumn", getProposedTargetColumn())
            .append("targetTable", getTargetTable())
            .append("targetColumn", getTargetColumn())
            .append("mappingType", getMappingType())
            .append("algorithmScore", getAlgorithmScore())
            .append("reviewStatus", getReviewStatus())
            .append("reviewedBy", getReviewedBy())
            .append("reviewedAt", getReviewedAt())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
