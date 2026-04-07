package com.example.sentinalx

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SentinelAccessibilityService : AccessibilityService() {

    private val TAG = "SentinelAccessibility"
    private lateinit var overlayManager: RiskOverlayManager
    private var analysisJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = RiskOverlayManager(this)
        ThreatRepository.setAccessibilityActive(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        ThreatRepository.setAccessibilityActive(false)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: ""
        if (ThreatRepository.ignoredPackages.value.contains(packageName)) return
        
        // Always monitor Settings to protect against malicious service activation
        val isSettings = packageName.contains("com.android.settings")
        if (!isMonitoredApp(packageName) && !isSettings) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                throttleAnalysis(packageName)
            }
        }
    }

    private fun throttleAnalysis(packageName: String) {
        analysisJob?.cancel()
        analysisJob = scope.launch {
            delay(500) // Debounce screen scraping
            
            val rootNode = rootInActiveWindow ?: return@launch
            
            launch(Dispatchers.Default) {
                val sb = StringBuilder()
                val sensitiveFieldsFound = mutableListOf<String>()
                
                // Deep scan the node tree
                analyzeNodeTree(rootNode, sb, sensitiveFieldsFound)
                
                val screenText = sb.toString().trim()
                
                if (sensitiveFieldsFound.isNotEmpty()) {
                    Log.d(TAG, "Sensitive fields detected in $packageName: $sensitiveFieldsFound")
                    // If we find password fields in an app that isn't a known browser or bank
                    if (!isKnownSafeApp(packageName)) {
                        analyzeContent(packageName, "SENSITIVE_INPUT_DETECTED: $screenText")
                    }
                } else if (screenText.isNotBlank() && screenText.length > 20) {
                    analyzeContent(packageName, screenText)
                }
            }
        }
    }

    private fun analyzeNodeTree(node: AccessibilityNodeInfo?, sb: StringBuilder, sensitiveFields: MutableList<String>) {
        if (node == null) return

        // Extract text content
        node.text?.let { sb.append(it).append(" ") }
        node.contentDescription?.let { sb.append(it).append(" ") }

        // Detect sensitive inputs
        if (node.isPassword) {
            sensitiveFields.add("Password Field")
        }
        
        // Look for common PIN/OTP field patterns in labels
        val hint = node.hintText?.toString()?.lowercase() ?: ""
        if (hint.contains("pin") || hint.contains("otp") || hint.contains("cvv")) {
            sensitiveFields.add("Secure Input ($hint)")
        }

        for (i in 0 until node.childCount) {
            try {
                analyzeNodeTree(node.getChild(i), sb, sensitiveFields)
            } catch (e: Exception) {
                // Ignore transient node invalidation
            }
        }
    }

    private fun isKnownSafeApp(packageName: String): Boolean {
        val safePrefixes = listOf("com.android.chrome", "org.mozilla.firefox", "com.google.android.apps.messaging")
        return safePrefixes.any { packageName.startsWith(it) } || packageName.contains("bank")
    }

    private fun analyzeContent(packageName: String, text: String) {
        val result = RiskAnalyzer.analyze(text)
        if (result.riskLevel == RiskLevel.HIGH) {
            Log.w(TAG, "HIGH RISK DETECTED ON SCREEN in $packageName: ${result.category}")
            // Switch back to Main for UI updates
            scope.launch {
                overlayManager.showScamAlert(result, text)
            }
            // Also log to repository for history
            ThreatRepository.addThreat(packageName, text.take(150) + "...", result)
        }
    }

    private fun isMonitoredApp(packageName: String): Boolean {
        if (packageName.contains("com.android.systemui") || packageName.contains("launcher")) return false
        
        val monitoredKeywords = listOf("chrome", "browser", "whatsapp", "telegram", "message", "bank", "pay", "wallet")
        return monitoredKeywords.any { packageName.lowercase().contains(it) }
    }

    override fun onInterrupt() {
        analysisJob?.cancel()
    }
}
