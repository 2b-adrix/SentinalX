# SentinelX - High-Polish Android Cybersecurity

SentinelX is a sophisticated, real-time Android cybersecurity application designed to defend users against modern digital threats through a combination of heuristic analysis and Machine Learning (ML). Developed with a focus on both security and high-end design, SentinelX offers a "Digital Risk Shield" dark theme and a premium, custom-engineered adaptive icon.

## 🛡️ Key Features

### 🔍 Threat Detection & Analysis
*   **Real-time Heuristic Monitoring**: Continuous background monitoring of device activities, including SMS, notifications, and app behavior, to detect potential scams.
*   **ML-Powered Scam Classification**: Utilizes an integrated TensorFlow Lite (TFLite) model for deep pattern recognition in suspicious communications.
*   **Network Integrity Scanning**: Active checks on Wi-Fi and mobile network stability to detect potential man-in-the-middle (MITM) attacks.
*   **Safe Browsing Simulation**: Proactive URL analysis and protection against malicious links.

### 🚨 Emergency Response
*   **Automated Emergency Alerts**: When a `HIGH` risk threat is detected, the app can automatically dispatch emergency SMS alerts with precise threat data.
*   **Intelligent Alert Cooldown**: Sophisticated logic to prevent notification fatigue while ensuring critical alerts are never missed.
*   **Risk Overlay System**: Real-time visual feedback using high-visibility UI overlays when critical threats are active.

### 📁 Forensic & Security Tools
*   **Forensic Reporting**: Export comprehensive JSON threat dossiers including `forensicLevel`, timestamps, and detailed `stats` (confidence scores, heuristic matches).
*   **Forensic Deep Scanner**: On-demand deep-dive analysis of historical threat data stored in the device's secure vault.
*   **Secure Vault (Database)**: Encrypted persistence using Room with SQLCipher integration, ensuring threat logs remain private even if the device is compromised.

### 🔐 Privacy & Branding
*   **Biometric Security**: Protects access to the security dashboard and forensic logs with native fingerprint and face-unlock integration.
*   **"Sentinel Shield" Identity**: A cohesive design language featuring a "Sentinel Blue" (#2563EB) and "Deep Space" (#0A0F1E) palette, with a premium vector-based adaptive icon system.

## 🚀 Tech Stack

*   **Language**: Kotlin 2.1.0 with advanced Coroutine management.
*   **UI Framework**: Jetpack Compose (BOM 2026.02.01) utilizing Material 3 components.
*   **Machine Learning**: TensorFlow Lite 2.16.1 (implemented with manual `MappedByteBuffer` loading to optimize memory and bypass library-level manifest conflicts).
*   **Database**: Room 2.7.0-alpha13 (SQLCipher encrypted).
*   **Services**: Android Accessibility Service & Notification Listener Service for deep system-level visibility.
*   **Build System**: AGP 9.1.0.

## 📁 Project Structure

*   `MainActivity.kt`: Entry point and Compose-based navigation controller.
*   `ScamClassifier.kt`: Core ML inference engine using TFLite.
*   `RiskAnalyzer.kt`: The "brain" of SentinelX, orchestrating heuristic and ML data.
*   `ForensicDeepScanner.kt`: Engine for processing and generating audit-ready forensic reports.
*   `SentinelAccessibilityService.kt`: Background monitor for UI-based threat detection.
*   `EmergencySmsManager.kt`: Secure dispatch system for critical risk alerts.
*   `Database.kt`: Room persistence layer with encrypted entities.

## 🛠️ Installation & Setup

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-repo/SentinelX.git
    ```
2.  **Open in Android Studio**: Open the project in Android Studio (Ladybug or newer).
3.  **Gradle Sync**: Wait for dependencies to download. Note the TFLite version pinning (2.16.1) to ensure manifest merger stability.
4.  **TFLite Model**: Ensure `scam_model.tflite` is present in `app/src/main/assets/`.
5.  **Permissions**: Grant SMS, Accessibility, and Notification Listener permissions upon first launch to enable full protection.

## 🏗️ Build Notes

*   **Theme Integration**: The base theme inherits from `android:Theme.Material.NoActionBar` in `themes.xml` to prevent AAPT linking errors while allowing the Compose `Material3` layer to handle the UI.
*   **Dependency Management**: `litert-support` is currently omitted from dependencies to avoid namespace collisions; all TFLite utilities are handled via manual asset buffer management.

## 📊 Forensic Export Example

```json
{
  "timestamp": "2024-05-20T14:32:01.452Z",
  "threatType": "PHISHING_SMS",
  "riskLevel": "HIGH",
  "forensicLevel": "DETAILED",
  "stats": {
    "model_confidence": 0.987,
    "heuristic_flags": ["suspicious_url", "emergency_tone"],
    "scanner_id": "SentinelX-A1-Core"
  }
}
```

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
