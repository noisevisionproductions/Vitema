package com.noisevisionsoftware.nutrilog.controller;

import com.noisevisionsoftware.nutrilog.dto.request.recipe.RecipeUpdateRequest;
import com.noisevisionsoftware.nutrilog.dto.response.recipe.RecipeResponse;
import com.noisevisionsoftware.nutrilog.mapper.recipe.RecipeMapper;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import com.noisevisionsoftware.nutrilog.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@Validated
public class RecipeController {
    private final RecipeService recipeService;
    private final RecipeMapper recipeMapper;

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getRecipeById(@PathVariable String id) {
        Recipe recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(recipeMapper.toResponse(recipe));
    }

    @GetMapping("/batch")
    public ResponseEntity<List<RecipeResponse>> getRecipesByIds(
            @RequestParam List<String> ids) {
        List<Recipe> recipes = recipeService.getRecipesByIds(ids);
        return ResponseEntity.ok(
                recipes.stream()
                        .map(recipeMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable String id,
            @Valid @RequestBody RecipeUpdateRequest request) {
        Recipe recipe = recipeMapper.toModel(request);
        Recipe updatedRecipe = recipeService.updateRecipe(id, recipe);
        return ResponseEntity.ok(recipeMapper.toResponse(updatedRecipe));
    }
}