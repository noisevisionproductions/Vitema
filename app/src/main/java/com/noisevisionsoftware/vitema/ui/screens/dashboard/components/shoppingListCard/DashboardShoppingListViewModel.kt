package com.noisevisionsoftware.vitema.ui.screens.dashboard.components.shoppingListCard

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.vitema.domain.alert.AlertManager
import com.noisevisionsoftware.vitema.data.localPreferences.PreferencesManager
import com.noisevisionsoftware.vitema.domain.model.shopping.CategorizedShoppingList
import com.noisevisionsoftware.vitema.domain.model.shopping.ProductCategory
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.domain.repository.AuthRepository
import com.noisevisionsoftware.vitema.domain.repository.dietRepository.DietRepository
import com.noisevisionsoftware.vitema.domain.repository.dietRepository.ShoppingListRepository
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.base.BaseViewModel
import com.noisevisionsoftware.vitema.ui.base.EventBus
import com.noisevisionsoftware.vitema.utils.DateUtils
import com.noisevisionsoftware.vitema.utils.formatDate
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
    private val dietRepository: DietRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _weeklyShoppingList =
        MutableStateFlow<ViewModelState<CategorizedShoppingList?>>(ViewModelState.Initial)
    val weeklyShoppingList = _weeklyShoppingList.asStateFlow()

    private val _remainingItems = MutableStateFlow<ViewModelState<Int>>(ViewModelState.Initial)
    val remainingItems = _remainingItems.asStateFlow()

    private val _checkedProducts = MutableStateFlow<Set<String>>(emptySet())
    val checkedProducts = _checkedProducts.asStateFlow()

    private val _categoryProgress =
        MutableStateFlow<Map<ProductCategory, Pair<Int, Int>>>(emptyMap())
    val categoryProgress = _categoryProgress.asStateFlow()

    init {
        loadCurrentWeekShoppingList()
        loadCheckedProducts()
        observeDietChanges()
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
                        updateCategoryProgress(it)
                    }
                    shoppingList
                }
            }
        }
    }

    private fun updateCategoryProgress(shoppingList: CategorizedShoppingList) {
        _categoryProgress.value = shoppingList.items.mapNotNull { (categoryId, items) ->
            ProductCategory.fromId(categoryId).let { category ->
                val checked = items.count { item -> _checkedProducts.value.contains(item.name) }
                category to (checked to items.size)
            }
        }.toMap()
    }

    private fun loadCheckedProducts() {
        viewModelScope.launch {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    preferencesManager.getCheckedProducts(userId).collect { savedProducts ->
                        _checkedProducts.value = savedProducts
                        (_weeklyShoppingList.value as? ViewModelState.Success)?.data?.let {
                            calculateRemainingItems(it)
                            updateCategoryProgress(it)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardShoppingList", "Error loading checked products", e)
            }
        }
    }

    private fun calculateRemainingItems(shoppingList: CategorizedShoppingList) {
        viewModelScope.launch {
            val totalItems = shoppingList.allProducts.size
            val checkedItems = _checkedProducts.value.size
            _remainingItems.value = ViewModelState.Success(totalItems - checkedItems)
        }
    }

    private suspend fun synchronizeCheckedProducts(userId: String) {
        val currentList = (_weeklyShoppingList.value as? ViewModelState.Success)?.data
        if (currentList != null) {
            val allValidProducts = currentList.allProducts.map { it.name }.toSet()

            val currentCheckedProducts = _checkedProducts.value
            val updatedCheckedProducts = currentCheckedProducts.filter { productId ->
                productId in allValidProducts
            }.toSet()

            if (updatedCheckedProducts != currentCheckedProducts) {
                _checkedProducts.value = updatedCheckedProducts
                preferencesManager.saveCheckedProducts(userId, updatedCheckedProducts)
                calculateRemainingItems(currentList)
                updateCategoryProgress(currentList)
            }
        }
    }

    private fun observeDietChanges() {
        viewModelScope.launch {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    dietRepository.observeDietChanges(userId)
                        .collect { diets ->
                            if (diets.isEmpty()) {
                                clearShoppingListData(userId)
                            } else {
                                refreshShoppingList()
                                synchronizeCheckedProducts(userId)
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e("DashboardShoppingList", "Error observing diet changes", e)
            }
        }
    }

    private suspend fun clearShoppingListData(userId: String) {
        _weeklyShoppingList.value = ViewModelState.Success(null)
        _remainingItems.value = ViewModelState.Success(0)
        _categoryProgress.value = emptyMap()
        preferencesManager.clearCheckedProducts(userId)
        _checkedProducts.value = emptySet()
    }

    fun refreshShoppingList() {
        loadCurrentWeekShoppingList()
    }

    override fun onRefreshData() {
        loadCurrentWeekShoppingList()
        loadCheckedProducts()
    }

    override fun onUserLoggedOut() {
        _weeklyShoppingList.value = ViewModelState.Initial
        _remainingItems.value = ViewModelState.Initial
        _categoryProgress.value = emptyMap()
    }
}