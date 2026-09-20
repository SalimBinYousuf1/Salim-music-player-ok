package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SalimIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current,
    strokeWidth: Dp = 2.dp,
    draw: DrawScope.(stroke: Stroke, tint: Color) -> Unit
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        draw(stroke, tint)
    }
}

object SalimIcons {

    @Composable
    fun Play(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val path = Path().apply {
                moveTo(this@SalimIcon.size.width * 0.32f, this@SalimIcon.size.height * 0.22f)
                lineTo(this@SalimIcon.size.width * 0.78f, this@SalimIcon.size.height * 0.5f)
                lineTo(this@SalimIcon.size.width * 0.32f, this@SalimIcon.size.height * 0.78f)
                close()
            }
            drawPath(path, color, style = stroke)
        }
    }

    @Composable
    fun Pause(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            drawLine(color, Offset(w * 0.35f, h * 0.24f), Offset(w * 0.35f, h * 0.76f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.65f, h * 0.24f), Offset(w * 0.65f, h * 0.76f), stroke.width, StrokeCap.Round)
        }
    }

    @Composable
    fun SkipNext(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            val triangle = Path().apply {
                moveTo(w * 0.22f, h * 0.25f)
                lineTo(w * 0.62f, h * 0.5f)
                lineTo(w * 0.22f, h * 0.75f)
                close()
            }
            drawPath(triangle, color, style = stroke)
            drawLine(color, Offset(w * 0.76f, h * 0.25f), Offset(w * 0.76f, h * 0.75f), stroke.width, StrokeCap.Round)
        }
    }

    @Composable
    fun SkipPrevious(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            val triangle = Path().apply {
                moveTo(w * 0.78f, h * 0.25f)
                lineTo(w * 0.38f, h * 0.5f)
                lineTo(w * 0.78f, h * 0.75f)
                close()
            }
            drawPath(triangle, color, style = stroke)
            drawLine(color, Offset(w * 0.24f, h * 0.25f), Offset(w * 0.24f, h * 0.75f), stroke.width, StrokeCap.Round)
        }
    }

    @Composable
    fun Shuffle(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height

            // Top arrow path
            val path1 = Path().apply {
                moveTo(w * 0.18f, h * 0.32f)
                lineTo(w * 0.45f, h * 0.32f)
                lineTo(w * 0.65f, h * 0.68f)
                lineTo(w * 0.82f, h * 0.68f)
            }
            drawPath(path1, color, style = stroke)
            // Arrow head
            drawLine(color, Offset(w * 0.74f, h * 0.60f), Offset(w * 0.82f, h * 0.68f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.74f, h * 0.76f), Offset(w * 0.82f, h * 0.68f), stroke.width, StrokeCap.Round)

            // Bottom arrow path
            val path2 = Path().apply {
                moveTo(w * 0.18f, h * 0.68f)
                lineTo(w * 0.42f, h * 0.68f)
                moveTo(w * 0.68f, h * 0.32f)
                lineTo(w * 0.82f, h * 0.32f)
            }
            drawPath(path2, color, style = stroke)
            drawLine(color, Offset(w * 0.74f, h * 0.24f), Offset(w * 0.82f, h * 0.32f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.74f, h * 0.40f), Offset(w * 0.82f, h * 0.32f), stroke.width, StrokeCap.Round)
        }
    }

    @Composable
    fun Repeat(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current, isOne: Boolean = false) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height

            val path = Path().apply {
                moveTo(w * 0.32f, h * 0.30f)
                lineTo(w * 0.75f, h * 0.30f)
                arcTo(androidx.compose.ui.geometry.Rect(w * 0.65f, h * 0.30f, w * 0.85f, h * 0.50f), 270f, 90f, false)
                lineTo(w * 0.85f, h * 0.60f)
                arcTo(androidx.compose.ui.geometry.Rect(w * 0.65f, h * 0.50f, w * 0.85f, h * 0.70f), 0f, 90f, false)
                lineTo(w * 0.25f, h * 0.70f)
                arcTo(androidx.compose.ui.geometry.Rect(w * 0.15f, h * 0.50f, w * 0.35f, h * 0.70f), 90f, 90f, false)
                lineTo(w * 0.15f, h * 0.40f)
                arcTo(androidx.compose.ui.geometry.Rect(w * 0.15f, h * 0.30f, w * 0.35f, h * 0.50f), 180f, 90f, false)
                lineTo(w * 0.25f, h * 0.30f)
            }
            drawPath(path, color, style = stroke)
            // Upper arrow head
            drawLine(color, Offset(w * 0.68f, h * 0.22f), Offset(w * 0.76f, h * 0.30f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.68f, h * 0.38f), Offset(w * 0.76f, h * 0.30f), stroke.width, StrokeCap.Round)

            if (isOne) {
                drawLine(color, Offset(w * 0.50f, h * 0.44f), Offset(w * 0.50f, h * 0.58f), stroke.width * 0.9f, StrokeCap.Round)
            }
        }
    }

    @Composable
    fun Queue(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            drawLine(color, Offset(w * 0.2f, h * 0.30f), Offset(w * 0.8f, h * 0.30f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.2f, h * 0.50f), Offset(w * 0.8f, h * 0.50f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.2f, h * 0.70f), Offset(w * 0.58f, h * 0.70f), stroke.width, StrokeCap.Round)
        }
    }

    @Composable
    fun Equalizer(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height

            // Bar 1
            drawLine(color, Offset(w * 0.25f, h * 0.2f), Offset(w * 0.25f, h * 0.8f), stroke.width, StrokeCap.Round)
            drawCircle(color, stroke.width * 1.5f, Offset(w * 0.25f, h * 0.40f))

            // Bar 2
            drawLine(color, Offset(w * 0.50f, h * 0.2f), Offset(w * 0.50f, h * 0.8f), stroke.width, StrokeCap.Round)
            drawCircle(color, stroke.width * 1.5f, Offset(w * 0.50f, h * 0.65f))

            // Bar 3
            drawLine(color, Offset(w * 0.75f, h * 0.2f), Offset(w * 0.75f, h * 0.8f), stroke.width, StrokeCap.Round)
            drawCircle(color, stroke.width * 1.5f, Offset(w * 0.75f, h * 0.35f))
        }
    }

    @Composable
    fun Timer(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height

            // Clock face
            drawCircle(color, w * 0.36f, Offset(w * 0.5f, h * 0.53f), style = stroke)
            // Top knob
            drawLine(color, Offset(w * 0.42f, h * 0.12f), Offset(w * 0.58f, h * 0.12f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.50f, h * 0.12f), Offset(w * 0.50f, h * 0.17f), stroke.width, StrokeCap.Round)
            // Clock hands
            drawLine(color, Offset(w * 0.5f, h * 0.53f), Offset(w * 0.5f, h * 0.33f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.5f, h * 0.53f), Offset(w * 0.65f, h * 0.53f), stroke.width, StrokeCap.Round)
        }
    }

    @Composable
    fun Search(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            drawCircle(color, w * 0.28f, Offset(w * 0.42f, h * 0.42f), style = stroke)
            drawLine(color, Offset(w * 0.62f, h * 0.62f), Offset(w * 0.82f, h * 0.82f), stroke.width, StrokeCap.Round)
        }
    }

    @Composable
    fun Sort(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            drawLine(color, Offset(w * 0.22f, h * 0.30f), Offset(w * 0.78f, h * 0.30f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.22f, h * 0.50f), Offset(w * 0.62f, h * 0.50f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.22f, h * 0.70f), Offset(w * 0.46f, h * 0.70f), stroke.width, StrokeCap.Round)
        }
    }

    @Composable
    fun Settings(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            drawCircle(color, w * 0.14f, Offset(w * 0.5f, h * 0.5f), style = stroke)
            drawCircle(color, w * 0.36f, Offset(w * 0.5f, h * 0.5f), style = stroke)
        }
    }

    @Composable
    fun ChevronDown(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            val path = Path().apply {
                moveTo(w * 0.25f, h * 0.38f)
                lineTo(w * 0.5f, h * 0.62f)
                lineTo(w * 0.75f, h * 0.38f)
            }
            drawPath(path, color, style = stroke)
        }
    }

    @Composable
    fun ChevronRight(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            val path = Path().apply {
                moveTo(w * 0.38f, h * 0.25f)
                lineTo(w * 0.62f, h * 0.50f)
                lineTo(w * 0.38f, h * 0.75f)
            }
            drawPath(path, color, style = stroke)
        }
    }

    @Composable
    fun Plus(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            drawLine(color, Offset(w * 0.2f, h * 0.5f), Offset(w * 0.8f, h * 0.5f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.5f, h * 0.2f), Offset(w * 0.5f, h * 0.8f), stroke.width, StrokeCap.Round)
        }
    }

    @Composable
    fun Close(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            drawLine(color, Offset(w * 0.28f, h * 0.28f), Offset(w * 0.72f, h * 0.72f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.72f, h * 0.28f), Offset(w * 0.28f, h * 0.72f), stroke.width, StrokeCap.Round)
        }
    }

    @Composable
    fun Check(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            val path = Path().apply {
                moveTo(w * 0.22f, h * 0.52f)
                lineTo(w * 0.42f, h * 0.72f)
                lineTo(w * 0.78f, h * 0.32f)
            }
            drawPath(path, color, style = stroke)
        }
    }

    @Composable
    fun MusicNote(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            // Stem
            drawLine(color, Offset(w * 0.65f, h * 0.22f), Offset(w * 0.65f, h * 0.65f), stroke.width, StrokeCap.Round)
            // Note head
            drawCircle(color, w * 0.16f, Offset(w * 0.50f, h * 0.68f), style = stroke)
            // Flag
            val flag = Path().apply {
                moveTo(w * 0.65f, h * 0.22f)
                cubicTo(w * 0.80f, h * 0.25f, w * 0.85f, h * 0.40f, w * 0.85f, h * 0.45f)
            }
            drawPath(flag, color, style = stroke)
        }
    }

    @Composable
    fun Trash(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            drawLine(color, Offset(w * 0.22f, h * 0.30f), Offset(w * 0.78f, h * 0.30f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.40f, h * 0.20f), Offset(w * 0.60f, h * 0.20f), stroke.width, StrokeCap.Round)
            val can = Path().apply {
                moveTo(w * 0.30f, h * 0.30f)
                lineTo(w * 0.34f, h * 0.80f)
                lineTo(w * 0.66f, h * 0.80f)
                lineTo(w * 0.70f, h * 0.30f)
            }
            drawPath(can, color, style = stroke)
        }
    }

    @Composable
    fun Refresh(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            val arcRect = androidx.compose.ui.geometry.Rect(w * 0.2f, h * 0.2f, w * 0.8f, h * 0.8f)
            drawArc(color, 45f, 270f, false, topLeft = arcRect.topLeft, size = arcRect.size, style = stroke)
            drawLine(color, Offset(w * 0.65f, h * 0.14f), Offset(w * 0.76f, h * 0.26f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(w * 0.86f, h * 0.16f), Offset(w * 0.76f, h * 0.26f), stroke.width, StrokeCap.Round)
        }
    }

    @Composable
    fun Folder(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color = LocalContentColor.current) {
        SalimIcon(modifier = modifier, size = size, tint = tint) { stroke, color ->
            val w = this@SalimIcon.size.width
            val h = this@SalimIcon.size.height
            val path = Path().apply {
                moveTo(w * 0.18f, h * 0.32f)
                lineTo(w * 0.40f, h * 0.32f)
                lineTo(w * 0.48f, h * 0.40f)
                lineTo(w * 0.82f, h * 0.40f)
                lineTo(w * 0.82f, h * 0.74f)
                lineTo(w * 0.18f, h * 0.74f)
                close()
            }
            drawPath(path, color, style = stroke)
        }
    }
}
