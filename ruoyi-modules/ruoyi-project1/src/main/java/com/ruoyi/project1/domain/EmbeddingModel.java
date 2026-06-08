package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 嵌入模型配置对象 p1p_embedding_model
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class EmbeddingModel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 嵌入模型主键 */
    private String embeddingModelKey;

    /** 嵌入模型显示名称 */
    @Excel(name = "嵌入模型显示名称")
    private String modelLabel;

    /** 模型提供商 */
    @Excel(name = "模型提供商")
    private String provider;

    /** 模型名称 */
    @Excel(name = "模型名称")
    private String modelName;

    /** 向量维度 */
    @Excel(name = "向量维度")
    private Long dimension;

    /** 是否启用：1启用，0停用 */
    @Excel(name = "是否启用：1启用，0停用")
    private Integer enabled;

    public void setEmbeddingModelKey(String embeddingModelKey) 
    {
        this.embeddingModelKey = embeddingModelKey;
    }

    public String getEmbeddingModelKey() 
    {
        return embeddingModelKey;
    }

    public void setModelLabel(String modelLabel) 
    {
        this.modelLabel = modelLabel;
    }

    public String getModelLabel() 
    {
        return modelLabel;
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

    public void setDimension(Long dimension) 
    {
        this.dimension = dimension;
    }

    public Long getDimension() 
    {
        return dimension;
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
            .append("embeddingModelKey", getEmbeddingModelKey())
            .append("modelLabel", getModelLabel())
            .append("provider", getProvider())
            .append("modelName", getModelName())
            .append("dimension", getDimension())
            .append("enabled", getEnabled())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
