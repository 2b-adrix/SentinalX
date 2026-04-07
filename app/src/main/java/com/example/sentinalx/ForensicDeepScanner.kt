package com.example.sentinalx

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

object ForensicDeepScanner {
    private val _scanProgress = MutableStateFlow(ScanProgress())
    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    suspend fun performFullScan(context: Context) = withContext(Dispatchers.IO) {
        _scanProgress.update { it.copy(isScanning = true, progress = 0f) }
        
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val total = apps.size
        val results = mutableListOf<ScanResult>()

        apps.forEachIndexed { index, appInfo ->
            val appName = pm.getApplicationLabel(appInfo).toString()
            _scanProgress.update { 
                it.copy(
                    currentApp = appName,
                    progress = (index + 1).toFloat() / total
                )
            }

            val flags = mutableListOf<String>()
            var riskScore = 0

            // Heuristic 1: Suspicious Keywords in App Name
            val suspiciousKeywords = listOf("cleaner", "booster", "optimizer", "battery", "fast", "ram")
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                if (suspiciousKeywords.any { appName.lowercase().contains(it) }) {
                    flags.add("Suspicious Utility Keyword Detected")
                    riskScore += 25
                }

                // Heuristic 2: Hidden Apps (Mock check for launcher intent)
                val intent = pm.getLaunchIntentForPackage(appInfo.packageName)
                if (intent == null) {
                    flags.add("No Launchable Activity (Hidden)")
                    riskScore += 15
                }
                
                // Simulate deep inspection delay
                delay(10)
            }

            if (riskScore > 0) {
                results.add(ScanResult(
                    packageName = appInfo.packageName,
                    appName = appName,
                    riskScore = riskScore,
                    flags = flags,
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0)
                ))
            }
        }

        _scanProgress.update { 
            it.copy(
                isScanning = false,
                progress = 1f,
                results = results.sortedByDescending { it.riskScore }
            )
        }
    }

    fun removeResult(packageName: String) {
        _scanProgress.update { state ->
            state.copy(results = state.results.filterNot { it.packageName == packageName })
        }
    }

    fun clearResults() {
        _scanProgress.value = ScanProgress()
    }
}
