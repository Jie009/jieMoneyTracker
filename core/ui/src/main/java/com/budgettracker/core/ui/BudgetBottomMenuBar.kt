package com.budgettracker.core.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BottomMenuDestination {
    Home,
    Charts,
    Add,
}

@Composable
fun BudgetBottomMenuBar(
    selectedDestination: BottomMenuDestination,
    onDestinationClick: (BottomMenuDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, BottomMenuStroke, RoundedCornerShape(24.dp))
            .background(BottomMenuPanel.copy(alpha = 0.92f))
            .padding(horizontal = 14.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val selectedIndex = BottomMenuItems.indexOfFirst { it.destination == selectedDestination }
                .coerceAtLeast(0)
            val itemWidth = maxWidth / BottomMenuItems.size
            val indicatorOffset = animateDpAsState(
                targetValue = itemWidth * selectedIndex + (itemWidth - SelectedIndicatorSize) / 2,
                animationSpec = tween(
                    durationMillis = 320,
                    easing = FastOutSlowInEasing,
                ),
                label = "bottom-menu-indicator",
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset.value, y = 0.dp)
                    .size(SelectedIndicatorSize)
                    .clip(CircleShape)
                    .background(BottomMenuSelectedPanel),
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BottomMenuItems.forEach { item ->
                    BottomMenuItem(
                        label = item.label,
                        icon = item.icon,
                        selected = selectedDestination == item.destination,
                        onClick = { onDestinationClick(item.destination) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomMenuItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White.copy(alpha = if (selected) 1f else 0.7f),
                modifier = Modifier.size(if (selected) 22.dp else 20.dp),
            )
        }
        Text(
            text = label,
            color = Color.White.copy(alpha = if (selected) 1f else 0.65f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private data class BottomMenuItemSpec(
    val destination: BottomMenuDestination,
    val label: String,
    val icon: ImageVector,
)

private val BottomMenuItems = listOf(
    BottomMenuItemSpec(BottomMenuDestination.Home, "Home", Icons.Filled.Home),
    BottomMenuItemSpec(BottomMenuDestination.Add, "Add", Icons.Filled.Add),
    BottomMenuItemSpec(BottomMenuDestination.Charts, "Analytics", Icons.Filled.PieChart),
)

private val SelectedIndicatorSize = 42.dp
private val BottomMenuPanel = Color(0xFF101B37)
private val BottomMenuSelectedPanel = Color(0xFF1C4E8B)
private val BottomMenuStroke = Color(0xFF243659)
