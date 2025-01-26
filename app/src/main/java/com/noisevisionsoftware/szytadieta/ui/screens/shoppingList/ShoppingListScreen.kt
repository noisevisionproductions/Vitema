package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.ShoppingList
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.CustomErrorMessage
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.common.LoadingOverlay
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.NoShoppingListMessage
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.ProductList
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.ShoppingListPeriodSelector

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit
) {
    val shoppingListState by viewModel.shoppingListState.collectAsState()
    val availablePeriods by viewModel.availablePeriods.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
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
            onRefreshClick = { viewModel.onRefreshData() }
        )

        if (availablePeriods.isNotEmpty() && shoppingListState !is ViewModelState.Loading) {
            ShoppingListPeriodSelector(
                availablePeriods = availablePeriods,
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { newPeriod -> viewModel.selectPeriod(newPeriod) }
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
                if (shoppingList.items.isEmpty()) {
                    NoShoppingListMessage(
                        hasAnyShoppingLists = availablePeriods.isNotEmpty(),
                        onNavigateToAvailablePeriod = if (availablePeriods.isNotEmpty()) {
                            { viewModel.navigateToClosestAvailableWeek() }
                        } else null,
                        onNavigate = onNavigate
                    )
                } else {
                    ShoppingListContent(
                        shoppingList = shoppingList,
                        checkedProducts = checkedProducts,
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
    checkedProducts: Set<String>,
    onProductCheckedChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        ProductList(
            products = shoppingList.items,
            checkedProducts = checkedProducts,
            onProductCheckedChange = onProductCheckedChange
        )
    }
}