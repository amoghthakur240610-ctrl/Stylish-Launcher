package com.stylishlauncher.util

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.awaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.vel
import kotlinx.coroutines.launch

object Gestures {

    // Swipe direction enum
    enum class SwipeDirection {
        UP, DOWN, LEFT, RIGHT
    }

    // Detect swipe gestures
    @Composable
    fun Modifier.swipeDetector(
        onSwipeUp: (() -> Unit)? = null,
        onSwipeDown: (() -> Unit)? = null,
        onSwipeLeft: (() -> Unit)? = null,
        onSwipeRight: (() -> Unit)? = null,
        threshold: Float = 100f,
        velocityThreshold: Float = 500f,
    ): Modifier = this.then(
        pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { },
                onDrag = { _, _ -> },
                onDragEnd = { velocity ->
                    val horizontalVelocity = velocity.x
                    val verticalVelocity = velocity.y

                    // Determine primary direction
                    if (abs(horizontalVelocity) > abs(verticalVelocity)) {
                        // Horizontal swipe
                        if (horizontalVelocity > velocityThreshold && abs(horizontalVelocity) > abs(verticalVelocity)) {
                            onSwipeRight?.invoke()
                        } else if (horizontalVelocity < -velocityThreshold && abs(horizontalVelocity) > abs(verticalVelocity)) {
                            onSwipeLeft?.invoke()
                        }
                    } else {
                        // Vertical swipe
                        if (verticalVelocity < -velocityThreshold) {
                            onSwipeUp?.invoke()
                        } else if (verticalVelocity > velocityThreshold) {
                            onSwipeDown?.invoke()
                        }
                    }
                },
                onDragCancel = { },
            )
        }
    )

    // Simple swipe detector based on drag distance (not velocity)
    @Composable
    fun Modifier.dragSwipeDetector(
        onSwipeUp: (() -> Unit)? = null,
        onSwipeDown: (() -> Unit)? = null,
        onSwipeLeft: (() -> Unit)? = null,
        onSwipeRight: (() -> Unit)? = null,
        threshold: Float = 120f,
    ): Modifier = this.then(
        pointerInput(Unit) {
            var startOffset: Offset? = null
            var isDragging = false

            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val changes = event.changes

                    for (change in changes) {
                        when (change.type) {
                            androidx.compose.ui.input.pointer.PointerType.DOWN -> {
                                startOffset = change.position
                                isDragging = true
                            }
                            androidx.compose.ui.input.pointer.PointerType.MOVE -> {
                                if (isDragging && startOffset != null) {
                                    val delta = change.position - startOffset!!
                                    // Track drag but don't consume
                                }
                            }
                            androidx.compose.ui.input.pointer.PointerType.UP -> {
                                if (isDragging && startOffset != null) {
                                    val delta = change.position - startOffset!!
                                    val absX = abs(delta.x)
                                    val absY = abs(delta.y)

                                    if (absX > threshold || absY > threshold) {
                                        if (absX > absY) {
                                            // Horizontal
                                            if (delta.x > 0) onSwipeRight?.invoke() else onSwipeLeft?.invoke()
                                        } else {
                                            // Vertical
                                            if (delta.y < 0) onSwipeUp?.invoke() else onSwipeDown?.invoke()
                                        }
                                    }
                                }
                                isDragging = false
                                startOffset = null
                            }
                            androidx.compose.ui.input.pointer.PointerType.CANCEL -> {
                                isDragging = false
                                startOffset = null
                            }
                        }
                    }
                }
            }
        }
    )

    // Double tap detector
    @Composable
    fun Modifier.doubleTapDetector(
        onDoubleTap: () -> Unit,
        timeoutMillis: Long = 300,
    ): Modifier = this.then(
        pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { _ -> onDoubleTap() },
                onTap = { _ -> },
                onLongPress = { _ -> },
                onPress = { _ -> },
            )
        }
    )

    // Long press detector
    @Composable
    fun Modifier.longPressDetector(
        onLongPress: () -> Unit,
        timeoutMillis: Long = 500,
    ): Modifier = this.then(
        pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val changes = event.changes

                    for (change in changes) {
                        when (change.type) {
                            androidx.compose.ui.input.pointer.PointerType.DOWN -> {
                                // Start timer
                                val job = launch {
                                    kotlinx.coroutines.delay(timeoutMillis)
                                    onLongPress()
                                }
                                // Store job in change data or use a map
                            }
                            androidx.compose.ui.input.pointer.PointerType.UP,
                            androidx.compose.ui.input.pointer.PointerType.CANCEL -> {
                                // Cancel timer
                            }
                        }
                    }
                }
            }
        }
    )

    // Pull to refresh / overscroll detector
    @Composable
    fun Modifier.pullDetector(
        onPull: (Float) -> Unit, // 0.0 to 1.0
        onRelease: () -> Unit,
        threshold: Float = 80f,
    ): Modifier = this.then(
        pointerInput(Unit) {
            var startY: Float? = null
            var isPulling = false

            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val changes = event.changes

                    for (change in changes) {
                        when (change.type) {
                            androidx.compose.ui.input.pointer.PointerType.DOWN -> {
                                startY = change.position.y
                            }
                            androidx.compose.ui.input.pointer.PointerType.MOVE -> {
                                if (startY != null) {
                                    val deltaY = change.position.y - startY!!
                                    if (deltaY > 0 && !isPulling) {
                                        isPulling = true
                                    }
                                    if (isPulling) {
                                        val progress = (deltaY / threshold).coerceIn(0f, 1f)
                                        onPull(progress)
                                    }
                                }
                            }
                            androidx.compose.ui.input.pointer.PointerType.UP,
                            androidx.compose.ui.input.pointer.PointerType.CANCEL -> {
                                if (isPulling) {
                                    onRelease()
                                    isPulling = false
                                }
                                startY = null
                            }
                        }
                    }
                }
            }
        }
    )
}

// Extension for Offset math
private operator fun Offset.minus(other: Offset): Offset {
    return Offset(x - other.x, y - other.y)
}