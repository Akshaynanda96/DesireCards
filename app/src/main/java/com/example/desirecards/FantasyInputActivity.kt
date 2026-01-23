package com.example.desirecards

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FantasyInputActivity : AppCompatActivity() {

    private lateinit var welcomeBackContainer: View
    private lateinit var mainContent: View
    private lateinit var btnResume: Button
    private lateinit var btnStartNew: Button

    private lateinit var priyaContainer: ScrollView
    private lateinit var akshayContainer: ScrollView
    private lateinit var confirmationContainer: LinearLayout
    private lateinit var priyaTitle: TextView
    private lateinit var akshayTitle: TextView
    private lateinit var savePriyaBtn: Button
    private lateinit var saveAkshayBtn: Button
    private lateinit var startGameBtn: Button

    private val femaleFantasies = mutableListOf<Pair<String, String>>()
    private val maleFantasies = mutableListOf<Pair<String, String>>()

    private lateinit var femaleName: String
    private lateinit var maleName: String

    private var lastFilledFemale = -1
    private var lastFilledMale = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fantasy_input)

        // Welcome back screen
        welcomeBackContainer = findViewById(R.id.welcome_back_container)
        mainContent = findViewById(R.id.main_content)
        btnResume = welcomeBackContainer.findViewById(R.id.btn_resume)
        btnStartNew = welcomeBackContainer.findViewById(R.id.btn_start_new)

        // Main views
        priyaContainer = findViewById(R.id.priya_container)
        akshayContainer = findViewById(R.id.akshay_container)
        confirmationContainer = findViewById(R.id.confirmation_container)
        priyaTitle = findViewById(R.id.priya_title)
        akshayTitle = findViewById(R.id.akshay_title)
        savePriyaBtn = findViewById(R.id.save_priya_btn)
        saveAkshayBtn = findViewById(R.id.save_akshay_btn)
        startGameBtn = findViewById(R.id.start_game_btn)

        val prefs = getSharedPreferences("desire_cards", Context.MODE_PRIVATE)

        femaleName = prefs.getString("female_name", "Her") ?: "Her"
        maleName = prefs.getString("male_name", "Him") ?: "Him"

        priyaTitle.text = "$femaleName 💖\nYour 10 Naughtiest Fantasies Tonight"
        akshayTitle.text = "$maleName 💙\nYour 10 Dirtiest Fantasies Tonight"

        addFantasyInputs(priyaContainer.findViewById(R.id.priya_fantasies_container), femaleName)
        addFantasyInputs(akshayContainer.findViewById(R.id.akshay_fantasies_container), maleName)

        // Check if there's saved data
        val hasData = prefs.contains("female_fantasies") || prefs.contains("male_fantasies") ||
                prefs.contains("last_filled_female") || prefs.contains("last_filled_male")

        if (hasData) {
            welcomeBackContainer.visibility = View.VISIBLE
            mainContent.visibility = View.GONE
        } else {
            welcomeBackContainer.visibility = View.GONE
            mainContent.visibility = View.VISIBLE
            showFemaleForm()
        }

        // Resume
        btnResume.setOnClickListener {
            welcomeBackContainer.visibility = View.GONE
            mainContent.visibility = View.VISIBLE
            loadSavedFantasies(prefs)
            when {
                femaleFantasies.isEmpty() -> showFemaleForm()
                maleFantasies.isEmpty() -> {
                    showMaleForm()
                    scrollToNextEmpty(akshayContainer, lastFilledMale)
                }
                else -> showConfirmation()
            }
        }

        // Start Fresh
        btnStartNew.setOnClickListener {
            welcomeBackContainer.visibility = View.GONE
            mainContent.visibility = View.VISIBLE
            prefs.edit().clear().apply()
            femaleFantasies.clear()
            maleFantasies.clear()
            lastFilledFemale = -1
            lastFilledMale = -1
            showFemaleForm()
        }

        // Save female
        savePriyaBtn.setOnClickListener {
            if (saveFantasies(priyaContainer.findViewById(R.id.priya_fantasies_container), femaleFantasies)) {
                saveToSharedPreferences("female_fantasies", femaleFantasies)
                lastFilledFemale = getLastFilledIndex(femaleFantasies)
                saveProgress("last_filled_female", lastFilledFemale)
                showMaleForm()
                scrollToNextEmpty(priyaContainer, lastFilledFemale)
            } else {
                Toast.makeText(this, "Please fill at least 1 fantasy 💖", Toast.LENGTH_SHORT).show()
            }
        }

        // Save male
        saveAkshayBtn.setOnClickListener {
            if (saveFantasies(akshayContainer.findViewById(R.id.akshay_fantasies_container), maleFantasies)) {
                saveToSharedPreferences("male_fantasies", maleFantasies)
                lastFilledMale = getLastFilledIndex(maleFantasies)
                saveProgress("last_filled_male", lastFilledMale)
                showConfirmation()
                scrollToNextEmpty(akshayContainer, lastFilledMale)
            } else {
                Toast.makeText(this, "Please fill at least 1 fantasy 💙", Toast.LENGTH_SHORT).show()
            }
        }

        // Start game
        startGameBtn.setOnClickListener {
            startActivity(Intent(this, Card::class.java))
            finish()
        }
    }

    private fun loadSavedFantasies(prefs: SharedPreferences) {
        val gson = Gson()

        val femaleJson = prefs.getString("female_fantasies", null)
        if (femaleJson != null) {
            val type = object : TypeToken<List<Pair<String, String>>>() {}.type
            femaleFantasies.addAll(gson.fromJson(femaleJson, type))
            lastFilledFemale = prefs.getInt("last_filled_female", -1)
        }

        val maleJson = prefs.getString("male_fantasies", null)
        if (maleJson != null) {
            val type = object : TypeToken<List<Pair<String, String>>>() {}.type
            maleFantasies.addAll(gson.fromJson(maleJson, type))
            lastFilledMale = prefs.getInt("last_filled_male", -1)
        }

        fillInputs(priyaContainer.findViewById(R.id.priya_fantasies_container), femaleFantasies)
        fillInputs(akshayContainer.findViewById(R.id.akshay_fantasies_container), maleFantasies)
    }

    private fun fillInputs(container: LinearLayout, savedList: List<Pair<String, String>>) {
        for (i in savedList.indices) {
            if (i >= container.childCount) break
            val item = container.getChildAt(i)
            val fantasyEt: EditText = item.findViewById(R.id.fantasy_et)
            val videoUrlEt: EditText = item.findViewById(R.id.video_url_et)

            fantasyEt.setText(savedList[i].first)
            videoUrlEt.setText(savedList[i].second)
        }
    }

    private fun getLastFilledIndex(list: List<Pair<String, String>>): Int {
        return list.size - 1
    }

    private fun saveProgress(key: String, index: Int) {
        val prefs = getSharedPreferences("desire_cards", Context.MODE_PRIVATE)
        prefs.edit().putInt(key, index).apply()
    }

    private fun scrollToNextEmpty(scrollView: ScrollView, lastFilled: Int) {
        scrollView.post {
            val containerId = if (scrollView.id == R.id.priya_container)
                R.id.priya_fantasies_container
            else
                R.id.akshay_fantasies_container

            val container = scrollView.findViewById<LinearLayout>(containerId)
            val nextIndex = lastFilled + 1

            if (nextIndex < container.childCount) {
                val nextView = container.getChildAt(nextIndex)
                nextView.requestFocus()
                scrollView.smoothScrollTo(0, nextView.top - 100)
            }
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

    private fun saveToSharedPreferences(key: String, fantasies: List<Pair<String, String>>) {
        val prefs = getSharedPreferences("desire_cards", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(fantasies)
        prefs.edit().putString(key, json).apply()
    }
}