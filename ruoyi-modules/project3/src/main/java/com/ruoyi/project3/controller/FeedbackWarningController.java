package com.ruoyi.project3.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.file.FileUtils;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.utils.file.FileTypeUtils;
import com.ruoyi.common.core.utils.file.MimeTypeUtils;
import com.ruoyi.project3.service.FeedbackWarningService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

@RestController
@RequestMapping("/feedback/warning")
public class FeedbackWarningController {

    private final FeedbackWarningService warningService;
    @Value("${project3.profile:${user.dir}/uploadPath}")
    private String uploadRoot;

    public FeedbackWarningController(FeedbackWarningService warningService) {
        this.warningService = warningService;
    }

    @GetMapping("/parts")
    public AjaxResult parts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "risk_level", required = false) String riskLevel,
            @RequestParam(value = "sort_by", required = false, defaultValue = "part_code") String sortBy,
            @RequestParam(value = "sort_order", required = false, defaultValue = "asc") String sortOrder,
            @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize
    ) {
        return AjaxResult.success(warningService.partPage(keyword, riskLevel, sortBy, sortOrder, pageNum, pageSize));
    }

    @GetMapping("/parts/{part_id}/processes")
    public AjaxResult partProc(@PathVariable("part_id") String partId) {
        return AjaxResult.success(warningService.partProc(partId));
    }

    @GetMapping("/parts/{part_template_id}/instances")
    public AjaxResult partInstances(
            @PathVariable("part_template_id") String partTemplateId,
            @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize
    ) {
        return AjaxResult.success(warningService.partInstances(partTemplateId, pageNum, pageSize));
    }

    @PostMapping("/parts/{part_id}/image")
    public AjaxResult uploadPartImage(
            @PathVariable("part_id") String partId,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("上传图片不能为空");
        }

        String oldImage = warningService.partImage(partId);
        String imageUrl = uploadImage(file);
        try {
            warningService.updatePartImage(partId, imageUrl);
        } catch (Exception e) {
            deleteImage(imageUrl);
            throw e;
        }
        if (StringUtils.isNotEmpty(oldImage) && !oldImage.equals(imageUrl)) {
            deleteImage(oldImage);
        }

        AjaxResult ajax = AjaxResult.success();
        ajax.put("image_url", imageUrl);
        ajax.put("imageUrl", imageUrl);
        ajax.put("url", imageUrl);
        return ajax;
    }

    @DeleteMapping("/parts/{part_id}/image")
    public AjaxResult deletePartImage(@PathVariable("part_id") String partId) {
        String oldImage = warningService.partImage(partId);
        warningService.updatePartImage(partId, null);
        if (StringUtils.isNotEmpty(oldImage)) {
            deleteImage(oldImage);
        }
        return AjaxResult.success();
    }

    @GetMapping("/devices")
    public AjaxResult devs(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "risk_level", required = false) String riskLevel,
            @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize
    ) {
        return AjaxResult.success(warningService.devPage(keyword, riskLevel, pageNum, pageSize));
    }

    @GetMapping("/devices/{device_id}/parts")
    public AjaxResult devPart(@PathVariable("device_id") String deviceId) {
        return AjaxResult.success(warningService.devPart(deviceId));
    }

    @PostMapping("/detect/tasks")
    public AjaxResult startDetect(@RequestBody(required = false) String requestBody) {
        return AjaxResult.success(warningService.startDetect(requestBody));
    }

    @GetMapping("/detect/tasks/{task_id}/status")
    public AjaxResult detectStatus(@PathVariable("task_id") String taskId) {
        return AjaxResult.success(warningService.detectStatus(taskId));
    }

    @GetMapping("/detect/tasks/{task_id}/logs")
    public AjaxResult detectLogs(@PathVariable("task_id") String taskId) {
        return AjaxResult.success(warningService.detectLogs(taskId));
    }

    @GetMapping("/detect/results")
    public AjaxResult detectResults(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "target_type", required = false) String targetType,
            @RequestParam(value = "target_id", required = false) String targetId,
            @RequestParam(value = "begin_time", required = false) String beginTime,
            @RequestParam(value = "end_time", required = false) String endTime,
            @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize
    ) {
        return AjaxResult.success(warningService.detectResults(status, targetType, targetId, beginTime, endTime, pageNum, pageSize));
    }

    private String uploadImage(MultipartFile file) throws Exception {
        String extension = FileTypeUtils.getExtension(file);
        if (!Arrays.asList(MimeTypeUtils.IMAGE_EXTENSION).contains(extension)) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
        String originalName = file.getOriginalFilename();
        String baseName = originalName == null ? "image" : Paths.get(originalName).getFileName().toString();
        String fileName = System.currentTimeMillis() + "_" + baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path dir = Paths.get(uploadRoot, "project3", "warning").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path target = dir.resolve(fileName).normalize();
        if (!target.startsWith(dir)) {
            throw new IllegalArgumentException("Invalid file name");
        }
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return Constants.RESOURCE_PREFIX + "/project3/warning/" + fileName;
    }

    private void deleteImage(String imageUrl) {
        String prefix = Constants.RESOURCE_PREFIX + "/";
        if (StringUtils.isEmpty(imageUrl) || !imageUrl.startsWith(prefix)) {
            return;
        }
        String relative = imageUrl.substring(prefix.length()).replace('/', java.io.File.separatorChar);
        Path root = Paths.get(uploadRoot).toAbsolutePath().normalize();
        Path target = root.resolve(relative).normalize();
        if (target.startsWith(root)) {
            FileUtils.deleteFile(target.toString());
        }
    }
}
