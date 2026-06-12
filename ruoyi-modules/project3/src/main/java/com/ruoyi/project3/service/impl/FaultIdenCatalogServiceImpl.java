package com.ruoyi.project3.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.config.faultiden.FaultIdenFileProps;
import com.ruoyi.project3.domain.faultiden.FaultIdenSampleFile;
import com.ruoyi.project3.mapper.FaultIdenSampleMapper;
import com.ruoyi.project3.mapper.MonitorMapper;
import com.ruoyi.project3.service.FaultIdenCatalogService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.springframework.web.multipart.MultipartFile;

@Service
public class FaultIdenCatalogServiceImpl implements FaultIdenCatalogService
{
    private static final Logger log = LoggerFactory.getLogger(FaultIdenCatalogServiceImpl.class);
    private static final String DEFAULT_DATA_ROOT = "F:/TotalData";
    private static final String USAGE_FEATURE = "FEATURE_ANALYSIS";
    private static final String USAGE_PREDICT = "FAULT_PREDICT";
    private static final String USAGE_IDENTIFY = "FAULT_IDENTIFY";
    private static final String USAGE_WARNING = "PROCESS_ANOMALY";
    private static final String USAGE_KQC_MINING = "KQC_MINING";
    private static final String USAGE_KEY_PROCESS = "KEY_PROCESS";
    private static final String USAGE_MANUFACTURE_COMMON = "MANUFACTURE_COMMON";
    private static final String USAGE_COMMON = "COMMON";
    private static final String USAGE_ALL = "ALL";
    private static final String PURPOSE_FEATURE_ANALYSIS = "featureAnalysis";
    private static final String PURPOSE_FAULT_PREDICTION = "faultPrediction";
    private static final String PURPOSE_FAULT_IDENTIFY = "faultIdentify";
    private static final String PURPOSE_PROC_ANOM = "processAnomaly";
    private static final String OBJECT_BEARING = "bearing";
    private static final String FAULT_IDENTIFY_DIR = "FaultIdentifyData";
    private static final String BEARING_DATASET_DIR = "XJTU-SY_Bearing_Datasets";
    private static final String CHUNK_DIR = "_chunks";
    private static final long API_IMPORT_MAX_BYTES = 100L * 1024L * 1024L;
    private static final Pattern FIRST_NUMBER = Pattern.compile("(\\d+)");
    private static final String[][] NUMERIC_DATASET_DIRS = {
            {"35Hz12kN", "Bearing1_1"},
            {"35Hz12kN", "Bearing1_2"},
            {"35Hz12kN", "Bearing1_3"},
            {"35Hz12kN", "Bearing1_4"},
            {"35Hz12kN", "Bearing1_5"},
            {"37.5Hz11kN", "Bearing2_1"},
            {"37.5Hz11kN", "Bearing2_2"},
            {"37.5Hz11kN", "Bearing2_3"},
            {"37.5Hz11kN", "Bearing2_4"},
            {"37.5Hz11kN", "Bearing2_5"},
            {"40Hz10kN", "Bearing3_1"},
            {"40Hz10kN", "Bearing3_2"},
            {"40Hz10kN", "Bearing3_3"},
            {"40Hz10kN", "Bearing3_4"},
            {"40Hz10kN", "Bearing3_5"}
    };

    @Resource
    private FaultIdenFileProps props;

    @Resource
    private FaultIdenSampleMapper sampleMapper;

    @Resource
    private MonitorMapper monitorMapper;

    @Override
    public Map<String, Object> catalog(String rootPath, String defaultStartTime, Boolean verifyLineCount)
    {
        Path root = root(rootPath);
        int sampleCount = 0;
        try (Stream<Path> conds = Files.list(root))
        {
            List<Path> condDirs = conds.filter(Files::isDirectory)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
            for (Path condDir : condDirs)
            {
                sampleCount += scanCondition(condDir);
            }
        }
        catch (Exception e)
        {
            throw new ServiceException("故障预测的特征提取数据目录扫描失败：" + e.getMessage());
        }

        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("sampleCount", sampleCount);
        return ret;
    }

