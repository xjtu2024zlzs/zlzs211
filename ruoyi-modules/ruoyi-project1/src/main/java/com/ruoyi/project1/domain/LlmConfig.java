package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * LLM 配置对象 p1p_llm_config
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class LlmConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** LLM 配置主键 */
    private Long llmConfigId;

    /** 配置名称 */
    @Excel(name = "配置名称")
    private String configName;

    /** 模型提供商，下拉选择 */
    @Excel(name = "模型提供商，下拉选择")
    private String provider;

    /** 模型名称，下拉选择 */
    @Excel(name = "模型名称，下拉选择")
    private String modelName;

    /** 模型接口地址，由后端配置维护 */
    @Excel(name = "模型接口地址，由后端配置维护")
    private String baseUrl;

    /** 模型接口密钥密文，由后端配置维护 */
    @Excel(name = "模型接口密钥密文，由后端配置维护")
    private String apiKeyEnc;

    /** 是否启用：1启用，0停用 */
    @Excel(name = "是否启用：1启用，0停用")
    private Integer enabled;

    public void setLlmConfigId(Long llmConfigId) 
    {
        this.llmConfigId = llmConfigId;
    }

    public Long getLlmConfigId() 
    {
        return llmConfigId;
    }

    public void setConfigName(String configName) 
    {
        this.configName = configName;
    }

    public String getConfigName() 
    {
        return configName;
    }

    public void setProvider(String provider) 
    {
        this.provider = provider;
    }

    public String getProvider() 
    {
        return provider;
    }

    public void setModelName(String modelName) 
    {
        this.modelName = modelName;
    }

    public String getModelName() 
    {
        return modelName;
    }

    public void setBaseUrl(String baseUrl) 
    {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() 
    {
        return baseUrl;
    }

    public void setApiKeyEnc(String apiKeyEnc) 
    {
        this.apiKeyEnc = apiKeyEnc;
    }

    public String getApiKeyEnc() 
    {
        return apiKeyEnc;
    }

    public void setEnabled(Integer enabled) 
    {
        this.enabled = enabled;
    }

    public Integer getEnabled() 
    {
        return enabled;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("llmConfigId", getLlmConfigId())
            .append("configName", getConfigName())
            .append("provider", getProvider())
            .append("modelName", getModelName())
            .append("baseUrl", getBaseUrl())
            .append("apiKeyEnc", getApiKeyEnc())
            .append("enabled", getEnabled())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
