package com.stylishlauncher.util

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object Animations {

    // Spring configurations for different feels
    object SpringConfig {
        val Gentle = spring(dampingRatio = 0.9f, stiffness = 200f)
        val Standard = spring(dampingRatio = 0.8f, stiffness = 300f)
        val Snappy = spring(dampingRatio = 0.7f, stiffness = 400f)
        val Bouncy = spring(dampingRatio = 0.5f, stiffness = 500f)
        val Drawer = spring(dampingRatio = 0.85f, stiffness = 350f)
        val Modal = spring(dampingRatio = 0.9f, stiffness = 400f)
    }

    object TweenConfig {
        val Fast = tween(150)
        val Standard = tween(250)
        val Slow = tween(400)
        val Enter = tween(300, easing = { it * it * it }) // easeInOutCubic-ish
        val Exit = tween(200, easing = { 1 - (1 - it) * (1 - it) }) // easeOutQuad-ish
    }

    // Animate Dp with spring
    @Composable
    fun animateDp(
        targetValue: androidx.compose.ui.unit.Dp,
        config: androidx.compose.animation.core.SpringSpec<Float> = SpringConfig.Standard,
        label: String = "animateDp",
    ): androidx.compose.ui.unit.Dp {
        return animateDpAsState(targetValue, config, label).value
    }

    // Animate Float with spring
    @Composable
    fun animateFloat(
        targetValue: Float,
        config: androidx.compose.animation.core.SpringSpec<Float> = SpringConfig.Standard,
        label: String = "animateFloat",
    ): Float {
        return animateFloatAsState(targetValue, config, label).value
    }

    // Animate scale (0.0 to 1.0)
    @Composable
    fun animateScale(
        isVisible: Boolean,
        config: androidx.compose.animation.core.SpringSpec<Float> = SpringConfig.Snappy,
    ): Float {
        return animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = config,
            label = "animateScale"
        ).value
    }

    // Animate alpha (0.0 to 1.0)
    @Composable
    fun animateAlpha(
        isVisible: Boolean,
        config: androidx.compose.animation.core.SpringSpec<Float> = SpringConfig.Standard,
    ): Float {
        return animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = config,
            label = "animateAlpha"
        ).value
    }

    // Animate offset Y for slide animations
    @Composable
    fun animateSlideY(
        isVisible: Boolean,
        visibleOffset: androidx.compose.ui.unit.Dp = 0.dp,
        hiddenOffset: androidx.compose.ui.unit.Dp = 100.dp,
        config: androidx.compose.animation.core.SpringSpec<Float> = SpringConfig.Drawer,
    ): androidx.compose.ui.unit.Dp {
        return animateDpAsState(
            targetValue = if (isVisible) visibleOffset else hiddenOffset,
            animationSpec = config,
            label = "animateSlideY"
        ).value
    }

    // Animate width for expanding/collapsing
    @Composable
    fun animateWidth(
        isExpanded: Boolean,
        expandedWidth: androidx.compose.ui.unit.Dp,
        collapsedWidth: androidx.compose.ui.unit.Dp,
        config: androidx.compose.animation.core.SpringSpec<Float> = SpringConfig.Standard,
    ): androidx.compose.ui.unit.Dp {
        return animateDpAsState(
            targetValue = if (isExpanded) expandedWidth else collapsedWidth,
            animationSpec = config,
            label = "animateWidth"
        ).value
    }

    // Animate height
    @Composable
    fun animateHeight(
        isExpanded: Boolean,
        expandedHeight: androidx.compose.ui.unit.Dp,
        collapsedHeight: androidx.compose.ui.unit.Dp,
        config: androidx.compose.animation.core.SpringSpec<Float> = SpringConfig.Standard,
    ): androidx.compose.ui.unit.Dp {
        return animateDpAsState(
            targetValue = if (isExpanded) expandedHeight else collapsedHeight,
            animationSpec = config,
            label = "animateHeight"
        ).value
    }

    // Staggered animation helper for lists
    @Composable
    fun <T> staggeredAnimation(
        items: List<T>,
        baseDelay: Long = 50,
        itemDelay: Long = 30,
        config: androidx.compose.animation.core.SpringSpec<Float> = SpringConfig.Standard,
    ): List<Float> {
        return items.mapIndexed { index, _ ->
            animateFloatAsState(
                targetValue = 1f,
                animationSpec = config,
                label = "staggered_$index"
            ).value
        }
    }

    // Shared element transition key
    @Composable
    fun <T> sharedTransitionKey(item: T, prefix: String = "shared"): String {
        return "${prefix}_${item.hashCode()}"
    }

    // Pulse animation (for loading states)
    @Composable
    fun pulseAnimation(
        min: Float = 0.5f,
        max: Float = 1f,
        durationMillis: Int = 1000,
    ): Float {
        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
        return infiniteTransition.animateFloat(
            initialValue = min,
            targetValue = max,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = tween(durationMillis, easing = { it * it * (3 - 2 * it) }), // easeInOutCubic
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
            ),
            label = "pulse",
        )
    }

    // Rotate animation
    @Composable
    fun rotateAnimation(
        isRotating: Boolean,
        degrees: Float = 360f,
        durationMillis: Int = 1000,
    ): Float {
        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "rotate")
        return infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = degrees,
            animationSpec = if (isRotating) {
                androidx.compose.animation.core.infiniteRepeatable(
                    animation = tween(durationMillis, easing = androidx.compose.animation.core.LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
                )
            } else {
                androidx.compose.animation.core.snap()
            },
            label = "rotate",
        )
    }

    // Shimmer animation for placeholders
    @Composable
    fun shimmerAnimation(
        width: Float,
        durationMillis: Int = 1500,
    ): Float {
        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")
        return infiniteTransition.animateFloat(
            initialValue = -width * 2,
            targetValue = width * 2,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = tween(durationMillis, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
            ),
            label = "shimmer",
        )
    }
}