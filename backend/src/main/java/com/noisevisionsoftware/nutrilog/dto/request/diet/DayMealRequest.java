package com.noisevisionsoftware.nutrilog.dto.request.diet;

import com.noisevisionsoftware.nutrilog.model.diet.MealType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DayMealRequest {
    private String recipeId;
    private MealType mealType;
    private String time;
}
