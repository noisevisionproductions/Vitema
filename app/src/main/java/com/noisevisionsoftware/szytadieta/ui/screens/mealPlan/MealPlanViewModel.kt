package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.DayPlan
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.dietRepository.DietRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val dietRepository: DietRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager
) : BaseViewModel(networkManager, alertManager) {

    private val _mealPlanState =
        MutableStateFlow<ViewModelState<List<DayPlan>>>(ViewModelState.Initial)
    val mealPlanState = _mealPlanState.asStateFlow()

    private val _hasAnyMealPlans = MutableStateFlow<Boolean?>(null)
    val hasAnyMealPlans = _hasAnyMealPlans.asStateFlow()

    private val _currentDate = MutableStateFlow(System.currentTimeMillis())
    val currentDate = _currentDate.asStateFlow()

    init {
        loadMealPlan(_currentDate.value)
        checkForAnyPlans()
    }

    fun refreshMealPlan() {
        loadMealPlan(System.currentTimeMillis())
    }

    fun loadMealPlan(date: Long) {
        handleOperation(_mealPlanState) {
            try {
                val result = dietRepository.getUserDietForDate(date)

                result.map { diet ->
                    diet?.let {
                        Log.d("MealPlanViewModel", "Weekly plan: ${it.weeklyPlan}")
                        it.weeklyPlan
                    } ?: emptyList()
                }.getOrThrow()
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Error loading meal plan", e)
                throw e
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
                val closestDate = dietRepository.getClosestAvailableWeekDate().getOrNull()
                closestDate?.let {
                    _currentDate.value = it
                    loadMealPlan(it)
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    fun setCurrentDate(date: Long) {
        _currentDate.value = date
    }
}