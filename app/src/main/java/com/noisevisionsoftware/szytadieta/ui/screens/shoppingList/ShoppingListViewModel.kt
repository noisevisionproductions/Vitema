package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.localPreferences.PreferencesManager
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.DatePeriod
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.ShoppingList
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.repository.dietRepository.ShoppingListRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import com.noisevisionsoftware.szytadieta.utils.formatDate
import com.noisevisionsoftware.szytadieta.utils.isDateInRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _shoppingListState =
        MutableStateFlow<ViewModelState<ShoppingList>>(ViewModelState.Initial)
    val shoppingListState = _shoppingListState.asStateFlow()

    private val _availablePeriods = MutableStateFlow<List<DatePeriod>>(emptyList())
    val availablePeriods = _availablePeriods.asStateFlow()

    private val _selectedPeriod = MutableStateFlow<DatePeriod?>(null)
    val selectedPeriod = _selectedPeriod.asStateFlow()

    private val _checkedProducts = MutableStateFlow<Set<String>>(emptySet())
    val checkedProducts = _checkedProducts.asStateFlow()

    init {
        loadAvailableWeeks()
        loadCheckedProducts()
        observeDietChanges()
    }

    private fun loadAvailableWeeks() {
        viewModelScope.launch {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    val periods = shoppingListRepository.getAvailablePeriods(userId).getOrThrow()
                    _availablePeriods.value = periods

                    if (periods.isNotEmpty()) {
                        val currentDate = formatDate(DateUtils.getCurrentLocalDate())
                        val closestPeriod = periods.firstOrNull { period ->
                            isDateInRange(
                                currentDate,
                                period.startDate,
                                period.endDate
                            )
                        } ?: periods.first()

                        selectPeriod(closestPeriod)
                    } else {
                        _shoppingListState.value = ViewModelState.Success(ShoppingList())
                    }
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Error loading available weeks", e)
                _shoppingListState.value = ViewModelState.Error("Nie znaleziono listy zakupów")
            }
        }
    }

    private fun loadCheckedProducts() {
        viewModelScope.launch {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    preferencesManager.getCheckedProducts(userId).collect { savedProducts ->
                        _checkedProducts.value = savedProducts
                    }
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Error loading checked products", e)
                throw e
            }
        }
    }

    fun selectPeriod(period: DatePeriod) {
        _selectedPeriod.value = period
        loadShoppingListForPeriod(period)
    }

    private fun loadShoppingListForPeriod(period: DatePeriod) {
        handleOperation(_shoppingListState) {
            authRepository.withAuthenticatedUser { userId ->
                try {
                    shoppingListRepository.getShoppingListForDate(userId, period.startDate)
                        .getOrThrow()
                } catch (e: Exception) {
                    ShoppingList()
                }
            }
        }
    }

    fun toggleProductCheck(productName: String) {
        viewModelScope.launch {
            if (productName.isBlank()) return@launch

            try {
                authRepository.withAuthenticatedUser { userId ->
                    _checkedProducts.update { currentChecked ->
                        val newSet = currentChecked.toMutableSet()
                        if (currentChecked.contains(productName)) {
                            newSet.remove(productName)
                        } else {
                            newSet.add(productName)
                        }
                        newSet
                    }
                    preferencesManager.saveCheckedProducts(userId, _checkedProducts.value)
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Error toggling product", e)
                throw e
            }
        }
    }

    fun navigateToClosestAvailableWeek() {
        viewModelScope.launch {
            try {
                _availablePeriods.value.firstOrNull()?.let { closestPeriod ->
                    selectPeriod(closestPeriod)
                }
            } catch (e: Exception) {
                Log.e("Shopping List", "Error navigating to closest available week", e)
                throw e
            }
        }
    }

    private fun observeDietChanges() {
        viewModelScope.launch {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    shoppingListRepository.observeShoppingLists(userId)
                        .catch { e ->
                            Log.e("ShoppingListViewModel", "Error observing lists", e)
                            _shoppingListState.value =
                                ViewModelState.Error("Błąd podczas ładowania list zakupów")
                        }
                        .collect { lists ->
                            if (lists.isEmpty()) {
                                preferencesManager.clearCheckedProducts(userId)
                                _checkedProducts.value = emptySet()
                                _shoppingListState.value = ViewModelState.Success(ShoppingList())
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Error observing diet changes", e)
            }
        }
    }

    override fun onUserLoggedOut() {
        _shoppingListState.value = ViewModelState.Initial
    }

    public override fun onRefreshData() {
        loadAvailableWeeks()
        loadCheckedProducts()
    }
}