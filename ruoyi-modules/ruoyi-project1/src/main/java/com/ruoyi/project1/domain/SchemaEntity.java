package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 模式实体对象 p1p_schema_entity
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class SchemaEntity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模式实体主键 */
    private Long entityId;

    /** 模式快照 ID */
    @Excel(name = "模式快照 ID")
    private Long snapshotId;

    /** 来源数据源 ID */
    @Excel(name = "来源数据源 ID")
    private Long datasourceId;

    /** 实体名称，通常为源表名或接口实体名 */
    @Excel(name = "实体名称，通常为源表名或接口实体名")
    private String entityName;

    /** 源数据库名 */
    @Excel(name = "源数据库名")
    private String sourceDatabase;

    /** 源 Schema 名称 */
    @Excel(name = "源 Schema 名称")
    private String sourceSchema;

    /** 源表名 */
    @Excel(name = "源表名")
    private String sourceTable;

    /** 实体类型：table 表、view 视图、api API 实体 */
    @Excel(name = "实体类型：table 表、view 视图、api API 实体")
    private String entityType;

    /** 估算行数或样本行数 */
    @Excel(name = "估算行数或样本行数")
    private Long rowCount;

    /** 实体元数据 JSON */
    @Excel(name = "实体元数据 JSON")
    private String metadata;

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

    public void setDatasourceId(Long datasourceId) 
    {
        this.datasourceId = datasourceId;
    }

    public Long getDatasourceId() 
    {
        return datasourceId;
    }

    public void setEntityName(String entityName) 
    {
        this.entityName = entityName;
    }

    public String getEntityName() 
    {
        return entityName;
    }

    public void setSourceDatabase(String sourceDatabase) 
    {
        this.sourceDatabase = sourceDatabase;
    }

    public String getSourceDatabase() 
    {
        return sourceDatabase;
    }

    public void setSourceSchema(String sourceSchema) 
    {
        this.sourceSchema = sourceSchema;
    }

    public String getSourceSchema() 
    {
        return sourceSchema;
    }

    public void setSourceTable(String sourceTable) 
    {
        this.sourceTable = sourceTable;
    }

    public String getSourceTable() 
    {
        return sourceTable;
    }

    public void setEntityType(String entityType) 
    {
        this.entityType = entityType;
    }

    public String getEntityType() 
    {
        return entityType;
    }

    public void setRowCount(Long rowCount) 
    {
        this.rowCount = rowCount;
    }

    public Long getRowCount() 
    {
        return rowCount;
    }

    public void setMetadata(String metadata) 
    {
        this.metadata = metadata;
    }

    public String getMetadata() 
    {
        return metadata;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("entityId", getEntityId())
            .append("snapshotId", getSnapshotId())
            .append("datasourceId", getDatasourceId())
            .append("entityName", getEntityName())
            .append("sourceDatabase", getSourceDatabase())
            .append("sourceSchema", getSourceSchema())
            .append("sourceTable", getSourceTable())
            .append("entityType", getEntityType())
            .append("rowCount", getRowCount())
            .append("metadata", getMetadata())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
