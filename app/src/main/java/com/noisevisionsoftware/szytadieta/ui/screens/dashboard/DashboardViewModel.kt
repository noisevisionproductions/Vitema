package com.noisevisionsoftware.szytadieta.ui.screens.dashboard

import android.icu.util.Calendar
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.localPreferences.SessionManager
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
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
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
    private val sessionManager: SessionManager,
    private val bodyMeasurementRepository: BodyMeasurementRepository,
    private val dietRepository: DietRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _userState = MutableStateFlow<ViewModelState<UserRole>>(ViewModelState.Initial)
    val userRole = _userState.asStateFlow()

    private val _userData = MutableStateFlow<ViewModelState<User>>(ViewModelState.Initial)

    private val _latestWeight =
        MutableStateFlow<ViewModelState<BodyMeasurements?>>(ViewModelState.Initial)
    val latestWeight = _latestWeight.asStateFlow()

    private val _weightHistory =
        MutableStateFlow<ViewModelState<List<BodyMeasurements>>>(ViewModelState.Initial)
    val weightHistory = _weightHistory.asStateFlow()

    private val _latestMeasurements =
        MutableStateFlow<ViewModelState<BodyMeasurements?>>(ViewModelState.Initial)
    val latestMeasurements = _latestMeasurements.asStateFlow()

    private val _measurementsHistory =
        MutableStateFlow<ViewModelState<List<BodyMeasurements>>>(ViewModelState.Initial)
    val measurementsHistory = _measurementsHistory.asStateFlow()

    private val _todayMeals = MutableStateFlow<ViewModelState<List<Meal>>>(ViewModelState.Initial)
    val todayMeals = _todayMeals.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var _scrollPosition: Int? = null
    val scrollPosition: Int? get() = _scrollPosition

    init {
        loadUserRole()
        loadUserData()
        loadLatestWeight()
        loadLatestMeasurements()
        loadTodayMeals()
        observeUserSession()
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

    fun checkAdminAccess(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUserData().getOrThrow()
                onResult(currentUser?.role == UserRole.ADMIN)
            } catch (e: Exception) {
                onResult(false)
                throw e
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
                weightRepository.getLatestWeights(userId, 7).getOrNull()?.firstOrNull()
            }
        }

        handleOperation(_weightHistory) {
            authRepository.withAuthenticatedUser { userId ->
                weightRepository.getLatestWeights(userId, 7).getOrNull() ?: emptyList()
            }
        }
    }

    private fun loadLatestMeasurements() {
        handleOperation(_latestMeasurements) {
            authRepository.withAuthenticatedUser { userId ->
                bodyMeasurementRepository.getLatestMeasurements(userId, 7).getOrNull()
                    ?.firstOrNull()
            }
        }

        handleOperation(_measurementsHistory) {
            authRepository.withAuthenticatedUser { userId ->
                bodyMeasurementRepository.getLatestMeasurements(userId, 7).getOrNull()
                    ?: emptyList()
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

    private fun observeUserSession() {
        viewModelScope.launch {
            sessionManager.userSessionFlow.collect {
                loadUserRole()
            }
        }
    }

    override fun onUserLoggedOut() {
        _userState.value = ViewModelState.Initial
        _userData.value = ViewModelState.Initial
        _latestWeight.value = ViewModelState.Initial
        _weightHistory.value = ViewModelState.Initial
        _latestMeasurements.value = ViewModelState.Initial
        _measurementsHistory.value = ViewModelState.Initial
        _todayMeals.value = ViewModelState.Initial
    }

    fun saveScrollPosition(position: Int) {
        _scrollPosition = position
    }

    override fun onCleared() {
        super.onCleared()
        _scrollPosition = null
    }

    override fun onRefreshData() {
        loadUserRole()
        loadUserData()
        loadLatestWeight()
        loadLatestMeasurements()
        loadTodayMeals()
        observeUserSession()
    }
}