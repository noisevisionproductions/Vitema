package com.noisevisionsoftware.nutrilog.dto.response.diet;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DayResponse {
    private LocalDateTime date;
    private List<DayMealResponse> meals;
}
