package com.noisevisionsoftware.nutrilog.service;

import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import com.noisevisionsoftware.nutrilog.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {
    private final RecipeRepository recipeRepository;

    public Recipe getRecipeById(String id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recipe not found: " + id));
    }

    public List<Recipe> getRecipesByIds(Collection<String> ids) {
        return recipeRepository.findAllByIds(ids);
    }

    public Recipe updateRecipe(String id, Recipe recipe) {
        Recipe existingRecipe = getRecipeById(id);
        recipe.setId(id);
        recipe.setCreatedAt(existingRecipe.getCreatedAt());
        recipe.setPhotos(existingRecipe.getPhotos());
        recipeRepository.update(id, recipe);
        return recipe;
    }
}