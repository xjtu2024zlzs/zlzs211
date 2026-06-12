package com.ruoyi.project3.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.project3.client.FrameBeamCrackAlgorithmClient;
import com.ruoyi.project3.config.FrameBeamCrackProperties;
import com.ruoyi.project3.domain.PageRows;
import com.ruoyi.project3.domain.algorithm.AlgTaskResult;
import com.ruoyi.project3.domain.framebeam.FrameBeamHistoryVo;
import com.ruoyi.project3.domain.framebeam.FrameBeamIdentifyParams;
import com.ruoyi.project3.domain.framebeam.FrameBeamIdentifyRequest;
import com.ruoyi.project3.domain.framebeam.FrameBeamIdentifyResultVo;
import com.ruoyi.project3.domain.framebeam.FrameBeamMetricsVo;
import com.ruoyi.project3.domain.framebeam.FrameBeamObjectInfo;
import com.ruoyi.project3.domain.framebeam.FrameBeamProbabilityVo;
import com.ruoyi.project3.domain.framebeam.FrameBeamStftVo;
import com.ruoyi.project3.domain.framebeam.FrameBeamTaskVo;
import com.ruoyi.project3.domain.framebeam.FrameBeamWaveformVo;
import com.ruoyi.project3.mapper.AlgTaskMapper;
import com.ruoyi.project3.service.FrameBeamCrackIdentifyService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
public class FrameBeamCrackIdentifyServiceImpl implements FrameBeamCrackIdentifyService
{
    private static final String TASK_TYPE = "FRAME_BEAM_CRACK_IDENTIFY";
    private static final String TASK_NAME = "飞机框梁裂纹早期故障识别";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String TASK_TIME_PATTERN = "yyyyMMddHHmmssSSS";
    private static final ConcurrentHashMap<String, Object> IDEMPOTENCY_LOCKS = new ConcurrentHashMap<>();

    @Resource
    private AlgTaskMapper algTaskMapper;

    @Resource
    private FrameBeamCrackAlgorithmClient algorithmClient;

    @Resource
    private FrameBeamCrackProperties properties;

    private final TransactionTemplate tx;

    @Value("${quality.fault-identify.source-root:F:/TotalData}")
    private String sourceRoot;

