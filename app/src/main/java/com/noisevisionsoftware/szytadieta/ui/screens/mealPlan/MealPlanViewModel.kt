package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan

import android.util.Log
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.DayPlan
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.DietRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val dietRepository: DietRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager
): BaseViewModel(networkManager, alertManager){

    private val _mealPlanState = MutableStateFlow<ViewModelState<List<DayPlan>>>(ViewModelState.Initial)
    val mealPlanState = _mealPlanState.asStateFlow()

    init {
        loadMealPlan()
    }

    private fun loadMealPlan() {
        handleOperation(_mealPlanState) {
            try {
                val result = dietRepository.getCurrentUserDiet()

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

    fun refreshMealPlan() {
        loadMealPlan()
    }
}