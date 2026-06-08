package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.LlmConfig;

/**
 * LLM 配置Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface LlmConfigMapper 
{
    /**
     * 查询LLM 配置
     * 
     * @param llmConfigId LLM 配置主键
     * @return LLM 配置
     */
    public LlmConfig selectLlmConfigByLlmConfigId(Long llmConfigId);

    /**
     * 查询LLM 配置列表
     * 
     * @param llmConfig LLM 配置
     * @return LLM 配置集合
     */
    public List<LlmConfig> selectLlmConfigList(LlmConfig llmConfig);

    /**
     * 新增LLM 配置
     * 
     * @param llmConfig LLM 配置
     * @return 结果
     */
    public int insertLlmConfig(LlmConfig llmConfig);

    /**
     * 修改LLM 配置
     * 
     * @param llmConfig LLM 配置
     * @return 结果
     */
    public int updateLlmConfig(LlmConfig llmConfig);

    /**
     * 删除LLM 配置
     * 
     * @param llmConfigId LLM 配置主键
     * @return 结果
     */
    public int deleteLlmConfigByLlmConfigId(Long llmConfigId);

    /**
     * 批量删除LLM 配置
     * 
     * @param llmConfigIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLlmConfigByLlmConfigIds(Long[] llmConfigIds);
}
