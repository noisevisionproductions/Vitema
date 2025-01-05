package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList

import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.localPreferences.PreferencesManager
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.ShoppingList
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.repository.dietRepository.ShoppingListRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager
) : BaseViewModel(networkManager, alertManager) {

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
    }

    fun loadAvailableWeeks() {
        viewModelScope.launch {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    val weeks = shoppingListRepository.getAvailableWeeks(userId).getOrThrow()

                    _availableWeeks.value = weeks
                    if (weeks.isNotEmpty()) {
                        selectWeek(weeks.first())
                    }
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Error loading available weeks", e)
                throw e
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

    fun selectWeek(weekStartDate: Long) {
        _selectedWeek.value = weekStartDate
        loadShoppingListForWeek(weekStartDate)
    }

    private fun loadShoppingListForWeek(weekStartDate: Long) {
        handleOperation(_shoppingListState) {
            authRepository.withAuthenticatedUser { userId ->
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
            }
        }
    }

    fun getFormattedWeekDate(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val startDate = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val endDate = dateFormat.format(calendar.time)

        return "$startDate - $endDate"
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
}