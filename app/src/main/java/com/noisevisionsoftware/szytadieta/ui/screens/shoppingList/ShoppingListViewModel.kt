package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList

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
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import com.noisevisionsoftware.szytadieta.utils.getWeekStartDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

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

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _availableWeeks = MutableStateFlow<List<Long>>(emptyList())
    val availableWeeks = _availableWeeks.asStateFlow()

    private val _selectedWeek = MutableStateFlow<Long?>(null)
    val selectedWeek = _selectedWeek

    private val _checkedProducts = MutableStateFlow<Set<String>>(emptySet())
    val checkedProducts = _checkedProducts.asStateFlow()

    init {
        loadAvailableWeeks()
        loadCheckedProducts()
        observeDietChanges()
    }

    fun loadAvailableWeeks() {
        viewModelScope.launch {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    val weeks = shoppingListRepository.getAvailableWeeks(userId).getOrThrow()
                    _availableWeeks.value = weeks

                    if (weeks.isNotEmpty()) {
                        val currentWeekStart = getWeekStartDate(DateUtils.getCurrentLocalDate())
                        val closestWeek = weeks.minByOrNull {
                            abs(it - currentWeekStart)
                        } ?: weeks.first()

                        selectWeek(closestWeek)
                    } else {
                        _shoppingListState.value = ViewModelState.Success(
                            ShoppingList(categories = emptyList())
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Error loading available weeks", e)
                _shoppingListState.value = ViewModelState.Error("Nie znaleziono listy zakupów")
            }
        }
    }

    fun loadCheckedProducts() {
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

    fun selectWeek(selectedDate: Long) {
        val weekStartDate = getWeekStartDate(selectedDate)
        _selectedWeek.value = weekStartDate
        loadShoppingListForWeek(weekStartDate)
    }

    private fun loadShoppingListForWeek(weekStartDate: Long) {
        handleOperation(_shoppingListState) {
            authRepository.withAuthenticatedUser { userId ->
                try {
                    val shoppingList =
                        shoppingListRepository.getShoppingListForWeek(userId, weekStartDate)
                            .getOrThrow()

                    shoppingList.copy(
                        categories = shoppingList.categories.map { category ->
                            category.copy(
                                products = category.products.filter { it.name.isNotBlank() }
                            )
                        }.filter { it.products.isNotEmpty() }
                    )
                } catch (e: Exception) {
                    ShoppingList(categories = emptyList())
                }
            }
        }
    }

    fun selectedCategory(category: String?) {
        _selectedCategory.value = category
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
                availableWeeks.value.firstOrNull()?.let { closestDate ->
                    selectWeek(closestDate)
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
                    shoppingListRepository.observerDietChanges(userId).collect { hasActiveDiet ->
                        if (!hasActiveDiet) {
                            preferencesManager.clearCheckedProducts(userId)
                            _checkedProducts.value = emptySet()
                            _shoppingListState.value =
                                ViewModelState.Success(ShoppingList(categories = emptyList()))
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

    override fun onRefreshData() {
        loadAvailableWeeks()
        loadCheckedProducts()
    }
}