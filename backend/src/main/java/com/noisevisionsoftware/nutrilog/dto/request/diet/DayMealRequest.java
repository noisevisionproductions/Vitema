package com.noisevisionsoftware.nutrilog.dto.request.diet;

import com.noisevisionsoftware.nutrilog.model.diet.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DayMealRequest {

    @NotBlank
    private String recipeId;

    @NotNull
    private MealType mealType;

    @NotBlank
    private String time;
}
