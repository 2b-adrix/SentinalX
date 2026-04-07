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
        if (!isMonitoredApp(packageName)) return

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
            
            // Capture the root node on the main thread
            val rootNode = rootInActiveWindow ?: return@launch
            
            // Perform scraping and analysis in the background
            launch(Dispatchers.Default) {
                val screenText = getAllText(rootNode)
                
                // On modern Android (API 33+), recycling is managed by the system.
                // We'll skip manual recycling to avoid "IllegalStateException" if the system 
                // decides to recycle it first.
                
                if (screenText.isNotBlank() && screenText.length > 20) {
                    analyzeContent(packageName, screenText)
                }
            }
        }
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

    private fun getAllText(rootNode: AccessibilityNodeInfo?): String {
        if (rootNode == null) return ""
        val sb = StringBuilder()
        val stack = mutableListOf<AccessibilityNodeInfo>()
        stack.add(rootNode)

        // Limiting depth to prevent extreme cases in complex layouts
        var processedCount = 0
        val MAX_NODES = 500

        while (stack.isNotEmpty() && processedCount < MAX_NODES) {
            val node = stack.removeAt(stack.size - 1)
            processedCount++
            
            node.text?.let { sb.append(it).append(" ") }
            node.contentDescription?.let { sb.append(it).append(" ") }

            for (i in 0 until node.childCount) {
                try {
                    node.getChild(i)?.let { child ->
                        stack.add(child)
                    }
                } catch (e: Exception) {
                    // Node might be invalidated by the time we try to get child
                    Log.e(TAG, "Failed to get child node", e)
                }
            }
        }
        return sb.toString().trim()
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
