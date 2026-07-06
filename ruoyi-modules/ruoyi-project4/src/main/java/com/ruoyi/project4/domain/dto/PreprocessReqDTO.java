package com.ruoyi.project4.domain.dto;

import lombok.Data;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class PreprocessReqDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("data_root_path")
    private String dataRootPath;

    /** 数据编号 */
    @JsonProperty("key_num")
    private Integer keyNum;

    /** 滑窗长度 默认1024 */
    @JsonProperty("win_length")
    private Integer winLength = 1024;

    /** 是否去噪 */
    private Boolean denoise = true;

    /** 去噪模式 gaussian */
    @JsonProperty("denoise_mode")
    private String denoiseMode = "gaussian";

    /** 是否归一化 */
    private Boolean normalize = true;

    /** 归一化模式 zscore */
    @JsonProperty("normalize_mode")
    private String normalizeMode = "zscore";
}