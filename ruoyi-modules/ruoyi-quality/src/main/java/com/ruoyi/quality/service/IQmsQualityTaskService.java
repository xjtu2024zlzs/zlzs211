package com.ruoyi.quality.service;
import com.ruoyi.qms.api.domain.QualityTaskDto;
import java.util.List;
import com.ruoyi.quality.domain.QmsQualityTask;
import com.ruoyi.qms.api.domain.QualityTaskSubmitDto;
import com.ruoyi.qms.api.domain.QualityTaskDto;
/**
 * 质量问题模块处理任务Service接口
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
public interface IQmsQualityTaskService 
{
    /**
     * 模块提交质量任务处理结果
     *
     * @param submitDto 提交结果
     */
    public void submitQualityTaskResult(QualityTaskSubmitDto submitDto);
    /**
     * 查询质量问题模块处理任务
     * 
     * @param taskId 质量问题模块处理任务主键
     * @return 质量问题模块处理任务
     */
    public QmsQualityTask selectQmsQualityTaskByTaskId(Long taskId);

    /**
     * 查询质量问题模块处理任务列表
     * 
     * @param qmsQualityTask 质量问题模块处理任务
     * @return 质量问题模块处理任务集合
     */
    public List<QmsQualityTask> selectQmsQualityTaskList(QmsQualityTask qmsQualityTask);

    /**
     * 新增质量问题模块处理任务
     * 
     * @param qmsQualityTask 质量问题模块处理任务
     * @return 结果
     */
    public int insertQmsQualityTask(QmsQualityTask qmsQualityTask);

    /**
     * 修改质量问题模块处理任务
     * 
     * @param qmsQualityTask 质量问题模块处理任务
     * @return 结果
     */
    public int updateQmsQualityTask(QmsQualityTask qmsQualityTask);

    /**
     * 批量删除质量问题模块处理任务
     * 
     * @param taskIds 需要删除的质量问题模块处理任务主键集合
     * @return 结果
     */
    public int deleteQmsQualityTaskByTaskIds(Long[] taskIds);

    /**
     * 删除质量问题模块处理任务信息
     * 
     * @param taskId 质量问题模块处理任务主键
     * @return 结果
     */
    public int deleteQmsQualityTaskByTaskId(Long taskId);
    /**
     * 查询分派给指定模块的质量任务列表
     *
     * @param moduleCode 模块编码
     * @return 任务列表
     */
    public List<QualityTaskDto> selectQualityTaskDtoListForModule(String moduleCode);

    /**
     * 根据质量问题ID和模块编码查询指定模块任务
     *
     * @param problemId 质量问题ID
     * @param moduleCode 模块编码
     * @return 质量任务DTO
     */
    public QualityTaskDto selectQualityTaskDtoByProblemIdAndModuleCode(Long problemId, String moduleCode);
}
