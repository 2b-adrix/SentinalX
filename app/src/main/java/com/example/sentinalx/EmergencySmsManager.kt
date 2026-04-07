package com.example.sentinalx

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object EmergencySmsManager {
    private const val TAG = "EmergencySmsManager"
    private const val PREFS_NAME = "emergency_prefs"
    private const val KEY_NUMBER = "emergency_number"
    private const val COOLDOWN_MS = 5 * 60 * 1000L // 5 minutes

    private var lastSentTimestamp = 0L

    private val _smsStatus = MutableStateFlow<String?>(null)
    val smsStatus = _smsStatus.asStateFlow()

    fun getEmergencyNumber(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NUMBER, "") ?: ""
    }

    fun saveEmergencyNumber(context: Context, number: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NUMBER, number)
            .apply()
    }

    fun sendEmergencyAlert(context: Context) {
        val number = getEmergencyNumber(context)
        if (number.isBlank()) {
            Log.w(TAG, "No emergency number configured")
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSentTimestamp < COOLDOWN_MS) {
            Log.d(TAG, "SMS cooldown active")
            return
        }

        if (!PermissionUtils.hasPermission(context, android.Manifest.permission.SEND_SMS)) {
            Log.e(TAG, "SEND_SMS permission not granted")
            return
        }

        try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val message = "⚠️ SentinelX Alert: A high-risk threat was detected on this device. Please check immediately."
            smsManager?.sendTextMessage(number, null, message, null, null)

            lastSentTimestamp = currentTime
            _smsStatus.value = "Alert sent to emergency contact"
            Log.i(TAG, "Emergency SMS sent to $number")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS", e)
        }
    }

    fun clearStatus() {
        _smsStatus.value = null
    }
}
