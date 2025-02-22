package com.noisevisionsoftware.nutrilog.model.diet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DietMetadata {
    private int totalDays;
    private String fileName;
    private String fileUrl;
}