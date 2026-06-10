package com.ruoyi.designtask1.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.designtask1.mapper.DesignTaskFileMapper;
import com.ruoyi.designtask1.domain.DesignTaskFile;
import com.ruoyi.designtask1.service.IDesignTaskFileService;

@Service
public class DesignTaskFileServiceImpl implements IDesignTaskFileService {

    @Autowired
    private DesignTaskFileMapper fileMapper;

    @Override
    public List<DesignTaskFile> selectFileList(DesignTaskFile file) {
        return fileMapper.selectFileList(file);
    }

    @Override
    public DesignTaskFile selectFileById(Long fileId) {
        return fileMapper.selectFileById(fileId);
    }

    @Override
    public List<DesignTaskFile> selectFilesByTaskId(Long taskId) {
        return fileMapper.selectFilesByTaskId(taskId);
    }

    @Override
    @Transactional
    public int insertFile(DesignTaskFile file) {
        return fileMapper.insertFile(file);
    }

    @Override
    @Transactional
    public int deleteFileById(Long fileId) {
        return fileMapper.deleteFileById(fileId);
    }

    @Override
    @Transactional
    public int deleteFilesByTaskId(Long taskId) {
        return fileMapper.deleteFilesByTaskId(taskId);
    }
}