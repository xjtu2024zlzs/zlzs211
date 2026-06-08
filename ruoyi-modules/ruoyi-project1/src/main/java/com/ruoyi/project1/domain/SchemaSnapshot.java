package com.ruoyi.project1.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 数据源模式快照对象 p1p_schema_snapshot
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class SchemaSnapshot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模式快照主键 */
    private Long snapshotId;

    /** 来源数据源 ID */
    @Excel(name = "来源数据源 ID")
    private Long datasourceId;

    /** 模式快照名称 */
    @Excel(name = "模式快照名称")
    private String snapshotName;

    /** 读取状态：success 成功、failed 失败 */
    @Excel(name = "读取状态：success 成功、failed 失败")
    private String readStatus;

    /** 实体数量 */
    @Excel(name = "实体数量")
    private Long entityCount;

    /** 字段数量 */
    @Excel(name = "字段数量")
    private Long fieldCount;

    /** 完整模式快照 JSON */
    @Excel(name = "完整模式快照 JSON")
    private String schemaJson;

    /** 读取失败原因 */
    @Excel(name = "读取失败原因")
    private String errorMessage;

    /** 读取操作人 */
    @Excel(name = "读取操作人")
    private String readBy;

    /** 读取时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "读取时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date readTime;

    public void setSnapshotId(Long snapshotId) 
    {
        this.snapshotId = snapshotId;
    }

    public Long getSnapshotId() 
    {
        return snapshotId;
    }

    public void setDatasourceId(Long datasourceId) 
    {
        this.datasourceId = datasourceId;
    }

    public Long getDatasourceId() 
    {
        return datasourceId;
    }

    public void setSnapshotName(String snapshotName) 
    {
        this.snapshotName = snapshotName;
    }

    public String getSnapshotName() 
    {
        return snapshotName;
    }

    public void setReadStatus(String readStatus) 
    {
        this.readStatus = readStatus;
    }

    public String getReadStatus() 
    {
        return readStatus;
    }

    public void setEntityCount(Long entityCount) 
    {
        this.entityCount = entityCount;
    }

    public Long getEntityCount() 
    {
        return entityCount;
    }

    public void setFieldCount(Long fieldCount) 
    {
        this.fieldCount = fieldCount;
    }

    public Long getFieldCount() 
    {
        return fieldCount;
    }

    public void setSchemaJson(String schemaJson) 
    {
        this.schemaJson = schemaJson;
    }

    public String getSchemaJson() 
    {
        return schemaJson;
    }

    public void setErrorMessage(String errorMessage) 
    {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() 
    {
        return errorMessage;
    }

    public void setReadBy(String readBy) 
    {
        this.readBy = readBy;
    }

    public String getReadBy() 
    {
        return readBy;
    }

    public void setReadTime(Date readTime) 
    {
        this.readTime = readTime;
    }

    public Date getReadTime() 
    {
        return readTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("snapshotId", getSnapshotId())
            .append("datasourceId", getDatasourceId())
            .append("snapshotName", getSnapshotName())
            .append("readStatus", getReadStatus())
            .append("entityCount", getEntityCount())
            .append("fieldCount", getFieldCount())
            .append("schemaJson", getSchemaJson())
            .append("errorMessage", getErrorMessage())
            .append("readBy", getReadBy())
            .append("readTime", getReadTime())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
