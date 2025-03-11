package com.yueqi.ntas.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vertex {
    private String name;    // 城市名称
    private Edge firstEdge; // 指向第一条边
} 