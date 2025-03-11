package com.yueqi.ntas.domain.validation;

import lombok.Data;
import java.util.List;

@Data
public class ValidateCityResponse {
    private boolean valid;           // 验证是否通过
    private List<String> errors;     // 验证错误信息列表
    private String formattedName;    // 格式化后的城市名称
} 