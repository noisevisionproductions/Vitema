package com.noisevisionsoftware.nutrilog.dto.response.diet;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DietInfo {

    private boolean hasDiet;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
