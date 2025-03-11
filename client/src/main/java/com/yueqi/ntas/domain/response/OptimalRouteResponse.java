package com.yueqi.ntas.domain.response;

import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.domain.dto.RouteDisplayDTO;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimalRouteResponse {
    private List<RouteDisplayDTO> routes;  // 路线列表
    private double totalFare;           // 总费用
    private int totalMinutes;           // 总耗时（分钟）
    private String totalTime;           // 总耗时（格式化：x小时y分钟）
    private int transferCount;          // 中转次数
    private int totalWaitMinutes;       // 总等待时间（分钟）
    private String totalWaitTime;       // 总等待时间（格式化）
    private String routeSummary;        // 路线概要（如：上海->杭州->北京）
    private String routePath;           // 路线路径
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferInfo {
        private String city;
        private String waitTime;
    }
    private List<TransferInfo> transfers;
}