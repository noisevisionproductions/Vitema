package com.noisevisionsoftware.szytadieta.ui.screens.dashboard

import android.icu.util.Calendar
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.BodyMeasurements
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.Meal
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.WeekDay
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import com.noisevisionsoftware.szytadieta.domain.model.user.UserRole
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.repository.BodyMeasurementRepository
import com.noisevisionsoftware.szytadieta.domain.repository.WeightRepository
import com.noisevisionsoftware.szytadieta.domain.repository.dietRepository.DietRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val weightRepository: WeightRepository,
    private val bodyMeasurementRepository: BodyMeasurementRepository,
    private val dietRepository: DietRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager
) : BaseViewModel(networkManager, alertManager) {

    private val _userState = MutableStateFlow<ViewModelState<UserRole>>(ViewModelState.Initial)
    val userRole = _userState.asStateFlow()

    private val _userData = MutableStateFlow<ViewModelState<User>>(ViewModelState.Initial)
    val userData = _userData.asStateFlow()

    private val _latestWeight =
        MutableStateFlow<ViewModelState<BodyMeasurements?>>(ViewModelState.Initial)
    val latestWeight = _latestWeight.asStateFlow()

    private val _latestMeasurements =
        MutableStateFlow<ViewModelState<BodyMeasurements?>>(ViewModelState.Initial)
    val latestMeasurements = _latestMeasurements.asStateFlow()

    private val _todayMeals = MutableStateFlow<ViewModelState<List<Meal>>>(ViewModelState.Initial)
    val todayMeals = _todayMeals.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadUserRole()
        loadUserData()
        loadLatestWeight()
        loadLatestMeasurements()
        loadTodayMeals()
    }

    fun refreshDashboardData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                coroutineScope {
                    launch { loadUserRole() }
                    launch { loadUserData() }
                    launch { loadLatestWeight() }
                    launch { loadLatestMeasurements() }
                    launch { loadTodayMeals() }
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun loadUserRole() {
        handleOperation(_userState) {
            authRepository.getCurrentUserData()
                .getOrThrow()
                ?.role
                ?: throw AppException.AuthException("Nie można pobrać roli użytkownika")
        }
    }

    private fun loadUserData() {
        handleOperation(_userData) {
            authRepository.getCurrentUserData()
                .getOrThrow()
                ?: throw AppException.AuthException("Nie można pobrać danych użytkownika")
        }
    }

    private fun loadLatestWeight() {
        handleOperation(_latestWeight) {
            authRepository.withAuthenticatedUser { userId ->
                weightRepository.getLatestWeight(userId).getOrNull()
            }
        }
    }

    private fun loadLatestMeasurements() {
        handleOperation(_latestMeasurements) {
            authRepository.withAuthenticatedUser { userId ->
                bodyMeasurementRepository.getLatestMeasurements(userId).getOrNull()
            }
        }
    }

    private fun loadTodayMeals() {
        handleOperation(_todayMeals) {
            authRepository.withAuthenticatedUser {
                val calendar = Calendar.getInstance()
                val today = calendar.timeInMillis

                dietRepository.getUserDietForDate(today)
                    .getOrNull()
                    ?.weeklyPlan
                    ?.firstOrNull { it.dayOfWeek == getCurrentWeekDay() }
                    ?.meals
                    ?: emptyList()
            }
        }
    }

    private fun getCurrentWeekDay(): WeekDay {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> WeekDay.MONDAY
            Calendar.TUESDAY -> WeekDay.TUESDAY
            Calendar.WEDNESDAY -> WeekDay.WEDNESDAY
            Calendar.THURSDAY -> WeekDay.THURSDAY
            Calendar.FRIDAY -> WeekDay.FRIDAY
            Calendar.SATURDAY -> WeekDay.SATURDAY
            else -> WeekDay.SUNDAY
        }
    }
}