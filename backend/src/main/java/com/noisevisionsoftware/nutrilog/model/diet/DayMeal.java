package com.noisevisionsoftware.nutrilog.model.diet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DayMeal {
    private String recipeId;
    private MealType mealType;
    private String time;
}