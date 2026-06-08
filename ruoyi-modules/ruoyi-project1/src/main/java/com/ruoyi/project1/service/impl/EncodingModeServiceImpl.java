package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.EncodingModeMapper;
import com.ruoyi.project1.domain.EncodingMode;
import com.ruoyi.project1.service.IEncodingModeService;

/**
 * 编码模式配置Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class EncodingModeServiceImpl implements IEncodingModeService 
{
    @Autowired
    private EncodingModeMapper encodingModeMapper;

    /**
     * 查询编码模式配置
     * 
     * @param encodingModeKey 编码模式配置主键
     * @return 编码模式配置
     */
    @Override
    public EncodingMode selectEncodingModeByEncodingModeKey(String encodingModeKey)
    {
        return encodingModeMapper.selectEncodingModeByEncodingModeKey(encodingModeKey);
    }

    /**
     * 查询编码模式配置列表
     * 
     * @param encodingMode 编码模式配置
     * @return 编码模式配置
     */
    @Override
    public List<EncodingMode> selectEncodingModeList(EncodingMode encodingMode)
    {
        return encodingModeMapper.selectEncodingModeList(encodingMode);
    }

    /**
     * 新增编码模式配置
     * 
     * @param encodingMode 编码模式配置
     * @return 结果
     */
    @Override
    public int insertEncodingMode(EncodingMode encodingMode)
    {
        encodingMode.setCreateTime(DateUtils.getNowDate());
        return encodingModeMapper.insertEncodingMode(encodingMode);
    }

    /**
     * 修改编码模式配置
     * 
     * @param encodingMode 编码模式配置
     * @return 结果
     */
    @Override
    public int updateEncodingMode(EncodingMode encodingMode)
    {
        encodingMode.setUpdateTime(DateUtils.getNowDate());
        return encodingModeMapper.updateEncodingMode(encodingMode);
    }

    /**
     * 批量删除编码模式配置
     * 
     * @param encodingModeKeys 需要删除的编码模式配置主键
     * @return 结果
     */
    @Override
    public int deleteEncodingModeByEncodingModeKeys(String[] encodingModeKeys)
    {
        return encodingModeMapper.deleteEncodingModeByEncodingModeKeys(encodingModeKeys);
    }

    /**
     * 删除编码模式配置信息
     * 
     * @param encodingModeKey 编码模式配置主键
     * @return 结果
     */
    @Override
    public int deleteEncodingModeByEncodingModeKey(String encodingModeKey)
    {
        return encodingModeMapper.deleteEncodingModeByEncodingModeKey(encodingModeKey);
    }
}
