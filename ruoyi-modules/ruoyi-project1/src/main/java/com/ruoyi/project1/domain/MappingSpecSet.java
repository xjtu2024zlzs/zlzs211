package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 最终接入规则集对象 p1p_mapping_spec_set
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class MappingSpecSet extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 最终接入规则集主键 */
    private Long specSetId;

    /** 规则集名称 */
    @Excel(name = "规则集名称")
    private String specSetName;

    /** 来源原始结果集 ID，用于追溯算法输出 */
    @Excel(name = "来源原始结果集 ID，用于追溯算法输出")
    private Long resultSetId;

    /** 来源数据源 ID */
    @Excel(name = "来源数据源 ID")
    private Long sourceDatasourceId;

    /** 任务 ID */
    @Excel(name = "任务 ID")
    private Long taskId;

    /** 任务版本 ID */
    @Excel(name = "任务版本 ID")
    private Long versionId;

    /** 运行记录 ID */
    @Excel(name = "运行记录 ID")
    private Long recordId;

    /** 最终接入规则数量 */
    @Excel(name = "最终接入规则数量")
    private Long ruleCount;

    /** 是否当前默认接入规则集：1是，0否 */
    @Excel(name = "是否当前默认接入规则集：1是，0否")
    private Integer isDefault;

    /** 规则集状态：draft 草稿、active 生效、disabled 停用 */
    @Excel(name = "规则集状态：draft 草稿、active 生效、disabled 停用")
    private String specStatus;

    public void setSpecSetId(Long specSetId) 
    {
        this.specSetId = specSetId;
    }

    public Long getSpecSetId() 
    {
        return specSetId;
    }

    public void setSpecSetName(String specSetName) 
    {
        this.specSetName = specSetName;
    }

    public String getSpecSetName() 
    {
        return specSetName;
    }

    public void setResultSetId(Long resultSetId) 
    {
        this.resultSetId = resultSetId;
    }

    public Long getResultSetId() 
    {
        return resultSetId;
    }

    public void setSourceDatasourceId(Long sourceDatasourceId) 
    {
        this.sourceDatasourceId = sourceDatasourceId;
    }

    public Long getSourceDatasourceId() 
    {
        return sourceDatasourceId;
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

    public void setRuleCount(Long ruleCount) 
    {
        this.ruleCount = ruleCount;
    }

    public Long getRuleCount() 
    {
        return ruleCount;
    }

    public void setIsDefault(Integer isDefault) 
    {
        this.isDefault = isDefault;
    }

    public Integer getIsDefault() 
    {
        return isDefault;
    }

    public void setSpecStatus(String specStatus) 
    {
        this.specStatus = specStatus;
    }

    public String getSpecStatus() 
    {
        return specStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("specSetId", getSpecSetId())
            .append("specSetName", getSpecSetName())
            .append("resultSetId", getResultSetId())
            .append("sourceDatasourceId", getSourceDatasourceId())
            .append("taskId", getTaskId())
            .append("versionId", getVersionId())
            .append("recordId", getRecordId())
            .append("ruleCount", getRuleCount())
            .append("isDefault", getIsDefault())
            .append("specStatus", getSpecStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
