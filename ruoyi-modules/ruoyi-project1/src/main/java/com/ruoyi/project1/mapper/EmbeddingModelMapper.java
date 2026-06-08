package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.EmbeddingModel;

/**
 * 嵌入模型配置Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface EmbeddingModelMapper 
{
    /**
     * 查询嵌入模型配置
     * 
     * @param embeddingModelKey 嵌入模型配置主键
     * @return 嵌入模型配置
     */
    public EmbeddingModel selectEmbeddingModelByEmbeddingModelKey(String embeddingModelKey);

    /**
     * 查询嵌入模型配置列表
     * 
     * @param embeddingModel 嵌入模型配置
     * @return 嵌入模型配置集合
     */
    public List<EmbeddingModel> selectEmbeddingModelList(EmbeddingModel embeddingModel);

    /**
     * 新增嵌入模型配置
     * 
     * @param embeddingModel 嵌入模型配置
     * @return 结果
     */
    public int insertEmbeddingModel(EmbeddingModel embeddingModel);

    /**
     * 修改嵌入模型配置
     * 
     * @param embeddingModel 嵌入模型配置
     * @return 结果
     */
    public int updateEmbeddingModel(EmbeddingModel embeddingModel);

    /**
     * 删除嵌入模型配置
     * 
     * @param embeddingModelKey 嵌入模型配置主键
     * @return 结果
     */
    public int deleteEmbeddingModelByEmbeddingModelKey(String embeddingModelKey);

    /**
     * 批量删除嵌入模型配置
     * 
     * @param embeddingModelKeys 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteEmbeddingModelByEmbeddingModelKeys(String[] embeddingModelKeys);
}
