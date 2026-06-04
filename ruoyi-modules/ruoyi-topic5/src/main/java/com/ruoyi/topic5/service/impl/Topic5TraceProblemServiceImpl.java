package com.ruoyi.topic5.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;

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
import com.alibaba.fastjson2.JSON;

/**
 * 追溯问题Service业务层处理
 *
 * @author ruoyi
 * @date 2026-06-03
 */
@Service
public class Topic5TraceProblemServiceImpl implements ITopic5TraceProblemService
{
    @Value("${topic5.python.first-algorithm-url:http://127.0.0.1:8000/api/v1/diagnose}")
    private String firstAlgorithmUrl;

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

        // 初始化第二页面相关状态
        topic5TraceProblem.setTopic1KgStatus(0L);
        topic5TraceProblem.setSecondAlgorithmStatus(0L);

        topic5TraceProblem.setSourceAlgorithmStatus(0L);
        topic5TraceProblem.setTopic2PushStatus(0L);
        topic5TraceProblem.setTopic3PushStatus(0L);

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

        update.setTopic4Status(dto.getTopic4Status());

        update.setTopic4FaultType(dto.getFaultType());
        update.setTopic4FaultLocation(dto.getFaultLocation());
        update.setTopic4CauseAnalysis(dto.getCauseAnalysis());
        update.setTopic4DeductionProcess(dto.getDeductionProcess());
        update.setTopic4RootConfidence(dto.getRootConfidence());

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

        // 第一部分算法前置条件：课题四反馈已经回填，且流程至少完成多元特征提取
        if (!Long.valueOf(1L).equals(problem.getTopic4ResultFilled()))
        {
            throw new ServiceException("请先完成课题四结果回填，再运行第一部分算法");
        }

        if (problem.getWorkflowStage() == null || problem.getWorkflowStage() < 2L)
        {
            throw new ServiceException("请先完成数据处理，再运行第一部分算法");
        }

        // 先更新为运行中
        Topic5TraceProblem runningUpdate = new Topic5TraceProblem();
        runningUpdate.setId(id);
        runningUpdate.setAlgorithmStatus(1L);
        runningUpdate.setStatus("处理中");
        runningUpdate.setUpdateTime(DateUtils.getNowDate());
        topic5TraceProblemMapper.updateTopic5TraceProblem(runningUpdate);

        try
        {
            // 1. 组装 Python API 请求体，对应 Python 中 DiagnoseRequest
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("case_id", problem.getTraceNo() == null ? String.valueOf(problem.getId()) : problem.getTraceNo());
            requestBody.put("product_model", problem.getAircraftNo() == null ? "Aircraft-A" : problem.getAircraftNo());
            requestBody.put("algorithm", "时空图神经网络定位算法");

            // 2. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 3. 调用 Python FastAPI 服务
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    firstAlgorithmUrl,
                    entity,
                    Map.class
            );

            Map responseBody = response.getBody();

            if (responseBody == null)
            {
                throw new ServiceException("Python算法服务无返回结果");
            }

            Object codeObj = responseBody.get("code");

            if (codeObj == null || Integer.parseInt(codeObj.toString()) != 200)
            {
                Object messageObj = responseBody.get("message");
                throw new ServiceException("Python算法服务执行失败：" + (messageObj == null ? "未知错误" : messageObj.toString()));
            }

            Map data = (Map) responseBody.get("data");

            if (data == null)
            {
                throw new ServiceException("Python算法服务返回data为空");
            }

            // 4. 解析 Python 返回结果，整理成前端算法结果文本
            String algorithmResult = buildFirstAlgorithmResultText(data);

            // 5. 写回数据库
            Topic5TraceProblem update = new Topic5TraceProblem();
            update.setId(id);
            update.setAlgorithmStatus(2L);
            update.setAlgorithmResult(algorithmResult);

            // 新流程：故障根因分析完成，对应 workflow_stage = 3
            update.setWorkflowStage(3L);
            update.setStatus("第一部分算法完成");
            update.setUpdateTime(DateUtils.getNowDate());

