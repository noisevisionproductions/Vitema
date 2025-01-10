package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.DayPlan
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.dietRepository.DietRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val dietRepository: DietRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _mealPlanState =
        MutableStateFlow<ViewModelState<List<DayPlan>>>(ViewModelState.Initial)
    val mealPlanState = _mealPlanState.asStateFlow()

    private val _hasAnyMealPlans = MutableStateFlow<Boolean?>(null)
    val hasAnyMealPlans = _hasAnyMealPlans.asStateFlow()

    private val _currentDate = MutableStateFlow(DateUtils.getCurrentLocalDate())
    val currentDate = _currentDate.asStateFlow()

    private val _availableWeeks = MutableStateFlow<List<Long>>(emptyList())
    val availableWeeks = _availableWeeks.asStateFlow()

    init {
/*
        refreshMealPlan()
*/
        loadMealPlan(_currentDate.value)
        loadAvailableWeeks()
        checkForAnyPlans()
    }

    private fun loadMealPlan(date: Long) {
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

    private fun loadAvailableWeeks() {
        viewModelScope.launch {
            try {
                val weeks = dietRepository.getAvailableWeekDates().getOrNull() ?: emptyList()
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