package com.ruoyi.project4.domain.dto;

import java.util.List;

/**
 * 样本增强运行参数 DTO
 *
 * 前端传入选中样本、增强算法、增强倍数、流程号；
 * 后端根据 sampleIds 查询真实样本与 .mat 文件路径；
 * 生成 augment_input.json；
 * 调用 augment.py；
 * 读取 augment_result.json；
 * 写入 fd_augment_result 表。
 */
public class AugmentRunDto
{
    /** 选中的预处理样本ID列表 */
    private List<Long> sampleIds;

    /** 增强算法，前端传入，例如 random / noise / scale / SMOTE */
    private String augAlgorithm;

    /** 增强倍数，前端传入 */
    private Integer augMultiple;

    /**
     * 流程ID
     * 注意：前端流程号形如 P-1781377372797，所以这里必须用 String，不能用 Long。
     */
    private String pipelineId;

    /** 数据集ID，测试阶段默认 1 */
    private Long datasetId = 1L;

    /** 原始样本ID，对应 fd_augment_result.raw_sample_id */
    private Long rawSampleId;

    /** 原始样本编号，对应 fd_augment_result.raw_sample_code */
    private String rawSampleCode;

    /** 样本ID，兼容前端旧字段；若 rawSampleId 为空，则使用 sampleId */
    private Long sampleId;

    /** 样本编号，兼容前端旧字段；若 rawSampleCode 为空，则使用 sampleCode */
    private String sampleCode;

    /** CWRU .mat 文件路径 */
    private String filePath;

    /** CWRU 文件编号，例如 97、108、121、130、174、226 */
    private Integer keyNum;

    /** 信号长度，默认 1024 */
    private Integer length = 1024;

    /** 增强倍数，兼容 Python 参数名，默认 2 */
    private Integer augmentRatio = 2;

    /** 样本标签，默认 1 */
    private Integer label = 1;

    public List<Long> getSampleIds()
    {
        return sampleIds;
    }

    public void setSampleIds(List<Long> sampleIds)
    {
        this.sampleIds = sampleIds;
    }

    public String getAugAlgorithm()
    {
        return augAlgorithm;
    }

    public void setAugAlgorithm(String augAlgorithm)
    {
        this.augAlgorithm = augAlgorithm;
    }

    public Integer getAugMultiple()
    {
        return augMultiple;
    }

    public void setAugMultiple(Integer augMultiple)
    {
        this.augMultiple = augMultiple;
    }

    public String getPipelineId()
    {
        return pipelineId;
    }

    public void setPipelineId(String pipelineId)
    {
        this.pipelineId = pipelineId;
    }

    public Long getDatasetId()
    {
        return datasetId;
    }

    public void setDatasetId(Long datasetId)
    {
        this.datasetId = datasetId;
    }

    public Long getRawSampleId()
    {
        return rawSampleId;
    }

    public void setRawSampleId(Long rawSampleId)
    {
        this.rawSampleId = rawSampleId;
    }

    public String getRawSampleCode()
    {
        return rawSampleCode;
    }

    public void setRawSampleCode(String rawSampleCode)
    {
        this.rawSampleCode = rawSampleCode;
    }

    public Long getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(Long sampleId)
    {
        this.sampleId = sampleId;
    }

    public String getSampleCode()
    {
        return sampleCode;
    }

    public void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public Integer getKeyNum()
    {
        return keyNum;
    }

    public void setKeyNum(Integer keyNum)
    {
        this.keyNum = keyNum;
    }

    public Integer getLength()
    {
        return length;
    }

    public void setLength(Integer length)
    {
        this.length = length;
    }

    public Integer getAugmentRatio()
    {
        return augmentRatio;
    }

    public void setAugmentRatio(Integer augmentRatio)
    {
        this.augmentRatio = augmentRatio;
    }

    public Integer getLabel()
    {
        return label;
    }

    public void setLabel(Integer label)
    {
        this.label = label;
    }
}