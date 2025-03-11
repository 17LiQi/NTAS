package com.yueqi.ntas.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    private Integer id;
    
    // 修改字段名以匹配数据库列名
    private Integer fromCityId;  // from_city_id
    private Integer toCityId;    // to_city_id
    private String transportType; // transport_type
    private String routeNo;      // route_no
    private LocalTime departure;
    private LocalTime arrival;
    private BigDecimal fare;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 添加 toString 方法以便调试
    @Override
    public String toString() {
        return String.format("Route(id=%d, from=%d, to=%d, type=%s, no=%s)", 
            id, fromCityId, toCityId, transportType, routeNo);
    }
} 