package com.ruoyi.designtask1.service;

import java.util.List;
import com.ruoyi.designtask1.domain.DesignTaskFile;

public interface IDesignTaskFileService {

    List<DesignTaskFile> selectFileList(DesignTaskFile file);

    DesignTaskFile selectFileById(Long fileId);

    List<DesignTaskFile> selectFilesByTaskId(Long taskId);

    int insertFile(DesignTaskFile file);

    int deleteFileById(Long fileId);

    int deleteFilesByTaskId(Long taskId);
}