package com.moonlitplay.desirecards

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class Welcome : AppCompatActivity() {
	private var selectedVersion = 1

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportActionBar?.hide()

		setContentView(R.layout.activity_welcome)

		val maleNameField = findViewById<EditText>(R.id.male_name)
		val femaleNameField = findViewById<EditText>(R.id.female_name)
		val versionSpinner = findViewById<Spinner>(R.id.version_spinner)
		val startButton = findViewById<MaterialButton>(R.id.start_button)

		// Load versions into spinner
		val versionAdapter =
			ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, CardJsonLoader.versionList)

		versionSpinner.adapter = versionAdapter

		versionSpinner.setOnItemSelectedListener {
			val value = versionSpinner.selectedItem.toString().toInt()
			selectedVersion = value
		}

		startButton.setOnClickListener {

			val maleName = maleNameField.text.toString().trim()
			val femaleName = femaleNameField.text.toString().trim()

			if (maleName.isEmpty() || femaleName.isEmpty()) {
				Toast.makeText(this, "Please enter both names", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			// Load the selected version
			CardJsonLoader.loadVersion(selectedVersion)

			val intent = Intent(this, SpinningWheel::class.java)
			intent.putExtra("maleName", maleName)
			intent.putExtra("femaleName", femaleName)
			intent.putExtra("selectedVersion", selectedVersion)
			intent.putExtra("turn", "male")
			startActivity(intent)
		}
	}

	fun Spinner.setOnItemSelectedListener(onSelect: () -> Unit) {
		this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
			override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
				onSelect()
			}
			override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
		}
	}

}
