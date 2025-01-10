package com.noisevisionsoftware.szytadieta.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.subscription.SubscriptionPlan
import com.noisevisionsoftware.szytadieta.domain.model.subscription.SubscriptionType
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPlanScreen(
    viewModel: SubscriptionViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit
) {
    val subscriptionPlans = listOf(
        SubscriptionPlan(
            id = "basic",
            name = "Plan Basic",
            description = "Podstawowy plan z dostępem do spersonalizowanej diety",
            price = 49.99,
            features = listOf(
                "Spersonalizowany plan posiłków",
                "Lista zakupów",
                "Podstawowe przepisy",
                "Wsparcie email"
            ),
            durationInMonths = 1,
            type = SubscriptionType.BASIC
        ),
        SubscriptionPlan(
            id = "standard",
            name = "Plan Standard",
            description = "Rozszerzony plan z dodatkowymi funkcjami",
            price = 79.99,
            features = listOf(
                "Wszystko z planu Basic",
                "Alternatywne przepisy",
                "Konsultacje z dietetykiem",
                "Analiza postępów",
                "Wsparcie telefoniczne"
            ),
            durationInMonths = 1,
            type = SubscriptionType.STANDARD
        ),
        SubscriptionPlan(
            id = "premium",
            name = "Plan Premium",
            description = "Pełen dostęp do wszystkich funkcji",
            price = 129.99,
            features = listOf(
                "Wszystko z planu Standard",
                "Priorytetowe wsparcie 24/7",
                "Spersonalizowane treningi",
                "Cotygodniowe konsultacje",
                "Analiza składu ciała",
                "Indywidualne dostosowanie diety"
            ),
            durationInMonths = 1,
            type = SubscriptionType.PREMIUM
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopAppBar(
            title = "Plany subskrypcji",
            onBackClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Dashboard) }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Wybierz plan dla siebie\n" +
                            "Testowe plany",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(
                        bottom = 8.dp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            items(subscriptionPlans) { plan ->
                SubscriptionPlanCard(
                    plan = plan,
                    onSelectPlan = { viewModel.selectPlan() }
                )
            }
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    onSelectPlan: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectPlan() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${plan.price} zł/msc",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = plan.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                plan.features.forEach { feature ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Button(
                onClick = onSelectPlan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Wybierz plan")
            }
        }
    }
}