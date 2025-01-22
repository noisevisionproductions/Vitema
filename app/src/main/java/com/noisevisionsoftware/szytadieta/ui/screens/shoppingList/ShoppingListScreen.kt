package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.ShoppingList
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.CustomErrorMessage
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.common.LoadingOverlay
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.CategorySelector
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.NoShoppingListMessage
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.ProductList
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.ShoppingListPeriodSelector

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit
) {
    val shoppingListState by viewModel.shoppingListState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val availableWeeks by viewModel.availableWeeks.collectAsState()
    val selectedWeek by viewModel.selectedWeek.collectAsState()
    val checkedProducts by viewModel.checkedProducts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopAppBar(
            title = "Lista zakupów",
            onBackClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Dashboard) },
            showRefreshIcon = true,
            onRefreshClick = {
                viewModel.loadAvailableWeeks()
                viewModel.loadCheckedProducts()
            }
        )

        if (availableWeeks.isNotEmpty() && shoppingListState !is ViewModelState.Loading) {
            ShoppingListPeriodSelector(
                availableWeeks = availableWeeks,
                selectedWeek = selectedWeek,
                onWeekSelected = { newDate -> viewModel.selectWeek(newDate) }
            )
        }

        when (shoppingListState) {
            is ViewModelState.Initial -> LoadingOverlay()
            is ViewModelState.Loading -> LoadingOverlay()
            is ViewModelState.Error -> CustomErrorMessage(
                message = (shoppingListState as ViewModelState.Error).message
            )

            is ViewModelState.Success -> {
                val shoppingList = (shoppingListState as ViewModelState.Success<ShoppingList>).data
                if (shoppingList.categories.isEmpty()) {
                    NoShoppingListMessage(
                        hasAnyShoppingLists = availableWeeks.isNotEmpty(),
                        onNavigateToAvailableWeek = if (availableWeeks.isNotEmpty()) {
                            { viewModel.navigateToClosestAvailableWeek() }
                        } else null,
                        onNavigate = onNavigate
                    )
                } else {
                    ShoppingListContent(
                        shoppingList = shoppingList,
                        selectedCategory = selectedCategory,
                        checkedProducts = checkedProducts,
                        onCategorySelected = { viewModel.selectedCategory(it) },
                        onProductCheckedChange = { productName, _ ->
                            viewModel.toggleProductCheck(productName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingListContent(
    shoppingList: ShoppingList,
    selectedCategory: String?,
    checkedProducts: Set<String>,
    onCategorySelected: (String?) -> Unit,
    onProductCheckedChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {

        CategorySelector(
            categories = shoppingList.categories.map { it.name },
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        val productsToShow = if (selectedCategory != null) {
            shoppingList.categories
                .find { it.name == selectedCategory }
                ?.products ?: emptyList()
        } else {
            shoppingList.categories.flatMap { it.products }
        }

        ProductList(
            products = productsToShow,
            checkedProducts = checkedProducts,
            onProductCheckedChange = onProductCheckedChange
        )
    }
}