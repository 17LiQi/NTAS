package com.yueqi.ntas.domain.base;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class BaseRoute {
    protected String fromCity;    // 出发城市
    protected String toCity;      // 目的城市
    protected String type;        // 交通类型
    protected String routeNo;     // 路线编号
    protected String departure;   // 出发时间
    protected String arrival;     // 到达时间
    protected double fare;        // 费用
} 