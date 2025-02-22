package com.noisevisionsoftware.nutrilog.dto.request.diet;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DietRequest {
    private String userId;
    private List<DayRequest> days;
    private DietMetadataRequest metadata;
}