package com.ruoyi.topic5.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 课题五-零部件生命周期事件对象 t5_lifecycle_event
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class T5LifecycleEvent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 生命周期事件ID */
    private Long eventId;

    /** 零部件实例ID */
    @Excel(name = "零部件实例ID")
    private Long partId;

    /** 生命周期阶段编码 */
    @Excel(name = "生命周期阶段编码")
    private String stageCode;

    /** 生命周期阶段名称 */
    @Excel(name = "生命周期阶段名称")
    private String stageName;

    /** 事件类型 */
    @Excel(name = "事件类型")
    private String eventType;

    /** 事件名称 */
    @Excel(name = "事件名称")
    private String eventName;

    /** 事件描述 */
    @Excel(name = "事件描述")
    private String eventDesc;

    /** 事件发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "事件发生时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date eventTime;

    /** 工序编号 */
    @Excel(name = "工序编号")
    private String processNo;

    /** 工序名称 */
    @Excel(name = "工序名称")
    private String processName;

    /** 设备编号 */
    @Excel(name = "设备编号")
    private String equipmentNo;

    /** 操作人员 */
    @Excel(name = "操作人员")
    private String operatorName;

    /** 材料批次号 */
    @Excel(name = "材料批次号")
    private String materialBatchNo;

    /** 检测结果 */
    @Excel(name = "检测结果")
    private String inspectionResult;

    /** 事件参数JSON */
    @Excel(name = "事件参数JSON")
    private String parameterJson;

    /** 是否异常：0否，1是 */
    @Excel(name = "是否异常：0否，1是")
    private String abnormalFlag;

    /** 质量评分 */
    @Excel(name = "质量评分")
    private BigDecimal qualityScore;

    /** 来源系统 */
    @Excel(name = "来源系统")
    private String sourceSystem;

    /** 来源表 */
    @Excel(name = "来源表")
    private String sourceTable;

    /** 来源记录ID */
    @Excel(name = "来源记录ID")
    private String sourceRecordId;

    public void setEventId(Long eventId) 
    {
        this.eventId = eventId;
    }

    public Long getEventId() 
    {
        return eventId;
    }

    public void setPartId(Long partId) 
    {
        this.partId = partId;
    }

    public Long getPartId() 
    {
        return partId;
    }

    public void setStageCode(String stageCode) 
    {
        this.stageCode = stageCode;
    }

    public String getStageCode() 
    {
        return stageCode;
    }

    public void setStageName(String stageName) 
    {
        this.stageName = stageName;
    }

    public String getStageName() 
    {
        return stageName;
    }

    public void setEventType(String eventType) 
    {
        this.eventType = eventType;
    }

    public String getEventType() 
    {
        return eventType;
    }

    public void setEventName(String eventName) 
    {
        this.eventName = eventName;
    }

    public String getEventName() 
    {
        return eventName;
    }

    public void setEventDesc(String eventDesc) 
    {
        this.eventDesc = eventDesc;
    }

    public String getEventDesc() 
    {
        return eventDesc;
    }

    public void setEventTime(Date eventTime) 
    {
        this.eventTime = eventTime;
    }

    public Date getEventTime() 
    {
        return eventTime;
    }

    public void setProcessNo(String processNo) 
    {
        this.processNo = processNo;
    }

    public String getProcessNo() 
    {
        return processNo;
    }

    public void setProcessName(String processName) 
    {
        this.processName = processName;
    }

    public String getProcessName() 
    {
        return processName;
    }

    public void setEquipmentNo(String equipmentNo) 
    {
        this.equipmentNo = equipmentNo;
    }

    public String getEquipmentNo() 
    {
        return equipmentNo;
    }

    public void setOperatorName(String operatorName) 
    {
        this.operatorName = operatorName;
    }

    public String getOperatorName() 
    {
        return operatorName;
    }

    public void setMaterialBatchNo(String materialBatchNo) 
    {
        this.materialBatchNo = materialBatchNo;
    }

    public String getMaterialBatchNo() 
    {
        return materialBatchNo;
    }

    public void setInspectionResult(String inspectionResult) 
    {
        this.inspectionResult = inspectionResult;
    }

    public String getInspectionResult() 
    {
        return inspectionResult;
    }

    public void setParameterJson(String parameterJson) 
    {
        this.parameterJson = parameterJson;
    }

    public String getParameterJson() 
    {
        return parameterJson;
    }

    public void setAbnormalFlag(String abnormalFlag) 
    {
        this.abnormalFlag = abnormalFlag;
    }

    public String getAbnormalFlag() 
    {
        return abnormalFlag;
    }

    public void setQualityScore(BigDecimal qualityScore) 
    {
        this.qualityScore = qualityScore;
    }

    public BigDecimal getQualityScore() 
    {
        return qualityScore;
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

    public void setSourceRecordId(String sourceRecordId) 
    {
        this.sourceRecordId = sourceRecordId;
    }

    public String getSourceRecordId() 
    {
        return sourceRecordId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("eventId", getEventId())
            .append("partId", getPartId())
            .append("stageCode", getStageCode())
            .append("stageName", getStageName())
            .append("eventType", getEventType())
            .append("eventName", getEventName())
            .append("eventDesc", getEventDesc())
            .append("eventTime", getEventTime())
            .append("processNo", getProcessNo())
            .append("processName", getProcessName())
            .append("equipmentNo", getEquipmentNo())
            .append("operatorName", getOperatorName())
            .append("materialBatchNo", getMaterialBatchNo())
            .append("inspectionResult", getInspectionResult())
            .append("parameterJson", getParameterJson())
            .append("abnormalFlag", getAbnormalFlag())
            .append("qualityScore", getQualityScore())
            .append("sourceSystem", getSourceSystem())
            .append("sourceTable", getSourceTable())
            .append("sourceRecordId", getSourceRecordId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
