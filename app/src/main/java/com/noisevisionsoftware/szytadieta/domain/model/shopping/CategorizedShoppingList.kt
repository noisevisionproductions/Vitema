package com.noisevisionsoftware.szytadieta.domain.model.shopping

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CategorizedShoppingList(
    val id: String = "",
    val userId: String = "",
    val dietId: String = "",
    val version: Int = 3,

    @PropertyName("items")
    val items: Map<String, List<ShoppingListItem>> = emptyMap(),

    @field:JvmField
    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @field:JvmField
    @PropertyName("startDate")
    val startTimestamp: Timestamp = Timestamp.now(),

    @field:JvmField
    @PropertyName("endDate")
    val endTimestamp: Timestamp = Timestamp.now()
) {
    val startDate: String
        get() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(startTimestamp.seconds * 1000))

    val endDate: String
        get() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(endTimestamp.seconds * 1000))

    @get:Exclude
    val allProducts: List<ShoppingListItem>
        get() = items.values.flatten()

    fun getItemsForCategory(category: ProductCategory):List<ShoppingListItem> = items[category.id] ?: emptyList()

    fun getCategoriesWithItems(): List<ProductCategory> = items.keys.map { categoryId ->
        ProductCategory.fromId(categoryId)
    }.sortedBy { it.ordinal }
}

data class ShoppingListItem(
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val original: String = "",
    val context: ShoppingListProductContext? = null,

    @field:JvmField
    @PropertyName("hasCustomUnit")
    val hasCustomUnit: Boolean = false,

    @field:JvmField
    @PropertyName("categoryId")
    val categoryId: String = ""
)