package com.ruoyi.project4.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DiagnoseReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("data_root_path")
    private String dataRootPath;

    @JsonProperty("key_num")
    private Integer keyNum;

    @JsonProperty("win_length")
    private Integer winLength = 1024;

    private Boolean denoise = true;

    @JsonProperty("denoise_mode")
    private String denoiseMode = "gaussian";

    private Boolean normalize = true;

    @JsonProperty("normalize_mode")
    private String normalizeMode = "zscore";

    /** DE权重 */
    @JsonProperty("w_x1")
    private Double wx1 = 0.5;

    /** FE权重 */
    @JsonProperty("w_x2")
    private Double wx2 = 0.5;
}