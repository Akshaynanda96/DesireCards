package com.moonlitplay.desirecards

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt

class CustomArrowView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{

	init
	{
		setLayerType(LAYER_TYPE_SOFTWARE, null)
	}

	@SuppressLint("DrawAllocation")
	override fun onDraw(canvas: Canvas)
	{
		super.onDraw(canvas)

		val paint = Paint().apply {
			style = Paint.Style.FILL
			isAntiAlias = true
			setShadowLayer(10f, 0f, 5f, Color.BLACK)
			shader = LinearGradient(
				0f, 0f, 0f, height.toFloat(),
				"#FF6B6B".toColorInt(), "#C2185B".toColorInt(),
				Shader.TileMode.CLAMP
			)
		}

		val path = Path()
		path.moveTo(0f, 0f)
		path.lineTo(width.toFloat(), 0f)
		path.lineTo(width / 2f, height.toFloat())
		path.close()

		canvas.drawPath(path, paint)
	}
}