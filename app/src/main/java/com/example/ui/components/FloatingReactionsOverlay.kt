package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.FloatingReactionItem
import com.example.ui.theme.DarkSurface
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Overlay container displaying animated floating emotion reactions streaming upwards.
 */
@Composable
fun FloatingReactionsOverlay(
  reactions: List<FloatingReactionItem>,
  onReactionFinished: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.fillMaxSize()) {
    reactions.forEach { item ->
      SingleReactionBubble(
        item = item,
        onFinished = { onReactionFinished(item.id) }
      )
    }
  }
}

@Composable
private fun SingleReactionBubble(
  item: FloatingReactionItem,
  onFinished: () -> Unit
) {
  val configuration = LocalConfiguration.current
  val density = LocalDensity.current
  val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
  val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

  var progress by remember { mutableFloatStateOf(0f) }

  val animatedProgress by animateFloatAsState(
    targetValue = progress,
    animationSpec = tween(durationMillis = 2200, easing = LinearEasing),
    finishedListener = { onFinished() },
    label = "reaction_float"
  )

  LaunchedEffect(item.id) {
    progress = 1f
  }

  // Calculate trajectory: start near bottom dock, drift upward with horizontal sine wave
  val currentY = screenHeightPx * (item.startYFraction - animatedProgress * 0.75f)
  val waveOffset = sin(animatedProgress * Math.PI.toFloat() * 3f) * (screenWidthPx * 0.08f)
  val currentX = (screenWidthPx * item.xFraction) + waveOffset + (item.driftXFraction * screenWidthPx)

  // Fade out towards the top
  val currentAlpha = (1f - (animatedProgress * 1.1f)).coerceIn(0f, 1f)
  val currentScale = (0.7f + animatedProgress * 0.5f).coerceIn(0.5f, 1.4f)

  Box(
    modifier = Modifier
      .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
      .scale(currentScale)
      .alpha(currentAlpha)
      .size(46.dp)
      .clip(CircleShape)
      .background(DarkSurface),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = item.emotion.icon,
      contentDescription = item.emotion.label,
      tint = item.emotion.color,
      modifier = Modifier.size(26.dp)
    )
  }
}
