package com.example.sentinalx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                ThreatRepository.threats,
                ThreatRepository.ignoredPackages,
                ThreatRepository.isServiceActive,
                ThreatRepository.latestAlert,
                ForensicDeepScanner.scanProgress
            ) { threats, ignored, isActive, alert, scan ->
                // Calculate trend (dummy logic for now, could be grouped by date)
                val trend = calculateTrend(threats)
                
                // Calculate Global Health Score
                // 100 - (avg risk of last 5 threats) if active, else 0
                val health = if (isActive) {
                    val avgRisk = if (threats.isEmpty()) 0 else threats.take(5).map { it.riskScore }.average().toInt()
                    (100 - avgRisk).coerceIn(0, 100)
                } else 0

                _uiState.update { 
                    it.copy(
                        threats = threats, 
                        ignoredPackages = ignored,
                        isProtectionEnabled = isActive,
                        lastDetectedThreat = alert,
                        showTestOverlay = alert != null,
                        threatTrend = trend,
                        globalHealthScore = health,
                        activeThreatInterceptions = threats.size,
                        systemIntegrityCheck = isActive,
                        globalThreatPings = ThreatRepository.globalThreatPings.value,
                        scanProgress = scan,
                        networkIntegrityStatus = NetworkIntegrityScanner.networkStatus.value,
                        smsStatus = EmergencySmsManager.smsStatus.value,
                        emergencyNumber = EmergencySmsManager.getEmergencyNumber(MainApplication.instance)
                    )
                }
            }.collect()
        }
        
        // Refresh pings periodically
        viewModelScope.launch {
            ThreatRepository.globalThreatPings.collect { pings ->
                _uiState.update { it.copy(globalThreatPings = pings) }
            }
        }

        // Refresh SMS status
        viewModelScope.launch {
            EmergencySmsManager.smsStatus.collect { status ->
                _uiState.update { it.copy(smsStatus = status) }
            }
        }

        // Refresh network status
        viewModelScope.launch {
            NetworkIntegrityScanner.networkStatus.collect { status ->
                _uiState.update { it.copy(networkIntegrityStatus = status) }
            }
        }
    }

    fun scanNetworkIntegrity() {
        viewModelScope.launch {
            NetworkIntegrityScanner.scanNetwork()
        }
    }

    private fun calculateTrend(threats: List<ThreatEvent>): List<Int> {
        // Return a fixed size list for the UI (e.g., last 7 detections or time slices)
        if (threats.isEmpty()) return List(7) { 0 }
        
        // Just take the risk scores of the last 7 threats for the visualization
        return threats.take(7).map { it.riskScore }.reversed()
            .let { list ->
                if (list.size < 7) List(7 - list.size) { 0 } + list else list
            }
    }

    fun ignorePackage(packageName: String) {
        ThreatRepository.ignorePackage(packageName)
        ForensicDeepScanner.removeResult(packageName)
    }

    fun removeIgnoredPackage(packageName: String) {
        ThreatRepository.removeIgnoredPackage(packageName)
    }

    fun simulateScam(type: String) {
        val (appName, message) = when (type) {
            "PHISHING" -> "WhatsApp" to "URGENT: New Job Offer: Earn ₹5000/day working from home. Pay ₹500 registration fee now!"
            "BANK" -> "SMS" to "SBI Alert: Your account is BLOCKED. Update KYC immediately at http://192.168.1.1/sbi-secure.in"
            "FRAUD" -> "Telegram" to "Amazon Gift: You won a free iPhone lottery. Pay ₹1299 processing fee to claim immediately."
            "HINGLISH" -> "WhatsApp" to "Namaste! Aapka ₹25,00,000 ka KBC lottery prize nikla hai. Processing fee ke liye ₹12,500 jama kare jaldi kare!"
            "ID_THEFT" -> "Unknown App" to "SENSITIVE_INPUT_DETECTED: User attempting to enter password into unverified utility app. URGENT ACTION REQUIRED."
            "HOMOGRAPH" -> "Chrome" to "Security Alert: Verify your account immediately at https://amaz0n.in (Note: The 'o' is a zero)"
            else -> "System" to "Generic suspicious activity detected."
        }

        val analysis = RiskAnalyzer.analyze(message)
        ThreatRepository.addThreat(appName, message, analysis)
    }

    fun toggleDemoMode() { _uiState.update { it.copy(isDemoModeEnabled = !it.isDemoModeEnabled) } }
    fun toggleProtection() { 
        // In a real app, this would start/stop the service intent
        // For now, we manually toggle the state in the repo to show the UI is responsive
        val current = uiState.value.isProtectionEnabled
        ThreatRepository.setAccessibilityActive(!current)
        ThreatRepository.setNotificationActive(!current)
    }
    fun clearHistory() { ThreatRepository.clear() }
    
    fun exportForensicReport(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isForensicExporting = true) }
            val file = ForensicExporter.exportThreatHistory(context, uiState.value.threats)
            if (file != null) {
                ForensicExporter.shareFile(context, file)
            }
            _uiState.update { it.copy(isForensicExporting = false) }
        }
    }

    fun reportThreat(threat: ThreatEvent) { ThreatRepository.reportThreat(threat) }
    fun toggleTestOverlay(show: Boolean) { _uiState.update { it.copy(showTestOverlay = show) } }
    fun setHistoryLocked(locked: Boolean) { _uiState.update { it.copy(isHistoryLocked = locked) } }

    fun runForensicDeepScan(context: Context, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            ForensicDeepScanner.performFullScan(context)
            onComplete()
        }
    }

    fun saveEmergencyNumber(number: String) {
        EmergencySmsManager.saveEmergencyNumber(MainApplication.instance, number)
        _uiState.update { it.copy(emergencyNumber = number) }
    }

    fun clearSmsStatus() {
        EmergencySmsManager.clearStatus()
    }
}
