package com.ruoyi.topic5.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 全生命周期关联模型对象 t5_lifecycle_graph
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public class T5LifecycleGraph extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 全生命周期图模型ID */
    private Long graphId;

    /** 图模型编号 */
    @Excel(name = "图模型编号")
    private String graphNo;

    /** 图模型名称 */
    @Excel(name = "图模型名称")
    private String graphName;

    /** 追溯案例ID */
    @Excel(name = "追溯案例ID")
    private Long caseId;

    /** 零部件实例ID */
    @Excel(name = "零部件实例ID")
    private Long partId;

    /** 零件编号 */
    @Excel(name = "零件编号")
    private String partNo;

    /** 零件名称 */
    @Excel(name = "零件名称")
    private String partName;

    /** 建模算法ID */
    @Excel(name = "建模算法ID")
    private Long buildAlgorithmId;

    /** 建模算法名称 */
    @Excel(name = "建模算法名称")
    private String buildAlgorithmName;

    /** 图模型类型 */
    @Excel(name = "图模型类型")
    private String graphType;

    /** 节点数量 */
    @Excel(name = "节点数量")
    private Long nodeCount;

    /** 关系边数量 */
    @Excel(name = "关系边数量")
    private Long edgeCount;

    /** 构建状态：0待构建，1构建中，2成功，3失败 */
    @Excel(name = "构建状态：0待构建，1构建中，2成功，3失败")
    private String buildStatus;

    /** 构建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "构建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date buildTime;

    /** 图模型摘要 */
    @Excel(name = "图模型摘要")
    private String graphSummary;

    public void setGraphId(Long graphId) 
    {
        this.graphId = graphId;
    }

    public Long getGraphId() 
    {
        return graphId;
    }

    public void setGraphNo(String graphNo) 
    {
        this.graphNo = graphNo;
    }

    public String getGraphNo() 
    {
        return graphNo;
    }

    public void setGraphName(String graphName) 
    {
        this.graphName = graphName;
    }

    public String getGraphName() 
    {
        return graphName;
    }

    public void setCaseId(Long caseId) 
    {
        this.caseId = caseId;
    }

    public Long getCaseId() 
    {
        return caseId;
    }

    public void setPartId(Long partId) 
    {
        this.partId = partId;
    }

    public Long getPartId() 
    {
        return partId;
    }

    public void setPartNo(String partNo) 
    {
        this.partNo = partNo;
    }

    public String getPartNo() 
    {
        return partNo;
    }

    public void setPartName(String partName) 
    {
        this.partName = partName;
    }

    public String getPartName() 
    {
        return partName;
    }

    public void setBuildAlgorithmId(Long buildAlgorithmId) 
    {
        this.buildAlgorithmId = buildAlgorithmId;
    }

    public Long getBuildAlgorithmId() 
    {
        return buildAlgorithmId;
    }

    public void setBuildAlgorithmName(String buildAlgorithmName) 
    {
        this.buildAlgorithmName = buildAlgorithmName;
    }

    public String getBuildAlgorithmName() 
    {
        return buildAlgorithmName;
    }

    public void setGraphType(String graphType) 
    {
        this.graphType = graphType;
    }

    public String getGraphType() 
    {
        return graphType;
    }

    public void setNodeCount(Long nodeCount) 
    {
        this.nodeCount = nodeCount;
    }

    public Long getNodeCount() 
    {
        return nodeCount;
    }

    public void setEdgeCount(Long edgeCount) 
    {
        this.edgeCount = edgeCount;
    }

    public Long getEdgeCount() 
    {
        return edgeCount;
    }

    public void setBuildStatus(String buildStatus) 
    {
        this.buildStatus = buildStatus;
    }

    public String getBuildStatus() 
    {
        return buildStatus;
    }

    public void setBuildTime(Date buildTime) 
    {
        this.buildTime = buildTime;
    }

    public Date getBuildTime() 
    {
        return buildTime;
    }

    public void setGraphSummary(String graphSummary) 
    {
        this.graphSummary = graphSummary;
    }

    public String getGraphSummary() 
    {
        return graphSummary;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("graphId", getGraphId())
            .append("graphNo", getGraphNo())
            .append("graphName", getGraphName())
            .append("caseId", getCaseId())
            .append("partId", getPartId())
            .append("partNo", getPartNo())
            .append("partName", getPartName())
            .append("buildAlgorithmId", getBuildAlgorithmId())
            .append("buildAlgorithmName", getBuildAlgorithmName())
            .append("graphType", getGraphType())
            .append("nodeCount", getNodeCount())
            .append("edgeCount", getEdgeCount())
            .append("buildStatus", getBuildStatus())
            .append("buildTime", getBuildTime())
            .append("graphSummary", getGraphSummary())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
