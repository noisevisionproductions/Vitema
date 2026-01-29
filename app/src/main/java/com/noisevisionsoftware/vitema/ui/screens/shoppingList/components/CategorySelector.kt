package com.noisevisionsoftware.vitema.ui.screens.shoppingList.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.vitema.domain.model.shopping.CategorizedShoppingList
import com.noisevisionsoftware.vitema.domain.model.shopping.ProductCategory
import com.noisevisionsoftware.vitema.domain.model.shopping.ShoppingListItem
import java.util.Locale

@Composable
fun CategorySelector(
    categories: List<ProductCategory>,
    selectedCategory: ProductCategory?,
    onCategorySelected: (ProductCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Wszystko") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) },
                leadingIcon = {
                    Icon(
                        imageVector = category.icon.toIcon(),
                        contentDescription = null,
                        tint = Color(android.graphics.Color.parseColor(category.color))
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun CategorizedShoppingList(
    shoppingList: CategorizedShoppingList,
    selectedCategory: ProductCategory?,
    checkedProducts: Set<String>,
    categoryProgress: Map<ProductCategory, Pair<Int, Int>>,
    onProductCheckedChange: (ShoppingListItem, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = if (selectedCategory != null) {
        listOf(selectedCategory)
    } else {
        shoppingList.getCategoriesWithItems()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        categories.forEach { category ->
            val items = shoppingList.getItemsForCategory(category)
            item(key = category.id) {
                AnimatedCategoryTransition(
                    category = category,
                    items = items,
                    progress = categoryProgress[category] ?: (0 to 0),
                    checkedProducts = checkedProducts,
                    onProductCheckedChange = onProductCheckedChange
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    category: ProductCategory,
    items: List<ShoppingListItem>,
    progress: Pair<Int, Int>,
    checkedProducts: Set<String>,
    onProductCheckedChange: (ShoppingListItem, Boolean) -> Unit
) {
    var isCheckedItemsExpanded by remember { mutableStateOf(false) }

    val (checkedItems, uncheckedItems) = remember(items, checkedProducts) {
        items.partition { checkedProducts.contains(it.name) }
    }

    Card(
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryHeader(
                category = category,
                progress = progress
            )

            uncheckedItems.forEach { item ->
                ShoppingListItemRow(
                    item = item,
                    isChecked = false,
                    onCheckedChange = { checked ->
                        onProductCheckedChange(item, checked)
                    }
                )
            }

            if (checkedItems.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCheckedItemsExpanded = !isCheckedItemsExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kupione (${checkedItems.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Icon(
                        imageVector = if (isCheckedItemsExpanded) {
                            Icons.Default.ExpandLess
                        } else {
                            Icons.Default.ExpandMore
                        },
                        contentDescription = if (isCheckedItemsExpanded) {
                            "Zwiń kupione"
                        } else {
                            "Rozwiń kupione"
                        },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = isCheckedItemsExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        checkedItems.forEach { item ->
                            ShoppingListItemRow(
                                item = item,
                                isChecked = true,
                                onCheckedChange = { checked ->
                                    onProductCheckedChange(item, checked)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    category: ProductCategory,
    progress: Pair<Int, Int>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon.toIcon(),
                contentDescription = null,
                tint = Color(android.graphics.Color.parseColor(category.color)),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        BadgeWithProgress(
            checked = progress.first,
            total = progress.second
        )
    }
}

@Composable
private fun ShoppingListItemRow(
    item: ShoppingListItem,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.scale(0.9f)
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.quantity > 0 && item.unit.isNotBlank()) {
                Text(
                    text = formatQuantity(item.quantity, item.unit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.widthIn(min = 60.dp)
                )
            }

            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isChecked) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (isChecked) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BadgeWithProgress(
    checked: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(28.dp)
            .widthIn(min = 48.dp),
        shape = CircleShape,
        color = when {
            checked == total -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            checked > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$checked/$total",
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    checked == total -> MaterialTheme.colorScheme.primary
                    checked > 0 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

fun String.toIcon(): ImageVector {
    return when (this) {
        "milk" -> Icons.Default.LocalDrink
        "fish" -> Icons.Default.SetMeal
        "carrot" -> Icons.Default.Restaurant
        "apple" -> Icons.Default.Fastfood
        "wheat" -> Icons.Default.Grass
        "soup" -> Icons.Default.Restaurant
        "droplet" -> Icons.Default.WaterDrop
        "nut" -> Icons.Default.Cookie
        "beer" -> Icons.Default.WaterDrop
        "box" -> Icons.Default.Inventory
        "snowflake" -> Icons.Default.WaterDrop
        "cookie" -> Icons.Default.Cookie
        "package" -> Icons.Default.Inventory
        else -> Icons.Default.ShoppingBasket
    }
}

@Composable
private fun AnimatedCategoryTransition(
    category: ProductCategory,
    items: List<ShoppingListItem>,
    progress: Pair<Int, Int>,
    checkedProducts: Set<String>,
    onProductCheckedChange: (ShoppingListItem, Boolean) -> Unit
) {
    AnimatedVisibility(
        visible = items.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        CategorySection(
            category = category,
            items = items,
            progress = progress,
            checkedProducts = checkedProducts,
            onProductCheckedChange = onProductCheckedChange
        )
    }
}

private fun formatQuantity(quantity: Double, unit: String): String {
    val formattedQuantity = if (quantity % 1 == 0.0) {
        quantity.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", quantity).replace(".0", "")
    }
    return "$formattedQuantity $unit"
}