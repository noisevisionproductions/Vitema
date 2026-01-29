package com.noisevisionsoftware.vitema.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.noisevisionsoftware.vitema.domain.model.SearchableData
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.screens.admin.ErrorMessage

@Composable
fun <T> SearchableContent(
    state: ViewModelState<SearchableData<T>>,
    searchBarPlaceholder: String,
    onSearch: (String) -> Unit,
    showSearchBar: Boolean,
    onSearchBarVisibilityChange: (Boolean) -> Unit,
    content: @Composable (List<T>) -> Unit
) {
    Column {
        CustomSearchBar(
            visible = showSearchBar,
            onVisibilityChange = onSearchBarVisibilityChange,
            onSearch = onSearch,
            placeholder = searchBarPlaceholder,
            initialQuery = if (state is ViewModelState.Success) state.data.searchQuery else ""
        )

        when (state) {
            is ViewModelState.Initial -> Unit
            is ViewModelState.Loading -> LoadingIndicator()
            is ViewModelState.Success -> content(state.data.filteredItems)
            is ViewModelState.Error -> ErrorMessage(message = state.message)
        }
    }
}