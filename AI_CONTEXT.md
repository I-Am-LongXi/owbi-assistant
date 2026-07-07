# Owbi Assistant (Open WebUI Android Client)

## Overview
Owbi Assistant is a native Android wrapper designed to serve as a Digital Assistant client for self-hosted [Open WebUI](https://github.com/open-webui/open-webui) instances. 

By setting this app as the default Digital Assistant on an Android device (via Settings -> Apps -> Default Apps -> Digital assistant app), users can long-press their power button or swipe from the corner to immediately summon their Open WebUI chat in a voice-first mode.

## Architecture & AI Context
This section is explicitly written to help AI agents understand and modify the codebase in the future.

### 1. Core Components
* **`MainActivity.kt`**: The settings screen. It saves the server URL and toggle preferences to `SharedPreferences` (e.g., auto-start dictation, auto-start voice call, full-screen mode). The toggles are mutually exclusive.
* **`AssistantInteractionService.kt`**: Extends `VoiceInteractionService`. This is the system-level entry point when the user triggers the Android Assistant.
* **`AssistantSessionService.kt`**: Handles the lifecycle of the session and spawns `AssistantSession`.
* **`AssistantSession.kt`**: The Voice Assistant Overlay. It inflates `R.layout.assistant_overlay` which contains a `WebView`. It loads the user's Open WebUI URL in a floating panel over the current screen.
* **`FullScreenActivity.kt`**: An alternative flow. If the user enables "Open as Full Screen", `AssistantSession` immediately delegates to this Activity, which loads Open WebUI in a standard, immersive full-screen WebView.

### 2. The JavaScript Injection Logic (Crucial)
Open WebUI is a Single Page Application (SPA). The Android `WebViewClient.onPageFinished` callback fires *before* the SPA's React/Svelte components fully render. 
To auto-start the voice modes, we inject a JavaScript `setInterval` loop in `onPageFinished` (in both `AssistantSession` and `FullScreenActivity`). 
* **Voice Input (Dictation):** The script polls for `button[aria-label="Voice Input"]` and clicks it.
* **Voice Mode (Call):** The script polls for `button[aria-label="Voice mode"]` and clicks it.
*If the UI of Open WebUI changes in the future, these `aria-label` selectors are the first things that will break and need updating.*

### 3. Permissions and WebRTC
Voice interactions require the microphone.
* **Android Manifest:** Includes `RECORD_AUDIO` and `MODIFY_AUDIO_SETTINGS`.
* **WebView WebChromeClient:** Open WebUI uses browser WebRTC for audio. Both `AssistantSession` and `FullScreenActivity` override `onPermissionRequest` in their `WebChromeClient` to natively grant `request.grant(request.resources)` so the web app can access the microphone.

### 4. Production & Security Compliance
* **Rebranding:** The app is named "Owbi Assistant" to avoid impersonation policy strikes on the Play Store regarding Open WebUI or LAION's Open Assistant.
* **Network Security Config:** `res/xml/network_security_config.xml` explicitly allows cleartext traffic. This is required because users frequently host their Open WebUI instances on unencrypted local IP addresses (e.g., `http://192.168.1.100:8080`).
* **Minification (R8):** Enabled in `build.gradle.kts`. If the app crashes in Release mode but works in Debug mode, check if R8 is aggressively stripping required WebView or JavaScript interface classes.
* **Keystore:** Signed for production using `keystore.jks` (alias `key0`). Password is in `build.gradle.kts` and the file is in `.gitignore`.

### 5. Internationalization (i18n)
* **Supported Languages:** 11 languages (en, es, zh, zh-TW, hi, fr, ar, bn, ru, pt, id, de).
* **Implementation:** Uses `AppCompatDelegate.setApplicationLocales` for seamless runtime language switching via the Spinner in `MainActivity`.
* **Android 13+ Support:** Defined in `res/xml/locales_config.xml` and registered in `AndroidManifest.xml` via `android:localeConfig`.

### 6. Git Workflow & Commit Guidelines
When making future changes, AI agents must strictly adhere to the following version control rules:
* **Atomic Commits:** Each logical change or feature must be committed separately. Do not bundle unrelated changes.
* **Detailed Commit Messages:** 
  * Use the Conventional Commits format (e.g., `feat:`, `fix:`, `refactor:`, `docs:`, `chore:`).
  * The subject line must be a concise summary (under 50 characters).
  * The body must provide a detailed explanation of *why* the change was made, what files were touched, and any side-effects or considerations for future agents.
* **Branching (Optional but Recommended):** For large features, create a separate branch rather than committing directly to `master`.
