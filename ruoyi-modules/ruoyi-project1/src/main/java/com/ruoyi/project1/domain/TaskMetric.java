package com.ruoyi.project1.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 模式映射评估指标对象 p1p_task_metric
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class TaskMetric extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 评估指标主键 */
    private Long metricId;

    /** 任务 ID */
    @Excel(name = "任务 ID")
    private Long taskId;

    /** 任务版本 ID */
    @Excel(name = "任务版本 ID")
    private Long versionId;

    /** 运行记录 ID */
    @Excel(name = "运行记录 ID")
    private Long recordId;

    /** 结果集 ID */
    @Excel(name = "结果集 ID")
    private Long resultSetId;

    /** 指标键，如 MRR、Recall@20、All F1Score */
    @Excel(name = "指标键，如 MRR、Recall@20、All F1Score")
    private String metricKey;

    /** 指标显示名称 */
    @Excel(name = "指标显示名称")
    private String metricName;

    /** 指标数值 */
    @Excel(name = "指标数值")
    private BigDecimal metricValue;

    /** 图表类型，如 bar、stacked_bar、summary */
    @Excel(name = "图表类型，如 bar、stacked_bar、summary")
    private String chartType;

    /** 图表展示数据 JSON */
    @Excel(name = "图表展示数据 JSON")
    private String chartData;

    /** 算法结果目录 */
    @Excel(name = "算法结果目录")
    private String resultDir;

    public void setMetricId(Long metricId) 
    {
        this.metricId = metricId;
    }

    public Long getMetricId() 
    {
        return metricId;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setVersionId(Long versionId) 
    {
        this.versionId = versionId;
    }

    public Long getVersionId() 
    {
        return versionId;
    }

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }

    public void setResultSetId(Long resultSetId) 
    {
        this.resultSetId = resultSetId;
    }

    public Long getResultSetId() 
    {
        return resultSetId;
    }

    public void setMetricKey(String metricKey) 
    {
        this.metricKey = metricKey;
    }

    public String getMetricKey() 
    {
        return metricKey;
    }

    public void setMetricName(String metricName) 
    {
        this.metricName = metricName;
    }

    public String getMetricName() 
    {
        return metricName;
    }

    public void setMetricValue(BigDecimal metricValue) 
    {
        this.metricValue = metricValue;
    }

    public BigDecimal getMetricValue() 
    {
        return metricValue;
    }

    public void setChartType(String chartType) 
    {
        this.chartType = chartType;
    }

    public String getChartType() 
    {
        return chartType;
    }

    public void setChartData(String chartData) 
    {
        this.chartData = chartData;
    }

    public String getChartData() 
    {
        return chartData;
    }

    public void setResultDir(String resultDir) 
    {
        this.resultDir = resultDir;
    }

    public String getResultDir() 
    {
        return resultDir;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("metricId", getMetricId())
            .append("taskId", getTaskId())
            .append("versionId", getVersionId())
            .append("recordId", getRecordId())
            .append("resultSetId", getResultSetId())
            .append("metricKey", getMetricKey())
            .append("metricName", getMetricName())
            .append("metricValue", getMetricValue())
            .append("chartType", getChartType())
            .append("chartData", getChartData())
            .append("resultDir", getResultDir())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
