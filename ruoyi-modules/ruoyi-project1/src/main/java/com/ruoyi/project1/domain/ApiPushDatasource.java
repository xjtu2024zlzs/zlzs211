package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * API 推送数据源明细对象 p1p_api_push_datasource
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class ApiPushDatasource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 数据源 ID，主键兼关联数据源主表 */
    private Long datasourceId;

    /** 本系统接收推送的监听路径 */
    @Excel(name = "本系统接收推送的监听路径")
    private String listenPath;

    /** 推送数据格式，如 JSON、XML、CSV */
    @Excel(name = "推送数据格式，如 JSON、XML、CSV")
    private String payloadFormat;

    /** 鉴权方式，如签名 Header、Bearer Token、None */
    @Excel(name = "鉴权方式，如签名 Header、Bearer Token、None")
    private String authType;

    /** 签名 Header 名称 */
    @Excel(name = "签名 Header 名称")
    private String signatureHeader;

    /** 推送密钥密文 */
    @Excel(name = "推送密钥密文")
    private String pushSecretEnc;

    /** IP 白名单 */
    @Excel(name = "IP 白名单")
    private String ipWhitelist;

    /** 健康检查接口路径 */
    @Excel(name = "健康检查接口路径")
    private String healthEndpoint;

    /** 请求头扩展配置 JSON */
    @Excel(name = "请求头扩展配置 JSON")
    private String headersConfig;

    /** API 推送扩展配置 JSON */
    @Excel(name = "API 推送扩展配置 JSON")
    private String extraConfig;

    public void setDatasourceId(Long datasourceId) 
    {
        this.datasourceId = datasourceId;
    }

    public Long getDatasourceId() 
    {
        return datasourceId;
    }

    public void setListenPath(String listenPath) 
    {
        this.listenPath = listenPath;
    }

    public String getListenPath() 
    {
        return listenPath;
    }

    public void setPayloadFormat(String payloadFormat) 
    {
        this.payloadFormat = payloadFormat;
    }

    public String getPayloadFormat() 
    {
        return payloadFormat;
    }

    public void setAuthType(String authType) 
    {
        this.authType = authType;
    }

    public String getAuthType() 
    {
        return authType;
    }

    public void setSignatureHeader(String signatureHeader) 
    {
        this.signatureHeader = signatureHeader;
    }

    public String getSignatureHeader() 
    {
        return signatureHeader;
    }

    public void setPushSecretEnc(String pushSecretEnc) 
    {
        this.pushSecretEnc = pushSecretEnc;
    }

    public String getPushSecretEnc() 
    {
        return pushSecretEnc;
    }

    public void setIpWhitelist(String ipWhitelist) 
    {
        this.ipWhitelist = ipWhitelist;
    }

    public String getIpWhitelist() 
    {
        return ipWhitelist;
    }

    public void setHealthEndpoint(String healthEndpoint) 
    {
        this.healthEndpoint = healthEndpoint;
    }

    public String getHealthEndpoint() 
    {
        return healthEndpoint;
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
            .append("listenPath", getListenPath())
            .append("payloadFormat", getPayloadFormat())
            .append("authType", getAuthType())
            .append("signatureHeader", getSignatureHeader())
            .append("pushSecretEnc", getPushSecretEnc())
            .append("ipWhitelist", getIpWhitelist())
            .append("healthEndpoint", getHealthEndpoint())
            .append("headersConfig", getHeadersConfig())
            .append("extraConfig", getExtraConfig())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
