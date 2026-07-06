package com.ruoyi.project4.domain.mapper;

import com.ruoyi.project4.domain.entity.CwruFileMeta;
import java.util.List;

public interface CwruFileMetaMapper {

    List<CwruFileMeta> selectCwruFileMetaList();

    CwruFileMeta selectByFileName(String fileName);
}