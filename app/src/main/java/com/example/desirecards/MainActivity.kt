package com.example.desirecards

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textfield.TextInputEditText
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var heartsContainer: ConstraintLayout
    private lateinit var versionDropdown: AutoCompleteTextView  // Make it class-level for access in adapter

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find views
        val container: CardView = findViewById(R.id.container)
        heartsContainer = findViewById(R.id.inner_layout)
        val title: TextView = findViewById(R.id.title)
        val maleNameEt: TextInputEditText = findViewById(R.id.male_name)
        val femaleNameEt: TextInputEditText = findViewById(R.id.female_name)
        versionDropdown = findViewById(R.id.version_dropdown)  // ← Class-level
        val loginBtn: Button = findViewById(R.id.login_btn)

        // Dropdown versions
        val versions = listOf(
            "Select Your Desire Version",
            "Romantic ❤️",
            "Fantasy ✨",
            "Passion 🔥",
            "Adventure 🌙",
            "Mystery 🖤",
            "Seduction 💋",
            "Forbidden Love 😈",
            "Eternal Bond ♾️"
        )

        // Custom Adapter
        class VersionAdapter(
            context: Context,
            private val items: List<String>,
            private val inflater: LayoutInflater
        ) : ArrayAdapter<String>(context, R.layout.dropdown_item_version, items) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: inflater.inflate(R.layout.dropdown_item_version, parent, false)

                val icon = view.findViewById<TextView>(R.id.item_icon)
                val text = view.findViewById<TextView>(R.id.item_text)

                text.text = getItem(position)

                icon.text = when (getItem(position)) {
                    "Select Your Desire Version" -> ""
                    "Romantic ❤️" -> "❤️"
                    "Fantasy ✨" -> "✨"
                    "Passion 🔥" -> "🔥"
                    "Adventure 🌙" -> "🌙"
                    "Mystery 🖤" -> "🖤"
                    "Seduction 💋" -> "💋"
                    "Forbidden Love 😈" -> "😈"
                    "Eternal Bond ♾️" -> "♾️"
                    else -> ""
                }

                // Click handling – use the class-level versionDropdown
                view.setOnClickListener {
                    versionDropdown.setText(getItem(position), false)
                    versionDropdown.dismissDropDown()
                }

                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getView(position, convertView, parent)
            }
        }

        // Set adapter
        val adapter = VersionAdapter(this, versions, layoutInflater)
        versionDropdown.setAdapter(adapter)
        versionDropdown.setText(versions[0], false)

        // Glowing title gradient – fixed width issue
        title.post {
            val gradient = LinearGradient(
                0f, 0f, title.width.toFloat(), 0f,
                intArrayOf(
                    Color.parseColor("#FF0066"),
                    Color.parseColor("#FF3399"),
                    Color.parseColor("#CC33FF")
                ),
                null,
                Shader.TileMode.CLAMP
            )
            title.paint.shader = gradient
        }

        // Pulse glow animation
        val glowAnimator = ValueAnimator.ofFloat(1.0f, 1.05f)
        glowAnimator.duration = 3500
        glowAnimator.repeatMode = ValueAnimator.REVERSE
        glowAnimator.repeatCount = ValueAnimator.INFINITE
        glowAnimator.addUpdateListener {
            title.scaleX = it.animatedValue as Float
            title.scaleY = it.animatedValue as Float
        }
        glowAnimator.start()

        // Fade-in & scale animation on load
        container.alpha = 0f
        container.scaleX = 0.85f
        container.scaleY = 0.85f
        container.translationY = 40f
        ObjectAnimator.ofFloat(container, "alpha", 0f, 1f).apply { duration = 1200 }.start()
        ObjectAnimator.ofFloat(container, "scaleX", 0.85f, 1f).apply { duration = 1200 }.start()
        ObjectAnimator.ofFloat(container, "scaleY", 0.85f, 1f).apply { duration = 1200 }.start()
        ObjectAnimator.ofFloat(container, "translationY", 40f, 0f).apply { duration = 1200 }.start()

        // Button press effect
        loginBtn.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.scaleY = 0.97f
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.scaleY = 1f
            }
            false
        }

        // Floating hearts
        fun createHeart() {
            val heart = TextView(this@MainActivity).apply {
                text = "❤️"
                textSize = Random.nextFloat() * 14 + 14
                setTextColor(Color.argb(153, 255, 50, 120))
            }

            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            heart.x = Random.nextFloat() * heartsContainer.width
            heart.y = heartsContainer.height.toFloat()

            heart.layoutParams = params
            heartsContainer.addView(heart)

            val floatAnim = ObjectAnimator.ofFloat(
                heart, "translationY",
                heartsContainer.height.toFloat(), -200f
            )
            floatAnim.duration = (Random.nextFloat() * 8000 + 10000).toLong()
            floatAnim.addUpdateListener { animator ->
                heart.rotation = (animator.animatedFraction * 720)
            }
            floatAnim.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    heartsContainer.removeView(heart)
                }
            })
            floatAnim.start()

            ObjectAnimator.ofFloat(heart, "alpha", 1f, 0f).apply {
                duration = floatAnim.duration
                start()
            }
        }

        repeat(12) { createHeart() }

        val handler = Handler(Looper.getMainLooper())
        val heartRunnable = object : Runnable {
            override fun run() {
                createHeart()
                handler.postDelayed(this, 600)
            }
        }
        handler.postDelayed(heartRunnable, 600)

        // Login button logic
        loginBtn.setOnClickListener {
            val maleName = maleNameEt.text.toString().trim()
            val femaleName = femaleNameEt.text.toString().trim()
            val selectedVersion = versionDropdown.text.toString().trim()

            if (maleName.isEmpty() || femaleName.isEmpty()) {
                Toast.makeText(this, "Please enter both names 💕", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedVersion == "Select Your Desire Version") {
                Toast.makeText(this, "Please select a Desire Version 🔥", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("desire_cards", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("male_name", maleName)
                putString("female_name", femaleName)
                putString("desire_version", selectedVersion)
                apply()
            }

            startActivity(Intent(this, FantasyInputActivity::class.java))
            finish()
        }
    }
}