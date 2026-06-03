package com.ruoyi.topic5.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.topic5.domain.Topic5TraceAttachment;
import com.ruoyi.topic5.domain.Topic5TraceFlowLog;
import com.ruoyi.topic5.domain.Topic5TraceProblem;
import com.ruoyi.topic5.domain.dto.Topic4CallbackDTO;
import com.ruoyi.topic5.mapper.Topic5TraceAttachmentMapper;
import com.ruoyi.topic5.mapper.Topic5TraceFlowLogMapper;
import com.ruoyi.topic5.mapper.Topic5TraceProblemMapper;
import com.ruoyi.topic5.service.ITopic5TraceProblemService;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 追溯问题Service业务层处理
 *
 * @author ruoyi
 * @date 2026-06-03
 */
@Service
public class Topic5TraceProblemServiceImpl implements ITopic5TraceProblemService
{
    @Autowired
    private Topic5TraceProblemMapper topic5TraceProblemMapper;

    @Autowired
    private Topic5TraceAttachmentMapper traceAttachmentMapper;

    @Autowired
    private Topic5TraceFlowLogMapper flowLogMapper;


    /**
     * 查询追溯问题
     *
     * @param id 追溯问题主键
     * @return 追溯问题
     */
    @Override
    public Topic5TraceProblem selectTopic5TraceProblemById(Long id)
    {
        return topic5TraceProblemMapper.selectTopic5TraceProblemById(id);
    }

    /**
     * 查询追溯问题列表
     *
     * @param topic5TraceProblem 追溯问题
     * @return 追溯问题
     */
    @Override
    public List<Topic5TraceProblem> selectTopic5TraceProblemList(Topic5TraceProblem topic5TraceProblem)
    {
        return topic5TraceProblemMapper.selectTopic5TraceProblemList(topic5TraceProblem);
    }

    /**
     * 新增追溯问题
     *
     * @param topic5TraceProblem 追溯问题
     * @return 结果
     */
    @Override
    @Transactional
    public int insertTopic5TraceProblem(Topic5TraceProblem topic5TraceProblem)
    {
        topic5TraceProblem.setCreateTime(DateUtils.getNowDate());

        // 1. 自动生成追溯任务编号
        if (topic5TraceProblem.getTraceNo() == null || "".equals(topic5TraceProblem.getTraceNo()))
        {
            topic5TraceProblem.setTraceNo(generateTraceNo());
        }

        // 2. 初始化流程状态
        topic5TraceProblem.setWorkflowStage(1L);
        topic5TraceProblem.setStatus("未处理");

        // 3. 初始化课题四状态
        topic5TraceProblem.setTopic4Status(0L);
        topic5TraceProblem.setTopic4ResultFilled(0L);

        // 4. 初始化算法状态
        topic5TraceProblem.setAlgorithmStatus(0L);

        topic5TraceProblem.setKgRebuildStatus(0L);
        topic5TraceProblem.setTraceReportStatus(0L);

        // 5. 如果来源为空，默认人工填报
        if (topic5TraceProblem.getSource() == null || "".equals(topic5TraceProblem.getSource()))
        {
            topic5TraceProblem.setSource("人工填报");
        }

        int rows = topic5TraceProblemMapper.insertTopic5TraceProblem(topic5TraceProblem);

        // 6. 新增成功后，写入流程日志
        if (rows > 0 && topic5TraceProblem.getId() != null)
        {
            insertFlowLog(
                    topic5TraceProblem.getId(),
                    1L,
                    "追溯问题填报",
                    "成功",
                    "用户完成新的追溯问题填报"
            );
        }

        return rows;
    }

    /**
     * 修改追溯问题
     *
     * @param topic5TraceProblem 追溯问题
     * @return 结果
     */
    @Override
    public int updateTopic5TraceProblem(Topic5TraceProblem topic5TraceProblem)
    {
        topic5TraceProblem.setUpdateTime(DateUtils.getNowDate());
        return topic5TraceProblemMapper.updateTopic5TraceProblem(topic5TraceProblem);
    }

