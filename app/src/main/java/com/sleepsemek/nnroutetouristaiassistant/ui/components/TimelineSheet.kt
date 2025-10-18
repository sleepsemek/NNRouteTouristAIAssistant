package com.sleepsemek.nnroutetouristaiassistant.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sleepsemek.nnroutetouristaiassistant.R
import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteResponse

@Composable
fun TimelineSheet(
    sheetController: BottomSheetController,
    routes: List<RouteResponse>,
    onClose: () -> Unit,
    onSelectStep: (Int) -> Unit
) {
    var expandedIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ваш маршрут",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            routes.forEachIndexed { index, route ->
                TimelineItem(
                    route = route,
                    isExpanded = expandedIndex == index,
                    isFirst = index == 0,
                    isLast = index == routes.size - 1,
                    onClick = {
                        expandedIndex = if (expandedIndex == index) -1 else index
                    },
                    onNavigate = {
                        onSelectStep(index)
                        sheetController.collapse()
                    }
                )
            }
        }
    }
}

@Composable
fun TimelineItem(
    route: RouteResponse,
    isExpanded: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onNavigate: () -> Unit
) {
    var cardHeight by remember { mutableIntStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(with(LocalDensity.current) { cardHeight.toDp() }),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isFirst) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isFirst -> Icon(
                            painter = painterResource(R.drawable.baseline_directions_walk_24),
                            contentDescription = "Старт",
                            tint = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.size(16.dp)
                        )
                        isLast -> Icon(
                            painter = painterResource(R.drawable.baseline_flag_24),
                            contentDescription = "Финиш",
                            tint = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.size(16.dp)
                        )
                        else -> Icon(
                            painter = painterResource(R.drawable.baseline_circle_24),
                            contentDescription = "Промежуточная точка",
                            tint = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }

                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        Card(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .weight(1f)
                .animateContentSize()
                .onGloballyPositioned { coordinates ->
                    cardHeight = coordinates.size.height
                }
                .padding(
                    top = if (isFirst) 0.dp else 6.dp,
                    bottom = if (isLast) 0.dp else 6.dp
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = route.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = route.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded)
                            Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (isExpanded) {
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Badge(
                            icon = Icons.Default.DateRange,
                            text = "${route.duration} мин",
                            color = MaterialTheme.colorScheme.primary
                        )
                        Badge(
                            icon = Icons.Default.Place,
                            text = "${route.distance} м",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = route.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onNavigate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Посмотреть на карте")
                    }
                }
            }
        }
    }
}

