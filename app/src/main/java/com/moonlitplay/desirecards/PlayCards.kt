package com.moonlitplay.desirecards

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.Random

class PlayCards : AppCompatActivity() {

	private lateinit var cardTitle: TextView
	private lateinit var cardAction: TextView
	private lateinit var cardIcon: ImageView

	@SuppressLint("SetTextI18n")
	@RequiresApi(Build.VERSION_CODES.R)
	override fun onCreate(savedInstanceState: Bundle?) {

		supportActionBar?.hide()
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_play_cards)

		cardTitle = findViewById(R.id.card_title)
		cardAction = findViewById(R.id.card_action)
		cardIcon = findViewById(R.id.card_icon)

		val result = intent.getIntExtra("result", 1)
		val turn = intent.getStringExtra("turn") ?: "male"
		val maleName = intent.getStringExtra("maleName") ?: "Male"
		val femaleName = intent.getStringExtra("femaleName") ?: "Female"

		val random = Random()
		val imageIds = intArrayOf(
			R.drawable.p1, R.drawable.p2, R.drawable.p3,
			R.drawable.p4, R.drawable.p5, R.drawable.p6, R.drawable.p7
		)
		val randomImage = imageIds[random.nextInt(imageIds.size)]

		// 🔥 NEW — Using CardTypeService with JSON cards
		val service = CardTypeService()

		val rawCardText = if (turn == "female") {
			service.getCardType(1).cardsInfo[1] // Female card
		} else {
			service.getCardType(2).cardsInfo[1] // Male card
		}

		// Replace placeholders
		val finalCardText = rawCardText
			?.replace("{{malename}}", maleName)
			?.replace("{{fmalename}}", femaleName)
			?: "No card available."

		// Set UI text
		cardTitle.text = "Your Card"
		cardAction.text = finalCardText
		cardIcon.setImageResource(randomImage)
	}
}
