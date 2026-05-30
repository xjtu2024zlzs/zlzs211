package com.ruoyi.topic5.service;

import java.util.List;
import com.ruoyi.topic5.domain.T5PartInstance;

/**
 * 课题五-零部件实例Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface IT5PartInstanceService 
{
    /**
     * 查询课题五-零部件实例
     * 
     * @param partId 课题五-零部件实例主键
     * @return 课题五-零部件实例
     */
    public T5PartInstance selectT5PartInstanceByPartId(Long partId);

    /**
     * 查询课题五-零部件实例列表
     * 
     * @param t5PartInstance 课题五-零部件实例
     * @return 课题五-零部件实例集合
     */
    public List<T5PartInstance> selectT5PartInstanceList(T5PartInstance t5PartInstance);

    /**
     * 新增课题五-零部件实例
     * 
     * @param t5PartInstance 课题五-零部件实例
     * @return 结果
     */
    public int insertT5PartInstance(T5PartInstance t5PartInstance);

    /**
     * 修改课题五-零部件实例
     * 
     * @param t5PartInstance 课题五-零部件实例
     * @return 结果
     */
    public int updateT5PartInstance(T5PartInstance t5PartInstance);

    /**
     * 批量删除课题五-零部件实例
     * 
     * @param partIds 需要删除的课题五-零部件实例主键集合
     * @return 结果
     */
    public int deleteT5PartInstanceByPartIds(Long[] partIds);

    /**
     * 删除课题五-零部件实例信息
     * 
     * @param partId 课题五-零部件实例主键
     * @return 结果
     */
    public int deleteT5PartInstanceByPartId(Long partId);
}
