package com.noisevisionsoftware.nutrilog.dto.response.shopping;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategorizedShoppingListItemResponse {
    private String name;
    private double quantity;
    private String unit;
    private String original;
    private List<ShoppingListRecipeReferenceResponse> recipes;
}
