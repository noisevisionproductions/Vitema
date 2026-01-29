package com.noisevisionsoftware.vitema.ui.screens.mealPlan.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.vitema.domain.model.health.dietPlan.Recipe
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.common.CustomErrorMessage
import com.noisevisionsoftware.vitema.ui.common.CustomTopAppBar
import com.noisevisionsoftware.vitema.ui.common.LoadingOverlay
import com.noisevisionsoftware.vitema.ui.navigation.NavigationDestination
import com.noisevisionsoftware.vitema.ui.navigation.NavigationViewModel
import com.noisevisionsoftware.vitema.ui.screens.mealPlan.components.RecipeImagesCarousel
import com.noisevisionsoftware.vitema.ui.screens.mealPlan.recipe.components.InstructionsCard
import com.noisevisionsoftware.vitema.ui.screens.mealPlan.recipe.components.NutritionalValuesCard

@Composable
fun RecipeScreen(
    recipeId: String,
    viewModel: RecipeViewModel = hiltViewModel(),
    navigationViewModel: NavigationViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit
) {
    val recipeState by viewModel.recipeState.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            navigationViewModel.clearRecipeId()
        }
    }

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopAppBar(
            title = "Przepis",
            onBackClick = { onNavigate(NavigationDestination.AuthenticatedDestination.MealPlan) }
        )

        when (recipeState) {
            is ViewModelState.Initial -> Unit
            is ViewModelState.Loading -> LoadingOverlay()
            is ViewModelState.Error -> CustomErrorMessage(
                message = (recipeState as ViewModelState.Error).message
            )

            is ViewModelState.Success -> {
                val recipe = (recipeState as ViewModelState.Success<Recipe>).data
                RecipeContent(recipe = recipe)
            }
        }
    }
}

@Composable
private fun RecipeContent(
    recipe: Recipe,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (recipe.photos.isNotEmpty()) {
            item {
                RecipeImagesCarousel(
                    photos = recipe.photos,
                    modifier = Modifier.fillMaxWidth(),
                    initialDelayMillis = 300
                )
            }
        }

        item {
            NutritionalValuesCard(nutritionalValues = recipe.nutritionalValues)
        }
        /*

                item {
                    IngredientsCard(ingredients = recipe.ingredients)
                }
        */

        item {
            InstructionsCard(instructions = recipe.instructions)
        }
    }
}
/*

@Composable
private fun IngredientsCard(
    ingredients: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Składniki (${ingredients.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${ingredients.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded)
                                Icons.Default.ExpandLess
                            else
                                Icons.Default.ExpandMore,
                            contentDescription = if (expanded)
                                "Zwiń składniki"
                            else
                                "Rozwiń składniki",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ingredients.forEach { ingredient ->
                        IngredientItem(ingredient = ingredient)
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientItem(
    ingredient: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikona składnika
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = ingredient,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}*/
