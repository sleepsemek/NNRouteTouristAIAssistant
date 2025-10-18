package com.sleepsemek.nnroutetouristaiassistant.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sleepsemek.nnroutetouristaiassistant.R
import com.sleepsemek.nnroutetouristaiassistant.viewmodels.RoutesViewModel

enum class InterestCategory(
    val displayName: String,
    val backgroundImage: Int
) {
    STREET_ART("Стрит-арт", R.drawable.street_art_bg),
    HISTORY("История", R.drawable.history_bg),
    COFFEE("Кофейни", R.drawable.coffee_bg),
    PANORAMAS("Панорамы", R.drawable.panorama_bg),
    ARCHITECTURE("Архитектура", R.drawable.architecture_bg),
    PARKS("Парки", R.drawable.parks_bg),
    SHOPPING("Шоппинг", R.drawable.shopping_bg),
    FOOD("Еда", R.drawable.food_bg),
}

@Composable
fun RoutePlanningSheet(
    sheetController: BottomSheetController,
    viewModel: RoutesViewModel,
    isLoading: Boolean,
    error: String?
) {
    val uiState by viewModel.uiState.collectAsState()

    val selectedInterests = uiState.selectedInterests
    val walkingTime = uiState.walkingTime
    val useLocation = uiState.useLocation
    val errorContainerVisible = error != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Планировщик маршрута",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                InterestsSection(selectedInterests) { viewModel.updateSelectedInterests(it) }
            }

            item {
                TimeSelectionSection(
                    walkingTime = walkingTime,
                    onWalkingTimeChange = { viewModel.updateWalkingTime(it) },
                    useLocation = useLocation,
                    onUseLocationChange = { viewModel.updateUseLocation(it) }
                )
            }

            item {
                Column (
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedVisibility(
                        visible = errorContainerVisible,
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
                        modifier = Modifier.animateItem()
                    ) {
                        ErrorMessage(error = error.toString())
                    }

                    BuildRouteButton(
                        viewModel = viewModel,
                        isLoading = isLoading
                    )
                }
            }
        }
    }
}

@Composable
private fun InterestsSection(
    selectedInterests: Set<InterestCategory>,
    onInterestChange: (Set<InterestCategory>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Категории интересов",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        val chunks = InterestCategory.entries.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            chunks.forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { category ->
                        Box(modifier = Modifier.weight(1f)) {
                            InterestCard(
                                category = category,
                                isSelected = selectedInterests.contains(category),
                                onSelect = {
                                    val newSelection = if (selectedInterests.contains(category)) {
                                        selectedInterests - category
                                    } else {
                                        selectedInterests + category
                                    }
                                    onInterestChange(newSelection)
                                }
                            )
                        }
                    }

                    if (rowItems.size == 1) {
                        Box(modifier = Modifier.weight(1f)) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun InterestCard(
    category: InterestCategory,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(7f/4f),
        shape = MaterialTheme.shapes.medium,
        border = if (isSelected) BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.tertiary
        ) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onSelect
    ) {
        Box(
            modifier = Modifier
                .paint(
                    painter = painterResource(id = category.backgroundImage),
                    contentScale = ContentScale.FillBounds
                )
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSelectionSection(
    walkingTime: Float,
    onWalkingTimeChange: (Float) -> Unit,
    useLocation: Boolean,
    onUseLocationChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Время на прогулку",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Slider(
                value = walkingTime,
                onValueChange = onWalkingTimeChange,
                valueRange = 0.5f..8f,
                steps = 14,
                modifier = Modifier.fillMaxWidth(),
            )
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Badge(
                    text = "%.1f ч".format(walkingTime),
                    color = MaterialTheme.colorScheme.primary,
                    icon = null
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "С учетом местоположения",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = useLocation,
                        onCheckedChange = onUseLocationChange
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(error: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun BuildRouteButton(
    viewModel: RoutesViewModel,
    isLoading: Boolean,
) {
    Button(
        onClick = {
            viewModel.loadPointsOfInterest()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.medium,
        enabled = !isLoading
    ) {
        if (isLoading) {
            Row (
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Думаем",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Text(
                text = "Построить маршрут",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}