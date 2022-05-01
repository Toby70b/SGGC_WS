package com.sggc.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SGGCResponse {
    private boolean success;
    private Object body;
}
