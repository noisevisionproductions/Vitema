package com.noisevisionsoftware.nutrilog.model.recipe;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NutritionalValues {
    private double calories;
    private double protein;
    private double fat;
    private double carbs;
}

