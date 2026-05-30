package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5AlgorithmRunMapper;
import com.ruoyi.topic5.domain.T5AlgorithmRun;
import com.ruoyi.topic5.service.IT5AlgorithmRunService;

/**
 * 课题五-算法运行记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class T5AlgorithmRunServiceImpl implements IT5AlgorithmRunService 
{
    @Autowired
    private T5AlgorithmRunMapper t5AlgorithmRunMapper;

    /**
     * 查询课题五-算法运行记录
     * 
     * @param runId 课题五-算法运行记录主键
     * @return 课题五-算法运行记录
     */
    @Override
    public T5AlgorithmRun selectT5AlgorithmRunByRunId(Long runId)
    {
        return t5AlgorithmRunMapper.selectT5AlgorithmRunByRunId(runId);
    }

    /**
     * 查询课题五-算法运行记录列表
     * 
     * @param t5AlgorithmRun 课题五-算法运行记录
     * @return 课题五-算法运行记录
     */
    @Override
    public List<T5AlgorithmRun> selectT5AlgorithmRunList(T5AlgorithmRun t5AlgorithmRun)
    {
        return t5AlgorithmRunMapper.selectT5AlgorithmRunList(t5AlgorithmRun);
    }

    /**
     * 新增课题五-算法运行记录
     * 
     * @param t5AlgorithmRun 课题五-算法运行记录
     * @return 结果
     */
    @Override
    public int insertT5AlgorithmRun(T5AlgorithmRun t5AlgorithmRun)
    {
        t5AlgorithmRun.setCreateTime(DateUtils.getNowDate());
        return t5AlgorithmRunMapper.insertT5AlgorithmRun(t5AlgorithmRun);
    }

    /**
     * 修改课题五-算法运行记录
     * 
     * @param t5AlgorithmRun 课题五-算法运行记录
     * @return 结果
     */
    @Override
    public int updateT5AlgorithmRun(T5AlgorithmRun t5AlgorithmRun)
    {
        t5AlgorithmRun.setUpdateTime(DateUtils.getNowDate());
        return t5AlgorithmRunMapper.updateT5AlgorithmRun(t5AlgorithmRun);
    }

    /**
     * 批量删除课题五-算法运行记录
     * 
     * @param runIds 需要删除的课题五-算法运行记录主键
     * @return 结果
     */
    @Override
    public int deleteT5AlgorithmRunByRunIds(Long[] runIds)
    {
        return t5AlgorithmRunMapper.deleteT5AlgorithmRunByRunIds(runIds);
    }

    /**
     * 删除课题五-算法运行记录信息
     * 
     * @param runId 课题五-算法运行记录主键
     * @return 结果
     */
    @Override
    public int deleteT5AlgorithmRunByRunId(Long runId)
    {
        return t5AlgorithmRunMapper.deleteT5AlgorithmRunByRunId(runId);
    }
}
