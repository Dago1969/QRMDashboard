package com.qtm.dashboard.asl.dto;

import lombok.Data;

import java.util.List;

@Data
public class ASLImportRequest {
    private List<Long> sourceIds;
}
