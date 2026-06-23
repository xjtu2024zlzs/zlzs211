package com.ruoyi.project4.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 故障诊断-样本增强结果对象 t4_augment_result
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public class FdAugmentResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 增强ID */
    private Long augmentId;

    /** 增强编号 */
    @Excel(name = "增强编号")
    private String augmentCode;

    /** 原始样本ID */
    @Excel(name = "原始样本ID")
    private Long rawSampleId;

    /** 原始样本编号 */
    @Excel(name = "原始样本编号")
    private String rawSampleCode;

    /** 增强算法：SMOTE/ADASYN/GAN/VAE/时序插值 */
    @Excel(name = "增强算法：SMOTE/ADASYN/GAN/VAE/时序插值")
    private String algorithmName;

    /** 增强倍数 */
    @Excel(name = "增强倍数")
    private Long multiplier;

    /** 生成样本数量 */
    @Excel(name = "生成样本数量")
    private Long generatedCount;

    /** 质量校验策略 */
    @Excel(name = "质量校验策略")
    private String qualityPolicy;

    /** 有效性：有效/无效 */
    @Excel(name = "有效性：有效/无效")
    private String validity;

    /** 算法参数JSON字符串 */
    @Excel(name = "算法参数JSON字符串")
    private String paramJson;

    /** 结果摘要JSON字符串 */
    @Excel(name = "结果摘要JSON字符串")
    private String resultSummary;

    /** 增强样本存储路径 */
    @Excel(name = "增强样本存储路径")
    private String outputPath;

    /** 生成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生成时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date generateTime;

    /** 删除标志：0存在 2删除 */
    private String delFlag;

    public void setAugmentId(Long augmentId) 
    {
        this.augmentId = augmentId;
    }

    public Long getAugmentId() 
    {
        return augmentId;
    }

    public void setAugmentCode(String augmentCode) 
    {
        this.augmentCode = augmentCode;
    }

    public String getAugmentCode() 
    {
        return augmentCode;
    }

    public void setRawSampleId(Long rawSampleId) 
    {
        this.rawSampleId = rawSampleId;
    }

    public Long getRawSampleId() 
    {
        return rawSampleId;
    }

    public void setRawSampleCode(String rawSampleCode) 
    {
        this.rawSampleCode = rawSampleCode;
    }

    public String getRawSampleCode() 
    {
        return rawSampleCode;
    }

    public void setAlgorithmName(String algorithmName) 
    {
        this.algorithmName = algorithmName;
    }

    public String getAlgorithmName() 
    {
        return algorithmName;
    }

    public void setMultiplier(Long multiplier) 
    {
        this.multiplier = multiplier;
    }

    public Long getMultiplier() 
    {
        return multiplier;
    }

    public void setGeneratedCount(Long generatedCount) 
    {
        this.generatedCount = generatedCount;
    }

    public Long getGeneratedCount() 
    {
        return generatedCount;
    }

    public void setQualityPolicy(String qualityPolicy) 
    {
        this.qualityPolicy = qualityPolicy;
    }

    public String getQualityPolicy() 
    {
        return qualityPolicy;
    }

    public void setValidity(String validity) 
    {
        this.validity = validity;
    }

    public String getValidity() 
    {
        return validity;
    }

    public void setParamJson(String paramJson) 
    {
        this.paramJson = paramJson;
    }

    public String getParamJson() 
    {
        return paramJson;
    }

    public void setResultSummary(String resultSummary) 
    {
        this.resultSummary = resultSummary;
    }

    public String getResultSummary() 
    {
        return resultSummary;
    }

    public void setOutputPath(String outputPath) 
    {
        this.outputPath = outputPath;
    }

    public String getOutputPath() 
    {
        return outputPath;
    }

    public void setGenerateTime(Date generateTime) 
    {
        this.generateTime = generateTime;
    }

    public Date getGenerateTime() 
    {
        return generateTime;
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
            .append("augmentId", getAugmentId())
            .append("augmentCode", getAugmentCode())
            .append("rawSampleId", getRawSampleId())
            .append("rawSampleCode", getRawSampleCode())
            .append("algorithmName", getAlgorithmName())
            .append("multiplier", getMultiplier())
            .append("generatedCount", getGeneratedCount())
            .append("qualityPolicy", getQualityPolicy())
            .append("validity", getValidity())
            .append("paramJson", getParamJson())
            .append("resultSummary", getResultSummary())
            .append("outputPath", getOutputPath())
            .append("generateTime", getGenerateTime())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

