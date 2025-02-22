package com.noisevisionsoftware.nutrilog.dto.response.diet;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DietResponse {
    private String id;
    private String userId;
    private String userEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DayResponse> days;
    private DietMetadataResponse metadata;
}