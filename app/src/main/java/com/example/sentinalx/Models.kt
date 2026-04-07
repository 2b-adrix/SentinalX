package com.example.sentinalx

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

enum class TrustLevel {
    SAFE, SUSPICIOUS, DANGEROUS
}

data class AnalysisResult(
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val confidenceScore: Int,
    val trustLevel: TrustLevel,
    val reasons: List<String>,
    val category: String,
    val mlScore: Float = 0.0f
)

data class ThreatEvent(
    val id: String,
    val appName: String,
    val message: String,
    val riskLevel: RiskLevel,
    val riskScore: Int,
    val confidenceScore: Int,
    val trustLevel: TrustLevel,
    val timestamp: String,
    val category: String,
    val advice: String,
    val reasons: List<String>,
    val mlScore: Float = 0.0f,
    val isReported: Boolean = false
)

data class ScanResult(
    val packageName: String,
    val appName: String,
    val riskScore: Int,
    val flags: List<String>,
    val isSystemApp: Boolean
)

data class ScanProgress(
    val currentApp: String = "",
    val progress: Float = 0f,
    val isScanning: Boolean = false,
    val results: List<ScanResult> = emptyList()
)

data class NetworkStatus(
    val isSecure: Boolean = true,
    val ssid: String = "Internal Mesh",
    val signalStrength: Int = 100,
    val threatDetected: Boolean = false,
    val activeThreats: List<String> = emptyList(),
    val isScanning: Boolean = false
)

data class UiState(
    val threats: List<ThreatEvent> = emptyList(),
    val ignoredPackages: Set<String> = emptySet(),
    val isProtectionEnabled: Boolean = true,
    val isDemoModeEnabled: Boolean = false,
    val lastDetectedThreat: ThreatEvent? = null,
    val showTestOverlay: Boolean = false,
    val threatTrend: List<Int> = List(7) { 0 },
    val globalHealthScore: Int = 100,
    val isHistoryLocked: Boolean = true,
    val activeThreatInterceptions: Int = 0,
    val systemIntegrityCheck: Boolean = true,
    val networkIntegrityStatus: NetworkStatus = NetworkStatus(),
    val isForensicExporting: Boolean = false,
    val globalThreatPings: List<androidx.compose.ui.geometry.Offset> = emptyList(),
    val scanProgress: ScanProgress = ScanProgress(),
    val emergencyNumber: String = "",
    val smsStatus: String? = null
)
