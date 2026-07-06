package com.ruoyi.project4.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class FeatureFusionReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 上游预处理完整结果 */
    @JsonProperty("preprocess_result")
    private Map<String, Object> preprocessResult;

    /** DE模态权重 */
    @JsonProperty("w_x1")
    private Double wx1 = 0.5;

    /** FE模态权重 */
    @JsonProperty("w_x2")
    private Double wx2 = 0.5;
}