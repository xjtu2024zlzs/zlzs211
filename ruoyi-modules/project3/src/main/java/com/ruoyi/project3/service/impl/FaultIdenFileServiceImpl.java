package com.ruoyi.project3.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.config.faultiden.FaultIdenFileProps;
import com.ruoyi.project3.domain.faultiden.FaultIdenFilePackage;
import com.ruoyi.project3.domain.faultiden.FaultIdenSampleFile;
import com.ruoyi.project3.mapper.FaultIdenFilePackageMapper;
import com.ruoyi.project3.mapper.FaultIdenSampleMapper;
import com.ruoyi.project3.service.FaultIdenFileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FaultIdenFileServiceImpl implements FaultIdenFileService
{
    private static final String RAW_SINGLE = "RAW_SINGLE";
    private static final String RAW_MULTI = "RAW_MULTI";
    private static final String RAW_ZIP = "RAW_ZIP";
    private static final String USAGE_FEATURE = "FEATURE_ANALYSIS";
    private static final String USAGE_PROCESS_ANOMALY = "PROCESS_ANOMALY";
    private static final String USAGE_KEY_PROCESS = "KEY_PROCESS";
    private static final String USAGE_MANUFACTURE_COMMON = "MANUFACTURE_COMMON";

    @Resource
    private FaultIdenFileProps props;

    @Resource
    private FaultIdenSampleMapper sampleMapper;

    @Resource
    private FaultIdenFilePackageMapper packageMapper;

    @Override
    public FaultIdenFilePackage prepare(String taskId, List<Long> sampleIds)
    {
        return prepare(taskId, sampleIds, USAGE_FEATURE);
    }

    @Override
    public FaultIdenFilePackage prepare(String taskId, List<Long> sampleIds, String dataUsage)
    {
        List<Long> ids = ids(sampleIds);
        String usage = usage(dataUsage);
        if (ids.isEmpty())
        {
            throw new ServiceException("请选择至少一个CSV/TXT文件");
        }
        List<FaultIdenSampleFile> samples = sampleMapper.selectSamplesByIds(ids, null);
        if (samples.size() != ids.size())
        {
            throw new ServiceException("部分 sampleIds 不存在或不属于当前算法用途");
        }
        for (FaultIdenSampleFile sample : samples)
        {
            if (!matchesDataUsage(usage, sample.getDataUsage()))
            {
                throw new ServiceException("sampleIds dataUsage invalid");
            }
            checkSource(sample.getSourceFile());
        }

        FaultIdenFilePackage pack = samples.size() == 1
                ? single(taskId, samples.get(0), ids, usage)
                : multi(taskId, samples, ids, usage);
        packageMapper.inFaultIdenFilePackage(pack);
        return pack;
    }

    private boolean matchesDataUsage(String taskUsage, String sampleUsage)
    {
        if (taskUsage == null || taskUsage.equals(sampleUsage))
        {
            return true;
        }
        return (USAGE_PROCESS_ANOMALY.equals(taskUsage) || USAGE_KEY_PROCESS.equals(taskUsage))
                && USAGE_MANUFACTURE_COMMON.equals(sampleUsage);
    }

    @Override
    public FaultIdenSampleFile sample(Long sampleId)
    {
        FaultIdenSampleFile sample = sampleMapper.seFaultIdenSampleById(sampleId);
        if (sample == null)
        {
            throw new ServiceException("数据文件不存在");
        }
        return sample;
    }

    @Override
    public Path sourceFile(Long sampleId)
    {
        return checkSource(sample(sampleId).getSourceFile());
    }

    @Override
    public Path exportFile(String fileName)
    {
        if (fileName == null || fileName.contains("/") || fileName.contains("\\") || fileName.contains(".."))
        {
            throw new ServiceException("文件名不合法");
        }
        Path root = exportRoot();
        Path file = root.resolve(fileName).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file))
        {
            throw new ServiceException("导出文件不存在");
        }
        return file;
    }

    private FaultIdenFilePackage single(String taskId, FaultIdenSampleFile sample, List<Long> ids, String usage)
    {
        FaultIdenFilePackage pack = new FaultIdenFilePackage();
        pack.setTaskId(taskId);
        pack.setFileMode(RAW_SINGLE);
        pack.setFileType(fileType(sample.getFileName()));
        pack.setFileName(sample.getFileName());
        pack.setFilePath(checkSource(sample.getSourceFile()).toString());
        pack.setFileUrl(prefix(props.getSourceFileUrlPrefix()) + "/" + sample.getId());
        pack.setSelectedSampleIds(JSON.toJSONString(ids));
        pack.setDataUsage(usage);
        return pack;
    }

    private FaultIdenFilePackage multi(String taskId, List<FaultIdenSampleFile> samples, List<Long> ids, String usage)
    {
        FaultIdenFilePackage pack = new FaultIdenFilePackage();
        pack.setTaskId(taskId);
        pack.setFileMode(RAW_MULTI);
        pack.setFileType("multi");
        pack.setFileName(samples.size() + "_selected_raw_files");
        pack.setFilePath(commonDir(samples));
        pack.setFileUrl(prefix(props.getSourceFileUrlPrefix()) + "?ids=" + JSON.toJSONString(ids));
        pack.setSelectedSampleIds(JSON.toJSONString(ids));
        pack.setDataUsage(usage);
        return pack;
    }

    private String commonDir(List<FaultIdenSampleFile> samples)
    {
        Path common = null;
        for (FaultIdenSampleFile sample : samples)
        {
            Path parent = checkSource(sample.getSourceFile()).getParent();
            if (parent == null)
            {
                continue;
            }
            if (common == null)
            {
                common = parent;
                continue;
            }
            while (common != null && !parent.startsWith(common))
            {
                common = common.getParent();
            }
        }
        return common == null ? sourceRoot().toString() : common.toString();
    }

    private FaultIdenFilePackage zip(String taskId, List<FaultIdenSampleFile> samples, List<Long> ids, String usage)
    {
        try
        {
            Path dir = exportRoot();
            Files.createDirectories(dir);
            String name = taskId + "_" + samples.get(0).getBearingCode() + "_selected_raw.zip";
            Path zip = dir.resolve(name).normalize();
            if (!zip.startsWith(dir))
            {
                throw new ServiceException("ZIP 路径不合法");
            }
            try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip)))
            {
                for (FaultIdenSampleFile sample : samples)
                {
                    Path src = checkSource(sample.getSourceFile());
                    out.putNextEntry(new ZipEntry(sample.getSampleNo() + "_" + sample.getFileName()));
                    try (InputStream in = Files.newInputStream(src))
                    {
                        in.transferTo(out);
                    }
                    out.closeEntry();
                }
            }

            FaultIdenFilePackage pack = new FaultIdenFilePackage();
            pack.setTaskId(taskId);
            pack.setFileMode(RAW_ZIP);
            pack.setFileType("zip");
            pack.setFileName(name);
            pack.setFilePath(zip.toString());
            pack.setFileUrl(prefix(props.getExportFileUrlPrefix()) + "/" + name);
            pack.setSelectedSampleIds(JSON.toJSONString(ids));
            pack.setDataUsage(usage);
            return pack;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("原始数据打包失败：" + e.getMessage());
        }
    }

    private void sameBearing(List<FaultIdenSampleFile> samples)
    {
        String condition = samples.get(0).getConditionLabel();
        String bearing = samples.get(0).getBearingCode();
        for (FaultIdenSampleFile sample : samples)
        {
            if (!condition.equals(sample.getConditionLabel()) || !bearing.equals(sample.getBearingCode()))
            {
                throw new ServiceException("请选择同一工况、同一 Bearing 下的 CSV/TXT 文件");
            }
        }
    }

    private List<Long> ids(List<Long> sampleIds)
    {
        Set<Long> set = new LinkedHashSet<>();
        if (sampleIds != null)
        {
            for (Long id : sampleIds)
            {
                if (id != null)
                {
                    set.add(id);
                }
            }
        }
        return new ArrayList<>(set);
    }

    private Path checkSource(String file)
    {
        try
        {
            Path root = sourceRoot();
            Path src = Paths.get(file).toAbsolutePath().normalize();
            if (!src.startsWith(root) || !Files.isRegularFile(src))
            {
                throw new ServiceException("原始数据文件不在 source-root 下");
            }
            return src;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("原始数据文件不可读：" + e.getMessage());
        }
    }

    private Path sourceRoot()
    {
        String root = props.getSourceRoot();
        if (root == null || root.trim().isEmpty())
        {
            throw new ServiceException("缺少数据source-root 配置");
        }
        return Paths.get(root).toAbsolutePath().normalize();
    }

    private Path exportRoot()
    {
        String dir = props.getExportDir();
        if (dir == null || dir.trim().isEmpty())
        {
            throw new ServiceException("缺少数据export-dir 配置");
        }
        Path source = sourceRoot();
        Path export = Paths.get(dir).toAbsolutePath().normalize();
        if (export.equals(source) || export.startsWith(source))
        {
            throw new ServiceException("export-dir 不能和 source-root 相同，也不能放在 source-root 目录下");
        }
        return export;
    }

    private String prefix(String prefix)
    {
        if (prefix == null || prefix.trim().isEmpty())
        {
            throw new ServiceException("缺少数据文件URL前缀配置");
        }
        return prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
    }

    private String fileType(String fileName)
    {
        String name = fileName == null ? "" : fileName.toLowerCase();
        int dot = name.lastIndexOf('.');
        return dot >= 0 && dot < name.length() - 1 ? name.substring(dot + 1) : "csv";
    }

    private String usage(String value)
    {
        String text = value == null ? null : value.trim();
        return text == null || text.isEmpty() ? USAGE_FEATURE : text;
    }
}
