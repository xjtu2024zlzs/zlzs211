package com.ruoyi.topic5.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 零件定位结果对象 t5_algorithm_config
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
public class T5AlgorithmConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 算法ID */
    private Long algorithmId;

    /** 算法编码 */
    @Excel(name = "算法编码")
    private String algorithmCode;

    /** 算法名称 */
    @Excel(name = "算法名称")
    private String algorithmName;

    /** 算法类型：locate/kg_build/root_cause */
    @Excel(name = "算法类型：locate/kg_build/root_cause")
    private String algorithmType;

    /** 算法版本 */
    @Excel(name = "算法版本")
    private String algorithmVersion;

    /** 算法描述 */
    @Excel(name = "算法描述")
    private String algorithmDesc;

    /** 输入说明 */
    @Excel(name = "输入说明")
    private String inputDesc;

    /** 输出说明 */
    @Excel(name = "输出说明")
    private String outputDesc;

    /** 是否默认算法：0否，1是 */
    @Excel(name = "是否默认算法：0否，1是")
    private String defaultFlag;

    /** 状态：0启用，1停用 */
    @Excel(name = "状态：0启用，1停用")
    private String status;

    public void setAlgorithmId(Long algorithmId) 
    {
        this.algorithmId = algorithmId;
    }

    public Long getAlgorithmId() 
    {
        return algorithmId;
    }

    public void setAlgorithmCode(String algorithmCode) 
    {
        this.algorithmCode = algorithmCode;
    }

    public String getAlgorithmCode() 
    {
        return algorithmCode;
    }

    public void setAlgorithmName(String algorithmName) 
    {
        this.algorithmName = algorithmName;
    }

    public String getAlgorithmName() 
    {
        return algorithmName;
    }

    public void setAlgorithmType(String algorithmType) 
    {
        this.algorithmType = algorithmType;
    }

    public String getAlgorithmType() 
    {
        return algorithmType;
    }

    public void setAlgorithmVersion(String algorithmVersion) 
    {
        this.algorithmVersion = algorithmVersion;
    }

    public String getAlgorithmVersion() 
    {
        return algorithmVersion;
    }

    public void setAlgorithmDesc(String algorithmDesc) 
    {
        this.algorithmDesc = algorithmDesc;
    }

    public String getAlgorithmDesc() 
    {
        return algorithmDesc;
    }

    public void setInputDesc(String inputDesc) 
    {
        this.inputDesc = inputDesc;
    }

    public String getInputDesc() 
    {
        return inputDesc;
    }

    public void setOutputDesc(String outputDesc) 
    {
        this.outputDesc = outputDesc;
    }

    public String getOutputDesc() 
    {
        return outputDesc;
    }

    public void setDefaultFlag(String defaultFlag) 
    {
        this.defaultFlag = defaultFlag;
    }

    public String getDefaultFlag() 
    {
        return defaultFlag;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("algorithmId", getAlgorithmId())
            .append("algorithmCode", getAlgorithmCode())
            .append("algorithmName", getAlgorithmName())
            .append("algorithmType", getAlgorithmType())
            .append("algorithmVersion", getAlgorithmVersion())
            .append("algorithmDesc", getAlgorithmDesc())
            .append("inputDesc", getInputDesc())
            .append("outputDesc", getOutputDesc())
            .append("defaultFlag", getDefaultFlag())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
