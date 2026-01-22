package com.example.desirecards

import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.abs
import kotlin.random.Random

class Card : AppCompatActivity() {

    private lateinit var heartsContainer: ConstraintLayout
    private lateinit var cardElement: View
    private lateinit var cardFront: ConstraintLayout
    private lateinit var cardBack: ConstraintLayout
    private lateinit var turnHeader: TextView
    private lateinit var nextBtn: Button
    private lateinit var playBtn: Button
    private lateinit var completionContainer: ConstraintLayout // New completion screen

    private var currentTurn = "male" // "male" or "female"
    private var isFrontVisible = true
    private var isFlipping = false

    private val femaleUsedIndices = mutableSetOf<Int>() // For female fantasies
    private val maleUsedIndices = mutableSetOf<Int>()   // For male fantasies

    private lateinit var flipSound: MediaPlayer

    private lateinit var maleName: String
    private lateinit var femaleName: String
    private lateinit var femaleFantasies: List<Pair<String, String>> // <text, video_url>
    private lateinit var maleFantasies: List<Pair<String, String>>

    private var currentVideoUrl: String? = null  // Will hold the URL of the current fantasy

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        // Views
        heartsContainer = findViewById(R.id.root_layout)
        cardElement = findViewById(R.id.card)
        cardFront = findViewById(R.id.card_front)
        cardBack = findViewById(R.id.card_back)
        turnHeader = findViewById(R.id.turn_header)
        nextBtn = findViewById(R.id.next_btn)
        playBtn = findViewById(R.id.play_btn)
        completionContainer = findViewById(R.id.completion_container) // Add in XML

        flipSound = MediaPlayer.create(this, R.raw.flip_sound)

        // Load data from SharedPreferences
        loadDataFromPrefs()

        // Start floating hearts
        startFloatingHearts()

        // Show first card
        showNextCard()

