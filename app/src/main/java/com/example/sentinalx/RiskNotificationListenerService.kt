package com.example.sentinalx

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class RiskNotificationListenerService : NotificationListenerService() {

    private val TAG = "RiskNotificationListener"
    private lateinit var overlayManager: RiskOverlayManager

    override fun onCreate() {
        super.onCreate()
        overlayManager = RiskOverlayManager(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        ThreatRepository.setNotificationActive(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        ThreatRepository.setNotificationActive(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        if (ThreatRepository.ignoredPackages.value.contains(packageName)) return
        if (!isMonitoredApp(packageName)) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        
        if (text.isBlank()) return

        // Check for trusted senders if needed
        // For now, we use the refined analyzer which has negative signals for OTPs etc.

        val combinedContent = "$title $text"
        Log.d(TAG, "Analyzing notification from $packageName: $combinedContent")

        val result = RiskAnalyzer.analyze(combinedContent)
        
        // Always add to repository so it shows in the feed
        ThreatRepository.addThreat(packageName, combinedContent, result)

        if (result.riskLevel == RiskLevel.HIGH) {
            Log.w(TAG, "HIGH RISK DETECTED: ${result.category} Score: ${result.riskScore}")
            overlayManager.showScamAlert(result, combinedContent)
        }
    }

    private fun isMonitoredApp(packageName: String): Boolean {
        val monitored = listOf(
            "com.whatsapp",
            "com.google.android.apps.messaging", // SMS
            "org.telegram.messenger",
            "com.google.android.gm" // Gmail
        )
        return monitored.contains(packageName)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Handle if needed
    }
}
