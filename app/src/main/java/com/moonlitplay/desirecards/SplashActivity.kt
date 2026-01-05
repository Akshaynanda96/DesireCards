package com.moonlitplay.desirecards

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_splash)

		val animationView = findViewById<LottieAnimationView>(R.id.splashAnimation)
		animationView.playAnimation()

		CardJsonLoader.loadCards(this, Runnable {
			startActivity(Intent(this@SplashActivity, Welcome::class.java))
			finish()
		})
	}
}