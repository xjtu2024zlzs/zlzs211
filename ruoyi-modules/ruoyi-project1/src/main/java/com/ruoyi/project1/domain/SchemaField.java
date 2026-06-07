package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 模式字段对象 p1p_schema_field
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class SchemaField extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模式字段主键 */
    private Long fieldId;

    /** 模式实体 ID */
    @Excel(name = "模式实体 ID")
    private Long entityId;

    /** 模式快照 ID */
    @Excel(name = "模式快照 ID")
    private Long snapshotId;

    /** 字段名称 */
    @Excel(name = "字段名称")
    private String fieldName;

    /** 源字段名 */
    @Excel(name = "源字段名")
    private String sourceColumn;

    /** 字段类型 */
    @Excel(name = "字段类型")
    private String dataType;

    /** 字段顺序号 */
    @Excel(name = "字段顺序号")
    private Long ordinalPosition;

    /** 是否主键：1是，0否 */
    @Excel(name = "是否主键：1是，0否")
    private Integer isPrimaryKey;

    /** 是否可空：1可空，0不可空 */
    @Excel(name = "是否可空：1可空，0不可空")
    private Integer isNullable;

    /** 字段样本值 JSON */
    @Excel(name = "字段样本值 JSON")
    private String sampleValues;

    /** 源字段注释 */
    @Excel(name = "源字段注释")
    private String fieldComment;

    public void setFieldId(Long fieldId) 
    {
        this.fieldId = fieldId;
    }

    public Long getFieldId() 
    {
        return fieldId;
    }

    public void setEntityId(Long entityId) 
    {
        this.entityId = entityId;
    }

    public Long getEntityId() 
    {
        return entityId;
    }

    public void setSnapshotId(Long snapshotId) 
    {
        this.snapshotId = snapshotId;
    }

    public Long getSnapshotId() 
    {
        return snapshotId;
    }

    public void setFieldName(String fieldName) 
    {
        this.fieldName = fieldName;
    }

    public String getFieldName() 
    {
        return fieldName;
    }

    public void setSourceColumn(String sourceColumn) 
    {
        this.sourceColumn = sourceColumn;
    }

    public String getSourceColumn() 
    {
        return sourceColumn;
    }

    public void setDataType(String dataType) 
    {
        this.dataType = dataType;
    }

    public String getDataType() 
    {
        return dataType;
    }

    public void setOrdinalPosition(Long ordinalPosition) 
    {
        this.ordinalPosition = ordinalPosition;
    }

    public Long getOrdinalPosition() 
    {
        return ordinalPosition;
    }

    public void setIsPrimaryKey(Integer isPrimaryKey) 
    {
        this.isPrimaryKey = isPrimaryKey;
    }

    public Integer getIsPrimaryKey() 
    {
        return isPrimaryKey;
    }

    public void setIsNullable(Integer isNullable) 
    {
        this.isNullable = isNullable;
    }

    public Integer getIsNullable() 
    {
        return isNullable;
    }

    public void setSampleValues(String sampleValues) 
    {
        this.sampleValues = sampleValues;
    }

    public String getSampleValues() 
    {
        return sampleValues;
    }

    public void setFieldComment(String fieldComment) 
    {
        this.fieldComment = fieldComment;
    }

    public String getFieldComment() 
    {
        return fieldComment;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("fieldId", getFieldId())
            .append("entityId", getEntityId())
            .append("snapshotId", getSnapshotId())
            .append("fieldName", getFieldName())
            .append("sourceColumn", getSourceColumn())
            .append("dataType", getDataType())
            .append("ordinalPosition", getOrdinalPosition())
            .append("isPrimaryKey", getIsPrimaryKey())
            .append("isNullable", getIsNullable())
            .append("sampleValues", getSampleValues())
            .append("fieldComment", getFieldComment())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
