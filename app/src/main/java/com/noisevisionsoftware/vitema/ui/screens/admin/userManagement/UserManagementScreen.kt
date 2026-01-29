package com.noisevisionsoftware.vitema.ui.screens.admin.userManagement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.common.SearchableContent
import com.noisevisionsoftware.vitema.ui.screens.admin.userManagement.components.UserList

@Composable
fun UserManagementScreen(
    viewModel: UserManagementViewModel = hiltViewModel(),
    showSearchBar: Boolean,
    onSearchBarVisibilityChange: (Boolean) -> Unit
) {
    val userState by viewModel.userState.collectAsState()

    SearchableContent(
        state = userState,
        searchBarPlaceholder = "Wyszukaj użytkownika...",
        onSearch = viewModel::searchUsers,
        showSearchBar = showSearchBar,
        onSearchBarVisibilityChange = onSearchBarVisibilityChange
    ) { users ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            UsersCounter(
                totalUsers = (userState as? ViewModelState.Success)?.data?.items?.size ?: 0,
                filteredUsers = users.size,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            UserList(
                users = users,
                onRoleChange = viewModel::updateUserRole
            )
        }
    }
}

@Composable
private fun UsersCounter(
    totalUsers: Int,
    filteredUsers: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (filteredUsers != totalUsers) {
            "Wyświetlono: $filteredUsers z $totalUsers użytkowników"
        } else {
            "Liczba użytkowników: $totalUsers"
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

