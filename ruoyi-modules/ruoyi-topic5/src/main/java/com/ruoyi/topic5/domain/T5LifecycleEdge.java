package com.ruoyi.topic5.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 全生命周期图关系边对象 t5_lifecycle_edge
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public class T5LifecycleEdge extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 图关系边ID */
    private Long edgeId;

    /** 全生命周期图模型ID */
    @Excel(name = "全生命周期图模型ID")
    private Long graphId;

    /** 起始节点ID */
    @Excel(name = "起始节点ID")
    private Long sourceNodeId;

    /** 目标节点ID */
    @Excel(name = "目标节点ID")
    private Long targetNodeId;

    /** 关系编码 */
    @Excel(name = "关系编码")
    private String relationCode;

    /** 关系名称 */
    @Excel(name = "关系名称")
    private String relationName;

    /** 关系类型：has_stage/has_record/has_parameter/affects/related_to */
    @Excel(name = "关系类型：has_stage/has_record/has_parameter/affects/related_to")
    private String relationType;

    /** 关系权重 */
    @Excel(name = "关系权重")
    private BigDecimal edgeWeight;

    /** 关系置信度 */
    @Excel(name = "关系置信度")
    private BigDecimal confidence;

    /** 来源卷宗记录ID */
    @Excel(name = "来源卷宗记录ID")
    private Long sourceRecordId;

    /** 关系属性JSON */
    @Excel(name = "关系属性JSON")
    private String edgeProperties;

    public void setEdgeId(Long edgeId) 
    {
        this.edgeId = edgeId;
    }

    public Long getEdgeId() 
    {
        return edgeId;
    }

    public void setGraphId(Long graphId) 
    {
        this.graphId = graphId;
    }

    public Long getGraphId() 
    {
        return graphId;
    }

    public void setSourceNodeId(Long sourceNodeId) 
    {
        this.sourceNodeId = sourceNodeId;
    }

    public Long getSourceNodeId() 
    {
        return sourceNodeId;
    }

    public void setTargetNodeId(Long targetNodeId) 
    {
        this.targetNodeId = targetNodeId;
    }

    public Long getTargetNodeId() 
    {
        return targetNodeId;
    }

    public void setRelationCode(String relationCode) 
    {
        this.relationCode = relationCode;
    }

    public String getRelationCode() 
    {
        return relationCode;
    }

    public void setRelationName(String relationName) 
    {
        this.relationName = relationName;
    }

    public String getRelationName() 
    {
        return relationName;
    }

    public void setRelationType(String relationType) 
    {
        this.relationType = relationType;
    }

    public String getRelationType() 
    {
        return relationType;
    }

    public void setEdgeWeight(BigDecimal edgeWeight) 
    {
        this.edgeWeight = edgeWeight;
    }

    public BigDecimal getEdgeWeight() 
    {
        return edgeWeight;
    }

    public void setConfidence(BigDecimal confidence) 
    {
        this.confidence = confidence;
    }

    public BigDecimal getConfidence() 
    {
        return confidence;
    }

    public void setSourceRecordId(Long sourceRecordId) 
    {
        this.sourceRecordId = sourceRecordId;
    }

    public Long getSourceRecordId() 
    {
        return sourceRecordId;
    }

    public void setEdgeProperties(String edgeProperties) 
    {
        this.edgeProperties = edgeProperties;
    }

    public String getEdgeProperties() 
    {
        return edgeProperties;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("edgeId", getEdgeId())
            .append("graphId", getGraphId())
            .append("sourceNodeId", getSourceNodeId())
            .append("targetNodeId", getTargetNodeId())
            .append("relationCode", getRelationCode())
            .append("relationName", getRelationName())
            .append("relationType", getRelationType())
            .append("edgeWeight", getEdgeWeight())
            .append("confidence", getConfidence())
            .append("sourceRecordId", getSourceRecordId())
            .append("edgeProperties", getEdgeProperties())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
