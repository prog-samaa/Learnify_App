package com.example.learnify.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloat


@Composable
fun AnimatedRefreshButton(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    enabledWhenIdle: Boolean = true,
    idleTint: Color,
    refreshingTint: Color = Color.Gray,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_rotation")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing)
        ),
        label = "rotation_anim"
    )

    IconButton(
        onClick = onRefresh,
        enabled = enabledWhenIdle && !isRefreshing
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh",
            tint = if (isRefreshing) refreshingTint else idleTint,
            modifier = Modifier
                .size(26.dp)
                .rotate(if (isRefreshing) rotation else 0f)
        )
    }
}
