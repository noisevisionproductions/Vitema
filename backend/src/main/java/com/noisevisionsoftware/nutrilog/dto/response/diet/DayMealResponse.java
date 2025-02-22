package com.noisevisionsoftware.nutrilog.dto.response.diet;

import com.noisevisionsoftware.nutrilog.model.diet.MealType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DayMealResponse {
    private String recipeId;
    private MealType mealType;
    private String time;
}