package com.yueqi.ntas.domain.dto;

import com.yueqi.ntas.domain.base.BaseRoute;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RouteDTO extends BaseRoute {
    private String formattedFare;  // 格式化后的费用展示
} 