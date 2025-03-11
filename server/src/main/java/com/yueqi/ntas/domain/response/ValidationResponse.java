package com.yueqi.ntas.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {
    private boolean valid;
    private List<String> errors;
} 