package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.data.localPreferences.PreferencesManager
import com.noisevisionsoftware.szytadieta.domain.model.shopping.CategorizedShoppingList
import com.noisevisionsoftware.szytadieta.domain.model.shopping.DatePeriod
import com.noisevisionsoftware.szytadieta.domain.model.shopping.ProductCategory
import com.noisevisionsoftware.szytadieta.domain.model.shopping.ShoppingListItem
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
import kotlinx.coroutines.flow.onStart
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

    private var observerRegistration: ListenerRegistration? = null

    private val _shoppingListState =
        MutableStateFlow<ViewModelState<CategorizedShoppingList>>(ViewModelState.Initial)
    val shoppingListState = _shoppingListState.asStateFlow()

    private val _availablePeriods = MutableStateFlow<List<DatePeriod>>(emptyList())
    val availablePeriods = _availablePeriods.asStateFlow()

    private val _selectedPeriod = MutableStateFlow<DatePeriod?>(null)
    val selectedPeriod = _selectedPeriod.asStateFlow()

    private val _checkedProducts = MutableStateFlow<Set<String>>(emptySet())
    val checkedProducts = _checkedProducts.asStateFlow()

    private val _activeCategories = MutableStateFlow<Set<ProductCategory>>(emptySet())
    val activeCategories = _activeCategories.asStateFlow()

    private val _categoryProgress =
        MutableStateFlow<Map<ProductCategory, Pair<Int, Int>>>(emptyMap())
    val categoryProgress = _categoryProgress.asStateFlow()

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

                    Log.d("ShoppingListViewModel", "Available periods: $periods")

                    if (periods.isNotEmpty()) {
                        val currentDate = formatDate(DateUtils.getCurrentLocalDate())
                        val closestPeriod = periods.firstOrNull { period ->
                            isDateInRange(
                                currentDate,
                                period.startDate,
                                period.endDate
                            )
                        } ?: periods.first()

                        Log.d("ShoppingListViewModel", "Selected closest period: $closestPeriod")

                        selectPeriod(closestPeriod)
                    } else {
                        _shoppingListState.value = ViewModelState.Success(CategorizedShoppingList())
                    }
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Error loading available weeks", e)
                _shoppingListState.value = ViewModelState.Error("Nie znaleziono listy zakupów")
            }
        }
    }


    private fun updatedCategoryStates(shoppingList: CategorizedShoppingList) {
        _activeCategories.value = shoppingList.items.keys
            .map { ProductCategory.fromId(it) }
            .toSet()

        _categoryProgress.value = shoppingList.items.mapNotNull { (categoryId, items) ->
            ProductCategory.fromId(categoryId).let { category ->
                val total = items.size
                val checked = items.count { item -> _checkedProducts.value.contains(item.name) }
                category to (checked to total)
            }
        }.toMap()
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
                    val formattedDate = period.startDate
                    Log.d("ShoppingListViewModel", "Loading shopping list for period: $period")

                    val shoppingList = shoppingListRepository
                        .getShoppingListForDate(userId, formattedDate)
                        .getOrThrow()

                    Log.d("ShoppingListViewModel", "Loaded shopping list: $shoppingList")

                    updatedCategoryStates(shoppingList)
                    shoppingList
                } catch (e: Exception) {
                    Log.e("ShoppingListVM", "Error loading shopping list", e)
                    throw e
                }
            }
        }
    }

    fun toggleProductCheck(item: ShoppingListItem) {
        viewModelScope.launch {
            if (item.name.isBlank()) return@launch

            try {
                authRepository.withAuthenticatedUser { userId ->
                    _checkedProducts.update { currentChecked ->
                        currentChecked.toMutableSet().apply {
                            if (contains(item.name)) remove(item.name)
                            else add(item.name)
                        }
                    }
                    preferencesManager.saveCheckedProducts(userId, _checkedProducts.value)

                    (_shoppingListState.value as? ViewModelState.Success)?.data?.let {
                        updatedCategoryStates(it)
                    }
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
        observerRegistration?.remove()
        observerRegistration = null

        viewModelScope.launch {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    shoppingListRepository.observeShoppingLists(userId)
                        .onStart {
                            observerRegistration =
                                shoppingListRepository.addSnapshotListener { /* listener */ }
                        }
                        .catch { e ->
                            if (authRepository.getCurrentUser() != null) {
                                Log.e("ShoppingListViewModel", "Error observing lists", e)
                                _shoppingListState.value =
                                    ViewModelState.Error("Błąd podczas ładowania list zakupów")
                            }
                        }
                        .collect { lists ->
                            if (lists.isEmpty()) {
                                preferencesManager.clearCheckedProducts(userId)
                                _checkedProducts.value = emptySet()
                                _shoppingListState.value =
                                    ViewModelState.Success(CategorizedShoppingList())
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Error observing diet changes", e)
            }
        }
    }

    override fun onUserLoggedOut() {
        observerRegistration?.remove()
        observerRegistration = null
        _shoppingListState.value = ViewModelState.Initial
        _activeCategories.value = emptySet()
        _categoryProgress.value = emptyMap()
    }

    public override fun onRefreshData() {
        loadAvailableWeeks()
        loadCheckedProducts()
    }

    /*
        private fun getMealTypeOrder(mealType: MealType): Int = when (mealType) {
            MealType.BREAKFAST -> 0
            MealType.SECOND_BREAKFAST -> 1
            MealType.LUNCH -> 2
            MealType.SNACK -> 3
            MealType.DINNER -> 4
        }*/

    /*

/*
    private fun groupAsSingleList(shoppingList: ShoppingList): List<ShoppingListGroup> {
        val groupedProducts = shoppingList.items
            .flatMap { item ->
                item.recipes.map { recipe ->
                    val productId = "${recipe.dayIndex}_${recipe.recipeId}_${item.name}"
                    ShoppingListProductContext(
                        productId = productId,
                        name = item.name,
                        recipeId = recipe.recipeId,
                        dayIndex = recipe.dayIndex,
                        mealType = recipe.mealType,
                        quantity = 1
                    )
                }
            }
            .groupBy { it.name.lowercase().trim() }
            .map { (_, products) ->
                val firstProduct = products.first()
                ShoppingListProductContext(
                    productId = firstProduct.productId,
                    name = firstProduct.name,
                    recipeId = firstProduct.recipeId,
                    dayIndex = firstProduct.dayIndex,
                    mealType = firstProduct.mealType,
                    quantity = products.size,
                    occurrences = products.map { it.productId }
                )
            }

        return listOf(
            ShoppingListGroup.SingleList(
                items = groupedProducts,
                totalDays = shoppingList.items
                    .flatMap { it.recipes }
                    .map { it.dayIndex }
                    .distinct()
                    .size
            )
        )
    }*/

    fun updateGroupingMode(mode: GroupingMode) {
        _groupingMode.value = mode
        regroupItems()
    }

    private fun regroupItems() {
        val currentList = (_shoppingListState.value as? ViewModelState.Success)?.data ?: return

        _groupedItems.value = when (_groupingMode.value) {
            GroupingMode.BY_RECIPE -> groupByRecipe(currentList)
            GroupingMode.BY_DAY -> groupByDay(currentList)
            GroupingMode.SINGLE_LIST -> groupAsSingleList(currentList)
        }
    }
*/

    /*
        private fun groupByRecipe(shoppingList: ShoppingList): List<ShoppingListGroup> {
            return shoppingList.items
                .flatMap { item ->
                    item.contexts.map { context ->
                        val recipe = item.recipes.first { it.recipeId == context.recipeId }
                        Triple(context, recipe, item.name)
                    }
                }
                .groupBy { (context, recipe, _) ->
                    "${recipe.recipeName}_${context.dayIndex}_${context.mealType}"
                }
                .map { (_, groupedItems) ->
                    val firstItem = groupedItems.first()
                    val (context, recipe, _) = firstItem

                    val productsWithContext = groupedItems.map { (itemContext, _, productName) ->
                        ShoppingListProductContext(
                            productId = "${itemContext.dayIndex}_${itemContext.recipeId}_${productName}",
                            name = productName,
                            recipeId = itemContext.recipeId,
                            dayIndex = itemContext.dayIndex,
                            mealType = itemContext.mealType
                        )
                    }

                    ShoppingListGroup.ByRecipe(
                        recipeName = recipe.recipeName,
                        dayIndex = context.dayIndex,
                        mealType = context.mealType,
                        mealTime = recipe.mealTime,
                        items = productsWithContext
                    )
                }
                .sortedWith(compareBy({ it.dayIndex }, { getMealTypeOrder(it.mealType) }))
        }

        private fun groupByDay(shoppingList: ShoppingList): List<ShoppingListGroup> {
            return shoppingList.items
                .flatMap { item ->
                    item.contexts.map { context ->
                        val recipe = item.recipes.first { it.recipeId == context.recipeId }
                        Triple(context, recipe, item.name)
                    }
                }
                .groupBy { (context, _, _) -> context.dayIndex }
                .map { (dayIndex, dayItems) ->
                    val meals = dayItems
                        .groupBy { (context, _, _) -> context.mealType }
                        .map { (mealType, mealItems) ->
                            val recipes = mealItems
                                .groupBy { (_, recipe, _) -> recipe.recipeName }
                                .map { (recipeName, items) ->
                                    val productContexts = items.map { (context, _, productName) ->
                                        ShoppingListProductContext(
                                            productId = "${context.dayIndex}_${context.recipeId}_${productName}",
                                            name = productName,
                                            recipeId = context.recipeId,
                                            dayIndex = context.dayIndex,
                                            mealType = context.mealType
                                        )
                                    }
                                    recipeName to productContexts
                                }

                            MealGroup(
                                mealType = mealType,
                                mealTime = mealItems.first().second.mealTime,
                                recipes = recipes
                            )
                        }
                        .sortedBy { getMealTypeOrder(it.mealType) }

                    val dayProductContexts = dayItems.map { (context, _, productName) ->
                        ShoppingListProductContext(
                            productId = "${context.dayIndex}_${context.recipeId}_${productName}",
                            name = productName,
                            recipeId = context.recipeId,
                            dayIndex = context.dayIndex,
                            mealType = context.mealType
                        )
                    }

                    ShoppingListGroup.ByDay(
                        dayIndex = dayIndex,
                        meals = meals,
                        items = dayProductContexts
                    )
                }
                .sortedBy { it.dayIndex }
        }*/
}