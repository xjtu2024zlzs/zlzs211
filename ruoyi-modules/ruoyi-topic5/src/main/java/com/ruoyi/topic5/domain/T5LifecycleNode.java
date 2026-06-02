package com.ruoyi.topic5.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 全生命周期图节点对象 t5_lifecycle_node
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public class T5LifecycleNode extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 图节点ID */
    private Long nodeId;

    /** 全生命周期图模型ID */
    @Excel(name = "全生命周期图模型ID")
    private Long graphId;

    /** 节点编码 */
    @Excel(name = "节点编码")
    private String nodeCode;

    /** 节点名称 */
    @Excel(name = "节点名称")
    private String nodeName;

    /** 节点类型：part/stage/record/parameter/event */
    @Excel(name = "节点类型：part/stage/record/parameter/event")
    private String nodeType;

    /** 生命周期阶段 */
    @Excel(name = "生命周期阶段")
    private String lifecycleStage;

    /** 来源卷宗记录ID */
    @Excel(name = "来源卷宗记录ID")
    private Long sourceRecordId;

    /** 来源表名 */
    @Excel(name = "来源表名")
    private String sourceTable;

    /** 是否异常节点：0否，1是 */
    @Excel(name = "是否异常节点：0否，1是")
    private String abnormalFlag;

    /** 节点权重 */
    @Excel(name = "节点权重")
    private BigDecimal nodeWeight;

    /** 风险分数 */
    @Excel(name = "风险分数")
    private BigDecimal riskScore;

    /** 节点属性JSON */
    @Excel(name = "节点属性JSON")
    private String nodeProperties;

    /** 前端展示分类 */
    @Excel(name = "前端展示分类")
    private String displayCategory;

    public void setNodeId(Long nodeId) 
    {
        this.nodeId = nodeId;
    }

    public Long getNodeId() 
    {
        return nodeId;
    }

    public void setGraphId(Long graphId) 
    {
        this.graphId = graphId;
    }

    public Long getGraphId() 
    {
        return graphId;
    }

    public void setNodeCode(String nodeCode) 
    {
        this.nodeCode = nodeCode;
    }

    public String getNodeCode() 
    {
        return nodeCode;
    }

    public void setNodeName(String nodeName) 
    {
        this.nodeName = nodeName;
    }

    public String getNodeName() 
    {
        return nodeName;
    }

    public void setNodeType(String nodeType) 
    {
        this.nodeType = nodeType;
    }

    public String getNodeType() 
    {
        return nodeType;
    }

    public void setLifecycleStage(String lifecycleStage) 
    {
        this.lifecycleStage = lifecycleStage;
    }

    public String getLifecycleStage() 
    {
        return lifecycleStage;
    }

    public void setSourceRecordId(Long sourceRecordId) 
    {
        this.sourceRecordId = sourceRecordId;
    }

    public Long getSourceRecordId() 
    {
        return sourceRecordId;
    }

    public void setSourceTable(String sourceTable) 
    {
        this.sourceTable = sourceTable;
    }

    public String getSourceTable() 
    {
        return sourceTable;
    }

    public void setAbnormalFlag(String abnormalFlag) 
    {
        this.abnormalFlag = abnormalFlag;
    }

    public String getAbnormalFlag() 
    {
        return abnormalFlag;
    }

    public void setNodeWeight(BigDecimal nodeWeight) 
    {
        this.nodeWeight = nodeWeight;
    }

    public BigDecimal getNodeWeight() 
    {
        return nodeWeight;
    }

    public void setRiskScore(BigDecimal riskScore) 
    {
        this.riskScore = riskScore;
    }

    public BigDecimal getRiskScore() 
    {
        return riskScore;
    }

    public void setNodeProperties(String nodeProperties) 
    {
        this.nodeProperties = nodeProperties;
    }

    public String getNodeProperties() 
    {
        return nodeProperties;
    }

    public void setDisplayCategory(String displayCategory) 
    {
        this.displayCategory = displayCategory;
    }

    public String getDisplayCategory() 
    {
        return displayCategory;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("nodeId", getNodeId())
            .append("graphId", getGraphId())
            .append("nodeCode", getNodeCode())
            .append("nodeName", getNodeName())
            .append("nodeType", getNodeType())
            .append("lifecycleStage", getLifecycleStage())
            .append("sourceRecordId", getSourceRecordId())
            .append("sourceTable", getSourceTable())
            .append("abnormalFlag", getAbnormalFlag())
            .append("nodeWeight", getNodeWeight())
            .append("riskScore", getRiskScore())
            .append("nodeProperties", getNodeProperties())
            .append("displayCategory", getDisplayCategory())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
