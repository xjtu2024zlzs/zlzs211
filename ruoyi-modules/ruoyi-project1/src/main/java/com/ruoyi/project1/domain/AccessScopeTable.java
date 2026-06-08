package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 接入范围源目标关系对象 p1p_access_scope_table
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class AccessScopeTable extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 接入范围源表目标表关系主键 */
    private Long scopeTableId;

    /** 接入计划 ID */
    @Excel(name = "接入计划 ID")
    private Long accessPlanId;

    /** 最终接入规则集 ID */
    @Excel(name = "最终接入规则集 ID")
    private Long specSetId;

    /** 来源数据源 ID */
    @Excel(name = "来源数据源 ID")
    private Long sourceDatasourceId;

    /** 源数据库名 */
    @Excel(name = "源数据库名")
    private String sourceDatabase;

    /** 源表名 */
    @Excel(name = "源表名")
    private String sourceTable;

    /** 目标卷宗表名 */
    @Excel(name = "目标卷宗表名")
    private String targetTable;

    /** 源模式实体 ID */
    @Excel(name = "源模式实体 ID")
    private Long sourceEntityId;

    /** 字段映射数量 */
    @Excel(name = "字段映射数量")
    private Long fieldMappingCount;

    /** 范围状态：active 生效、disabled 停用 */
    @Excel(name = "范围状态：active 生效、disabled 停用")
    private String scopeStatus;

    public void setScopeTableId(Long scopeTableId) 
    {
        this.scopeTableId = scopeTableId;
    }

    public Long getScopeTableId() 
    {
        return scopeTableId;
    }

    public void setAccessPlanId(Long accessPlanId) 
    {
        this.accessPlanId = accessPlanId;
    }

    public Long getAccessPlanId() 
    {
        return accessPlanId;
    }

    public void setSpecSetId(Long specSetId) 
    {
        this.specSetId = specSetId;
    }

    public Long getSpecSetId() 
    {
        return specSetId;
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

    public void setTargetTable(String targetTable) 
    {
        this.targetTable = targetTable;
    }

    public String getTargetTable() 
    {
        return targetTable;
    }

    public void setSourceEntityId(Long sourceEntityId) 
    {
        this.sourceEntityId = sourceEntityId;
    }

    public Long getSourceEntityId() 
    {
        return sourceEntityId;
    }

    public void setFieldMappingCount(Long fieldMappingCount) 
    {
        this.fieldMappingCount = fieldMappingCount;
    }

    public Long getFieldMappingCount() 
    {
        return fieldMappingCount;
    }

    public void setScopeStatus(String scopeStatus) 
    {
        this.scopeStatus = scopeStatus;
    }

    public String getScopeStatus() 
    {
        return scopeStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("scopeTableId", getScopeTableId())
            .append("accessPlanId", getAccessPlanId())
            .append("specSetId", getSpecSetId())
            .append("sourceDatasourceId", getSourceDatasourceId())
            .append("sourceDatabase", getSourceDatabase())
            .append("sourceTable", getSourceTable())
            .append("targetTable", getTargetTable())
            .append("sourceEntityId", getSourceEntityId())
            .append("fieldMappingCount", getFieldMappingCount())
            .append("scopeStatus", getScopeStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
