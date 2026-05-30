package com.ruoyi.topic5.mapper;

import java.util.List;
import com.ruoyi.topic5.domain.T5AlgorithmRun;

/**
 * 课题五-算法运行记录Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface T5AlgorithmRunMapper 
{
    /**
     * 查询课题五-算法运行记录
     * 
     * @param runId 课题五-算法运行记录主键
     * @return 课题五-算法运行记录
     */
    public T5AlgorithmRun selectT5AlgorithmRunByRunId(Long runId);

    /**
     * 查询课题五-算法运行记录列表
     * 
     * @param t5AlgorithmRun 课题五-算法运行记录
     * @return 课题五-算法运行记录集合
     */
    public List<T5AlgorithmRun> selectT5AlgorithmRunList(T5AlgorithmRun t5AlgorithmRun);

    /**
     * 新增课题五-算法运行记录
     * 
     * @param t5AlgorithmRun 课题五-算法运行记录
     * @return 结果
     */
    public int insertT5AlgorithmRun(T5AlgorithmRun t5AlgorithmRun);

    /**
     * 修改课题五-算法运行记录
     * 
     * @param t5AlgorithmRun 课题五-算法运行记录
     * @return 结果
     */
    public int updateT5AlgorithmRun(T5AlgorithmRun t5AlgorithmRun);

    /**
     * 删除课题五-算法运行记录
     * 
     * @param runId 课题五-算法运行记录主键
     * @return 结果
     */
    public int deleteT5AlgorithmRunByRunId(Long runId);

    /**
     * 批量删除课题五-算法运行记录
     * 
     * @param runIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteT5AlgorithmRunByRunIds(Long[] runIds);
}
