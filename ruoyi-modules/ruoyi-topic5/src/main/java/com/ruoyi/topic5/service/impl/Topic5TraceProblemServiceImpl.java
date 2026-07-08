package com.ruoyi.topic5.service.impl;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.qms.api.domain.QualityProblemDto;
import com.ruoyi.qms.api.RemoteQualityTaskService;
import com.ruoyi.qms.api.domain.QualityTaskDto;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.qms.api.domain.QualityTaskSubmitDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import org.springframework.core.io.ClassPathResource;



/**
 * 追溯问题Service业务层处理
 *
 * @author ruoyi
 * @date 2026-06-03
 */
@Service
public class Topic5TraceProblemServiceImpl implements ITopic5TraceProblemService
{
    @Value("${topic5.python.first-algorithm-url:http://127.0.0.1:9781/api/v1/diagnose}")
    private String firstAlgorithmUrl;

    @Value("${topic5.python.second-algorithm-url:http://127.0.0.1:9782/api/v1/trace/run}")
    private String secondAlgorithmUrl;

    @Value("${topic5.python.third-algorithm-url:http://127.0.0.1:9782/api/v1/trace/run}")
    private String thirdAlgorithmUrl;

    @Value("${ruoyi.profile:D:/2.11/topic5_code}")
    private String ruoyiProfile;

    @Autowired
    private Topic5TraceProblemMapper topic5TraceProblemMapper;

//    @Autowired
//    private RemoteQualityProblemService remoteQualityProblemService;

    @Autowired
    private RemoteQualityTaskService remoteQualityTaskService;

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

    @Override
    public void downloadReportByPath(String filePath, HttpServletResponse response) throws Exception
    {
        if (StringUtils.isEmpty(filePath))
        {
            throw new ServiceException("报告路径不能为空");
        }

        String normalizedPath = filePath.replace("\\", "/");

        if (!normalizedPath.startsWith("/profile/topic5/report/"))
        {
            throw new ServiceException("非法报告路径：" + filePath);
        }

        String fileName = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1);

        if (StringUtils.isEmpty(fileName))
        {
            throw new ServiceException("报告文件名不能为空");
        }

        String lowerName = fileName.toLowerCase();

        if (!lowerName.endsWith(".doc") && !lowerName.endsWith(".docx"))
        {
            throw new ServiceException("当前文件不是Word报告：" + fileName);
        }

        Path reportPath = Paths.get(ruoyiProfile, "topic5", "report", fileName);

        if (!Files.exists(reportPath))
        {
            throw new ServiceException("报告文件不存在：" + reportPath);
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        Files.copy(reportPath, response.getOutputStream());
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
     * 将质量问题转换为课题五追溯问题
     */
    private Topic5TraceProblem buildTraceFromQualityProblem(QualityProblemDto problem)
    {
        Topic5TraceProblem traceProblem = new Topic5TraceProblem();

        // 质量问题编号 -> 追溯编号，用于后续去重
        traceProblem.setTraceNo(problem.getProblemCode());

        // 发生时间
        traceProblem.setEventTime(problem.getOccurTime());

        // 质量问题的产品型号 -> 课题五的架次/型号字段
        traceProblem.setAircraftNo(problem.getProductModel());

        // 涉及系统 -> 零部件/系统名称字段
        traceProblem.setPartName(problem.getRelatedSystem());

        // 问题类型先给一个固定值，后面需要时再细分
        traceProblem.setProblemType("质量问题同步");

        // 严重程度
        traceProblem.setSeverityLevel(problem.getSeverityLevel());

        // 问题描述
        traceProblem.setProblemDescription(problem.getProblemDescription());

        // 工作流阶段：这里用 0 表示刚同步/待处理，如果你系统里已有其他约定，就改成对应数字
        traceProblem.setWorkflowStage(0L);

        // 状态
        traceProblem.setStatus("待追溯");

        // 课题四相关状态先初始化为未回填
        traceProblem.setTopic4ResultFilled(0L);

        // 算法状态先初始化为未运行
        traceProblem.setAlgorithmStatus(0L);

        // 知识图谱重构状态、报告状态初始化
        traceProblem.setKgRebuildStatus(0L);
        traceProblem.setTraceReportStatus(0L);

        // 来源
        traceProblem.setSource("质量问题管理中心");

        // 备注中保留质量问题标题，防止 description 太简略
        traceProblem.setRemark(problem.getProblemTitle());

        // 若依通用字段
        traceProblem.setCreateTime(DateUtils.getNowDate());
        traceProblem.setDelFlag("0");

        return traceProblem;
    }
    /**
     * 从质量问题管理中心同步问题
     *
     * @return 新增数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncQualityProblemsFromQms()
    {
        R<List<QualityTaskDto>> result =
                remoteQualityTaskService.listTaskForModule("PROJECT_5", SecurityConstants.INNER);

        if (result == null)
        {
            throw new ServiceException("调用质量任务服务失败：返回结果为空");
        }

        if (R.FAIL == result.getCode())
        {
            throw new ServiceException(result.getMsg());
        }

        List<QualityTaskDto> taskList = result.getData();

        if (taskList == null || taskList.isEmpty())
        {
            return 0;
        }

        // 已经同步过的课题五质量任务ID
        List<Long> existedTaskIds = topic5TraceProblemMapper.selectAllQualityTaskIdList();
        Set<Long> existedTaskIdSet = new HashSet<>();

        if (existedTaskIds != null)
        {
            existedTaskIdSet.addAll(existedTaskIds);
        }

        int count = 0;

        for (QualityTaskDto task : taskList)
        {
            if (task == null || task.getTaskId() == null)
            {
                continue;
            }

            if (StringUtils.isEmpty(task.getProblemCode()))
            {
                continue;
            }

            /*
             * 新增逻辑：
             * 同步 PROJECT_5 任务时，根据同一个 problemId 反查 PROJECT_4
             * 如果 PROJECT_4 已完成，则把智能故障诊断结果同步到课题五追溯任务。
             */
            QualityTaskDto project4Task = getProject4FinishedTask(task);

            /*
             * 关键修改：
             * 如果这个 PROJECT_5 任务已经同步过，不能直接 continue。
             * 因为可能第一次同步时 PROJECT_4 还没完成，
             * 第二次点击同步时 PROJECT_4 已经完成，需要更新已有追溯任务。
             */
            if (existedTaskIdSet.contains(task.getTaskId()))
            {
                Topic5TraceProblem existedTrace =
                        topic5TraceProblemMapper.selectTopic5TraceProblemByQualityTaskId(task.getTaskId());

                if (existedTrace != null && project4Task != null)
                {
                    fillProject4DiagnosisResult(existedTrace, project4Task);
                    existedTrace.setUpdateTime(DateUtils.getNowDate());

                    topic5TraceProblemMapper.updateTopic5TraceProblem(existedTrace);

                    count++;
                }

                continue;
            }

            // 第一次同步该 PROJECT_5 任务
            Topic5TraceProblem traceProblem = buildTraceFromQualityTask(task);

            // 如果 PROJECT_4 已完成，则新增时直接写入智能故障诊断结果
            if (project4Task != null)
            {
                fillProject4DiagnosisResult(traceProblem, project4Task);
            }

            int rows = topic5TraceProblemMapper.insertTopic5TraceProblem(traceProblem);

            if (rows > 0)
            {
                count++;
                existedTaskIdSet.add(task.getTaskId());
            }
        }

