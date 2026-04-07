# SentinelX - High-Polish Android Cybersecurity

SentinelX is a sophisticated Android cybersecurity application designed to provide real-time heuristic and Machine Learning (ML) monitoring to protect users from digital threats. Featuring a "Digital Risk Shield" dark theme and a custom-designed adaptive icon, SentinelX combines high-end aesthetics with robust security features.

## 🛡️ Key Features

*   **Real-time Threat Monitoring**: Continuous heuristic and ML-based analysis of device activity to detect potential scams and security risks.
*   **ML-Powered Scam Classification**: Integrated TensorFlow Lite (TFLite) model for accurate identification of scam patterns.
*   **Automated Emergency Alerts**: Instant SMS dispatch for `HIGH` risk threats with built-in cooldown logic to prevent alert fatigue.
*   **Forensic Reporting**: Detailed JSON export of threat data, including `forensicLevel` and detailed `stats` for security auditing.
*   **Encrypted Data Persistence**: Secure storage using Room with SQLCipher for database encryption.
*   **Biometric Security**: Biometric locking mechanism to protect sensitive threat history and application settings.
*   **"Sentinel Shield" Branding**: Custom adaptive vector logo and a "Digital Risk Shield" dark theme (Blue #2563EB on Deep Space #0A0F1E).

## 🚀 Tech Stack

*   **Language**: Kotlin 2.1.0
*   **Build System**: Android Gradle Plugin (AGP) 9.1.0
*   **UI Framework**: Jetpack Compose (BOM 2026.02.01)
*   **Machine Learning**: TensorFlow Lite 2.16.1 (Manual `MappedByteBuffer` loading for maximum compatibility)
*   **Database**: Room 2.7.0-alpha13 with SQLCipher
*   **Architecture**: MVVM with modern Android development best practices.

## 🛠️ Installation & Setup

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-repo/SentinelX.git
    ```
2.  **Open in Android Studio**: Open the project in Android Studio (Ladybug or newer recommended).
3.  **Sync Gradle**: Allow the project to sync dependencies. Note that TFLite is pinned to `2.16.1` to avoid namespace collisions.
4.  **TFLite Model**: Ensure the `.tflite` model is correctly placed in the `assets` folder. The `ScamClassifier` uses manual loading via `MappedByteBuffer`.
5.  **Permissions**: The app requires SMS, Accessibility, and Notification permissions for full functionality.

## 🏗️ Build Notes

*   **Theme Stability**: The theme parent is set to `android:Theme.Material.NoActionBar` in `themes.xml` to ensure AAPT linking stability while maintaining full Compose UI compatibility.
*   **TFLite Support**: `tensorflow-lite-support` libraries are currently excluded to resolve manifest merger conflicts; manual asset loading is implemented as a robust alternative.

## 📊 Forensic Export Example

```json
{
  "timestamp": "2024-05-20T10:00:00Z",
  "threatType": "SCAM_SMS",
  "riskLevel": "HIGH",
  "forensicLevel": "DETAILED",
  "stats": {
    "confidence": 0.98,
    "heuristicMatches": 5
  }
}
```

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
