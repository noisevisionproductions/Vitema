package com.noisevisionsoftware.nutrilog.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.noisevisionsoftware.nutrilog.mapper.recipe.FirestoreRecipeMapper;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RecipeRepository {
    private final Firestore firestore;
    private final FirestoreRecipeMapper firestoreRecipeMapper;
    private static final String COLLECTION_NAME = "recipes";

    public Optional<Recipe> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot document = docRef.get().get();
            return Optional.ofNullable(firestoreRecipeMapper.toRecipe(document));
        } catch (Exception e) {
            log.error("Failed to fetch recipe by id: {}", id, e);
            throw new RuntimeException("Failed to fetch recipe", e);
        }
    }

    public List<Recipe> findAllByIds(Collection<String> ids) {
        try {
            List<Recipe> recipes = new ArrayList<>();
            for (String id : ids) {
                findById(id).ifPresent(recipes::add);
            }
            return recipes;
        } catch (Exception e) {
            log.error("Failed to fetch recipes by ids", e);
            throw new RuntimeException("Failed to fetch recipes", e);
        }
    }

    public void update(String id, Recipe recipe) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            Map<String, Object> data = firestoreRecipeMapper.toFirestoreMap(recipe);
            docRef.update(data).get();
        } catch (Exception e) {
            log.error("Failed to update recipe: {}", id, e);
            throw new RuntimeException("Failed to update recipe", e);
        }
    }
}