        return count;
    }
    private QualityTaskDto getProject4FinishedTask(QualityTaskDto project5Task)
    {
        if (project5Task == null || project5Task.getProblemId() == null)
        {
            return null;
        }

        try
        {
            R<QualityTaskDto> result =
                    remoteQualityTaskService.getTaskByProblemIdAndModuleCode(
                            project5Task.getProblemId(),
                            "PROJECT_4",
                            SecurityConstants.INNER
                    );

            if (result == null || R.FAIL == result.getCode())
            {
                return null;
            }

            QualityTaskDto project4Task = result.getData();

            if (!isProject4TaskFinished(project4Task))
            {
                return null;
            }

            return project4Task;
        }
        catch (Exception e)
        {
            System.err.println("查询 PROJECT_4 智能故障诊断任务失败：" + e.getMessage());
            return null;
        }
    }
    private boolean isProject4TaskFinished(QualityTaskDto task)
    {
        if (task == null)
        {
            return false;
        }

        String status = task.getTaskStatus();
        String processResult = task.getProcessResult();

        boolean finishedStatus =
                "CONFIRMED".equals(status)
                        || "SUBMITTED".equals(status)
                        || "COMPLETED".equals(status)
                        || "FINISHED".equals(status);

        return finishedStatus
                && StringUtils.isNotEmpty(processResult);
    }
    private void fillProject4DiagnosisResult(Topic5TraceProblem traceProblem, QualityTaskDto project4Task)
    {
        if (traceProblem == null || project4Task == null)
        {
            return;
        }

        String processResult = project4Task.getProcessResult();

        if (StringUtils.isEmpty(processResult))
        {
            return;
        }

        // 标记智能故障诊断与根源性分析平台已经完成
        traceProblem.setTopic4Status(2L);
        traceProblem.setTopic4ResultFilled(1L);

        // 保存智能故障诊断结果
        // 注意：Topic5TraceProblem 里没有 topic4Result 字段，所以不要写 setTopic4Result(...)
        traceProblem.setTopic4CauseAnalysis(processResult);

        // 如果实体类里有 topic4DeductionProcess 字段，可以同步保存一份
        traceProblem.setTopic4DeductionProcess(processResult);

        // 暂时先不解析 JSON，先给通用显示值
        traceProblem.setTopic4FaultType("智能故障诊断结果");
        traceProblem.setTopic4FaultLocation(
                StringUtils.isEmpty(traceProblem.getPartName()) ? "-" : traceProblem.getPartName()
        );
        traceProblem.setTopic4RootConfidence("-");

        if (StringUtils.isEmpty(traceProblem.getStatus()) || "未处理".equals(traceProblem.getStatus()))
        {
            traceProblem.setStatus("智能故障诊断已完成");
        }
    }
    private Topic5TraceProblem buildTraceFromQualityTask(QualityTaskDto task)
    {
        Topic5TraceProblem traceProblem = new Topic5TraceProblem();

        traceProblem.setQualityProblemId(task.getProblemId());
        traceProblem.setQualityTaskId(task.getTaskId());

        traceProblem.setTraceNo(task.getProblemCode());

        Date eventTime = task.getOccurTime();

        if (eventTime == null)
        {
            eventTime = task.getDispatchTime();
        }

        if (eventTime == null)
        {
            eventTime = DateUtils.getNowDate();
        }

        traceProblem.setEventTime(eventTime);

        traceProblem.setAircraftNo(
                StringUtils.isEmpty(task.getProductModel()) ? "未填写" : task.getProductModel()
        );

        traceProblem.setPartName(
                StringUtils.isEmpty(task.getInvolvedSystem()) ? "未填写" : task.getInvolvedSystem()
        );

        traceProblem.setProblemType("质量追溯");

        traceProblem.setSeverityLevel(
                StringUtils.isEmpty(task.getSeverity()) ? "一般" : normalizeSeverityLevel(task.getSeverity())
        );

        traceProblem.setProblemDescription(buildTraceProblemDescription(task));

        traceProblem.setWorkflowStage(1L);
        traceProblem.setStatus("未处理");

        traceProblem.setTopic4ResultFilled(0L);
        traceProblem.setTopic4Status(0L);
        traceProblem.setAlgorithmStatus(0L);
        traceProblem.setKgRebuildStatus(0L);
        traceProblem.setTraceReportStatus(0L);

        traceProblem.setSource("质量问题管理中心分派");
        traceProblem.setRemark(buildTraceRemark(task));

        traceProblem.setCreateTime(DateUtils.getNowDate());
        traceProblem.setDelFlag("0");

        return traceProblem;
    }
    private String normalizeSeverityLevel(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return "一般";
        }

        if ("紧急".equals(value))
        {
            return "严重";
        }

        if ("一般".equals(value) || "重要".equals(value) || "严重".equals(value))
        {
            return value;
        }

        return "一般";
    }

    private String buildTraceProblemDescription(QualityTaskDto task)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("质量问题标题：").append(emptyToDash(task.getProblemTitle())).append("\n");
        sb.append("质量问题编号：").append(emptyToDash(task.getProblemCode())).append("\n");

        // 新增字段
        sb.append("发生时间：").append(formatDate(task.getOccurTime())).append("\n");
        sb.append("发生部位：").append(emptyToDash(task.getModuleName())).append("\n");
        sb.append("部件编号：").append(emptyToDash(task.getModuleCode())).append("\n");
        sb.append("填报人员：").append(emptyToDash(task.getReporter())).append("\n");

        sb.append("产品型号：").append(emptyToDash(task.getProductModel())).append("\n");
        sb.append("涉及系统：").append(emptyToDash(task.getInvolvedSystem())).append("\n");
        sb.append("严重程度：").append(emptyToDash(task.getSeverity())).append("\n");
        sb.append("任务状态：").append(emptyToDash(task.getTaskStatus())).append("\n");
        sb.append("分派意见：").append(emptyToDash(task.getDispatchOpinion())).append("\n");
        sb.append("问题描述：").append(emptyToDash(task.getProblemDescription()));

        return sb.toString();
    }

    private String formatDate(Date date)
    {
        if (date == null)
        {
            return "-";
        }

        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private String buildTraceRemark(QualityTaskDto task)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("由质量问题管理中心分派任务同步生成。").append("\n");
        sb.append("来源质量任务ID：").append(task.getTaskId()).append("\n");
        sb.append("来源质量问题ID：").append(task.getProblemId()).append("\n");
        sb.append("来源质量问题编号：").append(emptyToDash(task.getProblemCode())).append("\n");
        sb.append("来源模块任务状态：").append(emptyToDash(task.getTaskStatus()));

        return sb.toString();
    }

    private String emptyToDash(String value)
    {
        return StringUtils.isEmpty(value) ? "-" : value;
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
        // 当前版本暂时取消课题四前置流程，以下字段初始化逻辑保留但不启用。
        // topic5TraceProblem.setTopic4Status(0L);
        // topic5TraceProblem.setTopic4ResultFilled(0L);

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

        // 从课题一数字卷宗获取传感器文件路径
        List<Topic5TraceAttachment> allList = buildAllDossierSensorFiles(problem);

// 从课题四智能故障诊断结果中提取“故障标签”
        Integer faultLabel = extractTopic4FaultLabel(problem);

// 如果没有解析到标签，默认按 0 处理，即展示前 5 个文件
        if (faultLabel == null)
        {
            faultLabel = 0;
        }

// 根据故障标签筛选数字卷宗附件
        List<Topic5TraceAttachment> list = selectDossierFilesByFaultLabel(allList, faultLabel);

        System.out.println("数字卷宗附件筛选：故障标签 = " + faultLabel + "，展示文件数 = " + list.size());

        for (Topic5TraceAttachment item : list)
        {
            item.setTraceId(id);

            /*
             * 当前版本取消“保存附件”按钮。
             * 调用数字卷宗后，认为数据已经可用于后续算法。
             */
            item.setSavedFlag(1L);
            item.setFileSource("数字卷宗");
            item.setCreateTime(DateUtils.getNowDate());

            traceAttachmentMapper.insertTopic5TraceAttachment(item);
        }

        /*
         * 关键修改：
         * 调用数字卷宗成功后，直接进入 workflow_stage = 2。
         * 因为 runAlgorithmMock 中判断 workflowStage >= 2 才允许运行根因诊断算法。
         */
        Topic5TraceProblem updateProblem = new Topic5TraceProblem();
        updateProblem.setId(id);
        updateProblem.setWorkflowStage(2L);
        updateProblem.setStatus("处理中");
        updateProblem.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(updateProblem);

        insertFlowLog(
                id,
                2L,
                "调用数字卷宗数据",
                "成功",
                "已从数字卷宗获取传感器数据，可运行根因诊断算法"
        );

        return list;
    }

    private List<Topic5TraceAttachment> buildAllDossierSensorFiles(Topic5TraceProblem problem)
    {
        List<Topic5TraceAttachment> list = new ArrayList<>();

        String basePath = "D:/2.11/topic5_code/code1/uci_data/";

        addDossierFile(list, "冷却效率参数 CE.txt", "冷却效率参数", "CE.txt", basePath + "CE.txt");
        addDossierFile(list, "冷却功率参数 CP.txt", "冷却功率参数", "CP.txt", basePath + "CP.txt");
        addDossierFile(list, "电机功率参数 EPS1.txt", "电机功率参数", "EPS1.txt", basePath + "EPS1.txt");
        addDossierFile(list, "供压流量传感器 FS1.txt", "供压流量传感器", "FS1.txt", basePath + "FS1.txt");
        addDossierFile(list, "回油流量传感器 FS2.txt", "回油流量传感器", "FS2.txt", basePath + "FS2.txt");

        addDossierFile(list, "入口压力传感器 PS1.txt", "入口压力传感器", "PS1.txt", basePath + "PS1.txt");
        addDossierFile(list, "出口压力传感器 PS2.txt", "出口压力传感器", "PS2.txt", basePath + "PS2.txt");
        addDossierFile(list, "弯管前段压力传感器 PS3.txt", "弯管前段压力传感器", "PS3.txt", basePath + "PS3.txt");
        addDossierFile(list, "弯管后段压力传感器 PS4.txt", "弯管后段压力传感器", "PS4.txt", basePath + "PS4.txt");
        addDossierFile(list, "主起供压支路压力传感器 PS5.txt", "主起供压支路压力传感器", "PS5.txt", basePath + "PS5.txt");
        addDossierFile(list, "泄压支路压力传感器 PS6.txt", "泄压支路压力传感器", "PS6.txt", basePath + "PS6.txt");

        addDossierFile(list, "系统效率状态序列 SE.txt", "系统效率状态序列", "SE.txt", basePath + "SE.txt");

        addDossierFile(list, "管路入口温度传感器 TS1.txt", "管路入口温度传感器", "TS1.txt", basePath + "TS1.txt");
        addDossierFile(list, "管路出口温度传感器 TS2.txt", "管路出口温度传感器", "TS2.txt", basePath + "TS2.txt");
        addDossierFile(list, "弯管外壁温度传感器 TS3.txt", "弯管外壁温度传感器", "TS3.txt", basePath + "TS3.txt");
        addDossierFile(list, "环境温度传感器 TS4.txt", "环境温度传感器", "TS4.txt", basePath + "TS4.txt");

        return list;
    }

    private void addDossierFile(
            List<Topic5TraceAttachment> list,
            String displayName,
            String sensorType,
            String fileName,
            String fileUrl)
    {
        Topic5TraceAttachment attachment = new Topic5TraceAttachment();

        attachment.setFileName(displayName);
        attachment.setSensorType(sensorType);
        attachment.setFileType("txt");
        attachment.setFileSize("-");
        attachment.setFileSource("数字卷宗");
        attachment.setFileUrl(fileUrl);
        attachment.setSavedFlag(1L);

        list.add(attachment);
    }

    /**
     * 根据课题四故障标签筛选数字卷宗附件。
     *
     * faultLabel = 0 -> 第1~5个文件
     * faultLabel = 1 -> 第2~6个文件
     * faultLabel = 2 -> 第3~7个文件
     * 超出范围则从头循环。
     */
    private List<Topic5TraceAttachment> selectDossierFilesByFaultLabel(
            List<Topic5TraceAttachment> allList,
            Integer faultLabel)
    {
        List<Topic5TraceAttachment> selectedList = new ArrayList<>();

        if (allList == null || allList.isEmpty())
        {
            return selectedList;
        }

        int fileCount = allList.size();
        int windowSize = Math.min(5, fileCount);

        int startIndex = 0;

        if (faultLabel != null)
        {
            startIndex = Math.floorMod(faultLabel, fileCount);
        }

        for (int i = 0; i < windowSize; i++)
        {
            int index = (startIndex + i) % fileCount;
            selectedList.add(allList.get(index));
        }

        return selectedList;
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

        // 3. 当前流程取消课题四前置依赖：
        //    保存数字卷宗附件后，直接进入 workflow_stage = 2，
        //    后续即可运行第一部分算法。
        Topic5TraceProblem updateProblem = new Topic5TraceProblem();
        updateProblem.setId(id);
        updateProblem.setWorkflowStage(2L);
        updateProblem.setStatus("处理中");
        updateProblem.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(updateProblem);

        insertFlowLog(
                id,
                2L,
                "数字卷宗附件保存",
                "成功",
                "已保存数字卷宗附件，当前流程已进入多元特征提取/数据处理阶段，可运行第一部分算法"
        );

        // 原逻辑保留，暂不启用：
        // updateWorkflowStage(id, 3L, "数字卷宗传感器附件已保存到追溯任务");
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

        /*
         * 课题四推送逻辑暂时停用。
         * 当前流程调整为：
         * 数字卷宗调用并保存附件完成后，即 workflow_stage = 2，
         * 可直接运行第一部分算法，不再要求推送课题四或等待课题四反馈。
         *
         * 原课题四推送逻辑保留在历史版本中，不删除。
         */

        insertFlowLog(
                id,
                2L,
                "课题四推送",
                "跳过",
                "当前版本已取消课题四前置流程，未执行课题四推送"
        );
    }

    /**
     * 接收课题四回调
     */
    @Override
    @Transactional
    public void receiveTopic4Callback(Topic4CallbackDTO dto)
    {
        /*
         * 课题四回调逻辑暂时停用。
         * 当前版本不再依赖课题四返回结果驱动 workflow_stage。
         *
         * 原逻辑包括：
         * 1. 校验 dto.traceId；
         * 2. 写入 topic4Status、topic4FaultType、topic4FaultLocation、
         *    topic4CauseAnalysis、topic4DeductionProcess、topic4RootConfidence；
         * 3. 写入“接收课题四结果”流程日志。
         *
         * 以上逻辑保留为注释说明，不删除。
         */
        if (dto == null || dto.getTraceId() == null)
        {
            return;
        }

        insertFlowLog(
                dto.getTraceId(),
                2L,
                "课题四回调",
                "跳过",
                "当前版本已取消课题四前置流程，未写入课题四回调结果"
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

        /*
         * 课题四结果回填逻辑暂时停用。
         * 原逻辑会检查 topic4Status == 2，并设置：
         * topic4ResultFilled = 1
         * workflowStage = 2
         * status = 处理中
         *
         * 当前版本 workflowStage = 2 已改由“保存数字卷宗附件”触发，
         * 因此这里不再更新课题四字段，也不再作为第一部分算法前置条件。
         */

        insertFlowLog(
                id,
                2L,
                "课题四结果回填",
                "跳过",
                "当前版本已取消课题四前置流程，workflow_stage 由数字卷宗附件保存触发"
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
    public Map<String, Object> runAlgorithmMock(Long id)
    {
        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法运行根因诊断算法");
        }

        // 第一部分算法前置条件已调整：
        // 当前版本不再判断课题四结果是否回填；
        // 只要数字卷宗附件保存完成，即 workflow_stage >= 2，就可以运行第一部分算法。
        /*
        if (!Long.valueOf(1L).equals(problem.getTopic4ResultFilled()))
        {
            throw new ServiceException("请先完成课题四结果回填，再运行第一部分算法");
        }
        */

        if (problem.getWorkflowStage() == null || problem.getWorkflowStage() < 2L)
        {
            throw new ServiceException("请先调用数字卷宗并保存附件，再运行根因诊断算法");
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

            // 新增：从智能故障诊断结果中提取“故障标签”并传给第一部分 Python 算法
            Integer topic4FaultLabel = extractTopic4FaultLabel(problem);

            if (topic4FaultLabel == null)
            {
                throw new ServiceException("未能从智能故障诊断结果中解析出故障标签，无法运行根因诊断算法");
            }

            // Python 端当前要求的字段名
            requestBody.put("pipe_fault_label", topic4FaultLabel);

            // 兼容保留字段
            requestBody.put("fault_label", topic4FaultLabel);
            requestBody.put("topic4_fault_label", topic4FaultLabel);

            // 智能故障诊断原始结果
            requestBody.put(
                    "topic4_diagnosis_result",
                    StringUtils.isEmpty(problem.getTopic4CauseAnalysis()) ? "" : problem.getTopic4CauseAnalysis()
            );

            System.out.println("传给第一部分Python的 pipe_fault_label = " + topic4FaultLabel);

            // 新增：同时把智能故障诊断原始文本也传过去，便于 Python 端记录或兜底解析
            requestBody.put(
                    "topic4_diagnosis_result",
                    StringUtils.isEmpty(problem.getTopic4CauseAnalysis()) ? "" : problem.getTopic4CauseAnalysis()
            );

            System.out.println("传给第一部分Python的故障标签 topic4_fault_label = " + topic4FaultLabel);

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
            update.setStatus("故障根因分析完成");
            update.setUpdateTime(DateUtils.getNowDate());

            topic5TraceProblemMapper.updateTopic5TraceProblem(update);

            insertFlowLog(
                    id,
                    3L,
                    "故障根因分析",
                    "成功",
                    "已调用Python时空图神经网络定位算法并完成根因诊断分析"
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
            failUpdate.setAlgorithmResult("根因诊断算法运行失败：" + e.getMessage());
            failUpdate.setStatus("处理中");
            failUpdate.setUpdateTime(DateUtils.getNowDate());
            topic5TraceProblemMapper.updateTopic5TraceProblem(failUpdate);

            insertFlowLog(
                    id,
                    3L,
                    "故障根因分析",
                    "失败",
                    "Python算法运行失败：" + e.getMessage()
            );

            throw new ServiceException("Python算法运行失败：" + e.getMessage());
        }
    }
    private Integer extractTopic4FaultLabel(Topic5TraceProblem problem)
    {
        if (problem == null)
        {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        if (!StringUtils.isEmpty(problem.getTopic4CauseAnalysis()))
        {
            sb.append(problem.getTopic4CauseAnalysis()).append(" ");
        }

        if (!StringUtils.isEmpty(problem.getTopic4DeductionProcess()))
        {
            sb.append(problem.getTopic4DeductionProcess()).append(" ");
        }

        String text = sb.toString();

        if (StringUtils.isEmpty(text))
        {
            return null;
        }

        try
        {
            java.util.regex.Pattern pattern =
                    java.util.regex.Pattern.compile("故障标签\\s*[:：]\\s*(\\d+)");

            java.util.regex.Matcher matcher = pattern.matcher(text);

            if (matcher.find())
            {
                return Integer.parseInt(matcher.group(1));
            }
        }
        catch (Exception e)
        {
            System.err.println("解析智能故障诊断故障标签失败：" + e.getMessage());
        }

        return null;
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
        pressureFile.setRemark("压力传感器数据文件");
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
        temperatureFile.setRemark("温度传感器数据文件");
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
        vibrationFile.setRemark("振动传感器数据文件");
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
                return "原始数据获取";
            case 3:
                return "故障根因分析";
            case 4:
                return "卷宗实体映射";
            case 5:
                return "溯源图谱构建";
            case 6:
                return "全链路追溯闭环";
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

        if (Long.valueOf(1L).equals(problem.getAlgorithmStatus()))
        {
            throw new ServiceException("根因诊断算法正在运行中，请勿重复点击");
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
            updateAttachment.setFileSource("本地保存");
            updateAttachment.setSavedFlag(1L);
            updateAttachment.setRemark("已从数字卷宗源路径复制到对应目录");

            traceAttachmentMapper.updateTopic5TraceAttachment(updateAttachment);
        }

        Topic5TraceProblem updateProblem = new Topic5TraceProblem();
        updateProblem.setId(id);
        updateProblem.setAttachmentSavePath(basePath);

        // 当前流程取消课题四前置依赖：
        // 保存数字卷宗附件后，直接进入 workflow_stage = 2，
        // 后续即可运行第一部分算法。
        updateProblem.setWorkflowStage(2L);
        updateProblem.setStatus("处理中");
        updateProblem.setUpdateTime(DateUtils.getNowDate());

        topic5TraceProblemMapper.updateTopic5TraceProblem(updateProblem);

        insertFlowLog(
                id,
                2L,
                "数字卷宗附件保存",
                "成功",
                "已将传感器文件复制到：" + basePath + "，当前已进入原始数据获取阶段，可运行根因诊断算法"
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
            throw new ServiceException("请先完成根因诊断算法运行，再导入知识图谱");
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
                "已从数字卷宗拉取并导入原始知识图谱"
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
        System.out.println("当前第二部分 Python 算法地址：" + secondAlgorithmUrl);

        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法运行知识图谱算法");
        }

        if (problem.getTopic1KgStatus() == null || !Long.valueOf(1L).equals(problem.getTopic1KgStatus()))
        {
            throw new ServiceException("请先从数字卷宗拉取知识图谱，再运行知识图谱算法");
        }

        if (problem.getAlgorithmStatus() == null || !Long.valueOf(2L).equals(problem.getAlgorithmStatus()))
        {
            throw new ServiceException("请先完成根因诊断算法运行，再运行知识图谱算法");
        }

        if (problem.getAlgorithmResult() == null || problem.getAlgorithmResult().trim().isEmpty())
        {
            throw new ServiceException("根因诊断算法结果为空，无法传入知识图谱算法");
        }

        if (algorithmName == null || "".equals(algorithmName.trim()))
        {
            throw new ServiceException("请选择知识图谱算法");
        }

        try
        {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("domain", "hydraulic");
            requestBody.put("case_id", "HFB-01676");
            requestBody.put("model_variant", "default");
            requestBody.put("generate_graph", true);
            requestBody.put("generate_report", false);
            requestBody.put("generate_echarts", true);
            requestBody.put("generate_docx", false);

            Map<String, Object> firstAlgorithmForPython = buildFirstAlgorithmPayloadForPython(problem);
            requestBody.put("first_algorithm_result", firstAlgorithmForPython);
            requestBody.put("first_algorithm_result_raw", problem.getAlgorithmResult());

            requestBody.put("first_algorithm_status", problem.getAlgorithmStatus());
            requestBody.put("first_algorithm_trace_id", problem.getId());
            requestBody.put("first_algorithm_trace_no", problem.getTraceNo());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            //System.out.println("第二部分 Python 请求地址：" + secondAlgorithmUrl);
            //System.out.println("第二部分 Python 请求体：" + JSON.toJSONString(requestBody));

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    secondAlgorithmUrl,
                    entity,
                    Map.class
            );

            Map responseBody = response.getBody();

            if (responseBody == null)
            {
                throw new ServiceException("Python算法服务无返回结果");
            }

            Object codeObj = responseBody.get("code");
            if (codeObj != null && Integer.parseInt(codeObj.toString()) != 200)
            {
                Object msgObj = responseBody.get("msg");
                if (msgObj == null)
                {
                    msgObj = responseBody.get("message");
                }

                throw new ServiceException("Python算法执行失败：" + (msgObj == null ? "未知错误" : msgObj.toString()));
            }

            String resultTableJson = buildSecondAlgorithmTableJsonFromPython(responseBody, problem, algorithmName);

            Topic5TraceProblem update = new Topic5TraceProblem();
            update.setId(id);
            update.setSecondAlgorithmName(algorithmName);
            update.setSecondAlgorithmStatus(2L);
            update.setSecondAlgorithmResultJson(resultTableJson);

            // 取消人工判定后，第二部分算法运行成功即视为结果已保存、知识图谱重构已完成
            update.setKgRebuildStatus(1L);
            update.setKgGraphUrl(null);

            // 流程进入第二部分算法完成阶段
            update.setWorkflowStage(5L);
            update.setStatus("溯源图谱构建完成");
            update.setUpdateTime(DateUtils.getNowDate());

            topic5TraceProblemMapper.updateTopic5TraceProblem(update);

            insertFlowLog(
                    id,
                    5L,
                    "知识图谱算法运行",
                    "成功",
                    "已调用Python FastAPI完成算法运行，系统自动保存算法结果：" + algorithmName
            );

            Topic5TraceProblem newProblem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);
            Map parsedResult = JSON.parseObject(resultTableJson, Map.class);

            Map<String, Object> result = new HashMap<>();
            result.put("traceProblem", newProblem);
            result.put("secondAlgorithmResultJson", resultTableJson);
            result.put("secondAlgorithmResult", parsedResult);
            result.put("summaryRows", parsedResult.get("summaryRows"));
            result.put("componentDiagnosisTop3", parsedResult.get("componentDiagnosisTop3"));
            result.put("subtypeTop5", parsedResult.get("subtypeTop5"));
            result.put("pythonResponse", responseBody);

            return result;
        }
        catch (Exception e)
        {
            Topic5TraceProblem failUpdate = new Topic5TraceProblem();
            failUpdate.setId(id);
            failUpdate.setSecondAlgorithmStatus(3L);
            failUpdate.setStatus("处理中");
            failUpdate.setUpdateTime(DateUtils.getNowDate());

            topic5TraceProblemMapper.updateTopic5TraceProblem(failUpdate);

            insertFlowLog(
                    id,
                    5L,
                    "知识图谱算法运行",
                    "失败",
                    "Python算法运行失败：" + e.getMessage()
            );

            throw new ServiceException("Python算法运行失败：" + e.getMessage());
        }
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
        try
        {
            ClassPathResource resource = new ClassPathResource("mock/topic5/c011_pipe_kg.json");

            String json;

            try (InputStream inputStream = resource.getInputStream())
            {
                json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            Map graphWrapper = JSON.parseObject(json, Map.class);

            /*
             * 你上传的 JSON 中已经包含 echartsOption。
             * 第二页面显示原始知识图谱时，直接返回 echartsOption 最稳。
             */
            Object echartsOption = graphWrapper.get("echartsOption");

            if (echartsOption != null)
            {
                return JSON.toJSONString(echartsOption);
            }

            /*
             * 如果后续某个 JSON 没有 echartsOption，但有 rawGraph，
             * 则自动把 rawGraph 转成 ECharts option。
             */
            Object rawGraph = graphWrapper.get("rawGraph");

            if (rawGraph != null)
            {
                return JSON.toJSONString(convertRawGraphToEchartsOption(rawGraph, "C011 管路总成知识图谱"));
            }

            return json;
        }
        catch (Exception e)
        {
            throw new ServiceException("读取C011原始知识图谱JSON失败：" + e.getMessage());
        }
    }
    private String buildMockSecondAlgorithmTableJson(Topic5TraceProblem problem, String algorithmName)
    {
        String partName = problem.getPartName() == null ? "故障部件" : problem.getPartName();

        List<Map<String, Object>> rows = new ArrayList<>();

        Map<String, Object> row1 = new HashMap<>();
        row1.put("rank", 1);
        row1.put("resultType", "候选根因");
        row1.put("causeName", "液压管路压力波动异常");
        row1.put("relatedEntity", "C011 管路总成");
        row1.put("evidence", "压力传感器异常能量较高，且与管路总成存在关联");
        row1.put("confidence", 0.91);
        row1.put("riskLevel", "高");
        row1.put("suggestion", "建议优先检查管路连接状态、密封状态及压力保持能力");
        rows.add(row1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("rank", 2);
        row2.put("resultType", "候选根因");
        row2.put("causeName", "节流阀流量调节异常");
        row2.put("relatedEntity", "C010 节流阀");
        row2.put("evidence", "流量传感器 FS1 异常，并沿压力链路传播");
        row2.put("confidence", 0.84);
        row2.put("riskLevel", "较高");
        row2.put("suggestion", "建议检查节流阀开度、堵塞状态和装配间隙");
        rows.add(row2);

        Map<String, Object> row3 = new HashMap<>();
        row3.put("rank", 3);
        row3.put("resultType", "候选根因");
        row3.put("causeName", "作动筒响应异常");
        row3.put("relatedEntity", "C003 作动筒");
        row3.put("evidence", "当前故障部位“" + partName + "”与作动筒存在直接功能关联");
        row3.put("confidence", 0.78);
        row3.put("riskLevel", "中");
        row3.put("suggestion", "建议检查作动筒密封、响应滞后和磨损状态");
        rows.add(row3);

        Map<String, Object> row4 = new HashMap<>();
        row4.put("rank", 4);
        row4.put("resultType", "辅助证据");
        row4.put("causeName", "传感器异常传播链路");
        row4.put("relatedEntity", "压力/流量/温度传感器");
        row4.put("evidence", "多源传感器异常与知识图谱实体存在多跳关联");
        row4.put("confidence", 0.69);
        row4.put("riskLevel", "中");
        row4.put("suggestion", "建议结合第一部分故障根因分析结果复核传感器异常能量分布");
        rows.add(row4);

        return JSON.toJSONString(rows);
    }
    private String buildSecondAlgorithmTableJsonFromPython(Map responseBody, Topic5TraceProblem problem, String algorithmName)
    {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> summaryRows = new ArrayList<>();

        Object dataObj = responseBody.get("data");
        if (dataObj == null)
        {
            dataObj = responseBody;
        }

        Map dataMap = null;
        if (dataObj instanceof Map)
        {
            dataMap = (Map) dataObj;
        }
        else
        {
            addRcaResultRow(summaryRows, 1, "Python算法返回结果", "raw_response", valueToText(responseBody), "Python接口未返回标准data结构，已展示原始返回内容");

            result.put("summaryRows", summaryRows);
            result.put("componentDiagnosisTop3", new ArrayList<>());
            result.put("subtypeTop5", new ArrayList<>());
            return JSON.toJSONString(result);
        }

        Map rcaContext = null;

        Object reasoningObj = dataMap.get("reasoning");
        if (reasoningObj instanceof Map)
        {
            Object rcaObj = ((Map) reasoningObj).get("rca_context");
            if (rcaObj instanceof Map)
            {
                rcaContext = (Map) rcaObj;
            }
        }

        if (rcaContext == null)
        {
            Object reasoningObj2 = responseBody.get("reasoning");
            if (reasoningObj2 instanceof Map)
            {
                Object rcaObj2 = ((Map) reasoningObj2).get("rca_context");
                if (rcaObj2 instanceof Map)
                {
                    rcaContext = (Map) rcaObj2;
                }
            }
        }

        if (rcaContext == null)
        {
            int rank = 1;
            for (Object keyObj : dataMap.keySet())
            {
                String key = String.valueOf(keyObj);
                Object value = dataMap.get(keyObj);
                addRcaResultRow(summaryRows, rank++, key, key, valueToText(value), "未找到 reasoning.rca_context，当前展示 Python data 中的原始字段");
            }

            result.put("summaryRows", summaryRows);
            result.put("componentDiagnosisTop3", new ArrayList<>());
            result.put("subtypeTop5", new ArrayList<>());
            return JSON.toJSONString(result);
        }

        addRcaResultRow(summaryRows, 1, "根因传感器", "root_sensor", rcaContext.get("root_sensor"), "RCA判断的首要异常传感器");
        addRcaResultRow(summaryRows, 2, "根因传感器置信度", "root_sensor_confidence", rcaContext.get("root_sensor_confidence"), "根因传感器对应的可信度评分");
        addRcaResultRow(summaryRows, 3, "次因传感器", "secondary_sensor", rcaContext.get("secondary_sensor"), "RCA判断的次级异常传感器");
        addRcaResultRow(summaryRows, 4, "次因传感器置信度", "secondary_sensor_confidence", rcaContext.get("secondary_sensor_confidence"), "次因传感器对应的可信度评分");
        addRcaResultRow(summaryRows, 5, "根因部件编码", "root_component_code", rcaContext.get("root_component_code"), "根因部件在生命周期质量知识图谱中的编码");
        addRcaResultRow(summaryRows, 6, "根因部件名称", "root_component_name", rcaContext.get("root_component_name"), "根因部件名称");
        addRcaResultRow(summaryRows, 7, "RCA置信度", "rca_confidence", rcaContext.get("rca_confidence"), "部件级根因定位的综合置信度");

        result.put("summaryRows", summaryRows);
        result.put("componentDiagnosisTop3", normalizeListObject(rcaContext.get("component_diagnosis_top3")));
        result.put("subtypeTop5", normalizeListObject(rcaContext.get("subtype_top5")));

        return JSON.toJSONString(result);
    }

    private List<Object> normalizeListObject(Object value)
    {
        List<Object> list = new ArrayList<>();

        if (value == null)
        {
            return list;
        }

        if (value instanceof List)
        {
            return (List<Object>) value;
        }

        list.add(value);
        return list;
    }
    private void addRcaResultRow(List<Map<String, Object>> rows, int rank, String fieldName, String fieldKey, Object fieldValue, String description)
    {
        Map<String, Object> row = new HashMap<>();
        row.put("rank", rank);
        row.put("fieldName", fieldName);
        row.put("fieldKey", fieldKey);
        row.put("fieldValue", valueToText(fieldValue));
        row.put("description", description);
        rows.add(row);
    }


    private Object getFirstNonNull(Map item, String... keys)
    {
        for (String key : keys)
        {
            Object value = item.get(key);
            if (value != null && !"".equals(value.toString()))
            {
                return value;
            }
        }

        return null;
    }
    private void fillEmptySecondAlgorithmRow(Map<String, Object> row)
    {
        if (row.get("causeName") == null)
        {
            row.put("causeName", "-");
        }

        if (row.get("relatedEntity") == null)
        {
            row.put("relatedEntity", "-");
        }

        if (row.get("evidence") == null)
        {
            row.put("evidence", "-");
        }

        if (row.get("riskLevel") == null)
        {
            row.put("riskLevel", "-");
        }

        if (row.get("suggestion") == null)
        {
            row.put("suggestion", "请结合原始 Python 返回结果进一步确认");
        }
    }
    private String valueToText(Object value)
    {
        if (value == null)
        {
            return "-";
        }

        if (value instanceof Map || value instanceof List)
        {
            return JSON.toJSONString(value);
        }

        return String.valueOf(value);
    }
    private Map<String, Object> parseGraphJsonToMap(String graphJson)
    {
        return JSON.parseObject(graphJson, Map.class);
    }
    @Override
    @Transactional
    public Map<String, Object> runSourceAlgorithm(Long id, String algorithmName)
    {
        System.out.println("当前第三部分 Python 算法地址：" + thirdAlgorithmUrl);

        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法运行最终溯源算法");
        }

        if (problem.getSecondAlgorithmStatus() == null || !Long.valueOf(2L).equals(problem.getSecondAlgorithmStatus()))
        {
            throw new ServiceException("请先完成第二部分算法运行，再进行最终溯源");
        }

        if (algorithmName == null || "".equals(algorithmName.trim()))
        {
            throw new ServiceException("请选择最终溯源算法");
        }

        try
        {
            // 1. 组装 Python FastAPI 请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("domain", "hydraulic");

            // 当前 Python 数据集中存在的 case_id。
            // 后续正式系统可以改成 problem.getPythonCaseId() 或数据库绑定字段。
            requestBody.put("case_id", "HFB-01676");

            requestBody.put("model_variant", "default");

            // 第三页面需要图谱，所以 generate_graph 必须为 true
            requestBody.put("generate_graph", true);

            // 先关闭报告生成，避免 docx / charts 路径问题导致 Python 500
            requestBody.put("generate_report", false);
            requestBody.put("generate_charts", false);
            requestBody.put("generate_docx", false);
            requestBody.put("generate_echarts", true);

            Map<String, Object> firstAlgorithmForPython = buildFirstAlgorithmPayloadForPython(problem);
            requestBody.put("first_algorithm_result", firstAlgorithmForPython);
            requestBody.put("first_algorithm_result_raw", problem.getAlgorithmResult());


            requestBody.put("first_algorithm_status", problem.getAlgorithmStatus());
            requestBody.put("first_algorithm_trace_id", problem.getId());
            requestBody.put("first_algorithm_trace_no", problem.getTraceNo());

            //System.out.println("========== 最终溯源 firstAlgorithmForPython ==========");
            //System.out.println(JSON.toJSONString(firstAlgorithmForPython));
            //System.out.println("========== 最终溯源 raw algorithmResult ==========");
            //System.out.println(problem.getAlgorithmResult());
            //System.out.println("========== 最终溯源 Python 请求体 ==========");
            //System.out.println(JSON.toJSONString(requestBody));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();

            // 2. 调用 Python FastAPI
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    thirdAlgorithmUrl,
                    entity,
                    Map.class
            );

            Map responseBody = response.getBody();
            //System.out.println("========== 最终溯源 Python 返回体 ==========");
            //System.out.println(JSON.toJSONString(responseBody));

            if (responseBody == null)
            {
                throw new ServiceException("Python最终溯源算法服务无返回结果");
            }

            Object codeObj = responseBody.get("code");
            if (codeObj != null && Integer.parseInt(codeObj.toString()) != 200)
            {
                Object msgObj = responseBody.get("msg");
                if (msgObj == null)
                {
                    msgObj = responseBody.get("message");
                }

                throw new ServiceException("Python最终溯源算法执行失败：" + (msgObj == null ? "未知错误" : msgObj.toString()));
            }

            // 3. 从 Python 返回中提取第三页面需要的数据
            String sourceGraphJson = extractSourceGraphJson(responseBody);
            String reasonTableJson = buildFinalTraceReasonTableJson(responseBody);
            String summary = buildFinalTraceSummary(responseBody);

            /*
             * 兜底修复：
             * Python 端 reasoning.reasonList 已经是处理后的最终原因表，
             * Java 外层 reasonTableJson 必须优先使用它。
             */
            Map reasoningMap = getReasoningMap(responseBody);

            if (reasoningMap != null)
            {
                Object pythonReasonList = reasoningMap.get("reasonList");


                if (pythonReasonList instanceof List && !((List) pythonReasonList).isEmpty())
                {
                    String patchedReasonTableJson = buildReasonTableJsonFromPythonReasonList(pythonReasonList);

                    if (patchedReasonTableJson != null && !"[]".equals(patchedReasonTableJson))
                    {
                        reasonTableJson = patchedReasonTableJson;


                    }
                }
            }
            sourceGraphJson = patchPipeDesignSourceGraphJson(sourceGraphJson, responseBody, reasonTableJson);
            //String traceReportUrl = extractTraceReportUrl(responseBody);

            // 4. 写回数据库
            Topic5TraceProblem update = new Topic5TraceProblem();
            update.setId(id);
            update.setSourceAlgorithmName(algorithmName);
            update.setSourceAlgorithmStatus(2L);
            update.setSourceGraphJson(sourceGraphJson);
            update.setSourceReasonTableJson(reasonTableJson);
            update.setSourceResultSummary(summary);

//            if (traceReportUrl != null && !"".equals(traceReportUrl))
//            {
//                update.setTraceReportUrl(traceReportUrl);
//                update.setTraceReportStatus(1L);
//            }

            // 最终溯源算法完成，对应全链路追溯闭环阶段
            update.setWorkflowStage(6L);
            update.setStatus("全链路追溯完成");
            update.setUpdateTime(DateUtils.getNowDate());

            topic5TraceProblemMapper.updateTopic5TraceProblem(update);

            insertFlowLog(
                    id,
                    6L,
                    "溯源算法运行",
                    "成功",
                    "已调用Python FastAPI完成最终溯源算法运行：" + algorithmName
            );

            Topic5TraceProblem newProblem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

            Map<String, Object> result = new HashMap<>();
            result.put("traceProblem", newProblem);
            result.put("sourceGraphJson", sourceGraphJson);
            result.put("reasonTableJson", reasonTableJson);
            result.put("summary", summary);
//            result.put("traceReportUrl", traceReportUrl);
            result.put("graphData", parseGraphJsonToMap(sourceGraphJson));
            result.put("reasonList", JSON.parseArray(reasonTableJson, Map.class));
            result.put("pythonResponse", responseBody);

            return result;
        }
        catch (Exception e)
        {
            Topic5TraceProblem failUpdate = new Topic5TraceProblem();
            failUpdate.setId(id);
            failUpdate.setSourceAlgorithmStatus(3L);
            failUpdate.setStatus("处理中");
            failUpdate.setUpdateTime(DateUtils.getNowDate());

            topic5TraceProblemMapper.updateTopic5TraceProblem(failUpdate);

            insertFlowLog(
                    id,
                    6L,
                    "溯源算法运行",
                    "失败",
                    "Python溯源算法运行失败：" + e.getMessage()
            );

            throw new ServiceException("Python溯源算法运行失败：" + e.getMessage());
        }
    }
    private String extractSourceGraphJson(Map responseBody)
    {
        if (responseBody == null)
        {
            return "{\"series\":[]}";
        }

        Object graphsObj = responseBody.get("graphs");

        // 兼容 data.graphs 的情况
        if (!(graphsObj instanceof Map))
        {
            Object dataObj = responseBody.get("data");
            if (dataObj instanceof Map)
            {
                graphsObj = ((Map) dataObj).get("graphs");
            }
        }

        if (graphsObj instanceof Map)
        {
            Map graphsMap = (Map) graphsObj;

            // 1. 优先读取 Python 已经生成好的 ECharts 故障增强图谱
            Object echartsFaultGraph = graphsMap.get("echarts_fault_enhanced_graph");
            if (echartsFaultGraph != null)
            {
                return JSON.toJSONString(echartsFaultGraph);
            }

            // 2. 如果没有故障增强图谱，则读取 ECharts 原始图谱
            Object echartsOriginalGraph = graphsMap.get("echarts_original_graph");
            if (echartsOriginalGraph != null)
            {
                return JSON.toJSONString(echartsOriginalGraph);
            }

            // 3. 如果只有普通 nodes / edges 格式，则转换为 ECharts option
            Object faultGraph = graphsMap.get("fault_enhanced_graph");
            if (faultGraph != null)
            {
                return JSON.toJSONString(convertRawGraphToEchartsOption(faultGraph, "故障增强知识图谱"));
            }

            Object originalGraph = graphsMap.get("original_graph");
            if (originalGraph != null)
            {
                return JSON.toJSONString(convertRawGraphToEchartsOption(originalGraph, "原始生命周期知识图谱"));
            }
        }

        return "{\"series\":[]}";
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private String patchPipeDesignSourceGraphJson(String sourceGraphJson, Map responseBody, String reasonTableJson)
    {
        if (sourceGraphJson == null || "".equals(sourceGraphJson.trim()))
        {
            return sourceGraphJson;
        }

        if (!isPipeDesignGraphRelated(responseBody, reasonTableJson))
        {
            return sourceGraphJson;
        }

        try
        {
            Map option = JSON.parseObject(sourceGraphJson, Map.class);

            String designCandidateId = findCandidateIdFromReasonTable(reasonTableJson, "PipeDesignParam", "HPIP-");
            String materialCandidateId = findCandidateIdFromReasonTable(reasonTableJson, "MaterialBatch", "HMAT-");

            if (designCandidateId == null || "".equals(designCandidateId))
            {
                designCandidateId = "HPIP-00632";
            }

            if (materialCandidateId == null || "".equals(materialCandidateId))
            {
                materialCandidateId = "HMAT-00362";
            }

            Object seriesObj = option.get("series");

            if (seriesObj instanceof List)
            {
                List seriesList = (List) seriesObj;

                for (Object seriesItemObj : seriesList)
                {
                    if (!(seriesItemObj instanceof Map))
                    {
                        continue;
                    }

                    Map seriesItem = (Map) seriesItemObj;

                    Object dataObj = seriesItem.get("data");
                    if (dataObj instanceof List)
                    {
                        patchPipeDesignGraphNodes((List) dataObj, designCandidateId, materialCandidateId);
                    }

                    Object linksObj = seriesItem.get("links");
                    if (linksObj instanceof List)
                    {
                        patchPipeDesignGraphLinks((List) linksObj, designCandidateId, materialCandidateId);
                    }
                }
            }

            patchPipeDesignGraphTitleAndSummary(option, designCandidateId);

            return JSON.toJSONString(option);
        }
        catch (Exception e)
        {
            System.err.println("[PIPE_GRAPH_PATCH] 图谱修正失败，保留原图谱：" + e.getMessage());
            return sourceGraphJson;
        }
    }
    private boolean isPipeDesignGraphRelated(Map responseBody, String reasonTableJson)
    {
        StringBuilder sb = new StringBuilder();

        if (responseBody != null)
        {
            sb.append(JSON.toJSONString(responseBody)).append(" ");
        }

        if (reasonTableJson != null)
        {
            sb.append(reasonTableJson).append(" ");
        }

        String text = sb.toString();

        return text.contains("C011")
                || text.contains("管路总成")
                || text.contains("PipeDesignParam")
                || text.contains("HPIP")
                || text.contains("管路设计参数")
                || text.contains("pipe_length")
                || text.contains("bend_radius")
                || text.contains("bend_angle");
    }
    private String findCandidateIdFromReasonTable(String reasonTableJson, String typeKeyword, String idPrefix)
    {
        if (reasonTableJson == null || "".equals(reasonTableJson.trim()))
        {
            return "";
        }

        try
        {
            List<Map> rows = JSON.parseArray(reasonTableJson, Map.class);

            if (rows == null)
            {
                return "";
            }

            for (Map row : rows)
            {
                String rowText = JSON.toJSONString(row);

                if (!rowText.contains(typeKeyword))
                {
                    continue;
                }

                Object reasonNameObj = row.get("reasonName");
                Object relatedPartObj = row.get("relatedPart");

                if (reasonNameObj != null && String.valueOf(reasonNameObj).startsWith(idPrefix))
                {
                    return String.valueOf(reasonNameObj);
                }

                if (relatedPartObj != null && String.valueOf(relatedPartObj).startsWith(idPrefix))
                {
                    return String.valueOf(relatedPartObj);
                }
            }
        }
        catch (Exception e)
        {
            return "";
        }

        return "";
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void patchPipeDesignGraphNodes(List data, String designCandidateId, String materialCandidateId)
    {
        for (Object nodeObj : data)
        {
            if (!(nodeObj instanceof Map))
            {
                continue;
            }

            Map node = (Map) nodeObj;
            String nodeId = getGraphItemId(node);

            Map properties = getOrCreateMap(node, "properties");

            if (designCandidateId.equals(nodeId))
            {
                node.put("category", "Top6疑似原因");
                node.put("symbolSize", 68);
                node.put("value", 0.96);

                properties.put("is_top_candidate", true);
                properties.put("rank", 1);
                properties.put("score", 0.96);
                properties.put("stage", "design");
                properties.put("stage_name", "设计阶段");
                properties.put("candidate_display_role", "首要复核对象");
                properties.put("display_conclusion", "设计阶段 / 管路设计参数");

                Map<String, Object> label = new HashMap<>();
                label.put("show", true);
                label.put("formatter", "Top1 管路设计参数\n" + designCandidateId);
                node.put("label", label);
            }
            else if (materialCandidateId.equals(nodeId))
            {
                node.put("category", "Top6疑似原因");
                node.put("symbolSize", 52);
                node.put("value", 0.8735);

                properties.put("is_top_candidate", true);
                properties.put("rank", 2);
                properties.put("score", 0.8735);
                properties.put("stage", "material");
                properties.put("stage_name", "材料阶段");
                properties.put("candidate_display_role", "辅助排查对象");

                Map<String, Object> label = new HashMap<>();
                label.put("show", true);
                label.put("formatter", "Top2 材料批次\n" + materialCandidateId);
                node.put("label", label);
            }
            else if ("STAGE_design".equals(nodeId))
            {
                node.put("value", 1.0);
                node.put("symbolSize", 52);

                properties.put("score", 1.0);
                properties.put("stage_score", 1.0);
                properties.put("primary_stage", true);

                Map<String, Object> label = new HashMap<>();
                label.put("show", true);
                label.put("formatter", "首要反馈阶段：设计阶段");
                node.put("label", label);
            }
            else if ("STAGE_material".equals(nodeId))
            {
                node.put("value", 0.8735);
                node.put("symbolSize", 42);

                properties.put("score", 0.8735);
                properties.put("stage_score", 0.8735);
                properties.put("primary_stage", false);
                properties.put("display_role", "辅助排查阶段");
            }
            else if (nodeId != null && nodeId.startsWith("CONCLUSION_"))
            {
                properties.put("conclusion_text",
                        "系统识别该质量反馈与 C011 管路总成及管路设计参数相关，首要复核方向为设计阶段管路设计参数复核，建议反馈至设计子系统。材料、制造、装配、检测及使用/运维阶段候选作为辅助排查对象保留。");
            }
            else if (nodeId != null && nodeId.startsWith("RCA_TEXT_"))
            {
                properties.put("evidence_text",
                        "该案例表现为管路总成相关异常，系统结合 RCA 根因部件、生命周期路径和管路设计参数证据，将设计阶段管路参数复核作为首要反馈方向。");
            }
        }
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void patchPipeDesignGraphLinks(List links, String designCandidateId, String materialCandidateId)
    {
        for (Object linkObj : links)
        {
            if (!(linkObj instanceof Map))
            {
                continue;
            }

            Map link = (Map) linkObj;

            String target = valueToText(link.get("target"));
            String relation = valueToText(getFirstNonNull(link, "relation", "name"));

            Map properties = getOrCreateMap(link, "properties");

            if (designCandidateId.equals(target) && relation.contains("may_caused_by"))
            {
                link.put("value", 1.0);
                link.put("weight", 1.0);
                link.put("score", 0.96);
                link.put("name", "首要疑似原因");

                properties.put("rank", 1);
                properties.put("score", 0.96);
                properties.put("candidate_scope", "lifecycle_candidate");
                properties.put("display_role", "首要复核对象");
            }
            else if (materialCandidateId.equals(target) && relation.contains("may_caused_by"))
            {
                link.put("value", 0.8735);
                link.put("weight", 0.8735);
                link.put("score", 0.8735);
                link.put("name", "辅助疑似原因");

                properties.put("rank", 2);
                properties.put("score", 0.8735);
                properties.put("candidate_scope", "lifecycle_candidate");
                properties.put("display_role", "辅助排查对象");
            }
            else if ("STAGE_design".equals(target) && relation.contains("attributed_to_stage"))
            {
                link.put("value", 1.0);
                link.put("weight", 1.0);
                link.put("score", 1.0);
                link.put("name", "首要反馈阶段");

                properties.put("primary_stage", true);
                properties.put("score", 1.0);
            }
            else if ("STAGE_material".equals(target) && relation.contains("attributed_to_stage"))
            {
                link.put("value", 0.8735);
                link.put("weight", 0.8735);
                link.put("score", 0.8735);
                link.put("name", "辅助排查阶段");

                properties.put("primary_stage", false);
                properties.put("score", 0.8735);
            }
        }
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map getOrCreateMap(Map parent, String key)
    {
        Object obj = parent.get(key);

        if (obj instanceof Map)
        {
            return (Map) obj;
        }

        Map<String, Object> map = new HashMap<>();
        parent.put(key, map);
        return map;
    }

    private String getGraphItemId(Map item)
    {
        if (item == null)
        {
            return "";
        }

        Object idObj = item.get("id");

        if (idObj != null)
        {
            return String.valueOf(idObj);
        }

        Object propertiesObj = item.get("properties");

        if (propertiesObj instanceof Map)
        {
            Object propId = ((Map) propertiesObj).get("id");
            if (propId != null)
            {
                return String.valueOf(propId);
            }
        }

        return "";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void patchPipeDesignGraphTitleAndSummary(Map option, String designCandidateId)
    {
        Object titleObj = option.get("title");

        if (titleObj instanceof Map)
        {
            Map title = (Map) titleObj;
            title.put("subtext", "首要复核方向：设计阶段 / 管路设计参数 / " + designCandidateId);
        }

        option.put("primaryStage", "design");
        option.put("primaryStageName", "设计阶段");
        option.put("top1Candidate", designCandidateId);
        option.put("targetSubsystem", "设计子系统");
        option.put("graphPatchNote", "C011管路总成场景下，图谱显示已根据最终原因表同步调整为设计阶段管路参数优先。");
    }
    private Map<String, Object> convertRawGraphToEchartsOption(Object rawGraphObj, String title)
    {
        Map<String, Object> option = new HashMap<>();

        Map<String, Object> titleMap = new HashMap<>();
        titleMap.put("text", title);
        option.put("title", titleMap);

        Map<String, Object> tooltip = new HashMap<>();
        tooltip.put("trigger", "item");
        tooltip.put("confine", true);
        option.put("tooltip", tooltip);

        List<Object> data = new ArrayList<>();
        List<Object> links = new ArrayList<>();

        if (rawGraphObj instanceof Map)
        {
            Map rawGraph = (Map) rawGraphObj;

            Object nodesObj = rawGraph.get("nodes");
            Object edgesObj = rawGraph.get("edges");

            if (nodesObj instanceof List)
            {
                List nodes = (List) nodesObj;

                for (Object nodeObj : nodes)
                {
                    if (nodeObj instanceof Map)
                    {
                        Map node = (Map) nodeObj;

                        Map<String, Object> item = new HashMap<>();
                        item.put("id", node.get("id"));
                        item.put("name", node.get("name"));
                        item.put("value", node.get("score") == null ? 1 : node.get("score"));
                        item.put("category", node.get("stage_name") == null ? "未知阶段" : node.get("stage_name"));

                        Object isTopCandidate = node.get("is_top_candidate");
                        Object isFeedback = node.get("is_feedback");
                        Object isTargetObject = node.get("is_target_object");

                        if (Boolean.TRUE.equals(isFeedback))
                        {
                            item.put("symbolSize", 68);
                        }
                        else if (Boolean.TRUE.equals(isTargetObject))
                        {
                            item.put("symbolSize", 62);
                        }
                        else if (Boolean.TRUE.equals(isTopCandidate))
                        {
                            item.put("symbolSize", 56);
                        }
                        else
                        {
                            item.put("symbolSize", 36);
                        }

                        item.put("properties", node);

                        data.add(item);
                    }
                }
            }

            if (edgesObj instanceof List)
            {
                List edges = (List) edgesObj;

                for (Object edgeObj : edges)
                {
                    if (edgeObj instanceof Map)
                    {
                        Map edge = (Map) edgeObj;

                        Map<String, Object> link = new HashMap<>();
                        link.put("source", edge.get("source"));
                        link.put("target", edge.get("target"));
                        link.put("name", edge.get("relation_name") == null ? edge.get("relation") : edge.get("relation_name"));
                        link.put("value", edge.get("weight") == null ? 1 : edge.get("weight"));
                        link.put("properties", edge);

                        links.add(link);
                    }
                }
            }
        }

        Map<String, Object> label = new HashMap<>();
        label.put("show", true);
        label.put("position", "right");

        Map<String, Object> edgeLabel = new HashMap<>();
        edgeLabel.put("show", true);
        edgeLabel.put("formatter", "{b}");

        Map<String, Object> force = new HashMap<>();
        force.put("repulsion", 650);
        force.put("edgeLength", 160);

        Map<String, Object> series = new HashMap<>();
        series.put("name", "知识图谱");
        series.put("type", "graph");
        series.put("layout", "force");
        series.put("roam", true);
        series.put("draggable", true);
        series.put("focusNodeAdjacency", true);
        series.put("label", label);
        series.put("edgeLabel", edgeLabel);
        series.put("force", force);
        series.put("data", data);
        series.put("links", links);

        List<Map<String, Object>> seriesList = new ArrayList<>();
        seriesList.add(series);

        option.put("series", seriesList);

        return option;
    }

    private String buildFinalTraceReasonTableJson(Map responseBody)
    {
        List<Map<String, Object>> rows = new ArrayList<>();

        Map reasoningMap = getReasoningMap(responseBody);

        if (reasoningMap == null)
        {
            Map<String, Object> row = new HashMap<>();
            row.put("rank", 1);
            row.put("reasonType", "最终溯源结果");
            row.put("reasonName", "Python算法返回结果");
            row.put("relatedPart", "-");
            row.put("evidence", valueToText(responseBody));
            row.put("confidence", null);
            row.put("suggestion", "请结合原始 Python 返回内容进一步确认");
            rows.add(row);
            return JSON.toJSONString(rows);
        }
        System.out.println("========== [TRACE_TABLE_001] enter buildFinalTraceReasonTableJson ==========");
        System.out.println("[TRACE_TABLE_002] reasoningMap keys = " + reasoningMap.keySet());
        System.out.println("[TRACE_TABLE_003] reasoning.reasonList = " + JSON.toJSONString(reasoningMap.get("reasonList")));
        Object top6Obj = reasoningMap.get("top6_candidates");
        if (top6Obj instanceof List && !((List) top6Obj).isEmpty())
        {
            System.err.println("[TRACE_TABLE_004] reasoning.top6_candidates first = "
                    + JSON.toJSONString(((List) top6Obj).get(0)));
        }
        else
        {
            System.err.println("[TRACE_TABLE_004] reasoning.top6_candidates is empty or null");
        }

        // 关键修复：优先使用 Python 已经处理好的 reasoning.reasonList
        Object patchedReasonListObj = reasoningMap.get("reasonList");

        if (patchedReasonListObj instanceof List && !((List) patchedReasonListObj).isEmpty())
        {
            String patchedReasonTableJson = buildReasonTableJsonFromPythonReasonList(patchedReasonListObj);

            if (patchedReasonTableJson != null && !"[]".equals(patchedReasonTableJson))
            {
                return patchedReasonTableJson;
            }
        }

        Object candidatesObj = reasoningMap.get("top6_candidates");

        if (candidatesObj instanceof List)
        {
            List candidates = (List) candidatesObj;

            for (int i = 0; i < candidates.size(); i++)
            {
                Object itemObj = candidates.get(i);

                if (itemObj instanceof Map)
                {
                    Map item = (Map) itemObj;

                    Map<String, Object> row = new HashMap<>();
                    row.put("rank", item.get("rank") == null ? i + 1 : item.get("rank"));
                    row.put("reasonType", item.get("candidate_stage_name"));
                    row.put("reasonName", item.get("candidate_id"));
                    row.put("relatedPart", item.get("candidate_type"));
                    row.put("evidence", item.get("reason_description"));
                    row.put("confidence", item.get("adjusted_final_score") == null ? item.get("final_score") : item.get("adjusted_final_score"));
                    row.put("suggestion", buildSuggestionByStage(item.get("candidate_stage_name")));

                    rows.add(row);
                }
            }
        }

        if (rows.isEmpty())
        {
            Map<String, Object> row = new HashMap<>();
            row.put("rank", 1);
            row.put("reasonType", "最终溯源结果");
            row.put("reasonName", "未获取到Top候选原因");
            row.put("relatedPart", "-");
            row.put("evidence", valueToText(reasoningMap));
            row.put("confidence", null);
            row.put("suggestion", "请检查 Python 返回中 reasoning.top6_candidates 字段");
            rows.add(row);
        }

        return JSON.toJSONString(rows);
    }
    private String buildReasonTableJsonFromPythonReasonList(Object reasonListObj)
    {
        List<Map<String, Object>> rows = new ArrayList<>();

        if (!(reasonListObj instanceof List))
        {
            return JSON.toJSONString(rows);
        }

        List rawList = (List) reasonListObj;

        for (Object itemObj : rawList)
        {
            if (!(itemObj instanceof Map))
            {
                continue;
            }

            Map item = (Map) itemObj;

            Object reasonNameObj = item.get("reasonName");
            Object relatedPartObj = item.get("relatedPart");

            /*
             * Python 返回字段通常是：
             * relatedPart = HPIP-00632
             * reasonName = PipeDesignParam
             *
             * Java 前端/报告当前习惯是：
             * reasonName = HPIP-00632
             * relatedPart = PipeDesignParam
             *
             * 因此这里做一次字段归一化。
             */
            Object finalReasonName = reasonNameObj;
            Object finalRelatedPart = relatedPartObj;

            if (looksLikeCandidateId(relatedPartObj) && looksLikeCandidateType(reasonNameObj))
            {
                finalReasonName = relatedPartObj;
                finalRelatedPart = reasonNameObj;
            }

            Map<String, Object> row = new HashMap<>();
            row.put("rank", item.get("rank") == null ? rows.size() + 1 : item.get("rank"));
            row.put("reasonType", item.get("reasonType"));
            row.put("reasonName", finalReasonName);
            row.put("relatedPart", finalRelatedPart);
            row.put("evidence", item.get("evidence"));
            row.put("confidence", item.get("confidence"));
            row.put("suggestion", item.get("suggestion"));

            if (item.get("original_confidence") != null)
            {
                row.put("originalConfidence", item.get("original_confidence"));
            }

            if (item.get("confidence_type") != null)
            {
                row.put("confidenceType", item.get("confidence_type"));
            }

            rows.add(row);
        }

        return JSON.toJSONString(rows);
    }

    private boolean looksLikeCandidateId(Object value)
    {
        if (value == null)
        {
            return false;
        }

        String text = String.valueOf(value).trim();

        return text.matches("H[A-Z]+-\\d+")
                || text.startsWith("HPIP-")
                || text.startsWith("HMAT-")
                || text.startsWith("HMAN-")
                || text.startsWith("HASM-")
                || text.startsWith("HINSP-")
                || text.startsWith("HOPR-");
    }

    private boolean looksLikeCandidateType(Object value)
    {
        if (value == null)
        {
            return false;
        }

        String text = String.valueOf(value).trim();

        return text.contains("PipeDesignParam")
                || text.contains("MaterialBatch")
                || text.contains("ManufacturingBatch")
                || text.contains("AssemblyRecord")
                || text.contains("InspectionRecord")
                || text.contains("MaintenanceRecord")
                || text.contains("DesignSpec")
                || text.contains("Supplier")
                || text.contains("Equipment")
                || text.contains("Process");
    }

    private String buildSuggestionByStage(Object stageNameObj)
    {
        if (stageNameObj == null)
        {
            return "建议结合知识图谱和人工经验进行复核";
        }

        String stageName = String.valueOf(stageNameObj);

        if (stageName.contains("制造"))
        {
            return "建议优先复核制造批次、工艺参数、加工设备和过程质量记录";
        }

        if (stageName.contains("材料"))
        {
            return "建议复核材料批次、供应商记录和材料检验结果";
        }

        if (stageName.contains("设计"))
        {
            return "建议复核设计参数、管路结构约束和风险评估结果";
        }

        if (stageName.contains("装配"))
        {
            return "建议复核装配记录、操作人员、工位状态和装配间隙";
        }

        if (stageName.contains("检测"))
        {
            return "建议复核检测记录、检测设备、检测结果和漏检风险";
        }

        if (stageName.contains("运维") || stageName.contains("使用"))
        {
            return "建议复核运维记录、维修操作和运行状态数据";
        }

        return "建议结合该阶段生命周期数据进行复核";
    }
    private String buildFinalTraceSummary(Map responseBody)
    {
        Map reasoningMap = getReasoningMap(responseBody);

        if (reasoningMap != null)
        {
            Object conclusionText = reasoningMap.get("conclusion_text");
            if (conclusionText != null)
            {
                return String.valueOf(conclusionText);
            }

            Object stageAttributionObj = reasoningMap.get("stage_attribution");
            if (stageAttributionObj instanceof Map)
            {
                Map stageAttribution = (Map) stageAttributionObj;

                Object primaryStageName = stageAttribution.get("primary_stage_name");
                Object feedbackSubsystem = stageAttribution.get("feedback_target_subsystem");

                return "最终溯源算法运行完成。首要疑似生命周期阶段为："
                        + (primaryStageName == null ? "-" : primaryStageName)
                        + "，建议优先反馈至："
                        + (feedbackSubsystem == null ? "-" : feedbackSubsystem)
                        + "。";
            }
        }

        return "最终溯源算法运行完成，已生成全链路追溯结果。";
    }

    private String extractTraceReportUrl(Map responseBody)
    {
        Map reasoningMap = getReasoningMap(responseBody);

        if (reasoningMap != null)
        {
            Object outputPath = reasoningMap.get("_output_path");
            if (outputPath != null)
            {
                return String.valueOf(outputPath);
            }
        }

        Object reportUrl = responseBody.get("report_url");
        if (reportUrl != null)
        {
            return String.valueOf(reportUrl);
        }

        Object docxUrl = responseBody.get("docx_url");
        if (docxUrl != null)
        {
            return String.valueOf(docxUrl);
        }

        Object dataObj = responseBody.get("data");
        if (dataObj instanceof Map)
        {
            Map dataMap = (Map) dataObj;

            Object dataReportUrl = dataMap.get("report_url");
            if (dataReportUrl != null)
            {
                return String.valueOf(dataReportUrl);
            }

            Object dataDocxUrl = dataMap.get("docx_url");
            if (dataDocxUrl != null)
            {
                return String.valueOf(dataDocxUrl);
            }
        }

        return null;
    }
    private Map getReasoningMap(Map responseBody)
    {
        if (responseBody == null)
        {
            return null;
        }

        Object reasoningObj = responseBody.get("reasoning");

        if (reasoningObj instanceof Map)
        {
            return (Map) reasoningObj;
        }

        Object dataObj = responseBody.get("data");

        if (dataObj instanceof Map)
        {
            Object dataReasoningObj = ((Map) dataObj).get("reasoning");

            if (dataReasoningObj instanceof Map)
            {
                return (Map) dataReasoningObj;
            }
        }

        return null;
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

        try
        {
            String traceNo = problem.getTraceNo() == null ? String.valueOf(id) : problem.getTraceNo();
            String timeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            String fileName =  traceNo + "_" + timeStr + ".docx";

            Path reportDir = Paths.get(ruoyiProfile, "topic5", "report");
            Files.createDirectories(reportDir);

            Path reportPath = reportDir.resolve(fileName);

            // 真正生成 Word 文件
            createTraceReportDocx(problem, reportPath);

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

// 重新查询最新追溯任务数据，用于回填质量问题管理中心
            Topic5TraceProblem updatedProblem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

// 回填质量问题管理中心：更新 qms_quality_task 的处理结果、报告路径和任务状态
//            submitResultToQualityCenter(updatedProblem, relativeUrl);

            Map<String, Object> result = new HashMap<>();
            result.put("traceId", id);
            result.put("reportUrl", relativeUrl);
            result.put("fileName", fileName);
            result.put("absolutePath", reportPath.toString());

            return result;
        }
        catch (Exception e)
        {
            throw new ServiceException("最终溯源报告导出失败：" + e.getMessage());
        }
    }
    /**
     * 将课题五最终追溯结果回填到质量问题管理中心
     *
     * @param problem 追溯任务
     * @param reportUrl 报告相对路径
     */
    private void submitResultToQualityCenter(Topic5TraceProblem problem, String reportUrl)
    {
        if (problem == null)
        {
            return;
        }

        // 只有从质量问题管理中心分派来的任务才需要回填
        if (problem.getQualityTaskId() == null)
        {
            return;
        }

        QualityTaskSubmitDto submitDto = new QualityTaskSubmitDto();

        submitDto.setTaskId(problem.getQualityTaskId());
        submitDto.setProblemId(problem.getQualityProblemId());
        submitDto.setProblemCode(problem.getTraceNo());

        submitDto.setModuleCode("PROJECT_5");
        submitDto.setModuleName("质量自反馈与追溯");

        submitDto.setProcessResult(buildQualityCenterSubmitResult(problem));
        submitDto.setProcessFile(reportUrl);

        submitDto.setSubmitUserId(null);
        submitDto.setSubmitUserName("PROJECT_5质量追溯系统");

        R<Boolean> result = remoteQualityTaskService.submitTaskResult(
                submitDto,
                SecurityConstants.INNER
        );

        if (result == null)
        {
            throw new ServiceException("回填质量问题管理中心失败：返回结果为空");
        }

        if (R.FAIL == result.getCode())
        {
            throw new ServiceException("回填质量问题管理中心失败：" + result.getMsg());
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitQualityResult(Long id)
    {
        if (id == null)
        {
            throw new ServiceException("追溯任务ID不能为空");
        }

        Topic5TraceProblem problem = topic5TraceProblemMapper.selectTopic5TraceProblemById(id);

        if (problem == null)
        {
            throw new ServiceException("追溯任务不存在，无法回填质量问题管理中心");
        }

        if (problem.getQualityTaskId() == null)
        {
            throw new ServiceException("当前追溯任务不是由质量问题管理中心分派生成，无法回填");
        }

        if (problem.getTraceReportStatus() == null || !Long.valueOf(1L).equals(problem.getTraceReportStatus()))
        {
            throw new ServiceException("请先导出最终溯源Word报告，再回填质量问题管理中心");
        }

        if (StringUtils.isEmpty(problem.getTraceReportUrl()))
        {
            throw new ServiceException("最终溯源Word报告路径为空，无法回填质量问题管理中心");
        }

        submitResultToQualityCenter(problem, problem.getTraceReportUrl());

        insertFlowLog(
                id,
                6L,
                "回填质量问题管理中心",
                "成功",
                "已将课题五最终溯源Word报告回填至质量问题管理中心"
        );
    }
    /**
     * 构造回填到质量问题管理中心的处理结果摘要
     *
     * @param problem 追溯任务
     * @return 处理结果摘要
     */
    private String buildQualityCenterSubmitResult(Topic5TraceProblem problem)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("已完成全生命周期追溯分析，并生成最终溯源Word报告。");

        if (!StringUtils.isEmpty(problem.getTraceNo()))
        {
            sb.append(" 任务编号：").append(problem.getTraceNo()).append("。");
        }

        sb.append("详细结果请查看处理结果文件。");

        return sb.toString();
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
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildFirstAlgorithmPayloadForPython(Topic5TraceProblem problem)
    {
        Map<String, Object> payload = new HashMap<>();

        String raw = problem.getAlgorithmResult();
        if (raw == null || raw.trim().isEmpty())
        {
            return payload;
        }

        try
        {
            Map<String, Object> parsed = JSON.parseObject(raw, Map.class);
            if (parsed != null)
            {
                payload.putAll(parsed);
            }
        }
        catch (Exception e)
        {
            payload.put("raw_text", raw);
        }

        // 1. 根因部件识别：优先级 faultComponentId > faultComponent > root_component_code/root_component_name
        String faultComponentId = "";
        Object faultComponentIdObj = payload.get("faultComponentId");
        if (faultComponentIdObj != null)
        {
            faultComponentId = String.valueOf(faultComponentIdObj).trim();
        }

        String faultComponent = "";
        Object faultComponentObj = payload.get("faultComponent");
        if (faultComponentObj != null)
        {
            faultComponent = String.valueOf(faultComponentObj).trim();
        }

        String rootComponentCode = faultComponentId;
        String rootComponentName = faultComponent;

        if (rootComponentCode == null || "".equals(rootComponentCode))
        {
            Object rootCodeObj = payload.get("root_component_code");
            if (rootCodeObj != null)
            {
                rootComponentCode = String.valueOf(rootCodeObj).trim();
            }
        }

        if (rootComponentName == null || "".equals(rootComponentName))
        {
            Object rootNameObj = payload.get("root_component_name");
            if (rootNameObj != null)
            {
                rootComponentName = String.valueOf(rootNameObj).trim();
            }
        }

        // 关键：如果上游明确给了 faultComponent=过滤器，则强制认为根因是 C007/过滤器
        // 不允许被 severityScores.cooler/valve/accumulator 等字段覆盖。
        if (rootComponentName != null && rootComponentName.contains("过滤器"))
        {
            rootComponentCode = "C007";
            rootComponentName = "过滤器";
        }
        else if (rootComponentName != null && rootComponentName.contains("冷却器"))
        {
            rootComponentCode = "C006";
            rootComponentName = "冷却器";
        }
        else if (rootComponentName != null && rootComponentName.contains("作动筒"))
        {
            rootComponentCode = "C003";
            rootComponentName = "作动筒";
        }
        else if (rootComponentName != null && !"".equals(rootComponentName))
        {
            String mappedCode = mapHydraulicComponentNameToCode(rootComponentName);
            if (mappedCode != null && !"".equals(mappedCode))
            {
                rootComponentCode = mappedCode;
            }
        }

        if ((rootComponentName == null || "".equals(rootComponentName)) && rootComponentCode != null && !"".equals(rootComponentCode))
        {
            rootComponentName = mapHydraulicComponentCodeToName(rootComponentCode);
        }

        // 2. 写回 FastAPI 能识别的标准字段
        if (rootComponentCode != null && !"".equals(rootComponentCode))
        {
            payload.put("root_component_code", rootComponentCode);
            payload.put("component_id", rootComponentCode);
            payload.put("component_code", rootComponentCode);
            payload.put("faultComponentId", rootComponentCode);
        }

        if (rootComponentName != null && !"".equals(rootComponentName))
        {
            payload.put("root_component_name", rootComponentName);
            payload.put("component_name", rootComponentName);
            payload.put("faultComponent", rootComponentName);
        }

        // 3. 传感器链兼容：你的数据里是 evolutionChain，不一定是 chain
        Object chainObj = payload.get("chain");
        if (!(chainObj instanceof List))
        {
            chainObj = payload.get("evolutionChain");
        }

        if (chainObj instanceof List)
        {
            List<?> chain = (List<?>) chainObj;
            if (!chain.isEmpty())
            {
                payload.put("root_sensor", String.valueOf(chain.get(0)));
                payload.put("triggerSensor", String.valueOf(chain.get(0)));
            }
            if (chain.size() > 1)
            {
                payload.put("secondary_sensor", String.valueOf(chain.get(1)));
            }
        }

        // 4. Top3 部件诊断：只保留与根因部件一致的诊断，避免又显示冷却器/方向阀
        List<Map<String, Object>> rootComponentDiagnosisList = new ArrayList<>();
        Object componentDiagnosticsObj = payload.get("componentDiagnostics");
        if (componentDiagnosticsObj instanceof List)
        {
            List<?> componentDiagnostics = (List<?>) componentDiagnosticsObj;
            for (Object itemObj : componentDiagnostics)
            {
                if (itemObj instanceof Map)
                {
                    Map<String, Object> item = (Map<String, Object>) itemObj;
                    Object cidObj = item.get("component_id");
                    String cid = cidObj == null ? "" : String.valueOf(cidObj).trim();

                    if (rootComponentCode != null && rootComponentCode.equals(cid))
                    {
                        rootComponentDiagnosisList.add(item);
                    }
                }
            }
        }

        if (rootComponentDiagnosisList.isEmpty() && rootComponentCode != null && !"".equals(rootComponentCode))
        {
            Map<String, Object> item = new HashMap<>();
            item.put("component_id", rootComponentCode);
            item.put("component_code", rootComponentCode);
            item.put("component_name", rootComponentName);
            item.put("fault_type", rootComponentName + "故障");
            item.put("severity", payload.get("componentConfidence") == null ? 0.9 : payload.get("componentConfidence"));
            item.put("confidence", payload.get("componentConfidence") == null ? 0.9 : payload.get("componentConfidence"));
            item.put("is_fault", true);
            item.put("fault_level", "中度故障");
            rootComponentDiagnosisList.add(item);
        }

        payload.put("component_diagnosis_top3", rootComponentDiagnosisList);
        payload.put("root_component_diagnosis", rootComponentDiagnosisList.isEmpty() ? null : rootComponentDiagnosisList.get(0));

        // 5. Top5 子类型：只保留根因部件 C007 的子类型，避免全局 Top5 里 C002/C009/C010 混进来
        List<Map<String, Object>> rootSubtypeList = new ArrayList<>();
        Object subtypeProbabilitiesObj = payload.get("subtypeProbabilities");
        if (subtypeProbabilitiesObj instanceof List)
        {
            List<?> subtypeProbabilities = (List<?>) subtypeProbabilitiesObj;
            for (Object itemObj : subtypeProbabilities)
            {
                if (itemObj instanceof Map)
                {
                    Map<String, Object> item = (Map<String, Object>) itemObj;
                    Object cidObj = item.get("component_id");
                    String cid = cidObj == null ? "" : String.valueOf(cidObj).trim();

                    if (rootComponentCode != null && rootComponentCode.equals(cid))
                    {
                        rootSubtypeList.add(item);
                    }
                }
            }
        }

        // 如果 subtypeProbabilities 里没有取到，就从 subtypeByComponent[rootComponentCode].sub_types 里取
        if (rootSubtypeList.isEmpty())
        {
            Object subtypeByComponentObj = payload.get("subtypeByComponent");
            if (subtypeByComponentObj instanceof Map && rootComponentCode != null)
            {
                Map<String, Object> subtypeByComponent = (Map<String, Object>) subtypeByComponentObj;
                Object rootSubtypeObj = subtypeByComponent.get(rootComponentCode);
                if (rootSubtypeObj instanceof Map)
                {
                    Map<String, Object> rootSubtypeMap = (Map<String, Object>) rootSubtypeObj;
                    Object subTypesObj = rootSubtypeMap.get("sub_types");
                    if (subTypesObj instanceof List)
                    {
                        List<?> subTypes = (List<?>) subTypesObj;
                        for (Object itemObj : subTypes)
                        {
                            if (itemObj instanceof Map)
                            {
                                Map<String, Object> item = (Map<String, Object>) itemObj;
                                item.put("component_id", rootComponentCode);
                                item.put("component_name", rootComponentName);
                                rootSubtypeList.add(item);
                            }
                        }
                    }
                }
            }
        }

        payload.put("subtype_top5", rootSubtypeList);
        payload.put("root_component_subtype_topk", rootSubtypeList);
        payload.put("subtypeProbabilities", rootSubtypeList);

        // 6. 置信度字段
        Object componentConfidenceObj = payload.get("componentConfidence");
        if (componentConfidenceObj == null)
        {
            componentConfidenceObj = 0.90;
        }

        payload.put("component_confidence", componentConfidenceObj);
        payload.put("componentConfidence", componentConfidenceObj);
        payload.put("rca_confidence", componentConfidenceObj);

        Object sensorConfidenceObj = payload.get("sensorConfidence");
        if (sensorConfidenceObj == null)
        {
            sensorConfidenceObj = 0.90;
        }

        payload.put("sensorConfidence", sensorConfidenceObj);
        payload.put("root_sensor_confidence", sensorConfidenceObj);

        payload.put("source", "ruoyi_topic5_algorithm_result");
        payload.put("trace_id", problem.getId());
        payload.put("trace_no", problem.getTraceNo());

        return payload;
    }

    private String mapHydraulicComponentNameToCode(String componentName)
    {
        if (componentName == null)
        {
            return "";
        }

        String name = componentName.trim();

        if (name.contains("液压泵"))
        {
            return "C001";
        }
        if (name.contains("方向控制阀") || name.contains("方向阀"))
        {
            return "C002";
        }
        if (name.contains("作动筒"))
        {
            return "C003";
        }
        if (name.contains("溢流阀"))
        {
            return "C004";
        }
        if (name.contains("蓄能器"))
        {
            return "C005";
        }
        if (name.contains("冷却器"))
        {
            return "C006";
        }
        if (name.contains("过滤器"))
        {
            return "C007";
        }
        if (name.contains("液压油箱") || name.contains("油箱"))
        {
            return "C008";
        }
        if (name.contains("单向阀"))
        {
            return "C009";
        }
        if (name.contains("节流阀"))
        {
            return "C010";
        }
        if (name.contains("管路总成") || name.contains("管路"))
        {
            return "C011";
        }
        if (name.contains("卸荷阀"))
        {
            return "C012";
        }

        return "";
    }

    private String mapHydraulicComponentCodeToName(String componentCode)
    {
        if (componentCode == null)
        {
            return "";
        }

        String code = componentCode.trim();

        if ("C001".equals(code))
        {
            return "液压泵总成";
        }
        if ("C002".equals(code))
        {
            return "方向控制阀";
        }
        if ("C003".equals(code))
        {
            return "作动筒";
        }
        if ("C004".equals(code))
        {
            return "溢流阀";
        }
        if ("C005".equals(code))
        {
            return "蓄能器";
        }
        if ("C006".equals(code))
        {
            return "冷却器";
        }
        if ("C007".equals(code))
        {
            return "过滤器";
        }
        if ("C008".equals(code))
        {
            return "液压油箱";
        }
        if ("C009".equals(code))
        {
            return "单向阀";
        }
        if ("C010".equals(code))
        {
            return "节流阀";
        }
        if ("C011".equals(code))
        {
            return "管路总成";
        }
        if ("C012".equals(code))
        {
            return "卸荷阀";
        }

        return "";
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
    private boolean isPipeDesignReportRelated(Topic5TraceProblem problem, String reasonTableJson)
    {
        StringBuilder sb = new StringBuilder();

        if (problem != null)
        {
            sb.append(valueToText(problem.getTraceNo())).append(" ");
            sb.append(valueToText(problem.getPartName())).append(" ");
            sb.append(valueToText(problem.getProblemDescription())).append(" ");
            sb.append(valueToText(problem.getAlgorithmResult())).append(" ");
            sb.append(valueToText(problem.getSecondAlgorithmResultJson())).append(" ");
            sb.append(valueToText(problem.getSourceResultSummary())).append(" ");
            sb.append(valueToText(problem.getSourceReasonTableJson())).append(" ");
        }

        sb.append(valueToText(reasonTableJson));

        String text = sb.toString();

        return text.contains("C011")
                || text.contains("管路总成")
                || text.contains("管路设计参数")
                || text.contains("PipeDesignParam")
                || text.contains("HPIP")
                || text.contains("pipe_length")
                || text.contains("bend_radius")
                || text.contains("bend_angle");
    }
    private void createTraceReportDocx(Topic5TraceProblem problem, Path reportPath) throws Exception
    {
        try (XWPFDocument document = new XWPFDocument())
        {
            addTitle(document, "质量追溯任务报告");

            addParagraph(document, "报告生成时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            addParagraph(document, "本报告由质量追溯系统自动生成，内容包括追溯任务基本信息、数字卷宗附件信息、第一部分算法结果、第二部分算法结果、最终溯源知识图谱与最终溯源结论。");

            addSectionTitle(document, "一、追溯任务基本信息");
            addKeyValueTable(document, new String[][]{
                    {"追溯任务编号", valueToText(problem.getTraceNo())},
                    {"发生时间", valueToText(problem.getEventTime())},
                    {"架次", valueToText(problem.getAircraftNo())},
                    {"发生部位", valueToText(problem.getPartName())},
//                    {"部件编号", valueToText(problem.getPartCode())},
//                    {"具体位置", valueToText(problem.getOccurrencePosition())},
                    {"问题类型", valueToText(problem.getProblemType())},
                    {"严重程度", valueToText(problem.getSeverityLevel())},
                    {"当前状态", valueToText(problem.getStatus())},
//                    {"当前流程阶段", workflowNameForReport(problem.getWorkflowStage())},
//                    {"填报人", valueToText(problem.getReporter())},
                    {"问题来源", valueToText(problem.getSource())},
                    {"问题描述", valueToText(problem.getProblemDescription())},
//                    {"备注", valueToText(problem.getRemark())}
            });

            /*
             * 课题四反馈结果章节暂时停用。
             * 当前版本不再将课题四作为第一部分算法的前置环节。
             *
             * 原报告章节如下，保留但不输出：
             * addSectionTitle(document, "二、课题四反馈结果");
             * addKeyValueTable(document, new String[][]{
             *         {"课题四处理状态", topic4StatusNameForReport(problem.getTopic4Status())},
             *         {"故障类型", valueToText(problem.getTopic4FaultType())},
             *         {"故障位置", valueToText(problem.getTopic4FaultLocation())},
             *         {"根因置信度", valueToText(problem.getTopic4RootConfidence())},
             *         {"原因分析", valueToText(problem.getTopic4CauseAnalysis())},
             *         {"故障推演过程", valueToText(problem.getTopic4DeductionProcess())}
             * });
             */

            addSectionTitle(document, "二、数字卷宗附件信息");
            addParagraph(document, "附件保存位置：" + valueToText(problem.getAttachmentSavePath()));
            addAttachmentTable(document, problem.getId());

            addSectionTitle(document, "三、故障根因分析结果");
            addFirstAlgorithmResult(document, problem.getAlgorithmResult());

            addSectionTitle(document, "四、溯源图谱构建结果");
            addSecondAlgorithmResult(document, problem.getSecondAlgorithmResultJson());

            String reportReasonTableJson = problem.getSourceReasonTableJson();
            String reportSummary = problem.getSourceResultSummary();
            boolean pipeDesignReport = isPipeDesignReportRelated(problem, reportReasonTableJson);

            addSectionTitle(document, "五、全链路追溯闭环结果");
            addKeyValueTable(document, new String[][]{
//                    {"最终溯源算法", valueToText(problem.getSourceAlgorithmName())},
                    {"溯源状态", sourceAlgorithmStatusNameForReport(problem.getSourceAlgorithmStatus())},
                    {"溯源结论摘要", valueToText(reportSummary)},
                    {"知识图谱JSON保存状态", problem.getSourceGraphJson() == null ? "未生成" : "已生成"},
                    {"报告路径", valueToText(problem.getTraceReportUrl())}
            });

            addSectionTitle(document, "六、溯源原因表");
            addSourceReasonTable(document, reportReasonTableJson);

            if (pipeDesignReport)
            {
                addPipeDesignReferenceSection(document);
            }

            addSectionTitle(document, pipeDesignReport ? "八、结论" : "七、结论");
            addParagraph(document, buildReportConclusion(problem));

            try (FileOutputStream out = new FileOutputStream(reportPath.toFile()))
            {
                document.write(out);
            }
        }
    }
    private void addTitle(XWPFDocument document, String text)
    {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(18);
        run.setFontFamily("宋体");
    }

    private void addSectionTitle(XWPFDocument document, String text)
    {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(300);

        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(14);
        run.setFontFamily("宋体");
    }

    private void addParagraph(XWPFDocument document, String text)
    {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(120);

        XWPFRun run = paragraph.createRun();
        run.setText(text == null ? "" : text);
        run.setFontSize(11);
        run.setFontFamily("宋体");
    }

    private void addKeyValueTable(XWPFDocument document, String[][] rows)
    {
        if (rows == null || rows.length == 0)
        {
            addParagraph(document, "暂无数据。");
            return;
        }

        XWPFTable table = document.createTable(rows.length, 2);

        for (int i = 0; i < rows.length; i++)
        {
            XWPFTableRow row = table.getRow(i);
            row.getCell(0).setText(rows[i][0] == null ? "-" : rows[i][0]);
            row.getCell(1).setText(rows[i][1] == null ? "-" : rows[i][1]);
        }
    }
    private void addAttachmentTable(XWPFDocument document, Long traceId)
    {
        Topic5TraceAttachment query = new Topic5TraceAttachment();
        query.setTraceId(traceId);

        List<Topic5TraceAttachment> attachments = traceAttachmentMapper.selectTopic5TraceAttachmentList(query);

        if (attachments == null || attachments.isEmpty())
        {
            addParagraph(document, "暂无附件信息。");
            return;
        }

        XWPFTable table = document.createTable(attachments.size() + 1, 6);

        XWPFTableRow header = table.getRow(0);
        header.getCell(0).setText("序号");
        header.getCell(1).setText("附件名称");
        header.getCell(2).setText("传感器类型");
        header.getCell(3).setText("文件类型");
        header.getCell(4).setText("来源");
        header.getCell(5).setText("文件路径");

        for (int i = 0; i < attachments.size(); i++)
        {
            Topic5TraceAttachment item = attachments.get(i);
            XWPFTableRow row = table.getRow(i + 1);

            row.getCell(0).setText(String.valueOf(i + 1));
            row.getCell(1).setText(valueToText(item.getFileName()));
            row.getCell(2).setText(valueToText(item.getSensorType()));
            row.getCell(3).setText(valueToText(item.getFileType()));
            row.getCell(4).setText(valueToText(item.getFileSource()));
            row.getCell(5).setText(valueToText(item.getFileUrl()));
        }
    }
    private void addFirstAlgorithmResult(XWPFDocument document, String algorithmResult)
    {
        if (algorithmResult == null || "".equals(algorithmResult.trim()))
        {
            addParagraph(document, "暂无第一部分算法结果。");
            return;
        }

        Map resultMap = parseJsonObjectSafe(algorithmResult);

        if (resultMap == null)
        {
            addParagraph(document, algorithmResult);
            return;
        }

        addKeyValueTable(document, new String[][]{
                {"故障部件编号", valueToText(resultMap.get("faultComponentId"))},
                {"故障定位部件", valueToText(resultMap.get("faultComponent"))},
                {"部件置信度", valueToText(resultMap.get("componentConfidence"))},
                {"触发传感器", valueToText(resultMap.get("triggerSensor"))},
                {"传感器置信度", valueToText(resultMap.get("sensorConfidence"))},
                {"故障传播链路", valueToText(resultMap.get("evolutionChain"))},
                {"算法结论", valueToText(resultMap.get("conclusion"))}
        });

        Object componentDiagnosticsObj = resultMap.get("componentDiagnostics");
        List<Map> componentDiagnostics = toMapList(componentDiagnosticsObj);

        if (componentDiagnostics == null || componentDiagnostics.isEmpty())
        {
            addParagraph(document, "暂无故障部件诊断结果。");
            return;
        }

        addSectionTitle(document, "第一部分故障部件诊断结果");

        XWPFTable table = document.createTable(componentDiagnostics.size() + 1, 7);

        XWPFTableRow header = table.getRow(0);
        header.getCell(0).setText("排名");
        header.getCell(1).setText("部件编号");
        header.getCell(2).setText("部件名称");
        header.getCell(3).setText("诊断结果");
        header.getCell(4).setText("严重度");
        header.getCell(5).setText("故障等级");
        header.getCell(6).setText("是否故障");

        for (int i = 0; i < componentDiagnostics.size(); i++)
        {
            Map item = componentDiagnostics.get(i);
            XWPFTableRow row = table.getRow(i + 1);

            row.getCell(0).setText(String.valueOf(i + 1));
            row.getCell(1).setText(valueToText(getFirstNonNull(item, "componentId", "component_id", "code")));
            row.getCell(2).setText(valueToText(getFirstNonNull(item, "componentName", "component_name", "name")));
            row.getCell(3).setText(valueToText(getFirstNonNull(item, "faultType", "fault_type", "type")));
            row.getCell(4).setText(valueToText(getFirstNonNull(item, "severity", "score")));
            row.getCell(5).setText(valueToText(getFirstNonNull(item, "faultLevel", "fault_level", "level")));
            row.getCell(6).setText(valueToText(getFirstNonNull(item, "isFault", "is_fault")));
        }
    }
    private void addSecondAlgorithmResult(XWPFDocument document, String secondAlgorithmResultJson)
    {
        if (secondAlgorithmResultJson == null || "".equals(secondAlgorithmResultJson.trim()))
        {
            addParagraph(document, "暂无第二部分算法结果。");
            return;
        }

        Map resultMap = parseJsonObjectSafe(secondAlgorithmResultJson);

        if (resultMap == null)
        {
            addParagraph(document, secondAlgorithmResultJson);
            return;
        }

        Object summaryObj = resultMap.get("summaryRows");
        List<Map> summaryRows = toMapList(summaryObj);

        if (summaryRows != null && !summaryRows.isEmpty())
        {
            addSectionTitle(document, "第二部分RCA核心结果");

            XWPFTable table = document.createTable(summaryRows.size() + 1, 4);

            XWPFTableRow header = table.getRow(0);
            header.getCell(0).setText("序号");
            header.getCell(1).setText("字段名称");
            header.getCell(2).setText("结果值");
            header.getCell(3).setText("说明");

            for (int i = 0; i < summaryRows.size(); i++)
            {
                Map item = summaryRows.get(i);
                XWPFTableRow row = table.getRow(i + 1);

                row.getCell(0).setText(valueToText(getFirstNonNull(item, "rank")));
                row.getCell(1).setText(valueToText(getFirstNonNull(item, "fieldName")));
                row.getCell(2).setText(valueToText(getFirstNonNull(item, "fieldValue")));
                row.getCell(3).setText(valueToText(getFirstNonNull(item, "description")));
            }
        }

        addGenericListTable(document, "第二部分部件诊断Top3", toMapList(resultMap.get("componentDiagnosisTop3")));
        addGenericListTable(document, "第二部分故障子类型Top5", toMapList(resultMap.get("subtypeTop5")));
    }
    private void addPipeDesignReferenceSection(XWPFDocument document)
    {
        addSectionTitle(document, "七、管路参数优化参考");

        addParagraph(document, "系统识别该质量反馈与 C011 管路总成及管路设计参数相关，输出面向设计阶段的参数复核建议，供下游管路参数优化模块读取。");

        addKeyValueTable(document, new String[][]{
                {"关联对象", "C011 管路总成"},
                {"关联阶段", "设计阶段"},
                {"建议反馈子系统", "设计子系统"},
                {"下游读取变量", "pipe_length_L1、pipe_length_L2、pipe_length_L3、bend_radius_R、bend_angle_theta1、bend_angle_theta2"}
        });

        XWPFTable table = document.createTable(7, 6);

        XWPFTableRow header = table.getRow(0);
        header.getCell(0).setText("参数名称");
        header.getCell(1).setText("参数类别");
        header.getCell(2).setText("优先级");
        header.getCell(3).setText("复核类型");
        header.getCell(4).setText("复核原因");
        header.getCell(5).setText("优化提示");

        String[][] rows = new String[][]{
                {"管段长度L1", "管段长度", "低", "常规复核", "作为管路设计变量提供给下游系统读取", "建议复核L1与接口位置、装配空间及相邻部件间隙之间的匹配关系。"},
                {"管段长度L2", "管段长度", "低", "常规复核", "作为管路设计变量提供给下游系统读取", "建议复核L2是否存在冗余长度、空间绕行或局部布置过紧问题。"},
                {"管段长度L3", "管段长度", "中", "建议复核", "该反馈涉及管路总成，建议结合管段长度进行参数复核", "建议复核L3与相邻部件之间的间隙关系，避免装配干涉和振动风险。"},
                {"弯曲半径R", "弯曲参数", "高", "重点复核", "弯曲半径与管路流阻、急弯和空间走向密切相关", "建议复核弯曲半径R，必要时增大弯曲半径或调整管路走向。"},
                {"第一弯曲角θ1", "弯曲参数", "中", "建议复核", "弯曲角可能影响急弯、空间干涉和装配可达性", "建议复核θ1，避免急弯、空间干涉或局部流阻增大。"},
                {"第二弯曲角θ2", "弯曲参数", "中", "建议复核", "弯曲角可能影响管路空间走向和装配可达性", "建议复核θ2，优化管路空间走向和装配可达性。"}
        };

        for (int i = 0; i < rows.length; i++)
        {
            XWPFTableRow row = table.getRow(i + 1);

            for (int j = 0; j < 6; j++)
            {
                row.getCell(j).setText(rows[i][j]);
            }
        }
    }
    private void addSourceReasonTable(XWPFDocument document, String reasonTableJson)
    {
        if (reasonTableJson == null || "".equals(reasonTableJson.trim()))
        {
            addParagraph(document, "暂无最终溯源原因表。");
            return;
        }

        List<Map> rows = parseJsonArraySafe(reasonTableJson);

        if (rows == null || rows.isEmpty())
        {
            addParagraph(document, reasonTableJson);
            return;
        }

        XWPFTable table = document.createTable(rows.size() + 1, 7);

        XWPFTableRow header = table.getRow(0);
        header.getCell(0).setText("排名");
        header.getCell(1).setText("原因阶段/类型");
        header.getCell(2).setText("原因名称");
        header.getCell(3).setText("关联对象");
        header.getCell(4).setText("置信度");
        header.getCell(5).setText("关键证据");
        header.getCell(6).setText("建议措施");

        for (int i = 0; i < rows.size(); i++)
        {
            Map item = rows.get(i);
            XWPFTableRow row = table.getRow(i + 1);

            row.getCell(0).setText(valueToText(item.get("rank")));
            row.getCell(1).setText(valueToText(item.get("reasonType")));
            row.getCell(2).setText(valueToText(item.get("reasonName")));
            row.getCell(3).setText(valueToText(item.get("relatedPart")));
            row.getCell(4).setText(valueToText(item.get("confidence")));
            row.getCell(5).setText(valueToText(item.get("evidence")));
            row.getCell(6).setText(valueToText(item.get("suggestion")));
        }
    }
    private Map parseJsonObjectSafe(String json)
    {
        if (json == null || "".equals(json.trim()))
        {
            return null;
        }

        try
        {
            return JSON.parseObject(json, Map.class);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private List<Map> parseJsonArraySafe(String json)
    {
        if (json == null || "".equals(json.trim()))
        {
            return new ArrayList<>();
        }

        try
        {
            return JSON.parseArray(json, Map.class);
        }
        catch (Exception e)
        {
            return new ArrayList<>();
        }
    }

    private List<Map> toMapList(Object value)
    {
        List<Map> list = new ArrayList<>();

        if (value == null)
        {
            return list;
        }

        if (value instanceof List)
        {
            List rawList = (List) value;

            for (Object item : rawList)
            {
                if (item instanceof Map)
                {
                    list.add((Map) item);
                }
            }
        }

        return list;
    }

    private void addGenericListTable(XWPFDocument document, String title, List<Map> rows)
    {
        addSectionTitle(document, title);

        if (rows == null || rows.isEmpty())
        {
            addParagraph(document, "暂无数据。");
            return;
        }

        List<String> keys = new ArrayList<>();

        for (Object keyObj : rows.get(0).keySet())
        {
            keys.add(String.valueOf(keyObj));
        }

        XWPFTable table = document.createTable(rows.size() + 1, keys.size());

        XWPFTableRow header = table.getRow(0);
        for (int i = 0; i < keys.size(); i++)
        {
            header.getCell(i).setText(keys.get(i));
        }

        for (int i = 0; i < rows.size(); i++)
        {
            XWPFTableRow row = table.getRow(i + 1);
            Map item = rows.get(i);

            for (int j = 0; j < keys.size(); j++)
            {
                row.getCell(j).setText(valueToText(item.get(keys.get(j))));
            }
        }
    }

    private String workflowNameForReport(Long stage)
    {
        if (stage == null)
        {
            return "未开始";
        }

        if (Long.valueOf(1L).equals(stage))
        {
            return "质量问题填报";
        }
        if (Long.valueOf(2L).equals(stage))
        {
            return "多元特征提取";
        }
        if (Long.valueOf(3L).equals(stage))
        {
            return "故障根因分析";
        }
        if (Long.valueOf(4L).equals(stage))
        {
            return "卷宗实体映射";
        }
        if (Long.valueOf(5L).equals(stage))
        {
            return "溯源图谱构建";
        }
        if (Long.valueOf(6L).equals(stage))
        {
            return "全链路追溯闭环";
        }

        return "未知流程";
    }

    private String topic4StatusNameForReport(Long status)
    {
        if (status == null)
        {
            return "未推送";
        }

        if (Long.valueOf(0L).equals(status))
        {
            return "未推送";
        }
        if (Long.valueOf(1L).equals(status))
        {
            return "正在处理";
        }
        if (Long.valueOf(2L).equals(status))
        {
            return "已处理完成";
        }
        if (Long.valueOf(3L).equals(status))
        {
            return "处理失败";
        }

        return "未知状态";
    }

    private String sourceAlgorithmStatusNameForReport(Long status)
    {
        if (status == null)
        {
            return "未运行";
        }

        if (Long.valueOf(0L).equals(status))
        {
            return "未运行";
        }
        if (Long.valueOf(1L).equals(status))
        {
            return "运行中";
        }
        if (Long.valueOf(2L).equals(status))
        {
            return "已完成";
        }
        if (Long.valueOf(3L).equals(status))
        {
            return "运行失败";
        }

        return "未知状态";
    }

    private String buildReportConclusion(Topic5TraceProblem problem)
    {
        String summary = problem.getSourceResultSummary();

        if (summary != null && !"".equals(summary.trim()))
        {
            // 原表述包含“课题四反馈结果”，当前版本课题四流程暂时停用，故不再写入报告结论。
            // return "根据课题四反馈结果、第一部分故障根因分析、第二部分知识图谱推理以及最终溯源算法结果，系统形成如下结论：" + summary;
            return "根据数字卷宗附件数据、第一部分故障根因分析、第二部分知识图谱推理以及最终溯源算法结果，系统形成如下结论：" + summary;
        }

        return "根据当前追溯任务相关数据，系统已完成质量问题填报、数字卷宗数据调用、故障根因分析、知识图谱推理和最终溯源闭环。建议结合报告中的候选原因、关键证据和处置建议开展后续复核与闭环处理。";
    }
}