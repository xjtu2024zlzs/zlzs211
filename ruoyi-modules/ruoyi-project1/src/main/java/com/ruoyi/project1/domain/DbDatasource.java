package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 数据库直连数据源明细对象 p1p_db_datasource
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class DbDatasource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 数据源 ID，主键兼关联数据源主表 */
    private Long datasourceId;

    /** 数据库类型，如 PostgreSQL、MySQL、SQL Server、Oracle */
    @Excel(name = "数据库类型，如 PostgreSQL、MySQL、SQL Server、Oracle")
    private String dbType;

    /** 数据库主机地址 */
    @Excel(name = "数据库主机地址")
    private String host;

    /** 数据库端口 */
    @Excel(name = "数据库端口")
    private Long port;

    /** 数据库名称 */
    @Excel(name = "数据库名称")
    private String databaseName;

    /** 数据库 Schema 名称 */
    @Excel(name = "数据库 Schema 名称")
    private String schemaName;

    /** 数据库用户名 */
    @Excel(name = "数据库用户名")
    private String username;

    /** 数据库密码密文 */
    @Excel(name = "数据库密码密文")
    private String passwordEnc;

    /** 是否保存密码：1保存，0不保存 */
    @Excel(name = "是否保存密码：1保存，0不保存")
    private Integer savePassword;

    /** 连接参数，如 sslmode=prefer */
    @Excel(name = "连接参数，如 sslmode=prefer")
    private String connectionParams;

    public void setDatasourceId(Long datasourceId) 
    {
        this.datasourceId = datasourceId;
    }

    public Long getDatasourceId() 
    {
        return datasourceId;
    }

    public void setDbType(String dbType) 
    {
        this.dbType = dbType;
    }

    public String getDbType() 
    {
        return dbType;
    }

    public void setHost(String host) 
    {
        this.host = host;
    }

    public String getHost() 
    {
        return host;
    }

    public void setPort(Long port) 
    {
        this.port = port;
    }

    public Long getPort() 
    {
        return port;
    }

    public void setDatabaseName(String databaseName) 
    {
        this.databaseName = databaseName;
    }

    public String getDatabaseName() 
    {
        return databaseName;
    }

    public void setSchemaName(String schemaName) 
    {
        this.schemaName = schemaName;
    }

    public String getSchemaName() 
    {
        return schemaName;
    }

    public void setUsername(String username) 
    {
        this.username = username;
    }

    public String getUsername() 
    {
        return username;
    }

    public void setPasswordEnc(String passwordEnc) 
    {
        this.passwordEnc = passwordEnc;
    }

    public String getPasswordEnc() 
    {
        return passwordEnc;
    }

    public void setSavePassword(Integer savePassword) 
    {
        this.savePassword = savePassword;
    }

    public Integer getSavePassword() 
    {
        return savePassword;
    }

    public void setConnectionParams(String connectionParams) 
    {
        this.connectionParams = connectionParams;
    }

    public String getConnectionParams() 
    {
        return connectionParams;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("datasourceId", getDatasourceId())
            .append("dbType", getDbType())
            .append("host", getHost())
            .append("port", getPort())
            .append("databaseName", getDatabaseName())
            .append("schemaName", getSchemaName())
            .append("username", getUsername())
            .append("passwordEnc", getPasswordEnc())
            .append("savePassword", getSavePassword())
            .append("connectionParams", getConnectionParams())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
