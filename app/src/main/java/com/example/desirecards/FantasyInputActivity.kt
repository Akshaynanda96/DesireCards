package com.example.desirecards

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class FantasyInputActivity : AppCompatActivity() {

    private lateinit var priyaContainer: android.widget.ScrollView
    private lateinit var akshayContainer: android.widget.ScrollView
    private lateinit var confirmationContainer: LinearLayout
    private lateinit var priyaTitle: TextView
    private lateinit var akshayTitle: TextView
    private lateinit var savePriyaBtn: Button
    private lateinit var saveAkshayBtn: Button
    private lateinit var startGameBtn: Button

    private val femaleFantasies = mutableListOf<Pair<String, String>>() // <fantasy_text, video_url>
    private val maleFantasies = mutableListOf<Pair<String, String>>()

    private lateinit var femaleName: String
    private lateinit var maleName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fantasy_input)

        // Find views
        priyaContainer = findViewById(R.id.priya_container)
        akshayContainer = findViewById(R.id.akshay_container)
        confirmationContainer = findViewById(R.id.confirmation_container)
        priyaTitle = findViewById(R.id.priya_title)           // ← Add these IDs in XML
        akshayTitle = findViewById(R.id.akshay_title)         // ← Add these IDs in XML
        savePriyaBtn = findViewById(R.id.save_priya_btn)
        saveAkshayBtn = findViewById(R.id.save_akshay_btn)
        startGameBtn = findViewById(R.id.start_game_btn)

        // Load names from SharedPreferences (saved in MainActivity)
        val prefs = getSharedPreferences("desire_cards", Context.MODE_PRIVATE)
        femaleName = prefs.getString("female_name", "Her") ?: "Her"
        maleName = prefs.getString("male_name", "Him") ?: "Him"

        // Update titles dynamically
        priyaTitle.text = "$femaleName 💖\nYour 10 Naughtiest Fantasies Tonight"
        akshayTitle.text = "$maleName 💙\nYour 10 Dirtiest Fantasies Tonight"

        // Show female's form first
        showFemaleForm()

        // Generate 10 inputs for female
        addFantasyInputs(priyaContainer.findViewById(R.id.priya_fantasies_container), femaleName)

        // Generate 10 for male
        addFantasyInputs(akshayContainer.findViewById(R.id.akshay_fantasies_container), maleName)

        // Save female
        savePriyaBtn.setOnClickListener {
            if (saveFantasies(priyaContainer.findViewById(R.id.priya_fantasies_container), femaleFantasies)) {
                saveToSharedPreferences("female_fantasies", femaleFantasies)
                showMaleForm()
            } else {
                Toast.makeText(this, "Please fill at least 5 fantasies 💖", Toast.LENGTH_SHORT).show()
            }
        }

        // Save male
        saveAkshayBtn.setOnClickListener {
            if (saveFantasies(akshayContainer.findViewById(R.id.akshay_fantasies_container), maleFantasies)) {
                saveToSharedPreferences("male_fantasies", maleFantasies)
                showConfirmation()
            } else {
                Toast.makeText(this, "Please fill at least 5 fantasies 💙", Toast.LENGTH_SHORT).show()
            }
        }

        // Start the card game
        startGameBtn.setOnClickListener {
            startActivity(Intent(this, Card::class.java))
            finish()
        }
    }

    private fun showFemaleForm() {
        priyaContainer.visibility = View.VISIBLE
        akshayContainer.visibility = View.GONE
        confirmationContainer.visibility = View.GONE
    }

    private fun showMaleForm() {
        priyaContainer.visibility = View.GONE
        akshayContainer.visibility = View.VISIBLE
        confirmationContainer.visibility = View.GONE
    }

    private fun showConfirmation() {
        priyaContainer.visibility = View.GONE
        akshayContainer.visibility = View.GONE
        confirmationContainer.visibility = View.VISIBLE
    }

    private fun addFantasyInputs(container: LinearLayout, personName: String) {
        for (i in 1..10) {
            val itemView = layoutInflater.inflate(R.layout.item_fantasy_input, container, false)

            val numberTv: TextView = itemView.findViewById(R.id.number_tv)
            val fantasyEt: EditText = itemView.findViewById(R.id.fantasy_et)
            val videoUrlEt: EditText = itemView.findViewById(R.id.video_url_et)

            numberTv.text = "$i."

            // Dynamic hint with person's name
            fantasyEt.hint = "Tell $personName your naughty fantasy #${i}... 😈"

            container.addView(itemView)
        }
    }

    private fun saveFantasies(
        container: LinearLayout,
        list: MutableList<Pair<String, String>>
    ): Boolean {
        list.clear()
        var filledCount = 0

        for (i in 0 until container.childCount) {
            val item = container.getChildAt(i)
            val fantasyEt: EditText = item.findViewById(R.id.fantasy_et)
            val videoUrlEt: EditText = item.findViewById(R.id.video_url_et)

            val fantasy = fantasyEt.text.toString().trim()
            val url = videoUrlEt.text.toString().trim()

            if (fantasy.isNotEmpty()) {
                filledCount++
                list.add(Pair(fantasy, url))
            }
        }

        return filledCount >= 1
    }

    // Save list to SharedPreferences as JSON
    private fun saveToSharedPreferences(key: String, fantasies: List<Pair<String, String>>) {
        val prefs = getSharedPreferences("desire_cards", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(fantasies)
        prefs.edit().putString(key, json).apply()
    }
}