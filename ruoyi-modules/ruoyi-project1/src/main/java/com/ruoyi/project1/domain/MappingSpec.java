package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 最终接入字段规则对象 p1p_mapping_spec
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class MappingSpec extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 映射规则主键 */
    private Long mappingId;

    /** 最终接入规则集 ID */
    @Excel(name = "最终接入规则集 ID")
    private Long specSetId;

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

    /** 审核记录 ID */
    @Excel(name = "审核记录 ID")
    private Long reviewId;

    /** 源数据库名 */
    @Excel(name = "源数据库名")
    private String sourceDatabase;

    /** 源表名 */
    @Excel(name = "源表名")
    private String sourceTable;

    /** 源字段名 */
    @Excel(name = "源字段名")
    private String sourceColumn;

    /** 目标表名 */
    @Excel(name = "目标表名")
    private String targetTable;

    /** 目标字段名 */
    @Excel(name = "目标字段名")
    private String targetColumn;

    /** 映射类型：direct 直接写入、rename_cast 重命名转换、lookup_normalize 字典归一 */
    @Excel(name = "映射类型：direct 直接写入、rename_cast 重命名转换、lookup_normalize 字典归一")
    private String mappingType;

    /** 字段转换规则 JSON */
    @Excel(name = "字段转换规则 JSON")
    private String transformRule;

    /** 规则状态：draft 草稿、approved 已确认、blocked 阻断 */
    @Excel(name = "规则状态：draft 草稿、approved 已确认、blocked 阻断")
    private String specStatus;

    /** 目标字段装载顺序 */
    @Excel(name = "目标字段装载顺序")
    private Long loadOrder;

    public void setMappingId(Long mappingId) 
    {
        this.mappingId = mappingId;
    }

    public Long getMappingId() 
    {
        return mappingId;
    }

    public void setSpecSetId(Long specSetId) 
    {
        this.specSetId = specSetId;
    }

    public Long getSpecSetId() 
    {
        return specSetId;
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

    public void setReviewId(Long reviewId) 
    {
        this.reviewId = reviewId;
    }

    public Long getReviewId() 
    {
        return reviewId;
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

    public void setTransformRule(String transformRule) 
    {
        this.transformRule = transformRule;
    }

    public String getTransformRule() 
    {
        return transformRule;
    }

    public void setSpecStatus(String specStatus) 
    {
        this.specStatus = specStatus;
    }

    public String getSpecStatus() 
    {
        return specStatus;
    }

    public void setLoadOrder(Long loadOrder) 
    {
        this.loadOrder = loadOrder;
    }

    public Long getLoadOrder() 
    {
        return loadOrder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("mappingId", getMappingId())
            .append("specSetId", getSpecSetId())
            .append("taskId", getTaskId())
            .append("versionId", getVersionId())
            .append("recordId", getRecordId())
            .append("sourceDatasourceId", getSourceDatasourceId())
            .append("reviewId", getReviewId())
            .append("sourceDatabase", getSourceDatabase())
            .append("sourceTable", getSourceTable())
            .append("sourceColumn", getSourceColumn())
            .append("targetTable", getTargetTable())
            .append("targetColumn", getTargetColumn())
            .append("mappingType", getMappingType())
            .append("transformRule", getTransformRule())
            .append("specStatus", getSpecStatus())
            .append("loadOrder", getLoadOrder())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