        // Swipe to flip
        var startX = 0f
        cardElement.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startX = event.x
                MotionEvent.ACTION_UP -> {
                    if (abs(startX - event.x) > 80) flipCard()
                }
            }
            true
        }

        nextBtn.setOnClickListener { showNextCard() }

        playBtn.setOnClickListener {
            if (currentVideoUrl != null && currentVideoUrl!!.isNotBlank()) {
                val intent = Intent(this, VideoActivity::class.java)
                intent.putExtra("VIDEO_URL", currentVideoUrl)  // Pass the URL
                startActivity(intent)
            } else {
                Toast.makeText(this, "No video available for this task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDataFromPrefs() {
        val prefs = getSharedPreferences("desire_cards", Context.MODE_PRIVATE)
        maleName = prefs.getString("male_name", "Him") ?: "Him"
        femaleName = prefs.getString("female_name", "Her") ?: "Her"

        val gson = Gson()

        // Load female fantasies
        val femaleJson = prefs.getString("female_fantasies", "[]") ?: "[]"
        val femaleType = object : TypeToken<List<Pair<String, String>>>() {}.type
        femaleFantasies = gson.fromJson(femaleJson, femaleType) ?: emptyList()

        // Load male fantasies
        val maleJson = prefs.getString("male_fantasies", "[]") ?: "[]"
        val maleType = object : TypeToken<List<Pair<String, String>>>() {}.type
        maleFantasies = gson.fromJson(maleJson, maleType) ?: emptyList()
    }

    private fun flipCard() {
        if (isFlipping) return
        isFlipping = true

        cardElement.cameraDistance = resources.displayMetrics.density * 24000

        val spin = ObjectAnimator.ofFloat(cardElement, "rotationY", 0f, 1440f).apply {
            duration = 1400
            interpolator = android.view.animation.DecelerateInterpolator(2.2f)
        }

        spin.addUpdateListener {
            val angle = (it.animatedValue as Float) % 360
            if (angle in 85f..95f && isFrontVisible) {
                cardFront.visibility = View.GONE
                cardBack.visibility = View.VISIBLE
                isFrontVisible = false
            } else if (angle in 265f..275f && !isFrontVisible) {
                cardBack.visibility = View.GONE
                cardFront.visibility = View.VISIBLE
                isFrontVisible = true
            }
        }

        val scaleX = ObjectAnimator.ofFloat(cardElement, "scaleX", 1f, 1.08f, 1f)
        val scaleY = ObjectAnimator.ofFloat(cardElement, "scaleY", 1f, 1.08f, 1f)
        scaleX.duration = 1400
        scaleY.duration = 1400

        AnimatorSet().apply {
            playTogether(spin, scaleX, scaleY)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    cardElement.rotationY %= 360
                    isFlipping = false
                }
            })
            start()
        }

        flipSound.start()
    }

    private fun showNextCard() {
        // ... (your existing check for completion)

        flipCard()

        Handler(Looper.getMainLooper()).postDelayed({
            val fantasies = if (currentTurn == "male") femaleFantasies else maleFantasies
            val used = if (currentTurn == "male") femaleUsedIndices else maleUsedIndices

            // Find next unused index (sequential)
            var nextIndex = -1
            for (i in fantasies.indices) {
                if (!used.contains(i)) {
                    nextIndex = i
                    break
                }
            }

            if (nextIndex == -1) {
                used.clear()
                nextIndex = 0
            }

            val fantasy = fantasies[nextIndex]
            used.add(nextIndex)

            findViewById<TextView>(R.id.card_title).text = "Task #${nextIndex + 1}"
            findViewById<TextView>(R.id.card_text).text = fantasy.first

            // Save current video URL
            currentVideoUrl = if (fantasy.second.isNotBlank()) fantasy.second else null

            // Show/hide play button based on video URL
            playBtn.visibility = if (currentVideoUrl != null) View.VISIBLE else View.GONE

            // Update header & button
            if (currentTurn == "male") {
                turnHeader.text = "His Turn - $maleName 💙"
                turnHeader.setTextColor("#0077FD".toColorInt())
                nextBtn.text = "Her Turn Next →"
            } else {
                turnHeader.text = "Her Turn - $femaleName 💖"
                turnHeader.setTextColor("#FF3399".toColorInt())
                nextBtn.text = "His Turn Next →"
            }

            currentTurn = if (currentTurn == "male") "female" else "male"
        }, 350)
    }

    private fun showCompletionScreen() {
        // Hide game UI
        turnHeader.visibility = View.GONE
        cardElement.visibility = View.GONE
        nextBtn.visibility = View.GONE
        playBtn.visibility = View.GONE

        // Show completion screen
        completionContainer.visibility = View.VISIBLE

        // Optional: Add confetti, sound, or animation here later
    }

    private fun startFloatingHearts() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                createHeart()
                handler.postDelayed(this, 600)
            }
        })
    }

    private fun createHeart() {
        val heart = TextView(this).apply {
            text = "❤️"
            textSize = Random.nextFloat() * 16 + 20
            setTextColor(Color.argb(150, 255, 51, 153))
        }

        heart.x = Random.nextFloat() * heartsContainer.width
        heart.y = heartsContainer.height.toFloat()
        heartsContainer.addView(heart)

        ObjectAnimator.ofFloat(heart, "translationY", heartsContainer.height.toFloat(), -150f).apply {
            duration = (Random.nextFloat() * 8000 + 8000).toLong()
            addUpdateListener { heart.rotation = it.animatedFraction * 720 }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    heartsContainer.removeView(heart)
                }
            })
            start()
        }

        ObjectAnimator.ofFloat(heart, "alpha", 1f, 0f).apply {
            duration = 8000
            start()
        }

        // Find restart button and handle click
        val restartBtn: Button = findViewById(R.id.restart_btn)
        restartBtn.setOnClickListener {
            // Clear used indices and restart game
            femaleUsedIndices.clear()
            maleUsedIndices.clear()
            currentTurn = "male"

            // Hide completion screen
            completionContainer.visibility = View.GONE

            // Show game UI again
            turnHeader.visibility = View.VISIBLE
            cardElement.visibility = View.VISIBLE
            nextBtn.visibility = View.VISIBLE
            playBtn.visibility = View.VISIBLE

            // Start from first card again
            showNextCard()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        flipSound.release()
    }

}