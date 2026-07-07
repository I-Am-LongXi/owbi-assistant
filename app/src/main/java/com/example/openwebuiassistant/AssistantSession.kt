package com.example.openwebuiassistant

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.ContextCompat

class AssistantSession(context: Context) : VoiceInteractionSession(context) {

    private var webView: WebView? = null

    @SuppressLint("InflateParams")
    override fun onCreateContentView(): View {
        val view = layoutInflater.inflate(R.layout.assistant_overlay, null)
        webView = view.findViewById(R.id.webview)
        setupWebView()
        return view
    }

    private fun setupWebView() {
        webView?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.mediaPlaybackRequiresUserGesture = false
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    val prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
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
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: android.webkit.PermissionRequest) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(request.resources)
                    } else {
                        request.deny()
                        Toast.makeText(context, R.string.mic_permission_required, Toast.LENGTH_LONG).show()
                        val intent = Intent(context, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        hide()
                    }
                }
            }
        }
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        
        val prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val url = prefs.getString(MainActivity.KEY_URL, MainActivity.DEFAULT_URL) ?: MainActivity.DEFAULT_URL
        val fullScreen = prefs.getBoolean(MainActivity.KEY_FULL_SCREEN, false)

        val uri = Uri.parse(url)
        val finalUrl = uri.buildUpon().build().toString() // ?call=true no longer forced since we inject JS for both modes

        if (fullScreen) {
            val intent = Intent(context, FullScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("URL", finalUrl)
            }
            context.startActivity(intent)
            hide()
        } else {
            webView?.loadUrl(finalUrl)
        }
    }

    override fun onHide() {
        super.onHide()
        webView?.loadUrl("about:blank") // Clear to save memory when hidden
    }
}
