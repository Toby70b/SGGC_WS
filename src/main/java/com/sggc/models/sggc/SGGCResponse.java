package com.sggc.models.sggc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SGGCResponse {
    private boolean success;
    private Object body;
}
