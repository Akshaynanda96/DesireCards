package com.moonlitplay.desirecards

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.floor
import kotlin.random.Random
class SpinningWheel : AppCompatActivity()
{
	private lateinit var btnSpin: Button
	private lateinit var wheelView: ImageView
	private lateinit var tvResult: TextView
	private lateinit var tvMessage: TextView

	private lateinit var maleName: String
	private lateinit var femaleName: String
	private var turn: String = "male"

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_spinning_weel)

		btnSpin = findViewById(R.id.btnSpin)
		wheelView = findViewById(R.id.wheelView)
		tvResult = findViewById(R.id.tvResult)
		tvMessage = findViewById(R.id.tvMessage)

		maleName = intent.getStringExtra("maleName") ?: "Male"
		femaleName = intent.getStringExtra("femaleName") ?: "Female"
		turn = intent.getStringExtra("turn") ?: "male"

		updateTurnMessage()

		val random = Random(System.currentTimeMillis())

		btnSpin.setOnClickListener {
			btnSpin.isEnabled = false
			tvResult.text = ""

			val fullSpins = (5 + random.nextInt(5)) * 360f
			val randomOffset = random.nextFloat() * 360f
			val targetAngle = fullSpins + randomOffset

			val animator = ValueAnimator.ofFloat(0f, targetAngle)
			animator.duration = 4000L
			animator.interpolator = AccelerateDecelerateInterpolator()

			animator.addUpdateListener { animation ->
				val value = animation.animatedValue as Float
				wheelView.rotation = value
			}

			animator.addListener(object : AnimatorListenerAdapter()
			{
				@SuppressLint("SetTextI18n")
				override fun onAnimationEnd(animation: Animator)
				{
					val theta = targetAngle % 360f
					val selected_angle = ((270f - theta) % 360f + 360f) % 360f
					val index = floor(selected_angle / 36f).toInt()
					val result = index + 1
					tvResult.text = "You got $result!"

					val intent = Intent(this@SpinningWheel, PlayCards::class.java)
					intent.putExtra("result", result)
					intent.putExtra("maleName", maleName)
					intent.putExtra("femaleName", femaleName)
					intent.putExtra("turn", turn)
					startActivityForResult(intent, 1001)
				}
			})

			animator.start()
		}
	}

	@SuppressLint("SetTextI18n")
	private fun updateTurnMessage()
	{
		if (turn == "male")
			tvMessage.text = "Hey $maleName, spin!"
		else
			tvMessage.text = "Hey $femaleName, spin!"
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == 1001)
		{
			turn = if (turn == "male") "female" else "male"
			updateTurnMessage()
			tvResult.text = ""
			btnSpin.isEnabled = true
		}
	}
}
