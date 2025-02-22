package com.noisevisionsoftware.nutrilog.dto.request.diet;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DayRequest {
    private String date;
    private List<DayMealRequest> meals;
}