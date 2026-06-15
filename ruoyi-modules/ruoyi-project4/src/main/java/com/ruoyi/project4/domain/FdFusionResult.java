package com.ruoyi.project4.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 故障诊断-融合征结果对象 fd_fusion_result
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public class FdFusionResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 融合ID */
    private Long fusionId;

    /** 融合编号 */
    @Excel(name = "融合编号")
    private String fusionCode;

    /** 样本ID */
    @Excel(name = "样本ID")
    private Long sampleId;

    /** 样本编号 */
    @Excel(name = "样本编号")
    private String sampleCode;

    /** 关联特征ID列表，逗号分隔 */
    @Excel(name = "关联特征ID列表，逗号分隔")
    private String featureIds;

    /** 融合方法：注意力融合/特征拼接/加权融合/图谱映射融合 */
    @Excel(name = "融合方法：注意力融合/特征拼接/加权融合/图谱映射融合")
    private String fusionMethod;

    /** 置信权重 */
    @Excel(name = "置信权重")
    private BigDecimal confidenceWeight;

    /** 输出维度 */
    @Excel(name = "输出维度")
    private Long outputDimension;

    /** 向量长度 */
    @Excel(name = "向量长度")
    private Long vectorLength;

    /** 融合向量JSON字符串，小向量可存 */
    @Excel(name = "融合向量JSON字符串，小向量可存")
    private String vectorJson;

    /** 融合向量文件路径，大向量建议存文件 */
    @Excel(name = "融合向量文件路径，大向量建议存文件")
    private String vectorPath;

    /** 算法版本 */
    @Excel(name = "算法版本")
    private String algorithmVersion;

    /** 生成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生成时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date generateTime;

    /** 结果状态 */
    @Excel(name = "结果状态")
    private String resultStatus;

    /** 删除标志：0存在 2删除 */
    private String delFlag;

    public void setFusionId(Long fusionId) 
    {
        this.fusionId = fusionId;
    }

    public Long getFusionId() 
    {
        return fusionId;
    }

    public void setFusionCode(String fusionCode) 
    {
        this.fusionCode = fusionCode;
    }

    public String getFusionCode() 
    {
        return fusionCode;
    }

    public void setSampleId(Long sampleId) 
    {
        this.sampleId = sampleId;
    }

    public Long getSampleId() 
    {
        return sampleId;
    }

    public void setSampleCode(String sampleCode) 
    {
        this.sampleCode = sampleCode;
    }

    public String getSampleCode() 
    {
        return sampleCode;
    }

    public void setFeatureIds(String featureIds) 
    {
        this.featureIds = featureIds;
    }

    public String getFeatureIds() 
    {
        return featureIds;
    }

    public void setFusionMethod(String fusionMethod) 
    {
        this.fusionMethod = fusionMethod;
    }

    public String getFusionMethod() 
    {
        return fusionMethod;
    }

    public void setConfidenceWeight(BigDecimal confidenceWeight) 
    {
        this.confidenceWeight = confidenceWeight;
    }

    public BigDecimal getConfidenceWeight() 
    {
        return confidenceWeight;
    }

    public void setOutputDimension(Long outputDimension) 
    {
        this.outputDimension = outputDimension;
    }

    public Long getOutputDimension() 
    {
        return outputDimension;
    }

    public void setVectorLength(Long vectorLength) 
    {
        this.vectorLength = vectorLength;
    }

    public Long getVectorLength() 
    {
        return vectorLength;
    }

    public void setVectorJson(String vectorJson) 
    {
        this.vectorJson = vectorJson;
    }

    public String getVectorJson() 
    {
        return vectorJson;
    }

    public void setVectorPath(String vectorPath) 
    {
        this.vectorPath = vectorPath;
    }

    public String getVectorPath() 
    {
        return vectorPath;
    }

    public void setAlgorithmVersion(String algorithmVersion) 
    {
        this.algorithmVersion = algorithmVersion;
    }

    public String getAlgorithmVersion() 
    {
        return algorithmVersion;
    }

    public void setGenerateTime(Date generateTime) 
    {
        this.generateTime = generateTime;
    }

    public Date getGenerateTime() 
    {
        return generateTime;
    }

    public void setResultStatus(String resultStatus) 
    {
        this.resultStatus = resultStatus;
    }

    public String getResultStatus() 
    {
        return resultStatus;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("fusionId", getFusionId())
            .append("fusionCode", getFusionCode())
            .append("sampleId", getSampleId())
            .append("sampleCode", getSampleCode())
            .append("featureIds", getFeatureIds())
            .append("fusionMethod", getFusionMethod())
            .append("confidenceWeight", getConfidenceWeight())
            .append("outputDimension", getOutputDimension())
            .append("vectorLength", getVectorLength())
            .append("vectorJson", getVectorJson())
            .append("vectorPath", getVectorPath())
            .append("algorithmVersion", getAlgorithmVersion())
            .append("generateTime", getGenerateTime())
            .append("resultStatus", getResultStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
