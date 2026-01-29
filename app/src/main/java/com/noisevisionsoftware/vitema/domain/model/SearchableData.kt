package com.noisevisionsoftware.vitema.domain.model

data class SearchableData<T>(
    val items: List<T>,
    val searchQuery: String = "",
    val filteredItems: List<T> = items,
    val searchPredicate: (T, String) -> Boolean = { _, _ -> true }
) {
    fun updateSearch(query: String): SearchableData<T> {
        return copy(
            searchQuery = query,
            filteredItems = if (query.isBlank()) {
                items
            } else {
                items.filter { item -> searchPredicate(item, query) }
            }
        )
    }

    companion object {
        fun <T> create(
            items: List<T>,
            searchPredicate: (T, String) -> Boolean
        ): SearchableData<T> {
            return SearchableData(
                items = items,
                searchPredicate = searchPredicate
            )
        }
    }
}