package com.noisevisionsoftware.vitema.ui.screens.mealPlan.recipe

import android.net.Uri
import com.noisevisionsoftware.vitema.domain.alert.AlertManager
import com.noisevisionsoftware.vitema.domain.model.health.dietPlan.Recipe
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.domain.repository.dietRepository.DietRepository
import com.noisevisionsoftware.vitema.domain.repository.meals.RecipeRepository
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.base.BaseViewModel
import com.noisevisionsoftware.vitema.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val dietRepository: DietRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _recipeState = MutableStateFlow<ViewModelState<Recipe>>(ViewModelState.Initial)
    val recipeState = _recipeState.asStateFlow()

    private val _recipePhotosState =
        MutableStateFlow<ViewModelState<List<String>>>(ViewModelState.Initial)
    val recipePhotosState = _recipePhotosState.asStateFlow()

    fun loadRecipe(recipeId: String) {
        handleOperation(_recipeState) {
            recipeRepository.getRecipeById(recipeId).getOrThrow()
        }
    }

    fun addPhotoToRecipe(recipeId: String, photoUri: Uri) {
        handleOperation(_recipeState) {
            val photoUrl = recipeRepository.addPhotoToRecipe(recipeId, photoUri).getOrThrow()
            val currentRecipe = _recipeState.value.let { state ->
                if (state is ViewModelState.Success) state.data
                else throw Exception("Nie można zaktualizować zdjęć przepisu")
            }

            currentRecipe.copy(photos = currentRecipe.photos + photoUrl)
        }
    }

    fun updateRecipePhotosState(recipe: Recipe) {
        _recipePhotosState.value = ViewModelState.Success(recipe.photos)
    }
}