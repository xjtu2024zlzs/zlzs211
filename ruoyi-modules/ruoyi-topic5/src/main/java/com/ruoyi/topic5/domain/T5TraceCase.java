package com.ruoyi.topic5.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 零部件实例对象 t5_trace_case
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
public class T5TraceCase extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 追溯案例ID */
    private Long caseId;

    /** 追溯案例编号 */
    @Excel(name = "追溯案例编号")
    private String caseNo;

    /** 案例名称 */
    @Excel(name = "案例名称")
    private String caseName;

    /** 所属系统 */
    @Excel(name = "所属系统")
    private String systemName;

    /** 所属子系统 */
    @Excel(name = "所属子系统")
    private String subsystemName;

    /** 产品型号/装备型号 */
    @Excel(name = "产品型号/装备型号")
    private String productModel;

    /** 故障标题 */
    @Excel(name = "故障标题")
    private String faultTitle;

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

    /** 零件定位算法ID */
    @Excel(name = "零件定位算法ID")
    private Long locateAlgorithmId;

    /** 零件定位算法名称 */
    @Excel(name = "零件定位算法名称")
    private String locateAlgorithmName;

    /** 最终定位故障零件ID */
    @Excel(name = "最终定位故障零件ID")
    private Long finalPartId;

    /** 最终定位零件编号 */
    @Excel(name = "最终定位零件编号")
    private String finalPartNo;

    /** 最终定位零件名称 */
    @Excel(name = "最终定位零件名称")
    private String finalPartName;

    /** 零件定位置信度 */
    @Excel(name = "零件定位置信度")
    private BigDecimal locateConfidence;

    /** 关联知识图谱ID */
    @Excel(name = "关联知识图谱ID")
    private Long graphId;

    /** 关联根因分析结果ID */
    @Excel(name = "关联根因分析结果ID")
    private Long rootResultId;

    /** 案例状态：0待定位，1已定位零件，2已构建图谱，3已完成根因分析，4失败 */
    @Excel(name = "案例状态：0待定位，1已定位零件，2已构建图谱，3已完成根因分析，4失败")
    private String caseStatus;

    public void setCaseId(Long caseId) 
    {
        this.caseId = caseId;
    }

    public Long getCaseId() 
    {
        return caseId;
    }

    public void setCaseNo(String caseNo) 
    {
        this.caseNo = caseNo;
    }

    public String getCaseNo() 
    {
        return caseNo;
    }

    public void setCaseName(String caseName) 
    {
        this.caseName = caseName;
    }

    public String getCaseName() 
    {
        return caseName;
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

    public void setFaultTitle(String faultTitle) 
    {
        this.faultTitle = faultTitle;
    }

    public String getFaultTitle() 
    {
        return faultTitle;
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

    public void setLocateAlgorithmId(Long locateAlgorithmId) 
    {
        this.locateAlgorithmId = locateAlgorithmId;
    }

    public Long getLocateAlgorithmId() 
    {
        return locateAlgorithmId;
    }

    public void setLocateAlgorithmName(String locateAlgorithmName) 
    {
        this.locateAlgorithmName = locateAlgorithmName;
    }

    public String getLocateAlgorithmName() 
    {
        return locateAlgorithmName;
    }

    public void setFinalPartId(Long finalPartId) 
    {
        this.finalPartId = finalPartId;
    }

    public Long getFinalPartId() 
    {
        return finalPartId;
    }

    public void setFinalPartNo(String finalPartNo) 
    {
        this.finalPartNo = finalPartNo;
    }

    public String getFinalPartNo() 
    {
        return finalPartNo;
    }

    public void setFinalPartName(String finalPartName) 
    {
        this.finalPartName = finalPartName;
    }

    public String getFinalPartName() 
    {
        return finalPartName;
    }

    public void setLocateConfidence(BigDecimal locateConfidence) 
    {
        this.locateConfidence = locateConfidence;
    }

    public BigDecimal getLocateConfidence() 
    {
        return locateConfidence;
    }

    public void setGraphId(Long graphId) 
    {
        this.graphId = graphId;
    }

    public Long getGraphId() 
    {
        return graphId;
    }

    public void setRootResultId(Long rootResultId) 
    {
        this.rootResultId = rootResultId;
    }

    public Long getRootResultId() 
    {
        return rootResultId;
    }

    public void setCaseStatus(String caseStatus) 
    {
        this.caseStatus = caseStatus;
    }

    public String getCaseStatus() 
    {
        return caseStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("caseId", getCaseId())
            .append("caseNo", getCaseNo())
            .append("caseName", getCaseName())
            .append("systemName", getSystemName())
            .append("subsystemName", getSubsystemName())
            .append("productModel", getProductModel())
            .append("faultTitle", getFaultTitle())
            .append("faultPhenomenon", getFaultPhenomenon())
            .append("faultDesc", getFaultDesc())
            .append("faultLocation", getFaultLocation())
            .append("faultMode", getFaultMode())
            .append("abnormalParam", getAbnormalParam())
            .append("abnormalValue", getAbnormalValue())
            .append("normalRange", getNormalRange())
            .append("detectMethod", getDetectMethod())
            .append("locateAlgorithmId", getLocateAlgorithmId())
            .append("locateAlgorithmName", getLocateAlgorithmName())
            .append("finalPartId", getFinalPartId())
            .append("finalPartNo", getFinalPartNo())
            .append("finalPartName", getFinalPartName())
            .append("locateConfidence", getLocateConfidence())
            .append("graphId", getGraphId())
            .append("rootResultId", getRootResultId())
            .append("caseStatus", getCaseStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
