package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 编码模式配置对象 p1p_encoding_mode
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class EncodingMode extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 编码模式主键 */
    private String encodingModeKey;

    /** 编码模式名称 */
    @Excel(name = "编码模式名称")
    private String modeName;

    /** 编码模式说明 */
    @Excel(name = "编码模式说明")
    private String description;

    /** 是否启用：1启用，0停用 */
    @Excel(name = "是否启用：1启用，0停用")
    private Integer enabled;

    /** 排序值 */
    @Excel(name = "排序值")
    private Long sortOrder;

    public void setEncodingModeKey(String encodingModeKey) 
    {
        this.encodingModeKey = encodingModeKey;
    }

    public String getEncodingModeKey() 
    {
        return encodingModeKey;
    }

    public void setModeName(String modeName) 
    {
        this.modeName = modeName;
    }

    public String getModeName() 
    {
        return modeName;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setEnabled(Integer enabled) 
    {
        this.enabled = enabled;
    }

    public Integer getEnabled() 
    {
        return enabled;
    }

    public void setSortOrder(Long sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Long getSortOrder() 
    {
        return sortOrder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("encodingModeKey", getEncodingModeKey())
            .append("modeName", getModeName())
            .append("description", getDescription())
            .append("enabled", getEnabled())
            .append("sortOrder", getSortOrder())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
