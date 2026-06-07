package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.EmbeddingModelMapper;
import com.ruoyi.project1.domain.EmbeddingModel;
import com.ruoyi.project1.service.IEmbeddingModelService;

/**
 * 嵌入模型配置Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class EmbeddingModelServiceImpl implements IEmbeddingModelService 
{
    @Autowired
    private EmbeddingModelMapper embeddingModelMapper;

    /**
     * 查询嵌入模型配置
     * 
     * @param embeddingModelKey 嵌入模型配置主键
     * @return 嵌入模型配置
     */
    @Override
    public EmbeddingModel selectEmbeddingModelByEmbeddingModelKey(String embeddingModelKey)
    {
        return embeddingModelMapper.selectEmbeddingModelByEmbeddingModelKey(embeddingModelKey);
    }

    /**
     * 查询嵌入模型配置列表
     * 
     * @param embeddingModel 嵌入模型配置
     * @return 嵌入模型配置
     */
    @Override
    public List<EmbeddingModel> selectEmbeddingModelList(EmbeddingModel embeddingModel)
    {
        return embeddingModelMapper.selectEmbeddingModelList(embeddingModel);
    }

    /**
     * 新增嵌入模型配置
     * 
     * @param embeddingModel 嵌入模型配置
     * @return 结果
     */
    @Override
    public int insertEmbeddingModel(EmbeddingModel embeddingModel)
    {
        embeddingModel.setCreateTime(DateUtils.getNowDate());
        return embeddingModelMapper.insertEmbeddingModel(embeddingModel);
    }

    /**
     * 修改嵌入模型配置
     * 
     * @param embeddingModel 嵌入模型配置
     * @return 结果
     */
    @Override
    public int updateEmbeddingModel(EmbeddingModel embeddingModel)
    {
        embeddingModel.setUpdateTime(DateUtils.getNowDate());
        return embeddingModelMapper.updateEmbeddingModel(embeddingModel);
    }

    /**
     * 批量删除嵌入模型配置
     * 
     * @param embeddingModelKeys 需要删除的嵌入模型配置主键
     * @return 结果
     */
    @Override
    public int deleteEmbeddingModelByEmbeddingModelKeys(String[] embeddingModelKeys)
    {
        return embeddingModelMapper.deleteEmbeddingModelByEmbeddingModelKeys(embeddingModelKeys);
    }

    /**
     * 删除嵌入模型配置信息
     * 
     * @param embeddingModelKey 嵌入模型配置主键
     * @return 结果
     */
    @Override
    public int deleteEmbeddingModelByEmbeddingModelKey(String embeddingModelKey)
    {
        return embeddingModelMapper.deleteEmbeddingModelByEmbeddingModelKey(embeddingModelKey);
    }
}
