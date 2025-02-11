package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.DietDay
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.Recipe
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.RecipeRepository
import com.noisevisionsoftware.szytadieta.domain.repository.dietRepository.DietRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import com.noisevisionsoftware.szytadieta.utils.formatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val dietRepository: DietRepository,
    private val recipeRepository: RecipeRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _mealPlanState = MutableStateFlow<ViewModelState<DietDay>>(ViewModelState.Initial)
    val mealPlanState = _mealPlanState.asStateFlow()

    private val _recipesState = MutableStateFlow<Map<String, Recipe>>(emptyMap())
    val recipesState = _recipesState.asStateFlow()

    private val _hasAnyMealPlans = MutableStateFlow<Boolean?>(null)
    val hasAnyMealPlans = _hasAnyMealPlans.asStateFlow()

    private val _currentDate = MutableStateFlow(DateUtils.getCurrentLocalDate())
    val currentDate = _currentDate.asStateFlow()

    private val _availableWeeks = MutableStateFlow<List<Long>>(emptyList())
    val availableWeeks = _availableWeeks.asStateFlow()

    init {
        loadMealPlan(_currentDate.value)
        loadAvailableWeeks()
        checkForAnyPlans()
    }

    private fun loadMealPlan(date: Long) {
        handleOperation(_mealPlanState) {
            try {
                val result = dietRepository.getUserDietForDate(date)
                val diet = result.getOrNull()

                val timestampForDate = DateUtils.longToTimestamp(date)
                val dietDay = diet?.days?.firstOrNull { formatDate(date) == it.date }
                    ?: DietDay(timestamp = timestampForDate)

                if (dietDay.meals.isNotEmpty()) {
                    val recipesResult = recipeRepository.getRecipesForMeals(dietDay.meals)
                    when {
                        recipesResult.isSuccess -> {
                            val recipes = recipesResult.getOrNull() ?: emptyMap()
                            _recipesState.value = recipes
                        }

                        recipesResult.isFailure -> {
                            _recipesState.value = emptyMap()
                        }
                    }
                } else {
                    _recipesState.value = emptyMap()
                }

                dietDay
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Error loading meal plan", e)
                throw AppException.UnknownException("Wystąpił błąd podczas ładowania planu")
            }
        }
    }

    private fun loadAvailableWeeks() {
        viewModelScope.launch {
            try {
                val weeks = dietRepository.getAvailableDates().getOrNull() ?: emptyList()
                _availableWeeks.value = weeks
                _hasAnyMealPlans.value = weeks.isNotEmpty()
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Error loading available weeks", e)
                _hasAnyMealPlans.value = false
            }
        }
    }

    private fun checkForAnyPlans() {
        viewModelScope.launch {
            _hasAnyMealPlans.value = dietRepository.hasAnyDiets().getOrNull() ?: false
        }
    }

    fun navigateToClosestAvailableWeek() {
        viewModelScope.launch {
            try {
                val closestDate = dietRepository.getClosestAvailableDate().getOrNull()
                closestDate?.let {
                    _currentDate.value = it
                    loadMealPlan(it)
                }
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Error navigating to closest available week", e)
            }
        }
    }

    fun setCurrentDate(date: Long) {
        _currentDate.value = date
        loadMealPlan(_currentDate.value)
    }

    override fun onUserLoggedOut() {
        _mealPlanState.value = ViewModelState.Initial
    }

    fun refreshMealPlan() {
        loadMealPlan(_currentDate.value)
        loadAvailableWeeks()
        checkForAnyPlans()
    }

    override fun onRefreshData() {
        loadMealPlan(_currentDate.value)
        checkForAnyPlans()
        loadAvailableWeeks()
    }
}