    @Override
    public Map<String, Object> importNumericData(Map<String, Object> req)
    {
        NumericImportOptions options = numericImportOptions(req);
        String dataUsage = dataUsage(options.purpose);
        Path datasetRoot = datasetRoot(options);
        if (!Files.isDirectory(datasetRoot))
        {
            throw new ServiceException("数值型数据集目录不存在：" + datasetRoot);
        }

        int scannedDirectoryCount = 0;
        int missingDirectoryCount = 0;
        int scannedFileCount = 0;
        int selectedFileCount = 0;
        int insertedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();

        for (String[] dirDef : NUMERIC_DATASET_DIRS)
        {
            String condition = dirDef[0];
            String bearing = dirDef[1];
            if (!options.acceptDir(condition, bearing))
            {
                continue;
            }

            Path bearingDir = datasetRoot.resolve(condition).resolve(bearing).normalize();
            if (!bearingDir.startsWith(datasetRoot) || !Files.isDirectory(bearingDir))
            {
                missingDirectoryCount++;
                errors.add("目录不存在：" + bearingDir);
                continue;
            }
            scannedDirectoryCount++;

            try (Stream<Path> stream = Files.list(bearingDir))
            {
                List<Path> dataFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> isDataFile(path.getFileName().toString()))
                        .sorted(Comparator.comparingInt(path -> sampleNoFromFileName(path.getFileName().toString())))
                        .toList();
                int fallbackNo = 1;
                int selectedInBearing = 0;
                for (Path file : dataFiles)
                {
                    String fileName = file.getFileName().toString();
                    int sampleNo = sampleNoFromFileName(fileName);
                    if (sampleNo == Integer.MAX_VALUE)
                    {
                        sampleNo = fallbackNo++;
                    }
                    if (!options.acceptSampleNo(sampleNo))
                    {
                        continue;
                    }
                    if (!options.canSelectMore(selectedFileCount, selectedInBearing))
                    {
                        break;
                    }

                    scannedFileCount++;
                    selectedFileCount++;
                    selectedInBearing++;
                    try
                    {
                        if (sampleMapper.selectByUniqueKey(condition, bearing, fileName, dataUsage) != null)
                        {
                            skippedCount++;
                            continue;
                        }

                        while (sampleMapper.countByConditionBearingSampleNo(condition, bearing, sampleNo, dataUsage) > 0)
                        {
                            sampleNo++;
                        }

                        FaultIdenSampleFile sample = new FaultIdenSampleFile();
                        sample.setConditionLabel(condition);
                        sample.setBearingCode(bearing);
                        sample.setSampleNo(sampleNo);
                        sample.setFileName(fileName);
                        sample.setSourceFile(file.toAbsolutePath().normalize().toString());
                        sample.setFileSize(Files.size(file));
                        sample.setDataUsage(dataUsage);
                        sampleMapper.inFaultIdenSample(sample);
                        insertedCount++;
                    }
                    catch (Exception e)
                    {
                        failedCount++;
                        errors.add(file.toAbsolutePath().normalize() + ": " + (e.getMessage() == null ? "导入失败" : e.getMessage()));
                    }
                }
            }
            catch (Exception e)
            {
                failedCount++;
                errors.add(bearingDir + ": " + (e.getMessage() == null ? "目录扫描失败" : e.getMessage()));
            }

            if (options.totalLimitReached(selectedFileCount))
            {
                break;
            }
        }

        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("scannedDirectoryCount", scannedDirectoryCount);
        ret.put("missingDirectoryCount", missingDirectoryCount);
        ret.put("scannedFileCount", scannedFileCount);
        ret.put("selectedFileCount", selectedFileCount);
        ret.put("insertedCount", insertedCount);
        ret.put("skippedCount", skippedCount);
        ret.put("failedCount", failedCount);
        ret.put("errors", errors);
        ret.put("datasetRoot", datasetRoot.toString());
        ret.put("options", options.toMap());
        return ret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Map<String, Object> uploadNumericData(
            String purpose,
            String executionObject,
            String conditionLabel,
            Integer bearingNo,
            String airId,
            String subId,
            String eqpId,
            String cmpId,
            String ptId,
            MultipartFile[] files
    )
    {
        if (files == null || files.length == 0)
        {
            throw new ServiceException("请选择至少一个CSV/TXT文件");
        }

        Map<String, Object> req = new LinkedHashMap<>();
        req.put("purpose", purpose);
        req.put("executionObject", executionObject);
        NumericImportOptions options = numericImportOptions(req);
        String dataUsage = dataUsage(options.purpose);
        if (!PURPOSE_PROC_ANOM.equals(options.purpose))
        {
            ptId = null;
        }
        ObjBind bind = valObjBind(airId, subId, eqpId, cmpId, ptId);

        String condition = text(conditionLabel);
        Integer no = positiveInt(bearingNo);
        boolean bearingRoute = condition != null || no != null;
        if (bearingRoute && (condition == null || no == null))
        {
            throw new ServiceException("工况和轴承编号必须同时提供");
        }

        Path uploadRoot = bearingRoute ? datasetRoot(options) : objectNumericRoot(options);
        String sampleCondition = bearingRoute ? condition : "object_" + bind.tgtLv;
        String sampleBearing = bearingRoute ? bearingCode(condition, no) : bind.tgtId;
        Path uploadDir = bearingRoute
                ? uploadRoot.resolve(condition).resolve(sampleBearing).normalize()
                : uploadRoot.resolve(bind.tgtLv).resolve(safePathPart(bind.tgtId)).normalize();
        if (!uploadDir.startsWith(uploadRoot))
        {
            throw new ServiceException("上传目录不合法");
        }

        int selectedFileCount = 0;
        int insertedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();
        List<Path> createdTargets = new ArrayList<>();
        try
        {
            Files.createDirectories(uploadDir);
        }
        catch (Exception e)
        {
            throw new ServiceException("创建上传目录失败：" + e.getMessage());
        }

        int fallbackNo = 1;
        for (MultipartFile file : files)
        {
            if (file == null || file.isEmpty())
            {
                skippedCount++;
                continue;
            }
            String fileName = cleanFileName(file.getOriginalFilename());
            if (!isDataFile(fileName))
            {
                skippedCount++;
                continue;
            }

            selectedFileCount++;
            Path target = stableTarget(uploadDir, fileName);
            try
            {
                if (sampleMapper.selectByUniqueKey(sampleCondition, sampleBearing, fileName, dataUsage) != null)
                {
                    skippedCount++;
                    continue;
                }

                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                createdTargets.add(target);

                int sampleNo = sampleNoFromFileName(target.getFileName().toString());
                if (sampleNo == Integer.MAX_VALUE)
                {
                    sampleNo = fallbackNo++;
                }
                while (sampleMapper.countByConditionBearingSampleNo(sampleCondition, sampleBearing, sampleNo, dataUsage) > 0)
                {
                    sampleNo++;
                }

                FaultIdenSampleFile sample = new FaultIdenSampleFile();
                sample.setConditionLabel(sampleCondition);
                sample.setBearingCode(sampleBearing);
                sample.setSampleNo(sampleNo);
                sample.setFileName(target.getFileName().toString());
                sample.setSourceFile(target.toAbsolutePath().normalize().toString());
                sample.setFileSize(Files.size(target));
                sample.setDataUsage(dataUsage);
                sample.setAircraftId(bind.airId);
                sample.setSubsystemId(bind.subId);
                sample.setEquipmentId(bind.eqpId);
                sample.setComponentId(bind.cmpId);
                sampleMapper.inFaultIdenSample(sample);
                insertedCount++;
            }
            catch (Exception e)
            {
                createdTargets.forEach(this::deleteQuietly);
                failedCount++;
                errors.add(fileName + ": " + (e.getMessage() == null ? "上传导入失败" : e.getMessage()));
                throw new ServiceException("上传数值型数据失败，已回滚本次上传：" + errors.get(errors.size() - 1));
            }
        }

        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("datasetRoot", uploadRoot.toString());
        ret.put("uploadDir", uploadDir.toString());
        ret.put("conditionLabel", sampleCondition);
        ret.put("bearingCode", sampleBearing);
        ret.put("objectBinding", bind.toMap());
        ret.put("dataUsage", dataUsage);
        ret.put("selectedFileCount", selectedFileCount);
        ret.put("insertedCount", insertedCount);
        ret.put("skippedCount", skippedCount);
        ret.put("failedCount", failedCount);
        ret.put("errors", errors);
        return ret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Map<String, Object> uploadNumericFile(
            String purpose,
            String executionObject,
            String conditionLabel,
            Integer bearingNo,
            String airId,
            String subId,
            String eqpId,
            String cmpId,
            String ptId,
            String uploadBatchId,
            Integer fileIndex,
            Integer totalFiles,
            MultipartFile file,
            String relativePath
    )
    {
        UploadContext ctx = uploadContext(purpose, executionObject, conditionLabel, bearingNo, airId, subId, eqpId, cmpId, ptId);
        log.info("数值型数据单文件上传开始，batchId={}，fileIndex={}，totalFiles={}，fileName={}",
                text(uploadBatchId), fileIndex, totalFiles, file == null ? null : file.getOriginalFilename());
        Map<String, Object> ret = saveNumericSample(ctx, file, uploadBatchId, fileIndex, totalFiles, relativePath);
        log.info("数值型数据单文件上传完成，batchId={}，sampleId={}，fileName={}，status={}",
                text(uploadBatchId), ret.get("sampleId"), ret.get("fileName"), ret.get("uploadStatus"));
        return ret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Map<String, Object> uploadNumericApi(Map<String, Object> req)
    {
        if (req == null)
        {
            throw new ServiceException("API导入参数不能为空");
        }
        UploadContext ctx = uploadContext(
                text(req.get("purpose")),
                text(req.get("executionObject")),
                text(req.get("conditionLabel")),
                positiveInt(req.get("bearingNo")),
                text(req.get("aircraftId")),
                text(req.get("subsystemId")),
                text(req.get("equipmentId")),
                text(req.get("componentId")),
                text(req.get("partId"))
        );

        String apiUrl = text(req.get("apiUrl"));
        if (apiUrl == null)
        {
            throw new ServiceException("API地址不能为空");
        }
        URI uri;
        try
        {
            uri = URI.create(apiUrl);
        }
        catch (Exception e)
        {
            throw new ServiceException("API地址不合法");
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
        {
            throw new ServiceException("API地址仅支持HTTP/HTTPS");
        }

        String method = text(req.get("method"));
        method = method == null ? "GET" : method.toUpperCase();
        if (!"GET".equals(method) && !"POST".equals(method))
        {
            throw new ServiceException("API请求方法仅支持GET/POST");
        }

        String fileName = apiFileName(text(req.get("fileName")));
        String uploadBatchId = text(req.get("uploadBatchId"));
        Path targetDir = uploadTargetDir(ctx, uploadBatchId);
        FaultIdenSampleFile existing = sampleMapper.selectByUniqueKey(ctx.sampleCondition, ctx.sampleBearing, fileName, ctx.dataUsage);
        if (existing != null)
        {
            Map<String, Object> ret = sampleResult(ctx, existing, Paths.get(existing.getSourceFile()), uploadBatchId, "SKIPPED");
            ret.put("message", "同名文件已存在");
            ret.put("apiUrl", apiUrl);
            return ret;
        }
        Path target = stableTarget(targetDir, fileName);

        try
        {
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(120));
            addApiHeaders(builder, req.get("headers"));
            String body = text(req.get("body"));
            if ("POST".equals(method))
            {
                builder.POST(body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
            }
            else
            {
                builder.GET();
            }

            HttpResponse<InputStream> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build()
                    .send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("API请求失败，HTTP状态码：" + response.statusCode());
            }
            try (InputStream input = response.body(); OutputStream output = Files.newOutputStream(target))
            {
                copyWithLimit(input, output, API_IMPORT_MAX_BYTES);
            }
            Map<String, Object> ret = saveNumericSample(ctx, target, uploadBatchId, 1, 1);
            ret.put("apiUrl", apiUrl);
            ret.put("uploadStatus", "SUCCESS");
            return ret;
        }
        catch (ServiceException e)
        {
            try
            {
                Files.deleteIfExists(target);
            }
            catch (Exception ignored)
            {
            }
            throw e;
        }
        catch (Exception e)
        {
            try
            {
                Files.deleteIfExists(target);
            }
            catch (Exception ignored)
            {
            }
            throw new ServiceException("API导入失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> uploadNumericChunk(String uploadId, Integer chunkIndex, Integer chunkCount, String fileName, MultipartFile chunk)
    {
        String id = safeToken(uploadId, "uploadId");
        String cleanName = cleanFileName(fileName);
        if (!isDataFile(cleanName))
        {
            throw new ServiceException("仅支持上传 CSV/TXT 文件");
        }
        if (chunk == null || chunk.isEmpty())
        {
            throw new ServiceException("分片文件不能为空");
        }
        int idx = nonNegative(chunkIndex, "chunkIndex");
        Integer count = positiveInt(chunkCount);
        if (count == null || idx >= count)
        {
            throw new ServiceException("分片序号不合法");
        }

        try
        {
            Path dir = chunkRoot().resolve(id).normalize();
            if (!dir.startsWith(chunkRoot()))
            {
                throw new ServiceException("分片目录不合法");
            }
            Files.createDirectories(dir);
            Path part = dir.resolve(idx + ".part").normalize();
            if (!part.startsWith(dir))
            {
                throw new ServiceException("分片路径不合法");
            }
            Files.copy(chunk.getInputStream(), part, StandardCopyOption.REPLACE_EXISTING);
            log.info("数值型数据分片上传完成，uploadId={}，fileName={}，chunk={}/{}，size={}", id, cleanName, idx + 1, count, chunk.getSize());

            Map<String, Object> ret = new LinkedHashMap<>();
            ret.put("uploadId", id);
            ret.put("fileName", cleanName);
            ret.put("chunkIndex", idx);
            ret.put("chunkCount", count);
            ret.put("uploadedCount", uploadedChunkCount(id));
            ret.put("uploadStatus", "CHUNK_UPLOADED");
            return ret;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("分片上传失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Map<String, Object> mergeNumericChunks(
            String purpose,
            String executionObject,
            String conditionLabel,
            Integer bearingNo,
            String airId,
            String subId,
            String eqpId,
            String cmpId,
            String ptId,
            String uploadBatchId,
            String uploadId,
            String fileName,
            Integer totalFiles,
            String relativePath
    )
    {
        String id = safeToken(uploadId, "uploadId");
        String storedName = relativeDataPath(relativePath, fileName);
        String cleanName = Paths.get(storedName).getFileName().toString();
        if (!isDataFile(storedName))
        {
            throw new ServiceException("仅支持上传 CSV/TXT 文件");
        }
        UploadContext ctx = uploadContext(purpose, executionObject, conditionLabel, bearingNo, airId, subId, eqpId, cmpId, ptId);
        Path dir = chunkRoot().resolve(id).normalize();
        if (!dir.startsWith(chunkRoot()) || !Files.isDirectory(dir))
        {
            throw new ServiceException("分片目录不存在");
        }

        try
        {
            List<Path> parts;
            try (Stream<Path> stream = Files.list(dir))
            {
                parts = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".part"))
                        .sorted(Comparator.comparingInt(path -> Integer.parseInt(path.getFileName().toString().replace(".part", ""))))
                        .toList();
            }
            if (parts.isEmpty())
            {
                throw new ServiceException("没有可合并的分片");
            }
            for (int i = 0; i < parts.size(); i++)
            {
                String expect = i + ".part";
                if (!expect.equals(parts.get(i).getFileName().toString()))
                {
                    throw new ServiceException("分片缺失：" + expect);
                }
            }

            FaultIdenSampleFile existing = sampleMapper.selectByUniqueKey(ctx.sampleCondition, ctx.sampleBearing, storedName, ctx.dataUsage);
            if (existing != null)
            {
                deleteChunkDirectory(dir);
                Map<String, Object> ret = sampleResult(ctx, existing, Paths.get(existing.getSourceFile()), uploadBatchId, "SKIPPED");
                ret.put("uploadId", id);
                ret.put("uploadStatus", "SKIPPED");
                ret.put("message", "同名文件已存在");
                return ret;
            }

            Path targetDir = uploadTargetDir(ctx, uploadBatchId);
            Path target = relativeTarget(targetDir, storedName);
            try (OutputStream out = Files.newOutputStream(target))
            {
                for (Path part : parts)
                {
                    Files.copy(part, out);
                }
            }
            try
            {
                Map<String, Object> ret = saveNumericSample(ctx, target, uploadBatchId, null, totalFiles, storedName);
                deleteChunkDirectory(dir);
                log.info("数值型数据分片合并并清理完成，uploadId={}，fileName={}，chunkCount={}，target={}", id, cleanName, parts.size(), target);
                ret.put("uploadId", id);
                ret.put("uploadStatus", "SUCCESS");
                return ret;
            }
            catch (RuntimeException e)
            {
                deleteQuietly(target);
                throw e;
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("分片合并失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> numericChunkStatus(String uploadId)
    {
        String id = safeToken(uploadId, "uploadId");
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("uploadId", id);
        ret.put("uploadedCount", uploadedChunkCount(id));
        ret.put("uploadStatus", "WAITING");
        return ret;
    }

    private String bearingCode(String condition, int bearingNo)
    {
        int conditionNo;
        if ("35Hz12kN".equals(condition))
        {
            conditionNo = 1;
        }
        else if ("37.5Hz11kN".equals(condition))
        {
            conditionNo = 2;
        }
        else if ("40Hz10kN".equals(condition))
        {
            conditionNo = 3;
        }
        else
        {
            throw new ServiceException("不支持的工况：" + condition);
        }
        return "Bearing" + conditionNo + "_" + bearingNo;
    }

    private UploadContext uploadContext(
            String purpose,
            String executionObject,
            String conditionLabel,
            Integer bearingNo,
            String airId,
            String subId,
            String eqpId,
            String cmpId,
            String ptId
    )
    {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("purpose", purpose);
        req.put("executionObject", executionObject);
        NumericImportOptions options = numericImportOptions(req);
        String dataUsage = dataUsage(options.purpose);
        if (!PURPOSE_PROC_ANOM.equals(options.purpose))
        {
            ptId = null;
        }
        ObjBind bind = valObjBind(airId, subId, eqpId, cmpId, ptId);

        String condition = text(conditionLabel);
        Integer no = positiveInt(bearingNo);
        boolean bearingRoute = condition != null || no != null;
        if (bearingRoute && (condition == null || no == null))
        {
            throw new ServiceException("工况和轴承编号必须同时提供");
        }

        Path uploadRoot = bearingRoute ? datasetRoot(options) : objectNumericRoot(options);
        String sampleCondition = bearingRoute ? condition : "object_" + bind.tgtLv;
        String sampleBearing = bearingRoute ? bearingCode(condition, no) : bind.tgtId;
        Path uploadDir = bearingRoute
                ? uploadRoot.resolve(condition).resolve(sampleBearing).normalize()
                : uploadRoot.resolve(bind.tgtLv).resolve(safePathPart(bind.tgtId)).normalize();
        if (!uploadDir.startsWith(uploadRoot))
        {
            throw new ServiceException("上传目录不合法");
        }
        try
        {
            Files.createDirectories(uploadDir);
        }
        catch (Exception e)
        {
            throw new ServiceException("创建上传目录失败：" + e.getMessage());
        }

        UploadContext ctx = new UploadContext();
        ctx.options = options;
        ctx.dataUsage = dataUsage;
        ctx.uploadRoot = uploadRoot;
        ctx.uploadDir = uploadDir;
        ctx.sampleCondition = sampleCondition;
        ctx.sampleBearing = sampleBearing;
        ctx.bind = bind;
        return ctx;
    }

    private Map<String, Object> saveNumericSample(UploadContext ctx, MultipartFile file, String uploadBatchId, Integer fileIndex, Integer totalFiles)
    {
        return saveNumericSample(ctx, file, uploadBatchId, fileIndex, totalFiles, null);
    }

    private Map<String, Object> saveNumericSample(UploadContext ctx, MultipartFile file, String uploadBatchId, Integer fileIndex, Integer totalFiles, String relativePath)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("上传文件不能为空");
        }
        String fileName = relativeDataPath(relativePath, file.getOriginalFilename());
        if (!isDataFile(fileName))
        {
            throw new ServiceException("仅支持上传 CSV/TXT 文件");
        }
        Path target = null;
        try
        {
            Path dir = uploadTargetDir(ctx, uploadBatchId);
            FaultIdenSampleFile existing = sampleMapper.selectByUniqueKey(ctx.sampleCondition, ctx.sampleBearing, fileName, ctx.dataUsage);
            if (existing != null)
            {
                Map<String, Object> ret = sampleResult(ctx, existing, Paths.get(existing.getSourceFile()), uploadBatchId, "SKIPPED");
                ret.put("message", "同名文件已存在");
                return ret;
            }
            target = relativeTarget(dir, fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return saveNumericSample(ctx, target, uploadBatchId, fileIndex, totalFiles, fileName);
        }
        catch (ServiceException e)
        {
            deleteQuietly(target);
            throw e;
        }
        catch (Exception e)
        {
            deleteQuietly(target);
            throw new ServiceException("上传导入失败：" + e.getMessage());
        }
    }

    private Map<String, Object> saveNumericSample(UploadContext ctx, Path target, String uploadBatchId, Integer fileIndex, Integer totalFiles)
    {
        return saveNumericSample(ctx, target, uploadBatchId, fileIndex, totalFiles, null);
    }

    private Map<String, Object> saveNumericSample(UploadContext ctx, Path target, String uploadBatchId, Integer fileIndex, Integer totalFiles, String storedName)
    {
        try
        {
            String fileName = text(storedName) == null ? target.getFileName().toString() : storedName.replace("\\", "/");
            FaultIdenSampleFile existing = sampleMapper.selectByUniqueKey(ctx.sampleCondition, ctx.sampleBearing, fileName, ctx.dataUsage);
            if (existing != null)
            {
                Map<String, Object> ret = sampleResult(ctx, existing, Paths.get(existing.getSourceFile()), uploadBatchId, "SKIPPED");
                ret.put("message", "同名文件已存在");
                return ret;
            }

            int sampleNo = sampleNoFromFileName(fileName);
            if (sampleNo == Integer.MAX_VALUE)
            {
                sampleNo = fileIndex == null || fileIndex <= 0 ? 1 : fileIndex;
            }
            while (sampleMapper.countByConditionBearingSampleNo(ctx.sampleCondition, ctx.sampleBearing, sampleNo, ctx.dataUsage) > 0)
            {
                sampleNo++;
            }

            FaultIdenSampleFile sample = new FaultIdenSampleFile();
            sample.setConditionLabel(ctx.sampleCondition);
            sample.setBearingCode(ctx.sampleBearing);
            sample.setSampleNo(sampleNo);
            sample.setFileName(fileName);
            sample.setSourceFile(target.toAbsolutePath().normalize().toString());
            sample.setFileSize(Files.size(target));
            sample.setDataUsage(ctx.dataUsage);
            sample.setAircraftId(ctx.bind.airId);
            sample.setSubsystemId(ctx.bind.subId);
            sample.setEquipmentId(ctx.bind.eqpId);
            sample.setComponentId(ctx.bind.cmpId);
            sampleMapper.inFaultIdenSample(sample);

            Map<String, Object> ret = sampleResult(ctx, sample, target, uploadBatchId, "SUCCESS");
            ret.put("fileIndex", fileIndex);
            ret.put("totalFiles", totalFiles);
            return ret;
        }
        catch (ServiceException e)
        {
            deleteQuietly(target);
            throw e;
        }
        catch (Exception e)
        {
            deleteQuietly(target);
            throw new ServiceException("保存样本记录失败：" + e.getMessage());
        }
    }

    private Map<String, Object> sampleResult(UploadContext ctx, FaultIdenSampleFile sample, Path target, String uploadBatchId, String status)
    {
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("sampleId", sample == null ? null : sample.getId());
        ret.put("id", sample == null ? null : sample.getId());
        ret.put("fileName", target == null ? null : target.getFileName().toString());
        ret.put("filePath", target == null ? null : target.toAbsolutePath().normalize().toString());
        ret.put("sourceFile", target == null ? null : target.toAbsolutePath().normalize().toString());
        ret.put("relativePath", sample == null ? null : sample.getFileName());
        ret.put("batchPath", uploadTargetDir(ctx, uploadBatchId).toAbsolutePath().normalize().toString());
        try
        {
            ret.put("fileSize", target == null ? null : Files.size(target));
        }
        catch (Exception ignored)
        {
            ret.put("fileSize", null);
        }
        ret.put("uploadBatchId", text(uploadBatchId));
        ret.put("uploadStatus", status);
        ret.put("dataUsage", ctx.dataUsage);
        ret.put("conditionLabel", ctx.sampleCondition);
        ret.put("bearingCode", ctx.sampleBearing);
        ret.put("objectBinding", ctx.bind.toMap());
        return ret;
    }

    private Path uploadTargetDir(UploadContext ctx, String uploadBatchId)
    {
        String batch = text(uploadBatchId);
        Path dir = ctx.uploadDir;
        if (batch != null)
        {
            dir = dir.resolve(safeToken(batch, "uploadBatchId")).normalize();
        }
        if (!dir.startsWith(ctx.uploadDir))
        {
            throw new ServiceException("上传批次目录不合法");
        }
        try
        {
            Files.createDirectories(dir);
        }
        catch (Exception e)
        {
            throw new ServiceException("创建上传批次目录失败：" + e.getMessage());
        }
        return dir;
    }

    private Path chunkRoot()
    {
        String configured = props.getSourceRoot();
        String val = configured == null || configured.trim().isEmpty() ? DEFAULT_DATA_ROOT : configured;
        return Paths.get(val).toAbsolutePath().normalize()
                .resolve(FAULT_IDENTIFY_DIR)
                .resolve(CHUNK_DIR)
                .normalize();
    }

    private String safeToken(String value, String label)
    {
        String text = text(value);
        if (text == null || !text.matches("[A-Za-z0-9_-]{1,120}"))
        {
            throw new ServiceException(label + "不合法");
        }
        return text;
    }

    private int nonNegative(Integer value, String label)
    {
        if (value == null || value < 0)
        {
            throw new ServiceException(label + "必须为非负整数");
        }
        return value;
    }

    private long uploadedChunkCount(String uploadId)
    {
        try
        {
            Path dir = chunkRoot().resolve(uploadId).normalize();
            if (!dir.startsWith(chunkRoot()) || !Files.isDirectory(dir))
            {
                return 0;
            }
            try (Stream<Path> stream = Files.list(dir))
            {
                return stream.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".part")).count();
            }
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    private void deleteChunkDirectory(Path dir)
    {
        Path root = chunkRoot();
        if (dir == null)
        {
            return;
        }
        Path target = dir.toAbsolutePath().normalize();
        if (target.equals(root) || !target.startsWith(root))
        {
            throw new ServiceException("分片清理目录不合法");
        }
        if (!Files.exists(target))
        {
            return;
        }
        try (Stream<Path> stream = Files.walk(target))
        {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths)
            {
                Files.deleteIfExists(path);
            }
        }
        catch (Exception e)
        {
            throw new ServiceException("分片文件清理失败：" + e.getMessage());
        }
    }

    private void deleteTree(Path dir)
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

    private void deleteQuietly(Path file)
    {
        if (file == null)
        {
            return;
        }
        try
        {
            Files.deleteIfExists(file);
        }
        catch (Exception ignored)
        {
        }
    }

    private String cleanFileName(String fileName)
    {
        String clean = fileName == null ? "" : Paths.get(fileName).getFileName().toString().trim();
        if (clean.isEmpty() || clean.contains("/") || clean.contains("\\") || clean.contains(".."))
        {
            throw new ServiceException("文件名不合法：" + fileName);
        }
        return clean;
    }

    private String relativeDataPath(String relativePath, String fallbackName)
    {
        String raw = text(relativePath);
        if (raw == null)
        {
            return cleanFileName(fallbackName);
        }
        String normalized = raw.replace("\\", "/");
        while (normalized.startsWith("/"))
        {
            normalized = normalized.substring(1);
        }
        if (normalized.isEmpty() || normalized.contains("..") || normalized.contains(":"))
        {
            throw new ServiceException("相对路径不合法：" + relativePath);
        }
        String[] parts = normalized.split("/");
        List<String> cleaned = new ArrayList<>();
        for (String part : parts)
        {
            String value = part == null ? "" : part.trim();
            if (value.isEmpty() || ".".equals(value) || "..".equals(value))
            {
                throw new ServiceException("相对路径不合法：" + relativePath);
            }
            cleaned.add(cleanFileName(value));
        }
        String result = String.join("/", cleaned);
        if (!isDataFile(result))
        {
            throw new ServiceException("仅支持上传 CSV/TXT 文件");
        }
        return result;
    }

    private String apiFileName(String fileName)
    {
        String clean = text(fileName);
        if (clean == null)
        {
            clean = "api_numeric_" + System.currentTimeMillis() + ".csv";
        }
        if (!clean.toLowerCase().endsWith(".csv") && !clean.toLowerCase().endsWith(".txt"))
        {
            clean = clean + ".csv";
        }
        clean = cleanFileName(clean);
        if (!isDataFile(clean))
        {
            throw new ServiceException("API导入文件名仅支持CSV/TXT");
        }
        return clean;
    }

    @SuppressWarnings("unchecked")
    private void addApiHeaders(HttpRequest.Builder builder, Object headers)
    {
        if (!(headers instanceof Map<?, ?> map))
        {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            String key = text(entry.getKey());
            String value = text(entry.getValue());
            if (key == null || value == null)
            {
                continue;
            }
            String lower = key.toLowerCase();
            if ("host".equals(lower) || "content-length".equals(lower) || "connection".equals(lower))
            {
                continue;
            }
            builder.header(key, value);
        }
    }

    private void copyWithLimit(InputStream input, OutputStream output, long maxBytes) throws Exception
    {
        byte[] buffer = new byte[8192];
        long total = 0L;
        int len;
        while ((len = input.read(buffer)) >= 0)
        {
            total += len;
            if (total > maxBytes)
            {
                throw new ServiceException("API响应内容超过100MB限制");
            }
            output.write(buffer, 0, len);
        }
    }

    private Path stableTarget(Path dir, String fileName)
    {
        Path target = dir.resolve(fileName).normalize();
        if (!target.startsWith(dir))
        {
            throw new ServiceException("上传文件路径不合法：" + fileName);
        }
        return target;
    }

    private Path relativeTarget(Path dir, String relativePath) throws Exception
    {
        String safe = relativeDataPath(relativePath, relativePath);
        Path target = dir.resolve(safe).normalize();
        if (!target.startsWith(dir))
        {
            throw new ServiceException("上传文件路径不合法：" + relativePath);
        }
        if (target.getParent() != null)
        {
            Files.createDirectories(target.getParent());
        }
        return target;
    }

    private Path uniqueTarget(Path dir, String fileName)
    {
        Path target = dir.resolve(fileName).normalize();
        if (!target.startsWith(dir))
        {
            throw new ServiceException("上传文件路径不合法：" + fileName);
        }
        if (!Files.exists(target))
        {
            return target;
        }
        String base = fileName;
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot > 0)
        {
            base = fileName.substring(0, dot);
            ext = fileName.substring(dot);
        }
        for (int i = 1; i < 10000; i++)
        {
            Path next = dir.resolve(base + "_" + i + ext).normalize();
            if (!next.startsWith(dir))
            {
                throw new ServiceException("上传文件路径不合法：" + fileName);
            }
            if (!Files.exists(next))
            {
                return next;
            }
        }
        throw new ServiceException("同名文件过多：" + fileName);
    }

    private ObjBind valObjBind(String airId, String subId, String eqpId, String cmpId)
    {
        return valObjBind(airId, subId, eqpId, cmpId, null);
    }

    private ObjBind valObjBind(String airId, String subId, String eqpId, String cmpId, String ptId)
    {
        String air = optObjId(airId);
        String sub = optObjId(subId);
        String eqp = optObjId(eqpId);
        String cmp = optObjId(cmpId);
        String pt = optObjId(ptId);

        if (air == null && sub == null && eqp == null && cmp == null && pt == null)
        {
            throw new ServiceException("请选择数据归属对象");
        }
        if (sub != null && air == null)
        {
            throw new ServiceException("选择分系统时必须先选择飞机");
        }
        if (eqp != null && sub == null)
        {
            throw new ServiceException("选择设备时必须先选择分系统");
        }
        if (cmp != null && eqp == null)
        {
            throw new ServiceException("选择组件时必须先选择设备");
        }

        if (pt != null && cmp == null)
        {
            throw new ServiceException("选择零件时必须先选择组件");
        }

        if (air != null)
        {
            reqNode("aircraft", air);
        }
        if (sub != null)
        {
            reqParent("subsystem", sub, "aircraft:" + air);
        }
        if (eqp != null)
        {
            reqParent("equipment", eqp, "subsystem:" + sub);
        }
        if (cmp != null)
        {
            reqParent("component", cmp, "equipment:" + eqp);
        }
        if (pt != null)
        {
            reqParent("part_template", pt, "component:" + cmp);
        }

        ObjBind bind = new ObjBind();
        bind.airId = air;
        bind.subId = sub;
        bind.eqpId = eqp;
        bind.cmpId = cmp;
        bind.ptId = pt;
        if (pt != null)
        {
            bind.tgtLv = "part";
            bind.tgtId = pt;
        }
        else if (cmp != null)
        {
            bind.tgtLv = "component";
            bind.tgtId = cmp;
        }
        else if (eqp != null)
        {
            bind.tgtLv = "equipment";
            bind.tgtId = eqp;
        }
        else if (sub != null)
        {
            bind.tgtLv = "subsystem";
            bind.tgtId = sub;
        }
        else
        {
            bind.tgtLv = "aircraft";
            bind.tgtId = air;
        }
        return bind;
    }

    private String optObjId(String value)
    {
        String id = text(value);
        if (id == null || "none".equalsIgnoreCase(id) || "null".equalsIgnoreCase(id) || "无".equals(id))
        {
            return null;
        }
        return id;
    }

    private void reqParent(String type, String rawId, String expectedParentId)
    {
        Map<String, Object> node = reqNode(type, rawId);
        String parentId = text(first(node, "parent_id", "PARENT_ID"));
        if (!expectedParentId.equals(parentId))
        {
            throw new ServiceException("数据归属对象层级关系不正确");
        }
    }

    private Map<String, Object> reqNode(String type, String rawId)
    {
        Map<String, Object> node = monitorMapper.sel_node_by_id(type + ":" + rawId);
        if (node == null || node.isEmpty())
        {
            throw new ServiceException("数据归属对象不存在：" + rawId);
        }
        return node;
    }

    private String safePathPart(String value)
    {
        String part = text(value);
        if (part == null || part.contains("/") || part.contains("\\") || part.contains("..") || part.contains(":"))
        {
            throw new ServiceException("对象ID不合法：" + value);
        }
        return part;
    }

    private NumericImportOptions numericImportOptions(Map<String, Object> req)
    {
        NumericImportOptions options = new NumericImportOptions();
        options.purpose = text(req == null ? null : req.get("purpose"));
        options.executionObject = text(req == null ? null : first(req, "executionObject", "execution_object"));
        options.conditions = textSet(req == null ? null : req.get("conditions"));
        options.bearings = textSet(req == null ? null : req.get("bearings"));
        options.bearingNos = intSet(req == null ? null : first(req, "bearingNos", "bearingNumbers", "bearing_nos"));
        options.totalLimit = positiveInt(req == null ? null : req.get("limit"));
        options.perBearingLimit = positiveInt(req == null ? null : first(req, "perBearingLimit", "per_bearing_limit"));
        options.sampleNoStart = positiveInt(req == null ? null : first(req, "sampleNoStart", "sample_no_start"));
        options.sampleNoEnd = positiveInt(req == null ? null : first(req, "sampleNoEnd", "sample_no_end"));

        if (options.purpose == null)
        {
            options.purpose = PURPOSE_FAULT_PREDICTION;
        }
        if (options.executionObject == null)
        {
            options.executionObject = OBJECT_BEARING;
        }
        if (!PURPOSE_FAULT_PREDICTION.equals(options.purpose)
                && !PURPOSE_FAULT_IDENTIFY.equals(options.purpose)
                && !PURPOSE_FEATURE_ANALYSIS.equals(options.purpose)
                && !PURPOSE_PROC_ANOM.equals(options.purpose))
        {
            throw new ServiceException("当前仅支持特征提取/故障预测/故障识别/工序异常检测用途的数据导入");
        }
        if (!OBJECT_BEARING.equals(options.executionObject))
        {
            throw new ServiceException("当前仅支持轴承对象的数据导入");
        }

        if (options.sampleNoStart != null && options.sampleNoEnd != null
                && options.sampleNoStart > options.sampleNoEnd)
        {
            throw new ServiceException("样本编号起始值不能大于结束值");
        }
        return options;
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

    private String dataUsage(String purpose)
    {
        if (PURPOSE_FAULT_PREDICTION.equals(purpose))
        {
            return USAGE_PREDICT;
        }
        if (PURPOSE_FAULT_IDENTIFY.equals(purpose))
        {
            return USAGE_IDENTIFY;
        }
        if (PURPOSE_PROC_ANOM.equals(purpose))
        {
            return USAGE_MANUFACTURE_COMMON;
        }
        return USAGE_FEATURE;
    }

    private String sampleUsage(String value)
    {
        String usage = text(value);
        if (usage == null)
        {
            return USAGE_FEATURE;
        }
        if (USAGE_FEATURE.equals(usage) || USAGE_PREDICT.equals(usage) || USAGE_IDENTIFY.equals(usage)
                || USAGE_WARNING.equals(usage) || USAGE_KQC_MINING.equals(usage) || USAGE_KEY_PROCESS.equals(usage)
                || USAGE_MANUFACTURE_COMMON.equals(usage) || USAGE_COMMON.equals(usage))
        {
            return usage;
        }
        throw new ServiceException("数据用途不合法：" + usage);
    }

    private Object first(Map<String, Object> req, String... keys)
    {
        if (req == null || keys == null)
        {
            return null;
        }
        for (String key : keys)
        {
            if (req.containsKey(key))
            {
                return req.get(key);
            }
        }
        return null;
    }

    private Integer positiveInt(Object value)
    {
        if (value == null || String.valueOf(value).trim().isEmpty())
        {
            return null;
        }
        try
        {
            int parsed = value instanceof Number
                    ? ((Number) value).intValue()
                    : Integer.parseInt(String.valueOf(value).trim());
            return parsed > 0 ? parsed : null;
        }
        catch (Exception e)
        {
            throw new ServiceException("导入数量参数必须为正整数：" + value);
        }
    }

    private Set<String> textSet(Object value)
    {
        Set<String> set = new HashSet<>();
        if (value == null)
        {
            return set;
        }
        if (value instanceof Collection)
        {
            for (Object item : (Collection<?>) value)
            {
                addText(set, item);
            }
            return set;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty())
        {
            return set;
        }
        for (String item : text.split(","))
        {
            addText(set, item);
        }
        return set;
    }

    private Set<Integer> intSet(Object value)
    {
        Set<Integer> set = new HashSet<>();
        if (value == null)
        {
            return set;
        }
        if (value instanceof Collection)
        {
            for (Object item : (Collection<?>) value)
            {
                addInt(set, item);
            }
            return set;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty())
        {
            return set;
        }
        for (String item : text.split(","))
        {
            addInt(set, item);
        }
        return set;
    }

    private void addInt(Set<Integer> set, Object value)
    {
        Integer parsed = positiveInt(value);
        if (parsed != null)
        {
            set.add(parsed);
        }
    }

    private void addText(Set<String> set, Object value)
    {
        if (value == null)
        {
            return;
        }
        String text = String.valueOf(value).trim();
        if (!text.isEmpty())
        {
            set.add(text);
        }
    }

    private static class NumericImportOptions
    {
        private String purpose;
        private String executionObject;
        private Set<String> conditions = new HashSet<>();
        private Set<String> bearings = new HashSet<>();
        private Set<Integer> bearingNos = new HashSet<>();
        private Integer totalLimit;
        private Integer perBearingLimit;
        private Integer sampleNoStart;
        private Integer sampleNoEnd;

        private boolean acceptDir(String condition, String bearing)
        {
            return (conditions.isEmpty() || conditions.contains(condition))
                    && (bearings.isEmpty() || bearings.contains(bearing))
                    && (bearingNos.isEmpty() || bearingNos.contains(bearingNo(bearing)));
        }

        private boolean acceptSampleNo(int sampleNo)
        {
            if (sampleNoStart != null && sampleNo < sampleNoStart)
            {
                return false;
            }
            return sampleNoEnd == null || sampleNo <= sampleNoEnd;
        }

        private boolean canSelectMore(int selectedTotal, int selectedInBearing)
        {
            if (totalLimit != null && selectedTotal >= totalLimit)
            {
                return false;
            }
            return perBearingLimit == null || selectedInBearing < perBearingLimit;
        }

        private boolean totalLimitReached(int selectedTotal)
        {
            return totalLimit != null && selectedTotal >= totalLimit;
        }

        private Map<String, Object> toMap()
        {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("purpose", purpose);
            map.put("executionObject", executionObject);
            map.put("conditions", conditions);
            map.put("bearings", bearings);
            map.put("bearingNos", bearingNos);
            map.put("limit", totalLimit);
            map.put("perBearingLimit", perBearingLimit);
            map.put("sampleNoStart", sampleNoStart);
            map.put("sampleNoEnd", sampleNoEnd);
            return map;
        }

        private int bearingNo(String bearing)
        {
            if (bearing == null)
            {
                return Integer.MIN_VALUE;
            }
            int idx = bearing.lastIndexOf('_');
            String raw = idx < 0 ? bearing : bearing.substring(idx + 1);
            try
            {
                return Integer.parseInt(raw.trim());
            }
            catch (Exception e)
            {
                return Integer.MIN_VALUE;
            }
        }
    }

    private static class ObjBind
    {
        private String airId;
        private String subId;
        private String eqpId;
        private String cmpId;
        private String ptId;
        private String tgtLv;
        private String tgtId;

        private Map<String, Object> toMap()
        {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("aircraftId", airId);
            map.put("subsystemId", subId);
            map.put("equipmentId", eqpId);
            map.put("componentId", cmpId);
            map.put("partId", ptId);
            map.put("targetLevel", tgtLv);
            map.put("targetId", tgtId);
            return map;
        }
    }

    private static class UploadContext
    {
        private NumericImportOptions options;
        private String dataUsage;
        private Path uploadRoot;
        private Path uploadDir;
        private String sampleCondition;
        private String sampleBearing;
        private ObjBind bind;
    }

    @Override
    public Map<String, Object> conditions()
    {
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("data", sampleMapper.selectConditions());
        return ret;
    }

    @Override
    public Map<String, Object> bearings(String conditionLabel)
    {
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("data", sampleMapper.selectBearingsByConditionLabel(conditionLabel));
        return ret;
    }

    @Override
    public Map<String, Object> samples(
            String conditionLabel,
            String bearingCode,
            String keyword,
            String airId,
            String subId,
            String eqpId,
            String cmpId,
            String partId,
            String dataUsage,
            String uploadBatchId,
            Integer pageNum,
            Integer pageSize
    )
    {
        int page = pageNum == null || pageNum <= 0 ? 1 : pageNum;
        int size = pageSize == null || pageSize <= 0 ? 20 : pageSize;
        String usageText = text(dataUsage);
        String usage = USAGE_ALL.equalsIgnoreCase(String.valueOf(usageText)) ? null : sampleUsage(usageText);
        Map<String, Object> ret = new LinkedHashMap<>();
        boolean objectQuery = text(airId) != null || text(subId) != null
                || text(eqpId) != null || text(cmpId) != null || text(partId) != null;
        if (objectQuery)
        {
            ObjBind bind = valObjBind(airId, subId, eqpId, cmpId, partId);
            ret.put("rows", sampleMapper.selectSamplesByObject(
                    bind.tgtLv,
                    bind.tgtId,
                    bind.airId,
                    bind.subId,
                    bind.eqpId,
                    bind.cmpId,
                    usage,
                    uploadBatchId,
                    keyword,
                    (page - 1) * size,
                    size
            ));
            ret.put("total", sampleMapper.countSamplesByObject(
                    bind.tgtLv,
                    bind.tgtId,
                    bind.airId,
                    bind.subId,
                    bind.eqpId,
                    bind.cmpId,
                    usage,
                    uploadBatchId,
                    keyword
            ));
            return ret;
        }

        String condition = text(conditionLabel);
        String bearing = text(bearingCode);
        if (condition == null || bearing == null)
        {
            ret.put("rows", sampleMapper.selectAllSamples(usage, uploadBatchId, keyword, (page - 1) * size, size));
            ret.put("total", sampleMapper.countAllSamples(usage, uploadBatchId, keyword));
            return ret;
        }
        ret.put("rows", sampleMapper.selectSamplesByConditionAndBearing(condition, bearing, usage, uploadBatchId, keyword, (page - 1) * size, size));
        ret.put("total", sampleMapper.countSamplesByConditionAndBearing(condition, bearing, usage, uploadBatchId, keyword));
        return ret;
    }

    @Override
    public Map<String, Object> deleteSample(Long sampleId)
    {
        if (sampleId == null)
        {
            throw new ServiceException("数据文件ID不能为空");
        }
        FaultIdenSampleFile sample = sampleMapper.seFaultIdenSampleById(sampleId);
        if (sample == null)
        {
            throw new ServiceException("数据文件不存在");
        }
        String configuredRoot = props.getSourceRoot();
        String rootText = configuredRoot == null || configuredRoot.trim().isEmpty() ? DEFAULT_DATA_ROOT : configuredRoot;
        String fileText = text(sample.getSourceFile());
        if (fileText == null)
        {
            throw new ServiceException("数据文件路径不完整，不能删除");
        }

        Path root = Paths.get(rootText).toAbsolutePath().normalize();
        Path file = Paths.get(fileText).toAbsolutePath().normalize();
        if (!file.startsWith(root))
        {
            throw new ServiceException("数据文件不在允许删除的目录下");
        }

        try
        {
            Files.deleteIfExists(file);
            sampleMapper.deleteSampleById(sampleId);
        }
        catch (Exception e)
        {
            throw new ServiceException("删除数据文件失败：" + e.getMessage());
        }

        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("id", sampleId);
        ret.put("fileName", sample.getFileName());
        return ret;
    }

    @Override
    public Map<String, Object> updateSampleDataUsage(Long sampleId, String dataUsage)
    {
        if (sampleId == null)
        {
            throw new ServiceException("数据文件ID不能为空");
        }
        String usage = text(dataUsage);
        if (!USAGE_MANUFACTURE_COMMON.equals(usage) && !USAGE_WARNING.equals(usage)
                && !USAGE_KQC_MINING.equals(usage) && !USAGE_KEY_PROCESS.equals(usage))
        {
            throw new ServiceException("制造周期数据用途只能设置为制造通用数据、工序异常检测、关键质量特性挖掘或关键工序识别");
        }
        FaultIdenSampleFile sample = sampleMapper.seFaultIdenSampleById(sampleId);
        if (sample == null)
        {
            throw new ServiceException("数据文件不存在");
        }
        String oldUsage = text(sample.getDataUsage());
        if (!USAGE_MANUFACTURE_COMMON.equals(oldUsage) && !USAGE_WARNING.equals(oldUsage)
                && !USAGE_KQC_MINING.equals(oldUsage) && !USAGE_KEY_PROCESS.equals(oldUsage))
        {
            throw new ServiceException("仅支持修改制造周期数据用途");
        }
        int rows = sampleMapper.updateSampleDataUsage(sampleId, usage);
        if (rows <= 0)
        {
            throw new ServiceException("数据用途未更新");
        }
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("id", sampleId);
        ret.put("dataUsage", usage);
        return ret;
    }

    private int scanCondition(Path condDir) throws Exception
    {
        int count = 0;
        String condition = condDir.getFileName().toString();
        try (Stream<Path> bearings = Files.list(condDir))
        {
            List<Path> bearingDirs = bearings.filter(Files::isDirectory)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
            for (Path bearingDir : bearingDirs)
            {
                count += scanBearing(condition, bearingDir);
            }
        }
        return count;
    }

    private int scanBearing(String condition, Path bearingDir) throws Exception
    {
        int count = 0;
        String bearing = bearingDir.getFileName().toString();
        try (Stream<Path> files = Files.list(bearingDir))
        {
            List<Path> dataFiles = files.filter(Files::isRegularFile)
                    .filter(p -> isDataFile(p.getFileName().toString()))
                    .sorted(Comparator.comparingInt(p -> sampleNo(p.getFileName().toString())))
                    .toList();
            for (Path file : dataFiles)
            {
                int no = sampleNo(file.getFileName().toString());
                if (no == Integer.MAX_VALUE || sampleMapper.countByConditionBearingSampleNo(condition, bearing, no, USAGE_FEATURE) > 0)
                {
                    continue;
                }
                FaultIdenSampleFile sample = new FaultIdenSampleFile();
                sample.setConditionLabel(condition);
                sample.setBearingCode(bearing);
                sample.setSampleNo(no);
                sample.setFileName(file.getFileName().toString());
                sample.setSourceFile(file.toAbsolutePath().normalize().toString());
                sample.setFileSize(Files.size(file));
                sample.setDataUsage(USAGE_FEATURE);
                sampleMapper.inFaultIdenSample(sample);
                count++;
            }
        }
        return count;
    }

    private Path datasetRoot(NumericImportOptions options)
    {
        String configured = props.getSourceRoot();
        String val = configured == null || configured.trim().isEmpty() ? DEFAULT_DATA_ROOT : configured;
        Path root = Paths.get(val).toAbsolutePath().normalize();

        if (OBJECT_BEARING.equals(options.executionObject))
        {
            return root.resolve(FAULT_IDENTIFY_DIR).resolve(BEARING_DATASET_DIR).normalize();
        }
        return root;
    }

    private Path objectNumericRoot(NumericImportOptions options)
    {
        String configured = props.getSourceRoot();
        String val = configured == null || configured.trim().isEmpty() ? DEFAULT_DATA_ROOT : configured;
        return Paths.get(val).toAbsolutePath().normalize()
                .resolve(FAULT_IDENTIFY_DIR)
                .resolve("ObjectNumericData")
                .normalize();
    }

    private boolean isDataFile(String fileName)
    {
        if (fileName == null)
        {
            return false;
        }
        String name = fileName.toLowerCase();
        return name.endsWith(".csv") || name.endsWith(".txt");
    }

    private int sampleNoFromFileName(String name)
    {
        if (name == null)
        {
            return Integer.MAX_VALUE;
        }
        Matcher matcher = FIRST_NUMBER.matcher(name);
        if (!matcher.find())
        {
            return Integer.MAX_VALUE;
        }
        try
        {
            return Integer.parseInt(matcher.group(1));
        }
        catch (Exception e)
        {
            return Integer.MAX_VALUE;
        }
    }

    private Path root(String rootPath)
    {
        Path root;
        if (rootPath == null || rootPath.trim().isEmpty())
        {
            NumericImportOptions options = new NumericImportOptions();
            options.purpose = PURPOSE_FAULT_PREDICTION;
            options.executionObject = OBJECT_BEARING;
            root = datasetRoot(options);
        }
        else
        {
            root = Paths.get(rootPath).toAbsolutePath().normalize();
        }
        if (root == null)
        {
            throw new ServiceException("缺少数据source-root配置");
        }
        if (!Files.isDirectory(root))
        {
            throw new ServiceException("原始数据目录不存在：" + root);
        }
        return root;
    }

    private int sampleNo(String name)
    {
        try
        {
            String text = name.replaceFirst("(?i)\\.(csv|txt)$", "");
            return Integer.parseInt(text.trim());
        }
        catch (Exception e)
        {
            return Integer.MAX_VALUE;
        }
    }
}
