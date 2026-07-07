package com.example.openwebuiassistant

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import android.widget.AdapterView
import android.view.View

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
        val fullScreenSwitch = findViewById<SwitchMaterial>(R.id.fullScreenSwitch)
        val autoDictateSwitch = findViewById<SwitchMaterial>(R.id.autoDictateSwitch)
        val autoVoiceCallSwitch = findViewById<SwitchMaterial>(R.id.autoVoiceCallSwitch)
        val languageSpinner = findViewById<Spinner>(R.id.languageSpinner)
        val saveButton = findViewById<Button>(R.id.saveButton)

        urlEditText.setText(prefs.getString(KEY_URL, DEFAULT_URL))
        fullScreenSwitch.isChecked = prefs.getBoolean(KEY_FULL_SCREEN, false)
        autoDictateSwitch.isChecked = prefs.getBoolean(KEY_AUTO_DICTATE, true) // default to true since we enabled it earlier
        autoVoiceCallSwitch.isChecked = prefs.getBoolean(KEY_AUTO_VOICE_CALL, false)
        
        val languageCodes = arrayOf("", "en", "es", "zh", "zh-TW", "hi", "fr", "ar", "bn", "ru", "pt", "id", "de")
        val languageNames = arrayOf(
            "System Default", "English", "Español", "简体中文", "繁體中文", "हिन्दी", "Français", 
            "العربية", "বাংলা", "Русский", "Português", "Bahasa Indonesia", "Deutsch"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
        
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentLanguageTag = if (currentLocales.isEmpty) "" else currentLocales[0]?.language ?: ""
        
        val currentIndex = languageCodes.indexOfFirst { it == currentLanguageTag }.takeIf { it >= 0 } ?: 0
        languageSpinner.setSelection(currentIndex)
        
        var isSpinnerInitialSelection = true
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isSpinnerInitialSelection) {
                    isSpinnerInitialSelection = false
                    return
                }
                val code = languageCodes[position]
                val localeList = if (code.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(code)
                AppCompatDelegate.setApplicationLocales(localeList)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

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
