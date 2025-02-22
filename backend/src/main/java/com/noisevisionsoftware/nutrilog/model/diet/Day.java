package com.noisevisionsoftware.nutrilog.model.diet;

import lombok.Builder;
import lombok.Data;

import com.google.cloud.Timestamp;
import java.util.List;

@Data
@Builder
public class Day {
    private Timestamp date;
    private List<DayMeal> meals;
}