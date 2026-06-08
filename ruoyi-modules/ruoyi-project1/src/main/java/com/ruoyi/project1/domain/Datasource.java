package com.ruoyi.project1.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 数据源管理对象 p1p_datasource
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class Datasource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 数据源主键 */
    private Long datasourceId;

    /** 数据源名称 */
    @Excel(name = "数据源名称")
    private String datasourceName;

    /** 接入方式：db_direct 数据库直连、api_pull API 拉取、api_push API 推送 */
    @Excel(name = "接入方式：db_direct 数据库直连、api_pull API 拉取、api_push API 推送")
    private String accessMode;

    /** 使用状态：enabled 启用、paused 暂停、disabled 停用、unavailable 暂时不可用 */
    @Excel(name = "使用状态：enabled 启用、paused 暂停、disabled 停用、unavailable 暂时不可用")
    private String useStatus;

    /** 连接状态：untested 未测试、success 连接成功、failed 连接失败 */
    @Excel(name = "连接状态：untested 未测试、success 连接成功、failed 连接失败")
    private String connectionStatus;

    /** 最近测试时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最近测试时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastTestTime;

    /** 最近测试结果说明 */
    @Excel(name = "最近测试结果说明")
    private String lastTestMessage;

    /** 最新模式快照 ID */
    @Excel(name = "最新模式快照 ID")
    private Long latestSchemaSnapshotId;

    /** 数据库直连数据源明细 */
    private DbDatasource dbDetail;

    /** API 拉取数据源明细 */
    private ApiPullDatasource apiPullDetail;

    /** API 推送数据源明细 */
    private ApiPushDatasource apiPushDetail;

    /** 若依状态：0正常，1停用 */
    @Excel(name = "若依状态：0正常，1停用")
    private String status;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setDatasourceId(Long datasourceId) 
    {
        this.datasourceId = datasourceId;
    }

    public Long getDatasourceId() 
    {
        return datasourceId;
    }

    public void setDatasourceName(String datasourceName) 
    {
        this.datasourceName = datasourceName;
    }

    public String getDatasourceName() 
    {
        return datasourceName;
    }

    public void setAccessMode(String accessMode) 
    {
        this.accessMode = accessMode;
    }

    public String getAccessMode() 
    {
        return accessMode;
    }

    public void setUseStatus(String useStatus) 
    {
        this.useStatus = useStatus;
    }

    public String getUseStatus() 
    {
        return useStatus;
    }

    public void setConnectionStatus(String connectionStatus) 
    {
        this.connectionStatus = connectionStatus;
    }

    public String getConnectionStatus() 
    {
        return connectionStatus;
    }

    public void setLastTestTime(Date lastTestTime) 
    {
        this.lastTestTime = lastTestTime;
    }

    public Date getLastTestTime() 
    {
        return lastTestTime;
    }

    public void setLastTestMessage(String lastTestMessage) 
    {
        this.lastTestMessage = lastTestMessage;
    }

    public String getLastTestMessage() 
    {
        return lastTestMessage;
    }

    public void setLatestSchemaSnapshotId(Long latestSchemaSnapshotId) 
    {
        this.latestSchemaSnapshotId = latestSchemaSnapshotId;
    }

    public Long getLatestSchemaSnapshotId() 
    {
        return latestSchemaSnapshotId;
    }

    public void setDbDetail(DbDatasource dbDetail)
    {
        this.dbDetail = dbDetail;
    }

    public DbDatasource getDbDetail()
    {
        return dbDetail;
    }

    public void setApiPullDetail(ApiPullDatasource apiPullDetail)
    {
        this.apiPullDetail = apiPullDetail;
    }

    public ApiPullDatasource getApiPullDetail()
    {
        return apiPullDetail;
    }

    public void setApiPushDetail(ApiPushDatasource apiPushDetail)
    {
        this.apiPushDetail = apiPushDetail;
    }

    public ApiPushDatasource getApiPushDetail()
    {
        return apiPushDetail;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
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
            .append("datasourceId", getDatasourceId())
            .append("datasourceName", getDatasourceName())
            .append("accessMode", getAccessMode())
            .append("useStatus", getUseStatus())
            .append("connectionStatus", getConnectionStatus())
            .append("lastTestTime", getLastTestTime())
            .append("lastTestMessage", getLastTestMessage())
            .append("latestSchemaSnapshotId", getLatestSchemaSnapshotId())
            .append("dbDetail", getDbDetail())
            .append("apiPullDetail", getApiPullDetail())
            .append("apiPushDetail", getApiPushDetail())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
