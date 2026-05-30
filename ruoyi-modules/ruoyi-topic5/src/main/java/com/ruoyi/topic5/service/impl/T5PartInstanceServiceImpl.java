package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5PartInstanceMapper;
import com.ruoyi.topic5.domain.T5PartInstance;
import com.ruoyi.topic5.service.IT5PartInstanceService;

/**
 * 课题五-零部件实例Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class T5PartInstanceServiceImpl implements IT5PartInstanceService 
{
    @Autowired
    private T5PartInstanceMapper t5PartInstanceMapper;

    /**
     * 查询课题五-零部件实例
     * 
     * @param partId 课题五-零部件实例主键
     * @return 课题五-零部件实例
     */
    @Override
    public T5PartInstance selectT5PartInstanceByPartId(Long partId)
    {
        return t5PartInstanceMapper.selectT5PartInstanceByPartId(partId);
    }

    /**
     * 查询课题五-零部件实例列表
     * 
     * @param t5PartInstance 课题五-零部件实例
     * @return 课题五-零部件实例
     */
    @Override
    public List<T5PartInstance> selectT5PartInstanceList(T5PartInstance t5PartInstance)
    {
        return t5PartInstanceMapper.selectT5PartInstanceList(t5PartInstance);
    }

    /**
     * 新增课题五-零部件实例
     * 
     * @param t5PartInstance 课题五-零部件实例
     * @return 结果
     */
    @Override
    public int insertT5PartInstance(T5PartInstance t5PartInstance)
    {
        t5PartInstance.setCreateTime(DateUtils.getNowDate());
        return t5PartInstanceMapper.insertT5PartInstance(t5PartInstance);
    }

    /**
     * 修改课题五-零部件实例
     * 
     * @param t5PartInstance 课题五-零部件实例
     * @return 结果
     */
    @Override
    public int updateT5PartInstance(T5PartInstance t5PartInstance)
    {
        t5PartInstance.setUpdateTime(DateUtils.getNowDate());
        return t5PartInstanceMapper.updateT5PartInstance(t5PartInstance);
    }

    /**
     * 批量删除课题五-零部件实例
     * 
     * @param partIds 需要删除的课题五-零部件实例主键
     * @return 结果
     */
    @Override
    public int deleteT5PartInstanceByPartIds(Long[] partIds)
    {
        return t5PartInstanceMapper.deleteT5PartInstanceByPartIds(partIds);
    }

    /**
     * 删除课题五-零部件实例信息
     * 
     * @param partId 课题五-零部件实例主键
     * @return 结果
     */
    @Override
    public int deleteT5PartInstanceByPartId(Long partId)
    {
        return t5PartInstanceMapper.deleteT5PartInstanceByPartId(partId);
    }
}
