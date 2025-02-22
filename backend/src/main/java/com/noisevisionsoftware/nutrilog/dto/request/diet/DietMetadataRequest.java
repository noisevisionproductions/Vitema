package com.noisevisionsoftware.nutrilog.dto.request.diet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DietMetadataRequest {
    private int totalDays;
    private String fileName;
    private String fileUrl;
}