    /**
     * 批量删除追溯问题
     *
     * @param ids 需要删除的追溯问题主键
     * @return 结果
     */
    @Override
    public int deleteTopic5TraceProblemByIds(Long[] ids)
    {
        return topic5TraceProblemMapper.deleteTopic5TraceProblemByIds(ids);
    }

    /**
     * 删除追溯问题信息
     *
     * @param id 追溯问题主键
     * @return 结果
     */
    @Override
    public int deleteTopic5TraceProblemById(Long id)
    {
        return topic5TraceProblemMapper.deleteTopic5TraceProblemById(id);
    }

    /**
     * 获取追溯任务下拉框
     */
    @Override
    public List<Topic5TraceProblem> selectTraceOptions()
    {
        Topic5TraceProblem query = new Topic5TraceProblem();
        return topic5TraceProblemMapper.selectTopic5TraceProblemList(query);
    }

    /**
     * 模拟从课题一数字卷宗调用传感器文件
     */
    @Override
    @Transactional
    public List<Topic5TraceAttachment> importDossierFiles(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法调用数字卷宗数据");
        }

        // 模拟课题一根据发生时间和部位返回传感器文件路径
        List<Topic5TraceAttachment> list = mockCallTopic1DossierFiles(problem);

        // 建议：这里先把课题一返回的源文件路径保存到附件表，savedFlag = 0
        for (Topic5TraceAttachment item : list)
        {
            item.setTraceId(id);
            item.setSavedFlag(0L);
            item.setFileSource("课题一数字卷宗");
            item.setCreateTime(DateUtils.getNowDate());

            traceAttachmentMapper.insertTopic5TraceAttachment(item);
        }

        insertFlowLog(
                id,
                1L,
                "调用课题一数字卷宗",
                "成功",
                "已根据发生时间和部位获取课题一传感器数据路径"
        );

