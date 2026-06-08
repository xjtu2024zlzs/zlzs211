package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 接入范围字段对象 p1p_access_scope_field
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class AccessScopeField extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 接入范围字段主键 */
    private Long scopeFieldId;

    /** 接入范围目标表 ID */
    @Excel(name = "接入范围目标表 ID")
    private Long scopeTableId;

    /** 接入计划 ID */
    @Excel(name = "接入计划 ID")
    private Long accessPlanId;

    /** 最终映射规则 ID */
    @Excel(name = "最终映射规则 ID")
    private Long mappingId;

    /** 源模式字段 ID */
    @Excel(name = "源模式字段 ID")
    private Long sourceFieldId;

    /** 源字段名 */
    @Excel(name = "源字段名")
    private String sourceColumn;

    /** 目标字段名 */
    @Excel(name = "目标字段名")
    private String targetColumn;

    /** 映射类型：direct 直接写入、rename_cast 重命名转换、lookup_normalize 字典归一 */
    @Excel(name = "映射类型：direct 直接写入、rename_cast 重命名转换、lookup_normalize 字典归一")
    private String mappingType;

    /** 字段转换规则 JSON */
    @Excel(name = "字段转换规则 JSON")
    private String transformRule;

    /** 字段范围状态：active 生效、disabled 停用 */
    @Excel(name = "字段范围状态：active 生效、disabled 停用")
    private String fieldStatus;

    public void setScopeFieldId(Long scopeFieldId) 
    {
        this.scopeFieldId = scopeFieldId;
    }

    public Long getScopeFieldId() 
    {
        return scopeFieldId;
    }

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

    public void setMappingId(Long mappingId) 
    {
        this.mappingId = mappingId;
    }

    public Long getMappingId() 
    {
        return mappingId;
    }

    public void setSourceFieldId(Long sourceFieldId) 
    {
        this.sourceFieldId = sourceFieldId;
    }

    public Long getSourceFieldId() 
    {
        return sourceFieldId;
    }

    public void setSourceColumn(String sourceColumn) 
    {
        this.sourceColumn = sourceColumn;
    }

    public String getSourceColumn() 
    {
        return sourceColumn;
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

    public void setTransformRule(String transformRule) 
    {
        this.transformRule = transformRule;
    }

    public String getTransformRule() 
    {
        return transformRule;
    }

    public void setFieldStatus(String fieldStatus) 
    {
        this.fieldStatus = fieldStatus;
    }

    public String getFieldStatus() 
    {
        return fieldStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("scopeFieldId", getScopeFieldId())
            .append("scopeTableId", getScopeTableId())
            .append("accessPlanId", getAccessPlanId())
            .append("mappingId", getMappingId())
            .append("sourceFieldId", getSourceFieldId())
            .append("sourceColumn", getSourceColumn())
            .append("targetColumn", getTargetColumn())
            .append("mappingType", getMappingType())
            .append("transformRule", getTransformRule())
            .append("fieldStatus", getFieldStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
