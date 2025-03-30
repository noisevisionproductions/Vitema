package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.shopping.CategorizedShoppingList
import com.noisevisionsoftware.szytadieta.domain.model.shopping.ProductCategory
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.CustomErrorMessage
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.common.LoadingOverlay
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.CategorizedShoppingList
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.CategorySelector
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.NoShoppingListMessage
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.PeriodSelector
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.PeriodSelectorDropdownMenu

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit
) {
    val shoppingListState by viewModel.shoppingListState.collectAsState()
    val availablePeriods by viewModel.availablePeriods.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val checkedProducts by viewModel.checkedProducts.collectAsState()
    val activeCategories by viewModel.activeCategories.collectAsState()
    val categoryProgress by viewModel.categoryProgress.collectAsState()
    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }

    var showPeriodSelector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopAppBar(
            title = "Lista zakupów",
            onBackClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Dashboard) },
            showRefreshIcon = true,
            onRefreshClick = { viewModel.onRefreshData() }
        )

        if (availablePeriods.isNotEmpty()) {
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onClick = { showPeriodSelector = true }
            )

            PeriodSelectorDropdownMenu(
                expanded = showPeriodSelector,
                onDismissRequest = { showPeriodSelector = false },
                availablePeriods = availablePeriods,
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { period ->
                    viewModel.selectPeriod(period)
                    showPeriodSelector = false
                }
            )
        }

        if (activeCategories.isNotEmpty()) {
            CategorySelector(
                categories = activeCategories.toList(),
                selectedCategory = selectedCategory,
                onCategorySelected = { category -> selectedCategory = category }
            )
        }

        when (shoppingListState) {
            is ViewModelState.Initial -> LoadingOverlay()
            is ViewModelState.Loading -> LoadingOverlay()
            is ViewModelState.Error -> CustomErrorMessage(
                message = (shoppingListState as ViewModelState.Error).message
            )

            is ViewModelState.Success -> {
                val shoppingList =
                    (shoppingListState as ViewModelState.Success<CategorizedShoppingList>).data
                if (shoppingList.allProducts.isEmpty()) {
                    NoShoppingListMessage(
                        hasAnyShoppingLists = availablePeriods.isNotEmpty(),
                        onNavigateToAvailablePeriod = if (availablePeriods.isNotEmpty()) {
                            { viewModel.navigateToClosestAvailableWeek() }
                        } else null
                    )
                } else {
                    CategorizedShoppingList(
                        shoppingList = shoppingList,
                        selectedCategory = selectedCategory,
                        checkedProducts = checkedProducts,
                        categoryProgress = categoryProgress,
                        onProductCheckedChange = { productName, _ ->
                            viewModel.toggleProductCheck(productName)
                        }
                    )
                }
            }
        }
    }
}

/*

@Composable
private fun GroupedShoppingList(
    groupedItems: List<ShoppingListGroup>,
    checkedProducts: Set<String>,
    onProductCheckedChange: (String, Boolean) -> Unit,
    groupingMode: GroupingMode,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(groupingMode) {
        listState.animateScrollToItem(0)
    }

    val sortedItems = remember(groupedItems, checkedProducts) {
        groupedItems.sortedWith(compareBy(
            { group -> isGroupCompleted(group, checkedProducts) },
            { group -> getGroupIndex(group) }
        ))
    }

    Column(modifier = modifier) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = sortedItems,
                key = { group -> generateGroupKey(group) }
            ) { group ->
                when (group) {
                    is ShoppingListGroup.ByRecipe -> RecipeGroupCard(
                        group = group,
                        checkedProducts = checkedProducts,
                        onProductCheckedChange = onProductCheckedChange
                    )

                    is ShoppingListGroup.ByDay -> DayGroupCard(
                        group = group,
                        checkedProducts = checkedProducts,
                        onProductCheckedChange = onProductCheckedChange
                    )

                    is ShoppingListGroup.SingleList -> SingleListCard(
                        group = group,
                        checkedProducts = checkedProducts,
                        onProductCheckedChange = onProductCheckedChange
                    )
                }
            }
        }
    }
}


@Composable
fun BadgeWithProgress(
    total: Int,
    checked: Int,
    modifier: Modifier = Modifier
) {
    val progress = checked.toFloat() / total
    val backgroundColor = when {
        progress == 1f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        progress > 0f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = modifier
            .height(24.dp)
            .widthIn(min = 48.dp),
        shape = CircleShape,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$checked/$total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun getMealTypeLabel(mealType: MealType): String = when (mealType) {
    MealType.BREAKFAST -> "Śniadanie"
    MealType.SECOND_BREAKFAST -> "Drugie śniadanie"
    MealType.LUNCH -> "Obiad"
    MealType.SNACK -> "Przekąska"
    MealType.DINNER -> "Kolacja"
}

private fun isGroupCompleted(group: ShoppingListGroup, checkedProducts: Set<String>): Boolean =
    when (group) {
        is ShoppingListGroup.ByRecipe -> group.items.all { checkedProducts.contains(it.productId) }
        is ShoppingListGroup.ByDay -> group.items.all { checkedProducts.contains(it.productId) }
        is ShoppingListGroup.SingleList -> group.items.all { checkedProducts.contains(it.productId) }
    }

private fun getGroupIndex(group: ShoppingListGroup): Int =
    when (group) {
        is ShoppingListGroup.ByRecipe -> group.dayIndex
        is ShoppingListGroup.ByDay -> group.dayIndex
        is ShoppingListGroup.SingleList -> 0
    }

private fun generateGroupKey(group: ShoppingListGroup): String =
    when (group) {
        is ShoppingListGroup.SingleList -> "single_list_${group.totalDays}"
        is ShoppingListGroup.ByRecipe -> "recipe_${group.dayIndex}_${group.mealType}_${group.recipeName}"
        is ShoppingListGroup.ByDay -> "day_${group.dayIndex}"
    }*/
