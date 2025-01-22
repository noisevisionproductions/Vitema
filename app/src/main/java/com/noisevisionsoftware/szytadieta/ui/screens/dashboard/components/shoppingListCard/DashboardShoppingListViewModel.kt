package com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.shoppingListCard

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.localPreferences.PreferencesManager
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.ShoppingList
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.repository.dietRepository.ShoppingListRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _weeklyShoppingList =
        MutableStateFlow<ViewModelState<ShoppingList?>>(ViewModelState.Initial)
    val weeklyShoppingList = _weeklyShoppingList.asStateFlow()

    private val _remainingItems = MutableStateFlow<ViewModelState<Int>>(ViewModelState.Initial)
    val remainingItems = _remainingItems.asStateFlow()

    private val _isRefreshingShoppingList = MutableStateFlow(false)

    private val _checkedProducts = MutableStateFlow<Set<String>>(emptySet())

    init {
        loadCurrentWeekShoppingList()
        loadCheckedProducts()
    }

    private fun loadCurrentWeekShoppingList() {
        viewModelScope.launch {
            handleOperation(_weeklyShoppingList) {
                authRepository.withAuthenticatedUser { userId ->
                    val currentWeekStart = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    shoppingListRepository.getShoppingListForWeek(userId, currentWeekStart)
                        .getOrNull()
                }
            }
        }
    }

    private fun loadCheckedProducts() {
        viewModelScope.launch {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    preferencesManager.getCheckedProducts(userId).collect { savedProducts ->
                        _checkedProducts.value = savedProducts
                        calculateRemainingItems()
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "Dashboard Shopping List View Model",
                    "Error while loading checked products:",
                    e
                )
                throw e
            }
        }
    }

    fun calculateRemainingItems() {
        viewModelScope.launch {
            val currentList = _weeklyShoppingList.value
            if (currentList is ViewModelState.Success && currentList.data != null) {
                val allProducts = currentList.data.categories
                    .flatMap { it.products }
                    .map { it.name }

                val remainingCount = allProducts.count { productName ->
                    !_checkedProducts.value.contains(productName)
                }

                _remainingItems.value = ViewModelState.Success(remainingCount)
            }
        }
    }

    fun getFormattedWeekDates(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val weekStart = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val weekEnd = dateFormat.format(calendar.time)

        return "$weekStart - $weekEnd"
    }


    suspend fun refreshShoppingList() {
        _isRefreshingShoppingList.value = true
        try {
            coroutineScope {
                launch { loadCurrentWeekShoppingList() }
            }
        } finally {
            _isRefreshingShoppingList.value = false
        }
    }

    override fun onUserLoggedOut() {
        _weeklyShoppingList.value = ViewModelState.Initial
        _remainingItems.value = ViewModelState.Initial
    }
}