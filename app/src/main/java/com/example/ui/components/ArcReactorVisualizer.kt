package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssistantState
import com.example.ui.theme.ArcBlueSecondary
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.ArcVioletTertiary
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextMutedBlue
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
    assistantState: AssistantState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_reactor")

    // Slow continuous rotation for HUD rings
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Fast counter rotation for processing
    val fastCounterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fast_rotation"
    )

    // Breathing pulse for idle & speaking
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreColor = when (assistantState) {
        is AssistantState.Error -> StatusRed
        is AssistantState.Listening -> ArcCyanPrimary
        is AssistantState.Processing -> ArcVioletTertiary
        is AssistantState.Speaking -> ArcCyanGlow
        AssistantState.Idle -> ArcBlueSecondary
    }

    val stateLabel = when (assistantState) {
        is AssistantState.Error -> "SYSTEM ALERT"
        is AssistantState.Listening -> "LISTENING..."
        is AssistantState.Processing -> "PROCESSING INTELLIGENCE..."
        is AssistantState.Speaking -> "JARVIS VOCALIZING"
        AssistantState.Idle -> "JARVIS STANDBY"
    }

    Column(
        modifier = modifier.testTag("arc_reactor_visualizer"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(190.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val baseRadius = size.minDimension / 2.3f

                val isListening = assistantState is AssistantState.Listening
                val rms = if (isListening) (assistantState as AssistantState.Listening).rmsDb else 0f
                val audioExpansion = (rms / 10f) * 16.dp.toPx()

                // Background subtle glow circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(coreColor.copy(alpha = 0.28f), Color.Transparent),
                        center = center,
                        radius = baseRadius * 1.35f
                    ),
                    center = center,
                    radius = baseRadius * 1.35f
                )

                // Outer decorative HUD tick marks
                rotate(degrees = rotationAngle, pivot = center) {
                    val numTicks = 32
                    for (i in 0 until numTicks) {
                        val angleRad = Math.toRadians((i * (360f / numTicks)).toDouble())
                        val r1 = baseRadius * 0.94f
                        val r2 = baseRadius * (if (i % 4 == 0) 1.04f else 0.98f)
                        val start = Offset(
                            center.x + (r1 * cos(angleRad)).toFloat(),
                            center.y + (r1 * sin(angleRad)).toFloat()
                        )
                        val end = Offset(
                            center.x + (r2 * cos(angleRad)).toFloat(),
                            center.y + (r2 * sin(angleRad)).toFloat()
                        )
                        drawLine(
                            color = coreColor.copy(alpha = if (i % 4 == 0) 0.7f else 0.3f),
                            start = start,
                            end = end,
                            strokeWidth = if (i % 4 == 0) 2.dp.toPx() else 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Intermediate counter-rotating arcs
                rotate(degrees = fastCounterRotation, pivot = center) {
                    val arcRadius = baseRadius * 0.78f
                    val arcSize = Size(arcRadius * 2, arcRadius * 2)
                    val arcTopLeft = Offset(center.x - arcRadius, center.y - arcRadius)

                    drawArc(
                        color = coreColor.copy(alpha = 0.85f),
                        startAngle = 30f,
                        sweepAngle = 75f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = coreColor.copy(alpha = 0.85f),
                        startAngle = 150f,
                        sweepAngle = 75f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = coreColor.copy(alpha = 0.85f),
                        startAngle = 270f,
                        sweepAngle = 75f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner reactive audio / pulse ring
                val reactiveRadius = (baseRadius * 0.52f * (if (isListening) 1f else pulseScale)) + audioExpansion
                drawCircle(
                    color = coreColor.copy(alpha = 0.45f),
                    center = center,
                    radius = reactiveRadius,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Glowing central arc reactor core
                val coreRadius = baseRadius * 0.28f * (if (assistantState is AssistantState.Speaking) pulseScale else 1f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, coreColor, coreColor.copy(alpha = 0.15f)),
                        center = center,
                        radius = coreRadius
                    ),
                    center = center,
                    radius = coreRadius
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stateLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.8.sp,
            color = coreColor
        )
    }
}
