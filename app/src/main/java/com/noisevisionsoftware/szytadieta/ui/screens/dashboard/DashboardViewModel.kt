package com.noisevisionsoftware.szytadieta.ui.screens.dashboard

import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.localPreferences.SessionManager
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.Meal
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.WeekDay
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.BodyMeasurements
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import com.noisevisionsoftware.szytadieta.domain.model.user.UserRole
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.repository.UserRepository
import com.noisevisionsoftware.szytadieta.domain.repository.dietRepository.DietRepository
import com.noisevisionsoftware.szytadieta.domain.repository.health.BodyMeasurementRepository
import com.noisevisionsoftware.szytadieta.domain.repository.health.WeightRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
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

    private val _isActive = MutableStateFlow(false)

    private val _showTutorial = MutableStateFlow(false)
    val showTutorial = _showTutorial.asStateFlow()

    init {
        loadUserRole()
        loadUserData()
        loadLatestWeight()
        loadLatestMeasurements()
        loadTodayMeals()
        observeUserSession()
        isDashboardTutorialShown()

        viewModelScope.launch {
            _isActive.collect { isActive ->
                if (isActive) {
                    refreshDashboardData()
                }
            }
        }
    }

    fun refreshDashboardData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                supervisorScope {
                    val jobs = listOf(
                        launch { loadUserRole() },
                        launch { loadUserData() },
                        launch { loadLatestWeight() },
                        launch { loadLatestMeasurements() },
                        launch { loadTodayMeals() }
                    )
                    jobs.joinAll()
                }
            } catch (e: Exception) {
                showError(e.message ?: "Wystąpił błąd podczas odświeżania danych")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun checkAdminAccess(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUserData().getOrThrow()
                onResult(currentUser?.role == UserRole.ADMIN)
            } catch (e: Exception) {
                onResult(false)
                throw e
            }
        }
    }

    private fun loadUserRole() {
        handleOperation(_userState) {
            if (authRepository.getCurrentUser() == null) {
                _userState.value = ViewModelState.Initial
                return@handleOperation UserRole.USER
            }

            userRepository.getCurrentUserData()
                .getOrThrow()
                ?.role
                ?: UserRole.USER
        }
    }

    private fun loadUserData() {
        handleOperation(_userData) {
            userRepository.getCurrentUserData()
                .getOrThrow()
                ?: throw AppException.AuthException("Nie można pobrać danych użytkownika")
        }
    }

    private fun loadLatestWeight() {
        handleOperation(_latestWeight) {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    weightRepository.getLatestWeights(userId, 7).getOrNull()?.firstOrNull()
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error loading latest weight", e)
                throw e
            }
        }

        handleOperation(_weightHistory) {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    weightRepository.getLatestWeights(userId, 7).getOrNull() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error loading weight history", e)
                throw e
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
            sessionManager.userSessionFlow.collect { user ->
                if (user == null) {
                    _userState.value = ViewModelState.Initial
                    _userData.value = ViewModelState.Initial
                } else {
                    loadUserRole()
                }
            }
        }
    }

    private fun isDashboardTutorialShown() {
        viewModelScope.launch {
            sessionManager.isDashboardTutorialShown.collect { isShown ->
                _showTutorial.value = !isShown
            }
        }
    }

    fun dismissTutorial() {
        viewModelScope.launch {
            sessionManager.markDashboardTutorialAsShown()
            _showTutorial.value = false
        }
    }

    override fun onUserLoggedOut() {
        viewModelScope.launch {
            sessionManager.saveDashboardScrollPosition(0, 0)
        }
        _userState.value = ViewModelState.Initial
        _userData.value = ViewModelState.Initial
        _latestWeight.value = ViewModelState.Initial
        _weightHistory.value = ViewModelState.Initial
        _latestMeasurements.value = ViewModelState.Initial
        _measurementsHistory.value = ViewModelState.Initial
        _todayMeals.value = ViewModelState.Initial
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