package com.yueqi.ntas.domain.dto;

import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.util.TimeUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteDisplayDTO {
    private String fromCity;
    private String toCity;
    private String type;
    private String routeNo;
    private String departure;
    private String arrival;
    private double fare;
    private String formattedFare;
    private String duration;

    public RouteDisplayDTO(Edge edge) {
        this.fromCity = edge.getFromCity();
        this.toCity = edge.getToCity();
        this.type = edge.getType();
        this.routeNo = edge.getRouteNo();
        this.departure = edge.getDeparture();
        this.arrival = edge.getArrival();
        this.fare = edge.getFare();
        this.formattedFare = String.format("%.1f 元", edge.getFare());
        this.duration = TimeUtils.formatDuration(
            TimeUtils.calculateTimeDifference(edge.getDeparture(), edge.getArrival())
        );
    }
} 