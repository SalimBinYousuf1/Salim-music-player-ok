package com.example.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Continuous corner curvature (superellipse / squircle) shape.
 * Emulates the Apple curvature continuity (G2) with cubic Bézier control points,
 * avoiding the abrupt curvature changes of traditional circular rounded corners.
 */
class SquircleShape(private val cornerRadius: Dp = 16.dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radiusPx = with(density) { cornerRadius.toPx() }.coerceAtMost(
            minOf(size.width, size.height) / 2f
        )
        val w = size.width
        val h = size.height

        val path = Path().apply {
            val c = radiusPx * 0.55f // cubic Bézier continuous smoothing factor

            moveTo(radiusPx, 0f)
            lineTo(w - radiusPx, 0f)
            cubicTo(w - radiusPx + c, 0f, w, radiusPx - c, w, radiusPx)
            lineTo(w, h - radiusPx)
            cubicTo(w, h - radiusPx + c, w - radiusPx + c, h, w - radiusPx, h)
            lineTo(radiusPx, h)
            cubicTo(radiusPx - c, h, 0f, h - radiusPx + c, 0f, h - radiusPx)
            lineTo(0f, radiusPx)
            cubicTo(0f, radiusPx - c, radiusPx - c, 0f, radiusPx, 0f)
            close()
        }

        return Outline.Generic(path)
    }
}
