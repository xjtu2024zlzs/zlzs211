package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.LlmConfigMapper;
import com.ruoyi.project1.domain.LlmConfig;
import com.ruoyi.project1.service.ILlmConfigService;

/**
 * LLM 配置Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class LlmConfigServiceImpl implements ILlmConfigService 
{
    @Autowired
    private LlmConfigMapper llmConfigMapper;

    /**
     * 查询LLM 配置
     * 
     * @param llmConfigId LLM 配置主键
     * @return LLM 配置
     */
    @Override
    public LlmConfig selectLlmConfigByLlmConfigId(Long llmConfigId)
    {
        return llmConfigMapper.selectLlmConfigByLlmConfigId(llmConfigId);
    }

    /**
     * 查询LLM 配置列表
     * 
     * @param llmConfig LLM 配置
     * @return LLM 配置
     */
    @Override
    public List<LlmConfig> selectLlmConfigList(LlmConfig llmConfig)
    {
        return llmConfigMapper.selectLlmConfigList(llmConfig);
    }

    /**
     * 新增LLM 配置
     * 
     * @param llmConfig LLM 配置
     * @return 结果
     */
    @Override
    public int insertLlmConfig(LlmConfig llmConfig)
    {
        llmConfig.setCreateTime(DateUtils.getNowDate());
        return llmConfigMapper.insertLlmConfig(llmConfig);
    }

    /**
     * 修改LLM 配置
     * 
     * @param llmConfig LLM 配置
     * @return 结果
     */
    @Override
    public int updateLlmConfig(LlmConfig llmConfig)
    {
        llmConfig.setUpdateTime(DateUtils.getNowDate());
        return llmConfigMapper.updateLlmConfig(llmConfig);
    }

    /**
     * 批量删除LLM 配置
     * 
     * @param llmConfigIds 需要删除的LLM 配置主键
     * @return 结果
     */
    @Override
    public int deleteLlmConfigByLlmConfigIds(Long[] llmConfigIds)
    {
        return llmConfigMapper.deleteLlmConfigByLlmConfigIds(llmConfigIds);
    }

    /**
     * 删除LLM 配置信息
     * 
     * @param llmConfigId LLM 配置主键
     * @return 结果
     */
    @Override
    public int deleteLlmConfigByLlmConfigId(Long llmConfigId)
    {
        return llmConfigMapper.deleteLlmConfigByLlmConfigId(llmConfigId);
    }
}
