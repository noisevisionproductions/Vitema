package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.ShoppingList
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.CustomErrorMessage
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.common.LoadingOverlay
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.CategorySelector
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.NoShoppingListAvailable
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.ProductList
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.WeekSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = hiltViewModel(),
    onBackClick: () -> Unit
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
            onBackClick = onBackClick,
            showRefreshIcon = true,
            onRefreshClick = {
                viewModel.loadAvailableWeeks()
                viewModel.loadCheckedProducts()
            }
        )

        when {
            shoppingListState is ViewModelState.Error -> CustomErrorMessage(
                message = (shoppingListState as ViewModelState.Error).message
            )

            shoppingListState is ViewModelState.Loading -> LoadingOverlay()

            availableWeeks.isEmpty() -> NoShoppingListAvailable()

            shoppingListState is ViewModelState.Success -> {
                ShoppingListContent(
                    shoppingList = (shoppingListState as ViewModelState.Success<ShoppingList>).data,
                    selectedCategory = selectedCategory,
                    availableWeeks = availableWeeks,
                    selectedWeek = selectedWeek,
                    onWeekSelected = { viewModel.selectWeek(it) },
                    onCategorySelected = { viewModel.selectedCategory(it) },
                    getFormattedWeekDate = { viewModel.getFormattedWeekDate(it) },
                    checkedProducts = checkedProducts,
                    onProductCheckedChange = { productName, _ ->
                        viewModel.toggleProductCheck(productName)
                    }
                )
            }
        }
    }
}

@Composable
private fun ShoppingListContent(
    shoppingList: ShoppingList,
    selectedCategory: String?,
    availableWeeks: List<Long>,
    selectedWeek: Long?,
    onWeekSelected: (Long) -> Unit,
    onCategorySelected: (String?) -> Unit,
    getFormattedWeekDate: (Long) -> String,
    checkedProducts: Set<String>,
    onProductCheckedChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        WeekSelector(
            availableWeeks = availableWeeks,
            selectedWeek = selectedWeek,
            onWeekSelected = onWeekSelected,
            getFormattedWeekDate = getFormattedWeekDate,
            modifier = Modifier.padding(vertical = 16.dp)
        )

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