package com.ruoyi.topic5.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 数字卷宗模拟数据对象 t5_dossier_sample_record
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public class T5DossierSampleRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 卷宗记录ID */
    private Long recordId;

    /** 零部件实例ID */
    @Excel(name = "零部件实例ID")
    private Long partId;

    /** 零件编号 */
    @Excel(name = "零件编号")
    private String partNo;

    /** 零件名称 */
    @Excel(name = "零件名称")
    private String partName;

    /** 模拟数字卷宗ID */
    @Excel(name = "模拟数字卷宗ID")
    private String dossierId;

    /** 生命周期阶段：DESIGN/MATERIAL/MANUFACTURING/INSPECTION/ASSEMBLY/OPERATION/MAINTENANCE */
    @Excel(name = "生命周期阶段：DESIGN/MATERIAL/MANUFACTURING/INSPECTION/ASSEMBLY/OPERATION/MAINTENANCE")
    private String lifecycleStage;

    /** 生命周期阶段顺序 */
    @Excel(name = "生命周期阶段顺序")
    private Long stageOrder;

    /** 记录类型 */
    @Excel(name = "记录类型")
    private String recordType;

    /** 记录名称 */
    @Excel(name = "记录名称")
    private String recordName;

    /** 记录描述 */
    @Excel(name = "记录描述")
    private String recordDesc;

    /** 记录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "记录时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date recordTime;

    /** 关联对象ID */
    @Excel(name = "关联对象ID")
    private String objectId;

    /** 关联对象名称 */
    @Excel(name = "关联对象名称")
    private String objectName;

    /** 参数名称 */
    @Excel(name = "参数名称")
    private String parameterName;

    /** 参数值 */
    @Excel(name = "参数值")
    private String parameterValue;

    /** 标准值/要求值 */
    @Excel(name = "标准值/要求值")
    private String standardValue;

    /** 结果状态：0正常，1异常 */
    @Excel(name = "结果状态：0正常，1异常")
    private String resultStatus;

    /** 风险等级：low/medium/high */
    @Excel(name = "风险等级：low/medium/high")
    private String riskLevel;

    /** 来源系统 */
    @Excel(name = "来源系统")
    private String sourceSystem;

    /** 模拟来源表名 */
    @Excel(name = "模拟来源表名")
    private String sourceTable;

    /** 模拟来源记录编号 */
    @Excel(name = "模拟来源记录编号")
    private String sourceRecordNo;

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
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

    public void setDossierId(String dossierId) 
    {
        this.dossierId = dossierId;
    }

    public String getDossierId() 
    {
        return dossierId;
    }

    public void setLifecycleStage(String lifecycleStage) 
    {
        this.lifecycleStage = lifecycleStage;
    }

    public String getLifecycleStage() 
    {
        return lifecycleStage;
    }

    public void setStageOrder(Long stageOrder) 
    {
        this.stageOrder = stageOrder;
    }

    public Long getStageOrder() 
    {
        return stageOrder;
    }

    public void setRecordType(String recordType) 
    {
        this.recordType = recordType;
    }

    public String getRecordType() 
    {
        return recordType;
    }

    public void setRecordName(String recordName) 
    {
        this.recordName = recordName;
    }

    public String getRecordName() 
    {
        return recordName;
    }

    public void setRecordDesc(String recordDesc) 
    {
        this.recordDesc = recordDesc;
    }

    public String getRecordDesc() 
    {
        return recordDesc;
    }

    public void setRecordTime(Date recordTime) 
    {
        this.recordTime = recordTime;
    }

    public Date getRecordTime() 
    {
        return recordTime;
    }

    public void setObjectId(String objectId) 
    {
        this.objectId = objectId;
    }

    public String getObjectId() 
    {
        return objectId;
    }

    public void setObjectName(String objectName) 
    {
        this.objectName = objectName;
    }

    public String getObjectName() 
    {
        return objectName;
    }

    public void setParameterName(String parameterName) 
    {
        this.parameterName = parameterName;
    }

    public String getParameterName() 
    {
        return parameterName;
    }

    public void setParameterValue(String parameterValue) 
    {
        this.parameterValue = parameterValue;
    }

    public String getParameterValue() 
    {
        return parameterValue;
    }

    public void setStandardValue(String standardValue) 
    {
        this.standardValue = standardValue;
    }

    public String getStandardValue() 
    {
        return standardValue;
    }

    public void setResultStatus(String resultStatus) 
    {
        this.resultStatus = resultStatus;
    }

    public String getResultStatus() 
    {
        return resultStatus;
    }

    public void setRiskLevel(String riskLevel) 
    {
        this.riskLevel = riskLevel;
    }

    public String getRiskLevel() 
    {
        return riskLevel;
    }

    public void setSourceSystem(String sourceSystem) 
    {
        this.sourceSystem = sourceSystem;
    }

    public String getSourceSystem() 
    {
        return sourceSystem;
    }

    public void setSourceTable(String sourceTable) 
    {
        this.sourceTable = sourceTable;
    }

    public String getSourceTable() 
    {
        return sourceTable;
    }

    public void setSourceRecordNo(String sourceRecordNo) 
    {
        this.sourceRecordNo = sourceRecordNo;
    }

    public String getSourceRecordNo() 
    {
        return sourceRecordNo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("partId", getPartId())
            .append("partNo", getPartNo())
            .append("partName", getPartName())
            .append("dossierId", getDossierId())
            .append("lifecycleStage", getLifecycleStage())
            .append("stageOrder", getStageOrder())
            .append("recordType", getRecordType())
            .append("recordName", getRecordName())
            .append("recordDesc", getRecordDesc())
            .append("recordTime", getRecordTime())
            .append("objectId", getObjectId())
            .append("objectName", getObjectName())
            .append("parameterName", getParameterName())
            .append("parameterValue", getParameterValue())
            .append("standardValue", getStandardValue())
            .append("resultStatus", getResultStatus())
            .append("riskLevel", getRiskLevel())
            .append("sourceSystem", getSourceSystem())
            .append("sourceTable", getSourceTable())
            .append("sourceRecordNo", getSourceRecordNo())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
