# Owbi Assistant

**Owbi Assistant** is a native Android application that serves as a seamless Digital Assistant client for your self-hosted [Open WebUI](https://github.com/open-webui/open-webui) instance.

By setting this app as your default Digital Assistant on Android, you can trigger your Open WebUI chat natively via a long-press of the power button, a swipe from the screen corner, or voice activation—all without opening a web browser.

## Features
- **Assistant Overlay:** Opens a quick, floating overlay window for rapid dictation or voice calling with your AI.
- **Full Screen Mode:** Optionally launch the interface in an immersive, full-screen native WebView.
- **Auto-Dictation & Auto-Voice:** Automatically starts listening the moment the assistant is triggered, mimicking the native Google Assistant experience.
- **Self-Host Friendly:** Configure your own custom server URL. Supports local IP addresses (e.g., `http://192.168.X.X`).
- **Privacy First:** All voice data is captured ephemerally and streamed directly to your configured Open WebUI server. No third parties are involved.

## Setup Instructions

1. **Install the App:** Download the latest APK from the [Releases](https://github.com/LongXi/owbi-assistant/releases) page or build it from source.
2. **Configure the Server:** Open the "Owbi Assistant" app from your app drawer. Enter your Open WebUI server URL (e.g., `https://your-open-webui-instance.com`) and choose your preferred launch behavior (Overlay vs. Full Screen, Auto-Dictation, etc.).
3. **Set as Default Assistant:**
   - Go to your Android **Settings**.
   - Navigate to **Apps** > **Default Apps** > **Digital assistant app**.
   - Select **Owbi Assistant**.
4. **Trigger:** Long-press your power button or swipe from the bottom corner to summon your AI!

## Building from Source

To compile the app yourself, you will need Android Studio and the Android SDK.

```bash
git clone https://github.com/LongXi/owbi-assistant.git
cd owbi-assistant
./gradlew assembleDebug
```

## Privacy Policy
Please refer to the [Privacy Policy](PRIVACY_POLICY.md) for details on how data is handled.

## Disclaimer
This application is an independent client designed to interface with the Open WebUI software. It is not affiliated with, endorsed by, or sponsored by Open WebUI Inc.
