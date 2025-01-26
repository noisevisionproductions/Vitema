package com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.shoppingListCard

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.localPreferences.PreferencesManager
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.ShoppingList
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.repository.dietRepository.ShoppingListRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import com.noisevisionsoftware.szytadieta.utils.formatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    private val _checkedProducts = MutableStateFlow<Set<String>>(emptySet())
    val checkedProducts = _checkedProducts.asStateFlow()

    init {
        loadCurrentWeekShoppingList()
        loadCheckedProducts()
    }

    private fun loadCurrentWeekShoppingList() {
        viewModelScope.launch {
            handleOperation(_weeklyShoppingList) {
                authRepository.withAuthenticatedUser { userId ->
                    val currentDate = DateUtils.getCurrentLocalDate()
                    val formattedDate = formatDate(currentDate)
                    val shoppingList =
                        shoppingListRepository.getShoppingListForDate(userId, formattedDate)
                            .getOrNull()
                    shoppingList?.also {
                        calculateRemainingItems(it)
                    }
                    shoppingList
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
                        (_weeklyShoppingList.value as? ViewModelState.Success)?.data?.let {
                            calculateRemainingItems(it)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardShoppingList", "Error loading checked products", e)
            }
        }
    }

    private fun calculateRemainingItems(shoppingList: ShoppingList) {
        viewModelScope.launch {
            val totalItems = shoppingList.items.size
            val checkedItems = _checkedProducts.value.size
            _remainingItems.value = ViewModelState.Success(totalItems - checkedItems)
        }
    }


    fun refreshShoppingList() {
        loadCurrentWeekShoppingList()
    }

    override fun onUserLoggedOut() {
        _weeklyShoppingList.value = ViewModelState.Initial
        _remainingItems.value = ViewModelState.Initial
    }
}