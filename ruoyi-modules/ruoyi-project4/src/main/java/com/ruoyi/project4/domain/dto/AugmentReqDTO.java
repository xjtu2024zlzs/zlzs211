package com.ruoyi.project4.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class AugmentReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 上游预处理完整结果 */
    @JsonProperty("preprocess_result")
    private Map<String, Object> preprocessResult;

    /** 增强模式 */
    @JsonProperty("aug_model")
    private String augModel = "scale";

    /** 增强倍数 */
    @JsonProperty("aug_scale")
    private Integer augScale = 2;
}