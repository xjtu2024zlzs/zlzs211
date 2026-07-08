package com.ruoyi.project4.domain.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * Python FastAPI 统一返回体
 */
@Data
public class PythonApiResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String msg;
    private T data;
}