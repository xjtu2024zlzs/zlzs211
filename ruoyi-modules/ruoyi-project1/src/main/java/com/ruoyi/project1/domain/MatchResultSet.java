package com.ruoyi.project1.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 模式映射结果展示对象 p1p_match_result_set
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class MatchResultSet extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 结果集主键 */
    private Long resultSetId;

    /** 结果版本 ID */
    @Excel(name = "结果版本 ID")
    private Long resultVersionId;

    /** 任务 ID */
    @Excel(name = "任务 ID")
    private Long taskId;

    /** 任务版本 ID */
    @Excel(name = "任务版本 ID")
    private Long versionId;

    /** 运行记录 ID */
    @Excel(name = "运行记录 ID")
    private Long recordId;

    /** 来源数据源 ID */
    @Excel(name = "来源数据源 ID")
    private Long sourceDatasourceId;

    /** 匹配方法 */
    @Excel(name = "匹配方法")
    private String method;

    /** 方法变体 */
    @Excel(name = "方法变体")
    private String variant;

    /** 结果集名称 */
    @Excel(name = "结果集名称")
    private String resultSetName;

    /** 是否候选默认结果集：1是，0否；接入执行仍以最终接入规则集为准 */
    @Excel(name = "是否候选默认结果集：1是，0否；接入执行仍以最终接入规则集为准")
    private Integer isDefault;

    /** 结果行数 */
    @Excel(name = "结果行数")
    private Long totalRows;

    /** 平均匹配分数 */
    @Excel(name = "平均匹配分数")
    private BigDecimal avgScore;

    public void setResultSetId(Long resultSetId) 
    {
        this.resultSetId = resultSetId;
    }

    public Long getResultSetId() 
    {
        return resultSetId;
    }

    public void setResultVersionId(Long resultVersionId) 
    {
        this.resultVersionId = resultVersionId;
    }

    public Long getResultVersionId() 
    {
        return resultVersionId;
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

    public void setSourceDatasourceId(Long sourceDatasourceId) 
    {
        this.sourceDatasourceId = sourceDatasourceId;
    }

    public Long getSourceDatasourceId() 
    {
        return sourceDatasourceId;
    }

    public void setMethod(String method) 
    {
        this.method = method;
    }

    public String getMethod() 
    {
        return method;
    }

    public void setVariant(String variant) 
    {
        this.variant = variant;
    }

    public String getVariant() 
    {
        return variant;
    }

    public void setResultSetName(String resultSetName) 
    {
        this.resultSetName = resultSetName;
    }

    public String getResultSetName() 
    {
        return resultSetName;
    }

    public void setIsDefault(Integer isDefault) 
    {
        this.isDefault = isDefault;
    }

    public Integer getIsDefault() 
    {
        return isDefault;
    }

    public void setTotalRows(Long totalRows) 
    {
        this.totalRows = totalRows;
    }

    public Long getTotalRows() 
    {
        return totalRows;
    }

    public void setAvgScore(BigDecimal avgScore) 
    {
        this.avgScore = avgScore;
    }

    public BigDecimal getAvgScore() 
    {
        return avgScore;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("resultSetId", getResultSetId())
            .append("resultVersionId", getResultVersionId())
            .append("taskId", getTaskId())
            .append("versionId", getVersionId())
            .append("recordId", getRecordId())
            .append("sourceDatasourceId", getSourceDatasourceId())
            .append("method", getMethod())
            .append("variant", getVariant())
            .append("resultSetName", getResultSetName())
            .append("isDefault", getIsDefault())
            .append("totalRows", getTotalRows())
            .append("avgScore", getAvgScore())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
