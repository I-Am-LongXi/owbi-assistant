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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class FullScreenActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var pendingPermissionRequest: PermissionRequest? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pendingPermissionRequest?.let { it.grant(it.resources) }
        } else {
            pendingPermissionRequest?.deny()
        }
        pendingPermissionRequest = null
    }

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
                        
                        function clickVoiceBtn() {
                            var voiceBtn = document.querySelector($selector);
                            if (voiceBtn) {
                                window.__voiceAutoStarted = true;
                                voiceBtn.click();
                                return true;
                            }
                            return false;
                        }

                        if (!clickVoiceBtn()) {
                            var observer = new MutationObserver(function(mutations, obs) {
                                if (clickVoiceBtn()) {
                                    obs.disconnect();
                                }
                            });
                            observer.observe(document.body, { childList: true, subtree: true });
                        }
                    })();
                """.trimIndent(), null)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                if (ContextCompat.checkSelfPermission(this@FullScreenActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    request.grant(request.resources)
                } else {
                    pendingPermissionRequest = request
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