            topic5TraceProblemMapper.updateTopic5TraceProblem(update);

            insertFlowLog(
                    id,
                    3L,
                    "故障根因分析",
                    "成功",
                    "已调用Python时空图神经网络定位算法并完成第一部分分析"
            );

            // 6. 返回给 Controller / 前端
            Map<String, Object> result = new HashMap<>();
            result.put("traceId", id);
            result.put("algorithmStatus", 2L);
            result.put("algorithmResult", algorithmResult);
            result.put("pythonResponse", responseBody);
            result.put("businessConclusion", data.get("business_conclusion"));
            result.put("algorithmDetails", data.get("algorithm_details"));
            result.put("visualizationData", data.get("visualization_data"));

            return result;
        }
        catch (Exception e)
        {
            Topic5TraceProblem failUpdate = new Topic5TraceProblem();
            failUpdate.setId(id);
            failUpdate.setAlgorithmStatus(3L);
            failUpdate.setAlgorithmResult("第一部分算法运行失败：" + e.getMessage());
            failUpdate.setStatus("处理中");
            failUpdate.setUpdateTime(DateUtils.getNowDate());
            topic5TraceProblemMapper.updateTopic5TraceProblem(failUpdate);

            insertFlowLog(
                    id,
                    3L,
                    "故障根因分析",
                    "失败",
                    "Python第一部分算法运行失败：" + e.getMessage()
            );

            throw new ServiceException("Python第一部分算法运行失败：" + e.getMessage());
        }
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
    @Override
    @Transactional
    public Map<String, Object> pullTopic1Kg(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法拉取知识图谱");
        }

        // 建议第二页面至少要求第一部分算法已完成
        if (problem.getWorkflowStage() == null || problem.getWorkflowStage() < 3L)
        {
            throw new ServiceException("请先完成第一部分算法运行，再导入知识图谱");
        }

        String graphJson = buildMockTopic1KgJson(problem);

        Topic5TraceProblem update = new Topic5TraceProblem();
        update.setId(id);
        update.setTopic1KgStatus(1L);
        update.setTopic1KgJson(graphJson);

        // 知识图谱导入完成，对应总流程第4步
        update.setWorkflowStage(4L);



        update.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(update);

        insertFlowLog(
                id,
                4L,
                "知识图谱导入",
                "成功",
                "已从课题一拉取并导入原始知识图谱"
        );

        Topic5TraceProblem newProblem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("traceProblem", newProblem);
        result.put("graphData", parseGraphJsonToMap(graphJson));

        return result;
    }
    @Override
    @Transactional
    public Map<String, Object> runSecondAlgorithm(Long id, String algorithmName)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法运行第二部分算法");
        }

        if (problem.getTopic1KgStatus() == null || problem.getTopic1KgStatus() != 1L)
        {
            throw new ServiceException("请先从课题一拉取知识图谱，再运行第二部分算法");
        }

        if (algorithmName == null || "".equals(algorithmName.trim()))
        {
            throw new ServiceException("请选择第二部分算法");
        }

        String resultGraphJson = buildMockSecondAlgorithmGraphJson(problem, algorithmName);

        Topic5TraceProblem update = new Topic5TraceProblem();
        update.setId(id);
        update.setSecondAlgorithmName(algorithmName);
        update.setSecondAlgorithmStatus(2L);
        update.setSecondAlgorithmResultJson(resultGraphJson);
        update.setSecondAlgorithmConfirmStatus(0L);
        update.setSecondAlgorithmConfirmRemark(null);

        // 第二部分算法完成，对应总流程第5步
        update.setWorkflowStage(5L);
        update.setStatus("第二部分算法完成");
        update.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(update);

        insertFlowLog(
                id,
                5L,
                "第二部分算法运行",
                "成功",
                "第二部分算法运行完成：" + algorithmName
        );

        Topic5TraceProblem newProblem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("traceProblem", newProblem);
        result.put("graphData", parseGraphJsonToMap(resultGraphJson));

        return result;
    }
    @Override
    public Map<String, Object> getTraceKg(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("topic1KgJson", problem.getTopic1KgJson());
        result.put("secondAlgorithmResultJson", problem.getSecondAlgorithmResultJson());
        result.put("traceProblem", problem);

        return result;
    }
    private String buildMockTopic1KgJson(Topic5TraceProblem problem)
    {
        Long id = problem.getId();
        String partName = problem.getPartName() == null ? "故障部件" : problem.getPartName();
        String aircraftNo = problem.getAircraftNo() == null ? "未知架次" : problem.getAircraftNo();

        return "{"
                + "\"nodes\":["
                + "{\"id\":\"T" + id + "\",\"name\":\"追溯任务" + id + "\",\"category\":\"追溯任务\"},"
                + "{\"id\":\"A" + id + "\",\"name\":\"" + aircraftNo + "\",\"category\":\"架次\"},"
                + "{\"id\":\"P" + id + "\",\"name\":\"" + partName + "\",\"category\":\"部件\"},"
                + "{\"id\":\"S1" + id + "\",\"name\":\"压力传感器\",\"category\":\"传感器\"},"
                + "{\"id\":\"S2" + id + "\",\"name\":\"温度传感器\",\"category\":\"传感器\"},"
                + "{\"id\":\"F" + id + "\",\"name\":\"质量异常\",\"category\":\"故障\"}"
                + "],"
                + "\"links\":["
                + "{\"source\":\"T" + id + "\",\"target\":\"A" + id + "\",\"name\":\"对应架次\"},"
                + "{\"source\":\"A" + id + "\",\"target\":\"P" + id + "\",\"name\":\"包含部件\"},"
                + "{\"source\":\"P" + id + "\",\"target\":\"S1" + id + "\",\"name\":\"关联传感器\"},"
                + "{\"source\":\"P" + id + "\",\"target\":\"S2" + id + "\",\"name\":\"关联传感器\"},"
                + "{\"source\":\"S1" + id + "\",\"target\":\"F" + id + "\",\"name\":\"检测异常\"}"
                + "]"
                + "}";
    }
    private String buildMockSecondAlgorithmGraphJson(Topic5TraceProblem problem, String algorithmName)
    {
        Long id = problem.getId();
        String partName = problem.getPartName() == null ? "故障部件" : problem.getPartName();

        return "{"
                + "\"nodes\":["
                + "{\"id\":\"P" + id + "\",\"name\":\"" + partName + "\",\"category\":\"部件\"},"
                + "{\"id\":\"C1" + id + "\",\"name\":\"液压压力异常\",\"category\":\"候选原因\"},"
                + "{\"id\":\"C2" + id + "\",\"name\":\"密封件老化\",\"category\":\"候选原因\"},"
                + "{\"id\":\"C3" + id + "\",\"name\":\"装配间隙异常\",\"category\":\"候选原因\"},"
                + "{\"id\":\"M" + id + "\",\"name\":\"" + algorithmName + "\",\"category\":\"算法模型\"}"
                + "],"
                + "\"links\":["
                + "{\"source\":\"M" + id + "\",\"target\":\"C1" + id + "\",\"name\":\"Top1 0.87\"},"
                + "{\"source\":\"M" + id + "\",\"target\":\"C2" + id + "\",\"name\":\"Top2 0.76\"},"
                + "{\"source\":\"M" + id + "\",\"target\":\"C3" + id + "\",\"name\":\"Top3 0.69\"},"
                + "{\"source\":\"C1" + id + "\",\"target\":\"P" + id + "\",\"name\":\"影响部件\"},"
                + "{\"source\":\"C2" + id + "\",\"target\":\"P" + id + "\",\"name\":\"影响部件\"},"
                + "{\"source\":\"C3" + id + "\",\"target\":\"P" + id + "\",\"name\":\"影响部件\"}"
                + "]"
                + "}";
    }
    private Map<String, Object> parseGraphJsonToMap(String graphJson)
    {
        return JSON.parseObject(graphJson, Map.class);
    }
    @Override
    @Transactional
    public void confirmSecondAlgorithmResult(Long id, Boolean saveFlag, String remark)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法进行人工判定");
        }

        if (saveFlag == null)
        {
            throw new ServiceException("请选择是否保存算法结果");
        }

        if (problem.getSecondAlgorithmStatus() == null || !Long.valueOf(2L).equals(problem.getSecondAlgorithmStatus()))
        {
            throw new ServiceException("第二部分算法尚未运行完成，不能进行人工判定");
        }

        if (saveFlag)
        {
            // 人工确认保存第二部分算法结果
            Topic5TraceProblem update = new Topic5TraceProblem();
            update.setId(id);

            // 人工确认状态
            update.setSecondAlgorithmConfirmStatus(1L);
            update.setSecondAlgorithmConfirmRemark(remark);

            // 保持第二部分算法已完成状态
            update.setSecondAlgorithmStatus(2L);

            // 人工确认保存后，第一个页面才允许查看知识图谱
            update.setKgRebuildStatus(1L);
            update.setKgGraphUrl("/topic5/graph?traceId=" + id);

            // 保持流程在第5步：第二部分算法运行
            update.setWorkflowStage(5L);
            update.setUpdateTime(DateUtils.getNowDate());

            topic5TraceProblemMapper.updateTopic5TraceProblem(update);

            insertFlowLog(
                    id,
                    5L,
                    "第二部分算法人工确认",
                    "成功",
                    "人工确认保存第二部分算法输出结果"
            );
        }
        else
        {
            // 人工驳回：删除第二部分算法输出结果，要求重新运行算法
            // 注意：这里不再重新定义 Topic5TraceProblem update，避免变量重复定义
            topic5TraceProblemMapper.rejectSecondAlgorithmResult(
                    id,
                    0L,
                    2L,
                    remark,
                    4L,
                    DateUtils.getNowDate()
            );

            insertFlowLog(
                    id,
                    4L,
                    "第二部分算法人工确认",
                    "驳回",
                    "人工驳回第二部分算法输出结果，需要重新运行算法"
            );
        }
    }
    @Override
    @Transactional
    public Map<String, Object> runSourceAlgorithm(Long id, String algorithmName)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法运行最终溯源算法");
        }

        if (problem.getSecondAlgorithmConfirmStatus() == null || !Long.valueOf(1L).equals(problem.getSecondAlgorithmConfirmStatus()))
        {
            throw new ServiceException("请先完成人工确认保存第二部分算法结果，再进行最终溯源");
        }

        if (algorithmName == null || "".equals(algorithmName.trim()))
        {
            throw new ServiceException("请选择最终溯源算法");
        }

        String sourceGraphJson = buildMockSourceGraphJson(problem, algorithmName);
        String reasonTableJson = buildMockReasonTableJson(problem);
        String summary = "最终溯源算法判断该问题主要由液压管路装配间隙异常引起，同时可能受到密封件性能退化和压力传感器波动的共同影响。";

        Topic5TraceProblem update = new Topic5TraceProblem();
        update.setId(id);
        update.setSourceAlgorithmName(algorithmName);
        update.setSourceAlgorithmStatus(2L);
        update.setSourceGraphJson(sourceGraphJson);
        update.setSourceReasonTableJson(reasonTableJson);
        update.setSourceResultSummary(summary);
        update.setStatus("最终溯源算法完成");
        update.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(update);

        insertFlowLog(
                id,
                5L,
                "最终溯源算法运行",
                "成功",
                "最终溯源算法运行完成：" + algorithmName
        );

        Topic5TraceProblem newProblem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("traceProblem", newProblem);
        result.put("sourceGraphJson", sourceGraphJson);
        result.put("reasonTableJson", reasonTableJson);
        result.put("summary", summary);
        result.put("graphData", parseGraphJsonToMap(sourceGraphJson));
        result.put("reasonList", JSON.parseArray(reasonTableJson, Map.class));

        return result;
    }

    @Override
    public Map<String, Object> getSourceResult(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("traceProblem", problem);
        result.put("sourceGraphJson", problem.getSourceGraphJson());
        result.put("reasonTableJson", problem.getSourceReasonTableJson());
        result.put("summary", problem.getSourceResultSummary());

        if (problem.getSourceGraphJson() != null)
        {
            result.put("graphData", parseGraphJsonToMap(problem.getSourceGraphJson()));
        }

        if (problem.getSourceReasonTableJson() != null)
        {
            result.put("reasonList", JSON.parseArray(problem.getSourceReasonTableJson(), Map.class));
        }

        return result;
    }

    private String buildMockSourceGraphJson(Topic5TraceProblem problem, String algorithmName)
    {
        Long id = problem.getId();
        String partName = problem.getPartName() == null ? "故障部件" : problem.getPartName();

        return "{"
                + "\"nodes\":["
                + "{\"id\":\"F" + id + "\",\"name\":\"质量异常\",\"category\":\"故障现象\"},"
                + "{\"id\":\"P" + id + "\",\"name\":\"" + partName + "\",\"category\":\"故障部件\"},"
                + "{\"id\":\"C1" + id + "\",\"name\":\"液压管路装配间隙异常\",\"category\":\"主导原因\"},"
                + "{\"id\":\"C2" + id + "\",\"name\":\"密封件性能退化\",\"category\":\"次要原因\"},"
                + "{\"id\":\"E1" + id + "\",\"name\":\"压力传感器波动\",\"category\":\"证据\"},"
                + "{\"id\":\"M" + id + "\",\"name\":\"" + algorithmName + "\",\"category\":\"溯源算法\"}"
                + "],"
                + "\"links\":["
                + "{\"source\":\"M" + id + "\",\"target\":\"C1" + id + "\",\"name\":\"Top1 0.91\"},"
                + "{\"source\":\"M" + id + "\",\"target\":\"C2" + id + "\",\"name\":\"Top2 0.82\"},"
                + "{\"source\":\"C1" + id + "\",\"target\":\"P" + id + "\",\"name\":\"导致\"},"
                + "{\"source\":\"C2" + id + "\",\"target\":\"P" + id + "\",\"name\":\"影响\"},"
                + "{\"source\":\"E1" + id + "\",\"target\":\"C1" + id + "\",\"name\":\"支持\"},"
                + "{\"source\":\"P" + id + "\",\"target\":\"F" + id + "\",\"name\":\"表现为\"}"
                + "]"
                + "}";
    }

    private String buildMockReasonTableJson(Topic5TraceProblem problem)
    {
        String partName = problem.getPartName() == null ? "故障部件" : problem.getPartName();

        return "["
                + "{"
                + "\"rank\":1,"
                + "\"reasonType\":\"装配阶段\","
                + "\"reasonName\":\"液压管路装配间隙异常\","
                + "\"relatedPart\":\"" + partName + "\","
                + "\"evidence\":\"压力传感器波动异常，知识图谱中存在装配间隙异常链路\","
                + "\"confidence\":0.91,"
                + "\"suggestion\":\"建议检查管路连接状态和作动筒密封状态\""
                + "},"
                + "{"
                + "\"rank\":2,"
                + "\"reasonType\":\"材料阶段\","
                + "\"reasonName\":\"密封件性能退化\","
                + "\"relatedPart\":\"密封件\","
                + "\"evidence\":\"温度升高后压力保持能力下降\","
                + "\"confidence\":0.82,"
                + "\"suggestion\":\"建议复核密封件批次和材料检验记录\""
                + "},"
                + "{"
                + "\"rank\":3,"
                + "\"reasonType\":\"传感器/运维阶段\","
                + "\"reasonName\":\"压力传感器采集异常\","
                + "\"relatedPart\":\"压力传感器\","
                + "\"evidence\":\"局部时间段存在压力信号尖峰\","
                + "\"confidence\":0.73,"
                + "\"suggestion\":\"建议复核传感器标定状态和采样记录\""
                + "}"
                + "]";
    }

    @Override
    @Transactional
    public Map<String, Object> exportSourceReport(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法导出报告");
        }

        if (problem.getSourceAlgorithmStatus() == null || !Long.valueOf(2L).equals(problem.getSourceAlgorithmStatus()))
        {
            throw new ServiceException("请先运行最终溯源算法，再导出报告");
        }

        String fileName = "trace_report_" + id + ".docx";
        String relativeUrl = "/profile/topic5/report/" + fileName;

        Topic5TraceProblem update = new Topic5TraceProblem();
        update.setId(id);
        update.setTraceReportStatus(1L);
        update.setTraceReportUrl(relativeUrl);

        // 导出报告后，总流程进入第6步
        update.setWorkflowStage(6L);
        update.setStatus("溯源完成");
        update.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(update);

        insertFlowLog(
                id,
                6L,
                "最终溯源报告导出",
                "成功",
                "已生成最终溯源报告：" + relativeUrl
        );

        Map<String, Object> result = new HashMap<>();
        result.put("traceId", id);
        result.put("reportUrl", relativeUrl);
        result.put("fileName", fileName);

        return result;
    }

    @Override
    @Transactional
    public void pushTopic2(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法推送课题二");
        }

        if (problem.getTraceReportStatus() == null || !Long.valueOf(1L).equals(problem.getTraceReportStatus()))
        {
            throw new ServiceException("请先导出最终溯源报告，再推送课题二");
        }

        Topic5TraceProblem update = new Topic5TraceProblem();
        update.setId(id);
        update.setTopic2PushStatus(1L);
        update.setTopic2PushTime(DateUtils.getNowDate());
        update.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(update);

        insertFlowLog(
                id,
                6L,
                "推送课题二",
                "成功",
                "最终溯源报告已模拟推送至课题二"
        );
    }
    @Override
    @Transactional
    public void pushTopic3(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法推送课题三");
        }

        if (problem.getTraceReportStatus() == null || !Long.valueOf(1L).equals(problem.getTraceReportStatus()))
        {
            throw new ServiceException("请先导出最终溯源报告，再推送课题三");
        }

        Topic5TraceProblem update = new Topic5TraceProblem();
        update.setId(id);
        update.setTopic3PushStatus(1L);
        update.setTopic3PushTime(DateUtils.getNowDate());
        update.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(update);

        insertFlowLog(
                id,
                6L,
                "推送课题三",
                "成功",
                "最终溯源报告已模拟推送至课题三"
        );
    }
    private String buildFirstAlgorithmResultText(Map data)
    {
        Map<String, Object> result = new HashMap<>();

        result.put("displayType", "PYTHON_AAGCN_RCA_V1");
        result.put("caseId", data.get("case_id"));

        Map businessConclusion = (Map) data.get("business_conclusion");
        if (businessConclusion != null)
        {
            result.put("faultComponentId", businessConclusion.get("fault_component_id"));
            result.put("faultComponent", businessConclusion.get("fault_component"));
            result.put("componentConfidence", businessConclusion.get("component_confidence"));
            result.put("componentStatus", businessConclusion.get("component_status"));
            result.put("severityScores", businessConclusion.get("severity_scores"));
            result.put("componentDiagnostics", businessConclusion.get("component_diagnostics"));
            result.put("subtypeProbabilities", businessConclusion.get("subtype_probabilities"));
            result.put("subtypeByComponent", businessConclusion.get("subtype_by_component"));
        }

        Map algorithmDetails = (Map) data.get("algorithm_details");
        if (algorithmDetails != null)
        {
            result.put("triggerSensor", algorithmDetails.get("trigger_sensor"));
            result.put("sensorConfidence", algorithmDetails.get("sensor_confidence"));
            result.put("evolutionChain", algorithmDetails.get("evolution_chain"));
        }

        Map visualizationData = (Map) data.get("visualization_data");
        if (visualizationData != null)
        {
            result.put("sensorAnomalyEnergy", visualizationData.get("sensor_anomaly_energy"));
            result.put("dynamicTopologyMatrix", visualizationData.get("dynamic_topology_matrix"));
        }

        result.put("conclusion", "当前已完成多元特征提取与初步故障根因分析，可进入后续卷宗实体映射和溯源图谱构建流程。");

        return JSON.toJSONString(result);
    }
}