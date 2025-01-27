package com.noisevisionsoftware.szytadieta.ui.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {
    private val _recipeId = MutableStateFlow<String?>(null)
    val recipeId = _recipeId.asStateFlow()

    fun setRecipeId(id: String) {
        _recipeId.value = id
    }

    fun clearRecipeId() {
        _recipeId.value = null
    }
}