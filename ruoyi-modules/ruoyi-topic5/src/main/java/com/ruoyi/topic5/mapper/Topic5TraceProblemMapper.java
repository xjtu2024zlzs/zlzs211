package com.ruoyi.topic5.mapper;

import java.util.List;
import com.ruoyi.topic5.domain.Topic5TraceProblem;

/**
 * 追溯问题Mapper接口
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
public interface Topic5TraceProblemMapper 
{
    /**
     * 查询追溯问题
     * 
     * @param id 追溯问题主键
     * @return 追溯问题
     */
    public Topic5TraceProblem selectTopic5TraceProblemById(Long id);

    /**
     * 查询追溯问题列表
     * 
     * @param topic5TraceProblem 追溯问题
     * @return 追溯问题集合
     */
    public List<Topic5TraceProblem> selectTopic5TraceProblemList(Topic5TraceProblem topic5TraceProblem);

    /**
     * 新增追溯问题
     * 
     * @param topic5TraceProblem 追溯问题
     * @return 结果
     */
    public int insertTopic5TraceProblem(Topic5TraceProblem topic5TraceProblem);

    /**
     * 修改追溯问题
     * 
     * @param topic5TraceProblem 追溯问题
     * @return 结果
     */
    public int updateTopic5TraceProblem(Topic5TraceProblem topic5TraceProblem);

    /**
     * 删除追溯问题
     * 
     * @param id 追溯问题主键
     * @return 结果
     */
    public int deleteTopic5TraceProblemById(Long id);

    /**
     * 批量删除追溯问题
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTopic5TraceProblemByIds(Long[] ids);
}
