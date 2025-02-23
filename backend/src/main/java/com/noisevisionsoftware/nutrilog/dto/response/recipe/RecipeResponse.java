package com.noisevisionsoftware.nutrilog.dto.response.recipe;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RecipeResponse {
    private String id;
    private String name;
    private String instructions;
    private LocalDateTime createdAt;
    private List<String> photos;
    private NutritionalValuesResponse nutritionalValues;
    private String parentRecipeId;
}