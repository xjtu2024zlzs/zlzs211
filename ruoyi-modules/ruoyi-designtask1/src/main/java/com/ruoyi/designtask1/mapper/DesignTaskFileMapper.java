package com.ruoyi.designtask1.mapper;

import java.util.List;
import com.ruoyi.designtask1.domain.DesignTaskFile;

public interface DesignTaskFileMapper {

    List<DesignTaskFile> selectFileList(DesignTaskFile file);

    DesignTaskFile selectFileById(Long fileId);

    List<DesignTaskFile> selectFilesByTaskId(Long taskId);

    int insertFile(DesignTaskFile file);

    int deleteFileById(Long fileId);

    int deleteFilesByTaskId(Long taskId);
}