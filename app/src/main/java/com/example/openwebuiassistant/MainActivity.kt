package com.example.openwebuiassistant

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "OpenWebuiAssistantPrefs"
        const val KEY_URL = "server_url"
        const val KEY_FULL_SCREEN = "full_screen_mode"
        const val KEY_AUTO_DICTATE = "auto_dictate_mode"
        const val KEY_AUTO_VOICE_CALL = "auto_voice_call_mode"
        const val DEFAULT_URL = "http://10.0.2.2:8080" // default for emulator to local machine
        const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request runtime permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
        }

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val urlEditText = findViewById<TextInputEditText>(R.id.urlEditText)
        val fullScreenSwitch = findViewById<Switch>(R.id.fullScreenSwitch)
        val autoDictateSwitch = findViewById<Switch>(R.id.autoDictateSwitch)
        val autoVoiceCallSwitch = findViewById<Switch>(R.id.autoVoiceCallSwitch)
        val saveButton = findViewById<Button>(R.id.saveButton)

        urlEditText.setText(prefs.getString(KEY_URL, DEFAULT_URL))
        fullScreenSwitch.isChecked = prefs.getBoolean(KEY_FULL_SCREEN, false)
        autoDictateSwitch.isChecked = prefs.getBoolean(KEY_AUTO_DICTATE, true) // default to true since we enabled it earlier
        autoVoiceCallSwitch.isChecked = prefs.getBoolean(KEY_AUTO_VOICE_CALL, false)

        autoDictateSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                autoVoiceCallSwitch.isChecked = false
            }
        }
        
        autoVoiceCallSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                autoDictateSwitch.isChecked = false
            }
        }

        saveButton.setOnClickListener {
            val url = urlEditText.text.toString().trim()
            
            prefs.edit()
                .putString(KEY_URL, url)
                .putBoolean(KEY_FULL_SCREEN, fullScreenSwitch.isChecked)
                .putBoolean(KEY_AUTO_DICTATE, autoDictateSwitch.isChecked)
                .putBoolean(KEY_AUTO_VOICE_CALL, autoVoiceCallSwitch.isChecked)
                .apply()
                
            Toast.makeText(this, R.string.url_saved, Toast.LENGTH_SHORT).show()
        }
    }
}
