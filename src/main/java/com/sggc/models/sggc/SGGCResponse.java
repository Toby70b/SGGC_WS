package com.sggc.models.sggc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SGGCResponse {
    private boolean success;
    private Object body;
}
