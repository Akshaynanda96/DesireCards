package com.moonlitplay.desirecards

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withRotation
import kotlin.math.cos
import kotlin.math.sin

class SpinningWheelView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{

	private var rotation = 0f

	override fun setRotation(degrees: Float)
	{
		rotation = degrees
		invalidate()
	}

	@SuppressLint("DrawAllocation")
	override fun onDraw(canvas: Canvas)
	{
		super.onDraw(canvas)
		val centerX = width / 2f
		val centerY = height / 2f

		val radius = kotlin.math.min(centerX, centerY) * 0.8f

		val rect = RectF(
			centerX - radius,
			centerY - radius,
			centerX + radius,
			centerY + radius
		)

		canvas.withRotation(rotation, centerX, centerY) {
			// Rotate the canvas clockwise by the rotation amount
			for (i in 0 until 10)
			{
				val startAngle = i * 36f
				val sweepAngle = 36f

				val paint = Paint().apply {
					isAntiAlias = true
					style = Paint.Style.FILL
					color = getSectorColor(i)
				}
				drawArc(rect, startAngle, sweepAngle, true, paint)

				// Number text
				val textPaint = Paint().apply {
					color = Color.WHITE
					textSize = 48f
					textAlign = Paint.Align.CENTER
					isAntiAlias = true
					typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
					setShadowLayer(4f, 2f, 2f, Color.BLACK) // Sexy shadow for depth
				}

				val angle = startAngle + sweepAngle / 2f
				val rad = Math.toRadians(angle.toDouble())
				val textRadius = radius * 0.6f
				val textX = centerX + (textRadius * cos(rad)).toFloat()
				val textY = centerY + (textRadius * sin(rad)).toFloat() - (textPaint.fontMetrics.ascent / 2) // Vertical center

				drawText((i + 1).toString(), textX, textY, textPaint)

				val iconPaint = Paint().apply {
					color = Color.WHITE
					isAntiAlias = true
				}
				val iconRadius = textRadius * 0.3f
				val iconX = centerX + (iconRadius * 1.2 * cos(rad)).toFloat()
				val iconY = centerY + (iconRadius * 1.2 * sin(rad)).toFloat()
				drawCircle(iconX, iconY, 8f, iconPaint)
			}

		}
	}

	private fun getSectorColor(i: Int): Int
	{
		val colors = intArrayOf(
			"#FF6B6B".toColorInt(), // Vibrant red
			"#4ECDC4".toColorInt(), // Teal
			"#45B7D1".toColorInt(), // Blue
			"#96CEB4".toColorInt(), // Mint
			"#FECA57".toColorInt(), // Yellow
			"#FF9FF3".toColorInt(), // Pink
			"#54A0FF".toColorInt(), // Light blue
			"#5F27CD".toColorInt(), // Purple
			"#00D2D3".toColorInt(), // Cyan
			"#FF9F43".toColorInt()  // Orange
		)
		return colors[i]
	}
}
