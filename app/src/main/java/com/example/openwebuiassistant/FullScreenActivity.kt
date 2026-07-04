package com.example.openwebuiassistant

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class FullScreenActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val RECORD_AUDIO_REQUEST_CODE = 101

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        webView = WebView(this)
        setContentView(webView)

        supportActionBar?.hide()

        setupWebView()

        val url = intent.getStringExtra("URL") ?: run {
            val prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(MainActivity.KEY_URL, MainActivity.DEFAULT_URL) ?: MainActivity.DEFAULT_URL
        }
        
        webView.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
                val autoDictate = prefs.getBoolean(MainActivity.KEY_AUTO_DICTATE, true)
                val autoCall = prefs.getBoolean(MainActivity.KEY_AUTO_VOICE_CALL, false)
                
                if (!autoDictate && !autoCall) return
                
                var selector = ""
                if (autoCall) {
                    selector = "'button[aria-label=\"Voice mode\"]'"
                } else if (autoDictate) {
                    selector = "'button[aria-label=\"Voice Input\"]'"
                }

                view?.evaluateJavascript("""
                    (function() {
                        if (window.__voiceAutoStarted) return;
                        var attempts = 0;
                        var checkExist = setInterval(function() {
                            var voiceBtn = document.querySelector($selector);
                            if (voiceBtn) {
                                window.__voiceAutoStarted = true;
                                voiceBtn.click();
                                clearInterval(checkExist);
                            }
                            attempts++;
                            if (attempts > 20) {
                                clearInterval(checkExist); // Stop after 10 seconds
                            }
                        }, 500);
                    })();
                """.trimIndent(), null)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                if (ContextCompat.checkSelfPermission(this@FullScreenActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    request.grant(request.resources)
                } else {
                    ActivityCompat.requestPermissions(this@FullScreenActivity, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
                    // Note: In a robust app, we would cache the request and grant it in onRequestPermissionsResult
                    // For simplicity, we just deny the initial request if permissions are missing and ask the user to try again.
                    request.deny() 
                }
            }
        }
    }
}
