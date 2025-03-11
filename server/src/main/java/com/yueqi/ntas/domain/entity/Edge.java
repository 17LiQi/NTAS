package com.yueqi.ntas.domain.entity;

import com.yueqi.ntas.domain.base.BaseRoute;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Edge extends BaseRoute {
    private String fromCity;
    private String toCity;
    private int toIndex;        // 目的城市索引
    private Edge next;          // 下一条边

    // 添加构造函数
    public Edge(int toIndex, String type, String routeNo, 
            String departure, String arrival, double fare, Edge next) {
        this.toIndex = toIndex;
        this.type = type;
        this.routeNo = routeNo;
        this.departure = departure;
        this.arrival = arrival;
        this.fare = fare;
        this.next = next;
    }
    
    // 计算相关的方法
    public int calculateDuration() {
        int startMinutes = timeToMinutes(getDeparture());
        int endMinutes = timeToMinutes(getArrival());
        return endMinutes < startMinutes ? 
            (24 * 60 - startMinutes) + endMinutes : 
            endMinutes - startMinutes;
    }

    public int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public int getTotalDuration(int currentTime, boolean isStartingPoint) {
        int departureMinutes = timeToMinutes(getDeparture());
        if (isStartingPoint || currentTime <= 0) {
            return calculateDuration();
        }
        
        int waitTime = currentTime > departureMinutes ?
            24 * 60 - currentTime + departureMinutes :
            departureMinutes - currentTime;
            
        return waitTime + calculateDuration();
    }
} 