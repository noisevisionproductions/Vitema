package com.noisevisionsoftware.vitema.ui.screens.shoppingList.components.grouping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.vitema.domain.model.shopping.MealGroup
import com.noisevisionsoftware.vitema.domain.model.shopping.ShoppingListGroup


@Composable
fun DayGroupCard(
    group: ShoppingListGroup.ByDay,
    checkedProducts: Set<String>,
    onProductCheckedChange: (String, Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val allProductsChecked = group.items.all { checkedProducts.contains(it.productId) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                allProductsChecked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dzień ${group.dayIndex + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    color = if (allProductsChecked)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )

              /*  BadgeWithProgress(
                    total = group.items.size,
                    checked = group.items.count { checkedProducts.contains(it.productId) }
                )*/

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (isExpanded) {
                group.meals.forEach { mealGroup ->
                    MealGroupSection(
                        mealGroup = mealGroup,
                        checkedProducts = checkedProducts,
                        onProductCheckedChange = onProductCheckedChange
                    )
                }
            }
        }
    }
}

@Composable
private fun MealGroupSection(
    mealGroup: MealGroup,
    checkedProducts: Set<String>,
    onProductCheckedChange: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
       /* Text(
            text = "${getMealTypeLabel(mealGroup.mealType)} (${mealGroup.mealTime})",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
*/
        mealGroup.recipes.forEach { (recipeName, items) ->
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = recipeName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                ProductListForGrouping(
                    products = items,
                    checkedProducts = checkedProducts,
                    onProductCheckedChange = onProductCheckedChange
                )
            }
        }
    }
}