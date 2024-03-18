package com.example.springbreakchooser

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.springbreakchooser.databinding.ActivityMainBinding
import android.speech.RecognizerIntent
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import java.util.*
import kotlin.math.sqrt


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), OnItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private var selectedLanguage = ""
    private var languages = arrayOf("English", "French", "Spanish", "Italian")
    private val languageTags = mapOf(
        "English" to "en-US",
        "French" to "fr-FR",
        "Spanish" to "es-ES",
        "Italian" to "it-IT",
    )

    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f


    private val speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
            val data: Intent? = result.data
            val res = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            binding.userInput.setText(res?.get(0) ?: "")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Shake Detection
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH



        // Speech Recognition
        binding.speakButton.setOnClickListener {
            val selectedLanguageTag = languageTags[selectedLanguage] ?: Locale.getDefault().toLanguageTag()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguageTag)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
            }
            speechResultLauncher.launch(intent)
        }

        // Spinner setup
        binding.languageSpinner.onItemSelectedListener = this
        val ad: ArrayAdapter<*> = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        )
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinner.adapter = ad
    }

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        Toast.makeText(applicationContext, languages[position], Toast.LENGTH_LONG).show()
        selectedLanguage = languages[position] ?: ""
        Log.d(TAG, "Selected language: $selectedLanguage")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {


            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration


            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta


            if (acceleration > 12) {
                Log.d(TAG, "Initializing sensors")
                Toast.makeText(this@MainActivity, "Shake Event Detected", Toast.LENGTH_SHORT).show()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }
}