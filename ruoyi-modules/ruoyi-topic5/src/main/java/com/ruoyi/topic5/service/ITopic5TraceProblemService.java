package com.ruoyi.topic5.service;

import java.util.List;
import com.ruoyi.topic5.domain.Topic5TraceProblem;
import java.util.List;
import java.util.Map;
import com.ruoyi.topic5.domain.Topic5TraceAttachment;
import com.ruoyi.topic5.domain.Topic5TraceProblem;
import com.ruoyi.topic5.domain.dto.Topic4CallbackDTO;
import java.util.Map;

/**
 * 追溯问题Service接口
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
public interface ITopic5TraceProblemService 
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
     * 批量删除追溯问题
     * 
     * @param ids 需要删除的追溯问题主键集合
     * @return 结果
     */
    public int deleteTopic5TraceProblemByIds(Long[] ids);

    /**
     * 删除追溯问题信息
     * 
     * @param id 追溯问题主键
     * @return 结果
     */
    public int deleteTopic5TraceProblemById(Long id);
    /**
     * 获取追溯任务下拉框
     */
    public List<Topic5TraceProblem> selectTraceOptions();

    /**
     * 模拟从课题一数字卷宗调用传感器文件
     */
    public List<Topic5TraceAttachment> importDossierFiles(Long id);

    /**
     * 保存数字卷宗附件
     */
    public void saveDossierAttachments(Long id);

    /**
     * 推送给课题四
     */
    public void pushToTopic4(Long id);

    /**
     * 接收课题四回调
     */
    public void receiveTopic4Callback(Topic4CallbackDTO dto);

    /**
     * 将课题四结果回填到追溯问题表
     */
    public void fillTopic4Result(Long id);

    /**
     * 查询当前流程阶段
     */
    public Long getWorkflowStage(Long id);

    /**
     * 模拟运行 Python 追溯算法
     */
    public Map<String, Object> runAlgorithmMock(Long id);

    /**
     * 保存附件并同步附件保存位置
     */
    void saveDossierAttachmentsWithPath(Long id, String attachmentSavePath);

    /**
     * 从课题一拉取知识图谱
     */
    public Map<String, Object> pullTopic1Kg(Long id);

    /**
     * 运行第二部分算法
     */
    public Map<String, Object> runSecondAlgorithm(Long id, String algorithmName);

    /**
     * 查询追溯任务知识图谱信息
     */
    public Map<String, Object> getTraceKg(Long id);

    /**
     * 人工判定第二部分算法结果
     */
    public void confirmSecondAlgorithmResult(Long id, Boolean saveFlag, String remark);
    /**
     * 运行最终溯源算法
     */
    public Map<String, Object> runSourceAlgorithm(Long id, String algorithmName);

    /**
     * 获取最终溯源结果
     */
    public Map<String, Object> getSourceResult(Long id);

    /**
     * 导出最终溯源报告
     */
    public Map<String, Object> exportSourceReport(Long id);

    /**
     * 推送课题二
     */
    public void pushTopic2(Long id);

    /**
     * 推送课题三
     */
    public void pushTopic3(Long id);


}
