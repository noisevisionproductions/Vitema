package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.DietDay
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.Recipe
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.repository.dietRepository.DietRepository
import com.noisevisionsoftware.szytadieta.domain.repository.meals.EatenMealsRepository
import com.noisevisionsoftware.szytadieta.domain.repository.meals.RecipeRepository
import com.noisevisionsoftware.szytadieta.domain.repository.meals.RecipeRepositoryOld
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import com.noisevisionsoftware.szytadieta.utils.formatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val dietRepository: DietRepository,
    private val recipeRepository: RecipeRepository,
    private val eatenMealsRepository: EatenMealsRepository,
    private val authRepository: AuthRepository,
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

    private val _eatenMeals = MutableStateFlow<Set<String>>(emptySet())
    val eatenMeals = _eatenMeals.asStateFlow()

    init {
        loadMealPlan(_currentDate.value)
        loadAvailableWeeks()
        checkForAnyPlans()
        observeEatenMeals()
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
                    val mealsIds = dietDay.meals.map { it.recipeId }
                    Log.d("MealPlanViewModel", "Próba ładowania przepisów: $mealsIds")

                    val recipesResult = recipeRepository.getRecipesForMeals(dietDay.meals)
                    when {
                        recipesResult.isSuccess -> {
                            val recipes = recipesResult.getOrNull() ?: emptyMap()

                            dietDay.meals.forEach { meal ->
                                if (meal.recipeId.isNotEmpty() && !recipes.containsKey(meal.recipeId)) {
                                    Log.e(
                                        "MealPlanViewModel",
                                        "Nie znaleziono przepisu o ID: ${meal.recipeId}"
                                    )
                                }
                            }

                            _recipesState.value = recipes
                        }

                        recipesResult.isFailure -> {
                            val exception = recipesResult.exceptionOrNull()
                            Log.e(
                                "MealPlanViewModel",
                                "Błąd podczas pobierania przepisów: ${exception?.message}",
                                exception
                            )
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
            _hasAnyMealPlans.value = dietRepository.hasAnyDiets().getOrNull() == true
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeEatenMeals() {
        viewModelScope.launch {
            combine(
                _currentDate,
                authRepository.getCurrentUserFlow()
            ) { date, user ->
                if (user != null) {
                    eatenMealsRepository.observeEatenMeals(
                        userId = user.id,
                        date = formatDate(date)
                    )
                } else {
                    flowOf(emptySet())
                }
            }.flatMapLatest { flow ->
                flow
            }.collect { eatenMeals ->
                _eatenMeals.value = eatenMeals
            }
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

    fun toggleMealEaten(mealId: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser() ?: return@launch
                val date = formatDate(_currentDate.value)

                val currentEatenMeals = eatenMealsRepository.getEatenMeals(user.uid, date)

                val newEatenMeals = if (mealId in currentEatenMeals) {
                    eatenMealsRepository.removeEatenMeal(user.uid, date, mealId)
                    currentEatenMeals - mealId
                } else {
                    eatenMealsRepository.saveEatenMeal(user.uid, date, mealId)
                    currentEatenMeals + mealId
                }

                _eatenMeals.value = newEatenMeals
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Error toggling meal", e)
                showError("Wystąpił błąd podczas zmiany stanu posiłku: ${e.localizedMessage}")
            }
        }
    }

    fun setCurrentDate(date: Long) {
        _currentDate.value = date
        loadMealPlan(_currentDate.value)
    }

    override fun onUserLoggedOut() {
        _mealPlanState.value = ViewModelState.Initial
        _eatenMeals.value = emptySet()
    }

    fun refreshMealPlan() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                eatenMealsRepository.syncWithRemote(
                    userId = user.uid,
                    date = formatDate(_currentDate.value)
                )
            }
        }
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