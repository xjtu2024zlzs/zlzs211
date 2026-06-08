package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * API 拉取数据源明细对象 p1p_api_pull_datasource
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class ApiPullDatasource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 数据源 ID，主键兼关联数据源主表 */
    private Long datasourceId;

    /** 接口基础地址 */
    @Excel(name = "接口基础地址")
    private String baseUrl;

    /** 数据拉取接口路径 */
    @Excel(name = "数据拉取接口路径")
    private String pullEndpoint;

    /** 模式读取接口路径 */
    @Excel(name = "模式读取接口路径")
    private String schemaEndpoint;

    /** 请求方法：GET 或 POST */
    @Excel(name = "请求方法：GET 或 POST")
    private String requestMethod;

    /** 认证方式，如 Bearer Token、Basic Auth、None */
    @Excel(name = "认证方式，如 Bearer Token、Basic Auth、None")
    private String authType;

    /** 健康检查接口路径 */
    @Excel(name = "健康检查接口路径")
    private String healthEndpoint;

    /** 接口密钥密文 */
    @Excel(name = "接口密钥密文")
    private String apiKeyEnc;

    /** 请求头扩展配置 JSON */
    @Excel(name = "请求头扩展配置 JSON")
    private String headersConfig;

    /** API 拉取扩展配置 JSON */
    @Excel(name = "API 拉取扩展配置 JSON")
    private String extraConfig;

    public void setDatasourceId(Long datasourceId) 
    {
        this.datasourceId = datasourceId;
    }

    public Long getDatasourceId() 
    {
        return datasourceId;
    }

    public void setBaseUrl(String baseUrl) 
    {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() 
    {
        return baseUrl;
    }

    public void setPullEndpoint(String pullEndpoint) 
    {
        this.pullEndpoint = pullEndpoint;
    }

    public String getPullEndpoint() 
    {
        return pullEndpoint;
    }

    public void setSchemaEndpoint(String schemaEndpoint) 
    {
        this.schemaEndpoint = schemaEndpoint;
    }

    public String getSchemaEndpoint() 
    {
        return schemaEndpoint;
    }

    public void setRequestMethod(String requestMethod) 
    {
        this.requestMethod = requestMethod;
    }

    public String getRequestMethod() 
    {
        return requestMethod;
    }

    public void setAuthType(String authType) 
    {
        this.authType = authType;
    }

    public String getAuthType() 
    {
        return authType;
    }

    public void setHealthEndpoint(String healthEndpoint) 
    {
        this.healthEndpoint = healthEndpoint;
    }

    public String getHealthEndpoint() 
    {
        return healthEndpoint;
    }

    public void setApiKeyEnc(String apiKeyEnc) 
    {
        this.apiKeyEnc = apiKeyEnc;
    }

    public String getApiKeyEnc() 
    {
        return apiKeyEnc;
    }

    public void setHeadersConfig(String headersConfig) 
    {
        this.headersConfig = headersConfig;
    }

    public String getHeadersConfig() 
    {
        return headersConfig;
    }

    public void setExtraConfig(String extraConfig) 
    {
        this.extraConfig = extraConfig;
    }

    public String getExtraConfig() 
    {
        return extraConfig;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("datasourceId", getDatasourceId())
            .append("baseUrl", getBaseUrl())
            .append("pullEndpoint", getPullEndpoint())
            .append("schemaEndpoint", getSchemaEndpoint())
            .append("requestMethod", getRequestMethod())
            .append("authType", getAuthType())
            .append("healthEndpoint", getHealthEndpoint())
            .append("apiKeyEnc", getApiKeyEnc())
            .append("headersConfig", getHeadersConfig())
            .append("extraConfig", getExtraConfig())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
