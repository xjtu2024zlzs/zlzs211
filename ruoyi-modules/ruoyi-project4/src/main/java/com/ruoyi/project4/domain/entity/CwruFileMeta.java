package com.ruoyi.project4.domain.entity;

import lombok.Data;

@Data
public class CwruFileMeta {
    private Long id;
    private String fileName;
    private String fileAbsPath;
    private Integer sampleRate;
    private Integer loadHp;
    private String dataDir;
}