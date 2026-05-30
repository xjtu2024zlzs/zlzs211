package com.ruoyi.topic5.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 课题五-故障征输入对象 t5_fault_symptom
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public class T5FaultSymptom extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 故障表征ID */
    private Long symptomId;

    /** 故障表征编号 */
    @Excel(name = "故障表征编号")
    private String symptomNo;

    /** 故障标题 */
    @Excel(name = "故障标题")
    private String symptomTitle;

    /** 所属系统 */
    @Excel(name = "所属系统")
    private String systemName;

    /** 所属子系统 */
    @Excel(name = "所属子系统")
    private String subsystemName;

    /** 产品型号/装备型号 */
    @Excel(name = "产品型号/装备型号")
    private String productModel;

    /** 故障现象 */
    @Excel(name = "故障现象")
    private String faultPhenomenon;

    /** 故障详细描述 */
    @Excel(name = "故障详细描述")
    private String faultDesc;

    /** 故障发生位置/区域 */
    @Excel(name = "故障发生位置/区域")
    private String faultLocation;

    /** 初步故障模式 */
    @Excel(name = "初步故障模式")
    private String faultMode;

    /** 异常参数描述 */
    @Excel(name = "异常参数描述")
    private String abnormalParam;

    /** 异常参数值 */
    @Excel(name = "异常参数值")
    private String abnormalValue;

    /** 正常范围 */
    @Excel(name = "正常范围")
    private String normalRange;

    /** 发现方式/检测方法 */
    @Excel(name = "发现方式/检测方法")
    private String detectMethod;

    /** 故障发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "故障发生时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date occurTime;

    /** 严重程度：1低，2中，3高 */
    @Excel(name = "严重程度：1低，2中，3高")
    private String severityLevel;

    /** 输入数据类型：text/image/sensor/mixed */
    @Excel(name = "输入数据类型：text/image/sensor/mixed")
    private String dataType;

    /** 附件ID集合 */
    @Excel(name = "附件ID集合")
    private String attachmentIds;

    /** 状态：0待追溯，1追溯中，2已完成 */
    @Excel(name = "状态：0待追溯，1追溯中，2已完成")
    private String symptomStatus;

    public void setSymptomId(Long symptomId) 
    {
        this.symptomId = symptomId;
    }

    public Long getSymptomId() 
    {
        return symptomId;
    }

    public void setSymptomNo(String symptomNo) 
    {
        this.symptomNo = symptomNo;
    }

    public String getSymptomNo() 
    {
        return symptomNo;
    }

    public void setSymptomTitle(String symptomTitle) 
    {
        this.symptomTitle = symptomTitle;
    }

    public String getSymptomTitle() 
    {
        return symptomTitle;
    }

    public void setSystemName(String systemName) 
    {
        this.systemName = systemName;
    }

    public String getSystemName() 
    {
        return systemName;
    }

    public void setSubsystemName(String subsystemName) 
    {
        this.subsystemName = subsystemName;
    }

    public String getSubsystemName() 
    {
        return subsystemName;
    }

    public void setProductModel(String productModel) 
    {
        this.productModel = productModel;
    }

    public String getProductModel() 
    {
        return productModel;
    }

    public void setFaultPhenomenon(String faultPhenomenon) 
    {
        this.faultPhenomenon = faultPhenomenon;
    }

    public String getFaultPhenomenon() 
    {
        return faultPhenomenon;
    }

    public void setFaultDesc(String faultDesc) 
    {
        this.faultDesc = faultDesc;
    }

    public String getFaultDesc() 
    {
        return faultDesc;
    }

    public void setFaultLocation(String faultLocation) 
    {
        this.faultLocation = faultLocation;
    }

    public String getFaultLocation() 
    {
        return faultLocation;
    }

    public void setFaultMode(String faultMode) 
    {
        this.faultMode = faultMode;
    }

    public String getFaultMode() 
    {
        return faultMode;
    }

    public void setAbnormalParam(String abnormalParam) 
    {
        this.abnormalParam = abnormalParam;
    }

    public String getAbnormalParam() 
    {
        return abnormalParam;
    }

    public void setAbnormalValue(String abnormalValue) 
    {
        this.abnormalValue = abnormalValue;
    }

    public String getAbnormalValue() 
    {
        return abnormalValue;
    }

    public void setNormalRange(String normalRange) 
    {
        this.normalRange = normalRange;
    }

    public String getNormalRange() 
    {
        return normalRange;
    }

    public void setDetectMethod(String detectMethod) 
    {
        this.detectMethod = detectMethod;
    }

    public String getDetectMethod() 
    {
        return detectMethod;
    }

    public void setOccurTime(Date occurTime) 
    {
        this.occurTime = occurTime;
    }

    public Date getOccurTime() 
    {
        return occurTime;
    }

    public void setSeverityLevel(String severityLevel) 
    {
        this.severityLevel = severityLevel;
    }

    public String getSeverityLevel() 
    {
        return severityLevel;
    }

    public void setDataType(String dataType) 
    {
        this.dataType = dataType;
    }

    public String getDataType() 
    {
        return dataType;
    }

    public void setAttachmentIds(String attachmentIds) 
    {
        this.attachmentIds = attachmentIds;
    }

    public String getAttachmentIds() 
    {
        return attachmentIds;
    }

    public void setSymptomStatus(String symptomStatus) 
    {
        this.symptomStatus = symptomStatus;
    }

    public String getSymptomStatus() 
    {
        return symptomStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("symptomId", getSymptomId())
            .append("symptomNo", getSymptomNo())
            .append("symptomTitle", getSymptomTitle())
            .append("systemName", getSystemName())
            .append("subsystemName", getSubsystemName())
            .append("productModel", getProductModel())
            .append("faultPhenomenon", getFaultPhenomenon())
            .append("faultDesc", getFaultDesc())
            .append("faultLocation", getFaultLocation())
            .append("faultMode", getFaultMode())
            .append("abnormalParam", getAbnormalParam())
            .append("abnormalValue", getAbnormalValue())
            .append("normalRange", getNormalRange())
            .append("detectMethod", getDetectMethod())
            .append("occurTime", getOccurTime())
            .append("severityLevel", getSeverityLevel())
            .append("dataType", getDataType())
            .append("attachmentIds", getAttachmentIds())
            .append("symptomStatus", getSymptomStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
