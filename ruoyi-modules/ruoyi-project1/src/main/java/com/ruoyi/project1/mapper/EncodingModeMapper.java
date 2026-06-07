package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.EncodingMode;

/**
 * 编码模式配置Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface EncodingModeMapper 
{
    /**
     * 查询编码模式配置
     * 
     * @param encodingModeKey 编码模式配置主键
     * @return 编码模式配置
     */
    public EncodingMode selectEncodingModeByEncodingModeKey(String encodingModeKey);

    /**
     * 查询编码模式配置列表
     * 
     * @param encodingMode 编码模式配置
     * @return 编码模式配置集合
     */
    public List<EncodingMode> selectEncodingModeList(EncodingMode encodingMode);

    /**
     * 新增编码模式配置
     * 
     * @param encodingMode 编码模式配置
     * @return 结果
     */
    public int insertEncodingMode(EncodingMode encodingMode);

    /**
     * 修改编码模式配置
     * 
     * @param encodingMode 编码模式配置
     * @return 结果
     */
    public int updateEncodingMode(EncodingMode encodingMode);

    /**
     * 删除编码模式配置
     * 
     * @param encodingModeKey 编码模式配置主键
     * @return 结果
     */
    public int deleteEncodingModeByEncodingModeKey(String encodingModeKey);

    /**
     * 批量删除编码模式配置
     * 
     * @param encodingModeKeys 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteEncodingModeByEncodingModeKeys(String[] encodingModeKeys);
}