    public FrameBeamCrackIdentifyServiceImpl(PlatformTransactionManager transactionManager)
    {
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Override
    public FrameBeamIdentifyResultVo importAndStart(MultipartFile file, String objectInfoJson, String paramsJson, String requestId)
    {
        validateUploadFile(file);
        FrameBeamIdentifyRequest request = parseMultipartRequest(objectInfoJson, paramsJson, requestId);
        SavedFile saved = null;
        try
        {
            saved = saveUploadFile(file);
            request.getParams().setFilePath(saved.path.toString());
            request.getParams().setDataDir(saved.path.toString());
            return startTask(request, saved.checksum);
        }
        catch (ServiceException e)
        {
            if (saved != null && shouldDeleteSavedFile(e))
            {
                deleteQuietly(saved.path);
            }
            throw e;
        }
        catch (Exception e)
        {
            if (saved != null)
            {
                deleteQuietly(saved.path);
            }
            throw new ServiceException("导入数据并启动识别失败：" + (e.getMessage() == null ? "未知错误" : e.getMessage()));
        }
    }

    @Override
    public Map<String, Object> uploadDataFile(MultipartFile file, String uploadBatchId, Integer fileIndex, Integer totalFiles, String relativePath)
    {
        validateUploadFile(file);
        String storedName = relativeDataPath(relativePath, file.getOriginalFilename());
        try
        {
            Path batchDir = frameBeamUploadDir(uploadBatchId);
            Path target = relativeTarget(batchDir, storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return uploadResult(target, batchDir, uploadBatchId, storedName, "SUCCESS", fileIndex, totalFiles);
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("Frame beam data upload failed: " + safeMessage(e));
        }
    }

    @Override
    public Map<String, Object> uploadDataChunk(String uploadId, Integer chunkIndex, Integer chunkCount, String fileName, MultipartFile chunk)
    {
        String id = safeToken(uploadId, "uploadId");
        String cleanName = relativeDataPath(fileName, fileName);
        if (chunk == null || chunk.isEmpty())
        {
            throw new ServiceException("Chunk file cannot be empty");
        }
        int idx = nonNegative(chunkIndex, "chunkIndex");
        int count = positive(chunkCount, "chunkCount");
        if (idx >= count)
        {
            throw new ServiceException("Invalid chunk index");
        }
        try
        {
            Path dir = frameBeamChunkRoot().resolve(id).normalize();
            if (!dir.startsWith(frameBeamChunkRoot()))
            {
                throw new ServiceException("Invalid chunk directory");
            }
            Files.createDirectories(dir);
            Path part = dir.resolve(idx + ".part").normalize();
            if (!part.startsWith(dir))
            {
                throw new ServiceException("Invalid chunk path");
            }
            Files.copy(chunk.getInputStream(), part, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> ret = new LinkedHashMap<>();
            ret.put("uploadId", id);
            ret.put("fileName", cleanName);
            ret.put("chunkIndex", idx);
            ret.put("chunkCount", count);
            ret.put("uploadStatus", "CHUNK_UPLOADED");
            return ret;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("Frame beam chunk upload failed: " + safeMessage(e));
        }
    }

    @Override
    public Map<String, Object> mergeDataChunks(String uploadBatchId, String uploadId, String fileName, Integer totalFiles, String relativePath)
    {
        String id = safeToken(uploadId, "uploadId");
        String storedName = relativeDataPath(relativePath, fileName);
        Path chunkDir = frameBeamChunkRoot().resolve(id).normalize();
        if (!chunkDir.startsWith(frameBeamChunkRoot()) || !Files.isDirectory(chunkDir))
        {
            throw new ServiceException("Chunk directory does not exist");
        }
        try
        {
            List<Path> parts;
            try (Stream<Path> stream = Files.list(chunkDir))
            {
                parts = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".part"))
                        .sorted(Comparator.comparingInt(path -> Integer.parseInt(path.getFileName().toString().replace(".part", ""))))
                        .toList();
            }
            if (parts.isEmpty())
            {
                throw new ServiceException("No chunks to merge");
            }
            for (int i = 0; i < parts.size(); i++)
            {
                String expected = i + ".part";
                if (!expected.equals(parts.get(i).getFileName().toString()))
                {
                    throw new ServiceException("Missing chunk: " + expected);
                }
            }

            Path batchDir = frameBeamUploadDir(uploadBatchId);
            Path target = relativeTarget(batchDir, storedName);
            try (OutputStream out = Files.newOutputStream(target))
            {
                for (Path part : parts)
                {
                    Files.copy(part, out);
                }
            }
            deleteDirectoryQuietly(chunkDir);
            Map<String, Object> ret = uploadResult(target, batchDir, uploadBatchId, storedName, "SUCCESS", null, totalFiles);
            ret.put("uploadId", id);
            return ret;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("Frame beam chunk merge failed: " + safeMessage(e));
        }
    }

    @Override
    public FrameBeamIdentifyResultVo startTask(FrameBeamIdentifyRequest request)
    {
        return startTask(request, null);
    }

    public FrameBeamIdentifyResultVo startTask(FrameBeamIdentifyRequest request, String checksum)
    {
        FrameBeamIdentifyRequest normalized = normalizeRequest(request);
        validate(normalized);
        String idempotencyKey = idempotencyKey(normalized, checksum);
        Object lock = IDEMPOTENCY_LOCKS.computeIfAbsent(idempotencyKey, key -> new Object());
        synchronized (lock)
        {
            try
            {
                AlgTaskResult existing = algTaskMapper.getByRequestIdAndType(idempotencyKey, TASK_TYPE);
                if (existing != null && (STATUS_SUCCESS.equals(existing.getStatus()) || STATUS_RUNNING.equals(existing.getStatus()) || STATUS_PENDING.equals(existing.getStatus())))
                {
                    return resultForExisting(existing);
                }
                return createRunAndSave(normalized, idempotencyKey);
            }
            finally
            {
                IDEMPOTENCY_LOCKS.remove(idempotencyKey);
            }
        }
    }

    private FrameBeamIdentifyResultVo createRunAndSave(FrameBeamIdentifyRequest request, String idempotencyKey)
    {
        String taskId = newTaskId();
        String objectName = objectName(request.getObjectInfo());
        AlgTaskResult created = tx.execute(status -> {
            AlgTaskResult record = new AlgTaskResult();
            record.setTaskId(taskId);
            record.setTaskType(TASK_TYPE);
            record.setTaskName(TASK_NAME);
            record.setStatus(STATUS_PENDING);
            record.setRequestId(idempotencyKey);
            record.setBizLevel("FRAME_BEAM");
            record.setBizName(objectName);
            record.setAircraftId(text(request.getObjectInfo().getAircraftModel()));
            record.setSubsystemId(text(request.getObjectInfo().getFuselageArea()));
            record.setEquipmentId(text(request.getObjectInfo().getFrameJointArea()));
            record.setComponentId(text(request.getObjectInfo().getMeasurePoint()));
            record.setSummary("");
            record.setReqJson(JSON.toJSONString(request));
            record.setStartAt(new Date());
            record.setRemark("FrameBeamCrackIdentify");
            algTaskMapper.insertTask(record);
            return record;
        });
        if (created == null)
        {
            throw new ServiceException("创建识别任务失败");
        }

        try
        {
            tx.executeWithoutResult(status -> updateStatus(taskId, STATUS_RUNNING, null));
            FrameBeamIdentifyResultVo result = ensureSafeResult(algorithmClient.identify(taskId, objectName, request));
            tx.executeWithoutResult(status -> saveSuccess(taskId, result));
            AlgTaskResult saved = algTaskMapper.getByTaskId(taskId);
            result.setHistoryRows(Collections.singletonList(toHistoryRow(saved == null ? created : saved, result)));
            return result;
        }
        catch (Exception e)
        {
            String message = e.getMessage() == null ? "识别任务执行失败" : e.getMessage();
            tx.executeWithoutResult(status -> updateStatus(taskId, STATUS_FAILED, message));
            throw e instanceof ServiceException ? (ServiceException) e : new ServiceException("识别任务执行失败：" + message);
        }
    }

    @Override
    public FrameBeamIdentifyResultVo getTaskResult(String taskId)
    {
        AlgTaskResult record = getRequiredRecord(taskId);
        if (STATUS_SUCCESS.equals(record.getStatus()))
        {
            return apiResult(record);
        }

        FrameBeamIdentifyResultVo vo = emptyResult();
        FrameBeamTaskVo task = vo.getTask();
        task.setTaskId(record.getTaskId());
        task.setObjectName(record.getBizName());
        task.setStatus(record.getStatus());
        task.setStatusType(statusType(record.getStatus()));
        task.setResult(record.getSummary());
        vo.setErrorMessage(record.getErrMsg());
        return vo;
    }

    @Override
    public PageRows getHistory(String keyword, String status, String result, Integer pageNum, Integer pageSize)
    {
        int p = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int s = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        int offset = (p - 1) * s;
        List<AlgTaskResult> records = algTaskMapper.getFrameBeamCrackResult(TASK_TYPE, text(keyword), text(status), text(result), offset, s);

        List<FrameBeamHistoryVo> rows = new ArrayList<>();
        for (AlgTaskResult record : records)
        {
            rows.add(toHistoryRow(record, apiResult(record)));
        }

        PageRows pageRows = new PageRows();
        pageRows.set_rows(rows);
        pageRows.set_total(algTaskMapper.countFrameBeamCrackResult(TASK_TYPE, text(keyword), text(status), text(result)));
        return pageRows;
    }

    @Override
    public FrameBeamIdentifyResultVo getHistoryDetail(String taskId)
    {
        return apiResult(getRequiredRecord(taskId));
    }

    @Override
    public int deleteHistory(String taskId)
    {
        AlgTaskResult record = getRequiredRecord(taskId);
        deleteTaskUploadData(record);
        int deleted = algTaskMapper.deleteByTaskIdAndType(record.getTaskId(), TASK_TYPE);
        if (deleted <= 0)
        {
            throw new ServiceException("历史识别记录删除失败");
        }
        return deleted;
    }

    private void deleteTaskUploadData(AlgTaskResult record)
    {
        FrameBeamIdentifyRequest request;
        try
        {
            request = JSON.parseObject(record.getReqJson(), FrameBeamIdentifyRequest.class);
        }
        catch (Exception e)
        {
            throw new ServiceException("任务上传数据路径解析失败，未删除数据库记录");
        }
        if (request == null || request.getParams() == null)
        {
            return;
        }

        FrameBeamIdentifyParams params = request.getParams();
        List<String> paths = new ArrayList<>();
        addText(paths, params.getBatchPath());
        if (paths.isEmpty())
        {
            addText(paths, params.getFilePath());
            if (params.getFilePaths() != null)
            {
                for (String filePath : params.getFilePaths())
                {
                    addText(paths, filePath);
                }
            }
        }
        if (paths.isEmpty())
        {
            addText(paths, params.getDataDir());
        }

        Path storageRoot = frameBeamStorageRoot();
        for (String rawPath : new LinkedHashSet<>(paths))
        {
            Path target;
            try
            {
                target = Paths.get(rawPath).toAbsolutePath().normalize();
            }
            catch (Exception e)
            {
                throw new ServiceException("上传数据路径不合法，未删除数据库记录");
            }
            if (target.equals(storageRoot) || !target.startsWith(storageRoot))
            {
                throw new ServiceException("仅允许删除框梁识别目录内的上传数据，未删除数据库记录");
            }
            deletePath(target);
        }
    }

    private void deletePath(Path target)
    {
        if (!Files.exists(target))
        {
            return;
        }
        try
        {
            if (Files.isDirectory(target))
            {
                try (Stream<Path> stream = Files.walk(target))
                {
                    List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
                    for (Path path : paths)
                    {
                        Files.deleteIfExists(path);
                    }
                }
            }
            else
            {
                Files.deleteIfExists(target);
            }
        }
        catch (Exception e)
        {
            throw new ServiceException("本地上传数据删除失败，未删除数据库记录：" + safeMessage(e));
        }
    }

    private FrameBeamIdentifyRequest parseMultipartRequest(String objectInfoJson, String paramsJson, String requestId)
    {
        FrameBeamIdentifyRequest request = new FrameBeamIdentifyRequest();
        try
        {
            request.setObjectInfo(StringUtils.isEmpty(text(objectInfoJson)) ? new FrameBeamObjectInfo() : JSON.parseObject(objectInfoJson, FrameBeamObjectInfo.class));
            request.setParams(StringUtils.isEmpty(text(paramsJson)) ? new FrameBeamIdentifyParams() : JSON.parseObject(paramsJson, FrameBeamIdentifyParams.class));
        }
        catch (Exception e)
        {
            throw new ServiceException("objectInfo 或 params 必须是合法JSON字符串");
        }
        request.setRequestId(requestId);
        return normalizeRequest(request);
    }

    private FrameBeamIdentifyRequest normalizeRequest(FrameBeamIdentifyRequest request)
    {
        FrameBeamIdentifyRequest normalized = request == null ? new FrameBeamIdentifyRequest() : request;
        if (normalized.getObjectInfo() == null)
        {
            normalized.setObjectInfo(new FrameBeamObjectInfo());
        }
        if (normalized.getParams() == null)
        {
            normalized.setParams(new FrameBeamIdentifyParams());
        }
        FrameBeamIdentifyParams params = normalized.getParams();
        if (params.getSegmentLength() == null)
        {
            params.setSegmentLength(2048);
        }
        if (params.getOverlapRate() == null)
        {
            params.setOverlapRate(0.8D);
        }
        if (params.getSampleRate() == null)
        {
            params.setSampleRate(51200);
        }
        if (params.getBatchSize() == null)
        {
            params.setBatchSize(32);
        }
        if (params.getEpochs() == null)
        {
            params.setEpochs(50);
        }
        if (text(params.getDataDir()) == null)
        {
            params.setDataDir(firstText(params.getBatchPath(), params.getFilePath()));
        }
        return normalized;
    }

    private void validate(FrameBeamIdentifyRequest request)
    {
        FrameBeamIdentifyParams params = request.getParams();
        if (text(params.getDataDir()) == null && text(params.getFilePath()) == null && text(params.getBatchPath()) == null)
        {
            throw new ServiceException("数据目录或上传文件路径不能为空");
        }
        if (params.getSegmentLength() == null || params.getSegmentLength() <= 0)
        {
            throw new ServiceException("单段信号长度必须大于0");
        }
        if (params.getOverlapRate() == null || params.getOverlapRate() < 0D || params.getOverlapRate() >= 1D)
        {
            throw new ServiceException("重叠率必须大于等于0且小于1");
        }
        if (params.getSampleRate() == null || params.getSampleRate() <= 0)
        {
            throw new ServiceException("采样频率必须大于0");
        }
        if (params.getBatchSize() == null || params.getBatchSize() <= 0)
        {
            throw new ServiceException("批大小必须大于0");
        }
        if (params.getEpochs() == null || params.getEpochs() <= 0)
        {
            throw new ServiceException("训练轮数必须大于0");
        }
    }

    private void validateUploadFile(MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("上传文件不能为空");
        }
        if (file.getSize() > properties.getImportConfig().getMaxFileSize())
        {
            throw new ServiceException("上传文件大小超过限制");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".csv") && !name.endsWith(".txt"))
        {
            throw new ServiceException("仅支持CSV/TXT数据文件");
        }
    }

    private SavedFile saveUploadFile(MultipartFile file) throws Exception
    {
        Path root = Paths.get(sourceRoot).toAbsolutePath().normalize()
                .resolve(properties.getImportConfig().getStorageDir())
                .resolve(new SimpleDateFormat("yyyyMMdd").format(new Date()))
                .normalize();
        Files.createDirectories(root);
        String filename = UUID.randomUUID().toString().replace("-", "") + "_" + sanitizeFilename(file.getOriginalFilename());
        Path target = root.resolve(filename).normalize();
        if (!target.startsWith(root))
        {
            throw new ServiceException("文件保存路径不合法");
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new DigestInputStream(file.getInputStream(), digest))
        {
            Files.copy(in, target);
        }
        return new SavedFile(target, hex(digest.digest()));
    }

    private boolean shouldDeleteSavedFile(ServiceException e)
    {
        String message = e.getMessage();
        return message != null && (message.contains("创建识别任务失败") || message.contains("参数") || message.contains("不能为空"));
    }

    private FrameBeamIdentifyResultVo resultForExisting(AlgTaskResult existing)
    {
        if (STATUS_SUCCESS.equals(existing.getStatus()))
        {
            return apiResult(existing);
        }
        return getTaskResult(existing.getTaskId());
    }

    private void updateStatus(String taskId, String status, String errMsg)
    {
        AlgTaskResult update = new AlgTaskResult();
        update.setTaskId(taskId);
        update.setStatus(status);
        update.setErrMsg(errMsg);
        if (STATUS_FAILED.equals(status))
        {
            update.setEndAt(new Date());
        }
        algTaskMapper.updateTask(update);
    }

    private void saveSuccess(String taskId, FrameBeamIdentifyResultVo result)
    {
        AlgTaskResult done = new AlgTaskResult();
        done.setTaskId(taskId);
        done.setStatus(STATUS_SUCCESS);
        done.setSummary(result.getTask() == null ? "" : result.getTask().getResult());
        done.setResultVal(confidenceText(result));
        done.setResultUnit("confidence");
        done.setResJson(JSON.toJSONString(result));
        done.setEndAt(new Date());
        algTaskMapper.updateTask(done);
    }

    private AlgTaskResult getRequiredRecord(String taskId)
    {
        String value = text(taskId);
        if (value == null)
        {
            throw new ServiceException("任务ID不能为空");
        }
        AlgTaskResult record = algTaskMapper.getByTaskId(value);
        if (record == null || !TASK_TYPE.equals(record.getTaskType()))
        {
            throw new ServiceException("识别任务不存在");
        }
        return record;
    }

    private FrameBeamIdentifyResultVo apiResult(AlgTaskResult record)
    {
        FrameBeamIdentifyResultVo vo = null;
        if (text(record.getResJson()) != null)
        {
            vo = JSON.parseObject(record.getResJson(), FrameBeamIdentifyResultVo.class);
        }
        vo = ensureSafeResult(vo);
        if (vo.getTask().getTaskId() == null)
        {
            vo.getTask().setTaskId(record.getTaskId());
        }
        vo.getTask().setObjectName(firstText(vo.getTask().getObjectName(), record.getBizName()));
        vo.getTask().setStatus(firstText(vo.getTask().getStatus(), record.getStatus()));
        vo.getTask().setStatusType(firstText(vo.getTask().getStatusType(), statusType(record.getStatus())));
        vo.setHistoryRows(Collections.singletonList(toHistoryRow(record, vo)));
        return vo;
    }

    private FrameBeamIdentifyResultVo ensureSafeResult(FrameBeamIdentifyResultVo vo)
    {
        if (vo == null)
        {
            vo = emptyResult();
        }
        if (vo.getTask() == null)
        {
            vo.setTask(new FrameBeamTaskVo());
        }
        if (vo.getProbabilities() == null)
        {
            vo.setProbabilities(new FrameBeamProbabilityVo());
        }
        if (vo.getProbabilities().getHealthProbability() == null)
        {
            vo.getProbabilities().setHealthProbability(0D);
        }
        if (vo.getProbabilities().getFaultProbability() == null)
        {
            vo.getProbabilities().setFaultProbability(0D);
        }
        if (vo.getMetrics() == null)
        {
            vo.setMetrics(new FrameBeamMetricsVo());
        }
        if (vo.getMetrics().getAccuracy() == null)
        {
            vo.getMetrics().setAccuracy(0D);
        }
        if (vo.getMetrics().getEarlyCrackAccuracy() == null)
        {
            vo.getMetrics().setEarlyCrackAccuracy(0D);
        }
        if (vo.getMetrics().getWeightedF1() == null)
        {
            vo.getMetrics().setWeightedF1(0D);
        }
        if (vo.getMetrics().getTestLoss() == null)
        {
            vo.getMetrics().setTestLoss(0D);
        }
        if (vo.getWaveform() == null)
        {
            vo.setWaveform(new FrameBeamWaveformVo());
        }
        if (vo.getStft() == null)
        {
            vo.setStft(new FrameBeamStftVo());
        }
        if (vo.getStft().getMin() == null)
        {
            vo.getStft().setMin(0D);
        }
        if (vo.getStft().getMax() == null)
        {
            vo.getStft().setMax(1D);
        }
        if (vo.getHistoryRows() == null)
        {
            vo.setHistoryRows(new ArrayList<>());
        }
        return vo;
    }

    private FrameBeamIdentifyResultVo emptyResult()
    {
        FrameBeamIdentifyResultVo vo = new FrameBeamIdentifyResultVo();
        vo.setTask(new FrameBeamTaskVo());
        vo.setProbabilities(new FrameBeamProbabilityVo());
        vo.setMetrics(new FrameBeamMetricsVo());
        vo.setWaveform(new FrameBeamWaveformVo());
        FrameBeamStftVo stft = new FrameBeamStftVo();
        stft.setMin(0D);
        stft.setMax(1D);
        vo.setStft(stft);
        vo.setHistoryRows(new ArrayList<>());
        return vo;
    }

    private FrameBeamHistoryVo toHistoryRow(AlgTaskResult record, FrameBeamIdentifyResultVo result)
    {
        FrameBeamHistoryVo row = new FrameBeamHistoryVo();
        row.setTaskId(record.getTaskId());
        row.setObjectName(record.getBizName());
        row.setInputData(inputData(record));
        row.setResult(resultText(record, result));
        row.setConfidence(confidenceText(result));
        row.setCreateTime(formatDate(record.getCreateTime()));
        row.setApiResult(resultSnapshot(result));
        return row;
    }

    private FrameBeamIdentifyResultVo resultSnapshot(FrameBeamIdentifyResultVo source)
    {
        FrameBeamIdentifyResultVo snapshot = ensureSafeResult(source);
        FrameBeamIdentifyResultVo copy = new FrameBeamIdentifyResultVo();
        copy.setTask(snapshot.getTask());
        copy.setProbabilities(snapshot.getProbabilities());
        copy.setMetrics(snapshot.getMetrics());
        copy.setWaveform(snapshot.getWaveform());
        copy.setStft(snapshot.getStft());
        copy.setErrorMessage(snapshot.getErrorMessage());
        copy.setHistoryRows(Collections.emptyList());
        return copy;
    }

    private String inputData(AlgTaskResult record)
    {
        try
        {
            FrameBeamIdentifyRequest request = JSON.parseObject(record.getReqJson(), FrameBeamIdentifyRequest.class);
            if (request != null && request.getParams() != null)
            {
                return firstText(request.getParams().getBatchPath(), firstText(request.getParams().getFilePath(), request.getParams().getDataDir()));
            }
        }
        catch (Exception ignored)
        {
        }
        return "";
    }

    private String resultText(AlgTaskResult record, FrameBeamIdentifyResultVo result)
    {
        if (result != null && result.getTask() != null && text(result.getTask().getResult()) != null)
        {
            return result.getTask().getResult();
        }
        return record.getSummary();
    }

    private String confidenceText(FrameBeamIdentifyResultVo result)
    {
        if (result == null || result.getProbabilities() == null)
        {
            return "";
        }
        Double value = "健康".equals(result.getTask() == null ? null : result.getTask().getResult())
                ? result.getProbabilities().getHealthProbability()
                : result.getProbabilities().getFaultProbability();
        return value == null ? "" : String.format("%.1f%%", value * 100D);
    }

    private String objectName(FrameBeamObjectInfo objectInfo)
    {
        List<String> parts = new ArrayList<>();
        addText(parts, objectInfo == null ? null : objectInfo.getAircraftModel());
        addText(parts, objectInfo == null ? null : objectInfo.getFuselageArea());
        addText(parts, objectInfo == null ? null : objectInfo.getFrameJointArea());
        addText(parts, objectInfo == null ? null : objectInfo.getMeasurePoint());
        return parts.isEmpty() ? "飞机框梁对象" : String.join(" / ", parts);
    }

    private String idempotencyKey(FrameBeamIdentifyRequest request, String checksum)
    {
        if (text(request.getRequestId()) != null)
        {
            return text(request.getRequestId());
        }
        String seed = firstText(checksum, firstText(request.getParams().getUploadBatchId(), firstText(request.getParams().getFilePath(), request.getParams().getDataDir())))
                + "|" + JSON.toJSONString(request.getObjectInfo())
                + "|" + JSON.toJSONString(request.getParams());
        return sha256(seed);
    }

    private String newTaskId()
    {
        String timestamp = new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date());
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "FBC" + timestamp + suffix;
    }

    private String statusType(String status)
    {
        if (STATUS_SUCCESS.equals(status))
        {
            return "success";
        }
        if (STATUS_RUNNING.equals(status) || STATUS_PENDING.equals(status))
        {
            return "warning";
        }
        if (STATUS_FAILED.equals(status))
        {
            return "danger";
        }
        return "info";
    }

    private String formatDate(Date date)
    {
        return date == null ? "" : new SimpleDateFormat(DATE_TIME_PATTERN).format(date);
    }

    private void addText(List<String> list, String value)
    {
        String text = text(value);
        if (text != null)
        {
            list.add(text);
        }
    }

    private String firstText(String first, String second)
    {
        return text(first) != null ? text(first) : text(second);
    }

    private String text(Object value)
    {
        if (value == null)
        {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private String sanitizeFilename(String name)
    {
        String filename = text(name);
        if (filename == null)
        {
            return "frame_beam_data.csv";
        }
        return filename.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String sha256(String value)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return hex(digest.digest((value == null ? "" : value).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        }
        catch (Exception e)
        {
            throw new ServiceException("生成幂等键失败");
        }
    }

    private String hex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
        {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void deleteQuietly(Path path)
    {
        if (path == null)
        {
            return;
        }
        try
        {
            Files.deleteIfExists(path);
        }
        catch (Exception ignored)
        {
        }
    }

    private Path frameBeamUploadDir(String uploadBatchId)
    {
        String batchId = text(uploadBatchId);
        if (batchId == null)
        {
            batchId = "FRAME_" + new SimpleDateFormat(TASK_TIME_PATTERN).format(new Date());
        }
        batchId = safeToken(batchId, "uploadBatchId");
        Path root = frameBeamStorageRoot().resolve("data").resolve(new SimpleDateFormat("yyyyMMdd").format(new Date())).resolve(batchId).normalize();
        if (!root.startsWith(frameBeamStorageRoot()))
        {
            throw new ServiceException("Invalid frame beam upload directory");
        }
        try
        {
            Files.createDirectories(root);
            return root;
        }
        catch (Exception e)
        {
            throw new ServiceException("Create frame beam upload directory failed: " + safeMessage(e));
        }
    }

    private Path frameBeamChunkRoot()
    {
        Path root = frameBeamStorageRoot().resolve("chunks").normalize();
        try
        {
            Files.createDirectories(root);
            return root;
        }
        catch (Exception e)
        {
            throw new ServiceException("Create frame beam chunk directory failed: " + safeMessage(e));
        }
    }

    private Path frameBeamStorageRoot()
    {
        return Paths.get(sourceRoot).toAbsolutePath().normalize().resolve("frame-beam-crack").normalize();
    }

    private Path relativeTarget(Path dir, String relativePath)
    {
        String safe = relativeDataPath(relativePath, relativePath);
        Path target = dir.resolve(safe).normalize();
        if (!target.startsWith(dir))
        {
            throw new ServiceException("Invalid upload file path");
        }
        try
        {
            if (target.getParent() != null)
            {
                Files.createDirectories(target.getParent());
            }
            return target;
        }
        catch (Exception e)
        {
            throw new ServiceException("Create upload file directory failed: " + safeMessage(e));
        }
    }

    private String relativeDataPath(String relativePath, String fallbackName)
    {
        String raw = text(relativePath);
        if (raw == null)
        {
            raw = text(fallbackName);
        }
        if (raw == null)
        {
            raw = "frame_beam_data.csv";
        }
        raw = raw.replace("\\", "/");
        while (raw.startsWith("/"))
        {
            raw = raw.substring(1);
        }
        if (raw.contains("..") || raw.contains(":"))
        {
            throw new ServiceException("Invalid relative path");
        }
        String[] parts = raw.split("/");
        List<String> cleanParts = new ArrayList<>();
        for (String part : parts)
        {
            String clean = sanitizeFilename(part);
            if (text(clean) != null)
            {
                cleanParts.add(clean);
            }
        }
        if (cleanParts.isEmpty())
        {
            cleanParts.add("frame_beam_data.csv");
        }
        String value = String.join("/", cleanParts);
        if (!isDataFile(value))
        {
            throw new ServiceException("Only CSV/TXT data files are supported");
        }
        return value;
    }

    private boolean isDataFile(String fileName)
    {
        String value = text(fileName);
        if (value == null)
        {
            return false;
        }
        String lower = value.toLowerCase();
        return lower.endsWith(".csv") || lower.endsWith(".txt");
    }

    private String safeToken(String value, String name)
    {
        String token = text(value);
        if (token == null || !token.matches("[A-Za-z0-9_.-]{1,80}"))
        {
            throw new ServiceException("Invalid " + name);
        }
        return token;
    }

    private int nonNegative(Integer value, String name)
    {
        if (value == null || value < 0)
        {
            throw new ServiceException("Invalid " + name);
        }
        return value;
    }

    private int positive(Integer value, String name)
    {
        if (value == null || value <= 0)
        {
            throw new ServiceException("Invalid " + name);
        }
        return value;
    }

    private Map<String, Object> uploadResult(Path target, Path batchDir, String uploadBatchId, String relativePath, String status, Integer fileIndex, Integer totalFiles)
    {
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("fileName", target.getFileName().toString());
        ret.put("filePath", target.toAbsolutePath().normalize().toString());
        ret.put("sourceFile", target.toAbsolutePath().normalize().toString());
        ret.put("relativePath", relativePath);
        ret.put("batchPath", batchDir.toAbsolutePath().normalize().toString());
        ret.put("uploadBatchId", text(uploadBatchId));
        ret.put("uploadStatus", status);
        ret.put("fileIndex", fileIndex);
        ret.put("totalFiles", totalFiles);
        try
        {
            ret.put("fileSize", Files.size(target));
        }
        catch (Exception ignored)
        {
            ret.put("fileSize", null);
        }
        return ret;
    }

    private String safeMessage(Exception e)
    {
        return e.getMessage() == null ? "unknown error" : e.getMessage();
    }

    private void deleteDirectoryQuietly(Path dir)
    {
        if (dir == null || !Files.exists(dir))
        {
            return;
        }
        try (Stream<Path> stream = Files.walk(dir))
        {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try
                {
                    Files.deleteIfExists(path);
                }
                catch (Exception ignored)
                {
                }
            });
        }
        catch (Exception ignored)
        {
        }
    }

    private static class SavedFile
    {
        private final Path path;
        private final String checksum;

        private SavedFile(Path path, String checksum)
        {
            this.path = path;
            this.checksum = checksum;
        }
    }
}