        return list;
    }
    private List<Topic5TraceAttachment> mockCallTopic1DossierFiles(Topic5TraceProblem problem)
    {
        List<Topic5TraceAttachment> list = new ArrayList<>();

        Topic5TraceAttachment pressureFile = new Topic5TraceAttachment();
        pressureFile.setFileName("pressure_20260603.csv");
        pressureFile.setFileType("csv");
        pressureFile.setSensorType("压力传感器");

        // 这里先用 fileUrl 临时存储源文件路径
        // 如果你后面增加了 sourceFileUrl 字段，则改成 setSourceFileUrl(...)
        pressureFile.setFileUrl("D:/topic1_dossier/pressure_20260603.csv");

        pressureFile.setFileSize("测试文件");
        pressureFile.setRemark("根据故障时间和部位从课题一数字卷宗返回的压力传感器文件");
        list.add(pressureFile);

        return list;
    }

    /**
     * 保存数字卷宗附件
     */
    @Override
    @Transactional
    public void saveDossierAttachments(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法保存附件");
        }

        // 1. 第一版仍然使用模拟附件
        List<Topic5TraceAttachment> list = buildMockDossierFiles(id);

        // 2. 写入附件表
        for (Topic5TraceAttachment item : list)
        {
            item.setSavedFlag(1L);
            item.setCreateTime(DateUtils.getNowDate());
            traceAttachmentMapper.insertTopic5TraceAttachment(item);
        }

        // 3. 更新流程到第3步：追溯任务保存
        //updateWorkflowStage(id, 3L, "数字卷宗传感器附件已保存到追溯任务");
    }

    /**
     * 推送给课题四
     */
    @Override
    @Transactional
    public void pushToTopic4(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法推送课题四");
        }

        // 新流程下，不能再用 workflowStage 判断附件是否保存。
        // 因为 workflowStage = 1 表示问题填报完成，workflowStage = 2 要等课题四结果回填后才更新。
        // 这里应当直接检查附件表中是否已经保存了附件。
        Topic5TraceAttachment attachmentQuery = new Topic5TraceAttachment();
        attachmentQuery.setTraceId(id);
        attachmentQuery.setSavedFlag(1L);

        List<Topic5TraceAttachment> savedAttachments =
                traceAttachmentMapper.selectTopic5TraceAttachmentList(attachmentQuery);

        if (savedAttachments == null || savedAttachments.isEmpty())
        {
            throw new ServiceException("请先完成数字卷宗数据调用和附件保存，再推送课题四");
        }

        Topic5TraceProblem update = new Topic5TraceProblem();
        update.setId(id);
        update.setTopic4Status(1L);
        update.setStatus("处理中");
        update.setUpdateTime(DateUtils.getNowDate());

        // 注意：这里不要再设置 workflowStage。
        // 数据处理阶段要等“课题四结果回填完成”后，才设置 workflowStage = 2L。
        topic5TraceProblemMapper.updateTopic5TraceProblem(update);

        insertFlowLog(
                id,
                1L,
                "推送课题四处理",
                "成功",
                "已将追溯任务模拟推送给课题四，等待课题四返回处理结果"
        );
    }

    /**
     * 接收课题四回调
     */
    @Override
    @Transactional
    public void receiveTopic4Callback(Topic4CallbackDTO dto)
    {
        if (dto == null || dto.getTraceId() == null)
        {
            throw new ServiceException("课题四回调参数错误，traceId不能为空");
        }

        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(dto.getTraceId());

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法接收课题四结果");
        }

        Topic5TraceProblem update = new Topic5TraceProblem();
        update.setId(dto.getTraceId());

        // 主表 topic4Status 是 Long 类型，因此这里使用 2L
        update.setTopic4Status(2L);

        update.setTopic4Result(dto.getTopic4Result());
        update.setSuggestedCause(dto.getSuggestedCause());
        update.setRiskLevel(dto.getRiskLevel());
        update.setSuggestedAction(dto.getSuggestedAction());
        update.setWorkflowStage(5L);
        update.setStatus("处理中");
        update.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(update);

        insertFlowLog(
                dto.getTraceId(),
                5L,
                "接收课题四结果",
                "成功",
                "课题四已完成处理，并返回初步识别结果"
        );
    }

    /**
     * 回填课题四结果
     */
    @Override
    @Transactional
    public void fillTopic4Result(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法回填课题四结果");
        }

        // topic4Status 是 Long 类型，所以这里不能用 Integer.valueOf(2)
        if (!Long.valueOf(2L).equals(problem.getTopic4Status()))
        {
            throw new ServiceException("课题四尚未处理完成，不能回填结果");
        }

        Topic5TraceProblem update = new Topic5TraceProblem();
        update.setId(id);
        update.setTopic4ResultFilled(1L);
        update.setWorkflowStage(2L);
        update.setStatus("处理中");
        update.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(update);

        insertFlowLog(
                id,
                2L,
                "数据处理",
                "成功",
                "课题四返回结果已回填，数据处理阶段完成"
        );
    }

    /**
     * 查询当前流程阶段
     */
    @Override
    public Long getWorkflowStage(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在");
        }

        return problem.getWorkflowStage();
    }

    /**
     * 模拟运行 Python 追溯算法
     */
    @Override
    @Transactional
    public Map<String, Object> runAlgorithmMock(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法运行第一部分算法");
        }

        // 新流程下，第一部分算法的前置条件是：
        // 1. 课题四结果已经回填 topic4_result_filled = 1
        // 2. workflow_stage 至少为 2，即数据处理完成
        if (!Long.valueOf(1L).equals(problem.getTopic4ResultFilled()))
        {
            throw new ServiceException("请先完成课题四结果回填，再运行第一部分算法");
        }

        if (problem.getWorkflowStage() == null || problem.getWorkflowStage() < 2L)
        {
            throw new ServiceException("请先完成数据处理，再运行第一部分算法");
        }

        String algorithmResult =
                "第一部分算法模拟运行完成：" +
                        "Top1 原因为液压压力异常，置信度 0.87；" +
                        "Top2 原因为密封件老化，置信度 0.76；" +
                        "Top3 原因为装配间隙异常，置信度 0.69。";

        Topic5TraceProblem update = new Topic5TraceProblem();
        update.setId(id);
        update.setAlgorithmStatus(2L);
        update.setAlgorithmResult(algorithmResult);

        // 新流程：第一部分算法运行完成后，workflow_stage = 3
        update.setWorkflowStage(3L);
        update.setStatus("第一部分算法完成");
        update.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(update);

        insertFlowLog(
                id,
                3L,
                "第一部分算法运行",
                "成功",
                "第一部分追溯算法模拟运行完成"
        );

        Map<String, Object> result = new HashMap<>();
        result.put("traceId", id);
        result.put("algorithmStatus", 2L);
        result.put("algorithmResult", algorithmResult);
        result.put("top1", "液压压力异常");
        result.put("score1", 0.87);
        result.put("top2", "密封件老化");
        result.put("score2", 0.76);
        result.put("top3", "装配间隙异常");
        result.put("score3", 0.69);

        return result;
    }

    /**
     * 生成追溯任务编号
     * 示例：TS202606030001
     */
    private String generateTraceNo()
    {
        String dateStr = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String timeStr = new java.text.SimpleDateFormat("HHmmss").format(new java.util.Date());
        return "TS" + dateStr + timeStr;
    }

    /**
     * 构造模拟的课题一数字卷宗传感器附件
     */
    private List<Topic5TraceAttachment> buildMockDossierFiles(Long traceId)
    {
        List<Topic5TraceAttachment> list = new ArrayList<>();

        Topic5TraceAttachment pressureFile = new Topic5TraceAttachment();
        pressureFile.setTraceId(traceId);
        pressureFile.setFileName("pressure_sensor_" + traceId + ".csv");
        pressureFile.setFileType("csv");
        pressureFile.setFileUrl("D:/2.11/1.txt");
        pressureFile.setFileSource("课题一数字卷宗");
        pressureFile.setSensorType("压力传感器");
        pressureFile.setFileSize("2.3MB");
        pressureFile.setSavedFlag(0L);
        pressureFile.setRemark("模拟压力传感器数据文件");
        list.add(pressureFile);

        Topic5TraceAttachment temperatureFile = new Topic5TraceAttachment();
        temperatureFile.setTraceId(traceId);
        temperatureFile.setFileName("temperature_sensor_" + traceId + ".csv");
        temperatureFile.setFileType("csv");
        temperatureFile.setFileUrl("D:/2.11/2.txt");
        temperatureFile.setFileSource("课题一数字卷宗");
        temperatureFile.setSensorType("温度传感器");
        temperatureFile.setFileSize("1.8MB");
        temperatureFile.setSavedFlag(0L);
        temperatureFile.setRemark("模拟温度传感器数据文件");
        list.add(temperatureFile);

        Topic5TraceAttachment vibrationFile = new Topic5TraceAttachment();
        vibrationFile.setTraceId(traceId);
        vibrationFile.setFileName("vibration_sensor_" + traceId + ".csv");
        vibrationFile.setFileType("csv");
        vibrationFile.setFileUrl("D:/2.11/3.txt");
        vibrationFile.setFileSource("课题一数字卷宗");
        vibrationFile.setSensorType("振动传感器");
        vibrationFile.setFileSize("3.1MB");
        vibrationFile.setSavedFlag(0L);
        vibrationFile.setRemark("模拟振动传感器数据文件");
        list.add(vibrationFile);

        return list;
    }

    /**
     * 更新流程阶段，并写入流程日志
     */
    private void updateWorkflowStage(Long traceId, Long stage, String remark)
    {
        Topic5TraceProblem update = new Topic5TraceProblem();
        update.setId(traceId);
        update.setWorkflowStage(stage);
        update.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(update);

        insertFlowLog(
                traceId,
                stage,
                getStageName(stage),
                "成功",
                remark
        );
    }

    /**
     * 写入流程日志
     *
     * 注意：
     * 这里 stage 使用 Long，是为了和 Topic5TraceProblem.workflowStage 保持一致。
     * 如果 Topic5TraceFlowLog.stageCode 是 Integer，则使用 stage.intValue()。
     */
    /**
     * 写入流程日志
     */
    private void insertFlowLog(Long traceId, Long stage, String stageName, String result, String remark)
    {
        Topic5TraceFlowLog log = new Topic5TraceFlowLog();
        log.setTraceId(traceId);
        log.setStageCode(stage);
        log.setStageName(stageName);
        log.setOperateTime(DateUtils.getNowDate());
        log.setOperateResult(result);
        log.setRemark(remark);

        flowLogMapper.insertTopic5TraceFlowLog(log);
    }

    /**
     * 根据流程阶段编号获取流程名称
     */
    private String getStageName(Long stage)
    {
        if (stage == null)
        {
            return "未开始";
        }

        switch (stage.intValue())
        {
            case 1:
                return "问题填报";
            case 2:
                return "数据处理";
            case 3:
                return "第一部分算法运行";
            case 4:
                return "知识图谱导入";
            case 5:
                return "第二部分算法运行";
            case 6:
                return "进行溯源";
            default:
                return "未知流程";
        }
    }
    @Override
    @Transactional
    public void saveDossierAttachmentsWithPath(Long id, String attachmentSavePath)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法保存附件");
        }

        if (attachmentSavePath == null || "".equals(attachmentSavePath.trim()))
        {
            throw new ServiceException("附件保存位置不能为空");
        }

        String basePath = attachmentSavePath.trim().replace("\\", "/");
        if (!basePath.endsWith("/"))
        {
            basePath = basePath + "/";
        }

        // 查询课题一返回的临时附件记录
        Topic5TraceAttachment query = new Topic5TraceAttachment();
        query.setTraceId(id);
        query.setSavedFlag(0L);

        List<Topic5TraceAttachment> sourceFiles =
                traceAttachmentMapper.selectTopic5TraceAttachmentList(query);

        if (sourceFiles == null || sourceFiles.isEmpty())
        {
            throw new ServiceException("未找到课题一返回的传感器文件路径，请先调用数字卷宗数据");
        }

        try
        {
            Files.createDirectories(Paths.get(basePath));
        }
        catch (Exception e)
        {
            throw new ServiceException("创建附件保存目录失败：" + basePath);
        }

        for (Topic5TraceAttachment item : sourceFiles)
        {
            String sourcePath = item.getFileUrl();
            String targetPath = basePath + item.getFileName();

            copyFileToTarget(sourcePath, targetPath);

            Topic5TraceAttachment updateAttachment = new Topic5TraceAttachment();
            updateAttachment.setId(item.getId());
            updateAttachment.setFileUrl(targetPath);
            updateAttachment.setFileSource("课题五本地保存");
            updateAttachment.setSavedFlag(1L);
            updateAttachment.setRemark("已从课题一源路径复制到课题五设置目录");

            traceAttachmentMapper.updateTopic5TraceAttachment(updateAttachment);
        }

        Topic5TraceProblem updateProblem = new Topic5TraceProblem();
        updateProblem.setId(id);
        updateProblem.setAttachmentSavePath(basePath);
        updateProblem.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(updateProblem);

        insertFlowLog(
                id,
                1L,
                "附件保存",
                "成功",
                "已将课题一传感器文件复制到：" + basePath
        );
    }
    /**
     * 将课题一传来的文件复制到用户指定目录
     */
    private void copyFileToTarget(String sourceFileUrl, String targetFileUrl)
    {
        if (sourceFileUrl == null || "".equals(sourceFileUrl.trim()))
        {
            throw new ServiceException("源文件路径为空，无法保存附件");
        }

        try
        {
            String source = sourceFileUrl.trim().replace("\\", "/");
            Path targetPath = Paths.get(targetFileUrl.replace("\\", "/"));

            if (targetPath.getParent() != null)
            {
                Files.createDirectories(targetPath.getParent());
            }

            if (source.startsWith("http://") || source.startsWith("https://"))
            {
                try (InputStream inputStream = new URL(source).openStream())
                {
                    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                return;
            }

            Path sourcePath = Paths.get(source);

            if (!Files.exists(sourcePath))
            {
                throw new ServiceException("源文件不存在：" + source);
            }

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e)
        {
            throw new ServiceException("附件文件复制失败：" + e.getMessage());
        }
    }
}