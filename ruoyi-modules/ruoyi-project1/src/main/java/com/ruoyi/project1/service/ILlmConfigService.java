package com.ruoyi.project1.service;

import java.util.List;
import com.ruoyi.project1.domain.LlmConfig;

/**
 * LLM 配置Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface ILlmConfigService 
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
     * 批量删除LLM 配置
     * 
     * @param llmConfigIds 需要删除的LLM 配置主键集合
     * @return 结果
     */
    public int deleteLlmConfigByLlmConfigIds(Long[] llmConfigIds);

    /**
     * 删除LLM 配置信息
     * 
     * @param llmConfigId LLM 配置主键
     * @return 结果
     */
    public int deleteLlmConfigByLlmConfigId(Long llmConfigId);
}
