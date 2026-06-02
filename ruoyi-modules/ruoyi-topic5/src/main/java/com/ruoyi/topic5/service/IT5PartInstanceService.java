package com.ruoyi.topic5.service;

import java.util.List;
import com.ruoyi.topic5.domain.T5PartInstance;

/**
 * 追溯案例Service接口
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
public interface IT5PartInstanceService 
{
    /**
     * 查询追溯案例
     * 
     * @param partId 追溯案例主键
     * @return 追溯案例
     */
    public T5PartInstance selectT5PartInstanceByPartId(Long partId);

    /**
     * 查询追溯案例列表
     * 
     * @param t5PartInstance 追溯案例
     * @return 追溯案例集合
     */
    public List<T5PartInstance> selectT5PartInstanceList(T5PartInstance t5PartInstance);

    /**
     * 新增追溯案例
     * 
     * @param t5PartInstance 追溯案例
     * @return 结果
     */
    public int insertT5PartInstance(T5PartInstance t5PartInstance);

    /**
     * 修改追溯案例
     * 
     * @param t5PartInstance 追溯案例
     * @return 结果
     */
    public int updateT5PartInstance(T5PartInstance t5PartInstance);

    /**
     * 批量删除追溯案例
     * 
     * @param partIds 需要删除的追溯案例主键集合
     * @return 结果
     */
    public int deleteT5PartInstanceByPartIds(Long[] partIds);

    /**
     * 删除追溯案例信息
     * 
     * @param partId 追溯案例主键
     * @return 结果
     */
    public int deleteT5PartInstanceByPartId(Long partId);
}
