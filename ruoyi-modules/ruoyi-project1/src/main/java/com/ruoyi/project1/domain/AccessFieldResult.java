package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 目标字段接入结果对象 p1p_access_field_result
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class AccessFieldResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 目标字段接入结果主键 */
    private Long fieldResultId;

    /** 目标表接入结果 ID */
    @Excel(name = "目标表接入结果 ID")
    private Long tableResultId;

    /** 接入执行批次 ID */
    @Excel(name = "接入执行批次 ID")
    private Long accessBatchId;

    /** 接入计划 ID */
    @Excel(name = "接入计划 ID")
    private Long accessPlanId;

    /** 接入范围字段 ID */
    @Excel(name = "接入范围字段 ID")
    private Long scopeFieldId;

    /** 源字段名 */
    @Excel(name = "源字段名")
    private String sourceColumn;

    /** 目标字段名 */
    @Excel(name = "目标字段名")
    private String targetColumn;

    /** 映射类型：direct 直接写入、rename_cast 重命名转换、lookup_normalize 字典归一 */
    @Excel(name = "映射类型：direct 直接写入、rename_cast 重命名转换、lookup_normalize 字典归一")
    private String mappingType;

    /** 转换状态：pending 待转换、success 成功、failed 失败 */
    @Excel(name = "转换状态：pending 待转换、success 成功、failed 失败")
    private String transformStatus;

    /** 字段转换成功数 */
    @Excel(name = "字段转换成功数")
    private Long successCount;

    /** 字段转换失败数 */
    @Excel(name = "字段转换失败数")
    private Long failedCount;

    /** 字段转换失败原因 */
    @Excel(name = "字段转换失败原因")
    private String errorMessage;

    public void setFieldResultId(Long fieldResultId) 
    {
        this.fieldResultId = fieldResultId;
    }

    public Long getFieldResultId() 
    {
        return fieldResultId;
    }

    public void setTableResultId(Long tableResultId) 
    {
        this.tableResultId = tableResultId;
    }

    public Long getTableResultId() 
    {
        return tableResultId;
    }

    public void setAccessBatchId(Long accessBatchId) 
    {
        this.accessBatchId = accessBatchId;
    }

    public Long getAccessBatchId() 
    {
        return accessBatchId;
    }

    public void setAccessPlanId(Long accessPlanId) 
    {
        this.accessPlanId = accessPlanId;
    }

    public Long getAccessPlanId() 
    {
        return accessPlanId;
    }

    public void setScopeFieldId(Long scopeFieldId) 
    {
        this.scopeFieldId = scopeFieldId;
    }

    public Long getScopeFieldId() 
    {
        return scopeFieldId;
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

    public void setTransformStatus(String transformStatus) 
    {
        this.transformStatus = transformStatus;
    }

    public String getTransformStatus() 
    {
        return transformStatus;
    }

    public void setSuccessCount(Long successCount) 
    {
        this.successCount = successCount;
    }

    public Long getSuccessCount() 
    {
        return successCount;
    }

    public void setFailedCount(Long failedCount) 
    {
        this.failedCount = failedCount;
    }

    public Long getFailedCount() 
    {
        return failedCount;
    }

    public void setErrorMessage(String errorMessage) 
    {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() 
    {
        return errorMessage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("fieldResultId", getFieldResultId())
            .append("tableResultId", getTableResultId())
            .append("accessBatchId", getAccessBatchId())
            .append("accessPlanId", getAccessPlanId())
            .append("scopeFieldId", getScopeFieldId())
            .append("sourceColumn", getSourceColumn())
            .append("targetColumn", getTargetColumn())
            .append("mappingType", getMappingType())
            .append("transformStatus", getTransformStatus())
            .append("successCount", getSuccessCount())
            .append("failedCount", getFailedCount())
            .append("errorMessage", getErrorMessage())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
