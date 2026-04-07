package com.example.sentinalx

import android.content.Context
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

object ThreatRepository {
    private val _threats = MutableStateFlow<List<ThreatEvent>>(emptyList())
    val threats: StateFlow<List<ThreatEvent>> = _threats.asStateFlow()

    private val _ignoredPackages = MutableStateFlow<Set<String>>(setOf("com.android.settings", "com.google.android.packageinstaller", "com.example.sentinalx"))
    val ignoredPackages: StateFlow<Set<String>> = _ignoredPackages.asStateFlow()

    private val _isAccessibilityActive = MutableStateFlow(false)
    val isAccessibilityActive: StateFlow<Boolean> = _isAccessibilityActive.asStateFlow()

    private val _isNotificationActive = MutableStateFlow(false)
    val isNotificationActive: StateFlow<Boolean> = _isNotificationActive.asStateFlow()

    val isServiceActive: StateFlow<Boolean> = combine(_isAccessibilityActive, _isNotificationActive) { a, n ->
        a || n
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, false)

    private val _latestAlert = MutableStateFlow<ThreatEvent?>(null)
    val latestAlert: StateFlow<ThreatEvent?> = _latestAlert.asStateFlow()

    private val _globalThreatPings = MutableStateFlow<List<Offset>>(emptyList())
    val globalThreatPings: StateFlow<List<Offset>> = _globalThreatPings.asStateFlow()

    private var database: AppDatabase? = null
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    fun init(context: Context) {
        database = AppDatabase.getDatabase(context)
        repositoryScope.launch {
            database?.threatDao()?.getAllThreats()?.collect { entities ->
                _threats.value = entities.map { it.toEvent() }.reversed()
            }
        }

        // Start simulated global threat ping generator
        repositoryScope.launch {
            val random = java.util.Random()
            val cities = listOf("Mumbai", "London", "New York", "Tokyo", "Singapore", "Berlin", "Dubai")
            while (true) {
                kotlinx.coroutines.delay((2000L..5000L).random())
                val newPing = Offset(
                    x = 0.1f + random.nextFloat() * 0.8f,
                    y = 0.1f + random.nextFloat() * 0.8f
                )
                _globalThreatPings.update { (it + newPing).takeLast(8) }
                kotlinx.coroutines.delay(3000)
                _globalThreatPings.update { it - newPing }
            }
        }
    }

    fun setAccessibilityActive(active: Boolean) {
        _isAccessibilityActive.value = active
    }

    fun setNotificationActive(active: Boolean) {
        _isNotificationActive.value = active
    }

    fun addThreat(appName: String, message: String, analysis: AnalysisResult) {
        if (_ignoredPackages.value.contains(appName)) return

        val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val newThreat = ThreatEvent(
            id = UUID.randomUUID().toString(),
            appName = appName,
            message = message,
            riskLevel = analysis.riskLevel,
            riskScore = analysis.riskScore,
            confidenceScore = analysis.confidenceScore,
            trustLevel = analysis.trustLevel,
            timestamp = "Today, $currentTime",
            category = analysis.category,
            advice = getAdviceForCategory(analysis.category),
            reasons = analysis.reasons,
            mlScore = analysis.mlScore,
            isReported = false
        )
        
        repositoryScope.launch {
            database?.threatDao()?.insert(newThreat.toEntity())
        }
        
        if (analysis.riskLevel == RiskLevel.HIGH) {
            _latestAlert.value = newThreat
        }
    }

    fun clearAlert() {
        _latestAlert.value = null
    }

    fun ignorePackage(packageName: String) {
        _ignoredPackages.update { it + packageName }
    }

    fun removeIgnoredPackage(packageName: String) {
        _ignoredPackages.update { it - packageName }
    }

    fun clear() {
        _threats.value = emptyList()
        repositoryScope.launch {
            database?.threatDao()?.deleteAll()
        }
    }

    fun reportThreat(threat: ThreatEvent) {
        repositoryScope.launch {
            kotlinx.coroutines.delay(1500)
            database?.threatDao()?.markAsReported(threat.id)
        }
    }

    private fun getAdviceForCategory(category: String): String = when (category) {
        "Job Scam" -> "Legitimate companies never ask for money during the hiring process."
        "KYC/Bank Fraud" -> "Banks never ask for KYC updates via SMS links."
        "Payment Scam" -> "Never pay 'shipping' or 'taxes' to claim a prize."
        else -> "Do not click any links or share sensitive information."
    }
}

fun ThreatEvent.toEntity() = ThreatEntity(
    id = id,
    appName = appName,
    message = message,
    riskLevel = riskLevel.name,
    riskScore = riskScore,
    confidenceScore = confidenceScore,
    trustLevel = trustLevel.name,
    timestamp = timestamp,
    category = category,
    advice = advice,
    reasonsJson = reasons.joinToString("|"),
    mlScore = mlScore,
    isReported = isReported
)

fun ThreatEntity.toEvent() = ThreatEvent(
    id = id,
    appName = appName,
    message = message,
    riskLevel = try { RiskLevel.valueOf(riskLevel) } catch (e: Exception) { RiskLevel.MEDIUM },
    riskScore = riskScore,
    confidenceScore = confidenceScore,
    trustLevel = try { TrustLevel.valueOf(trustLevel) } catch (e: Exception) { TrustLevel.SUSPICIOUS },
    timestamp = timestamp,
    category = category,
    advice = advice,
    reasons = reasonsJson.split("|").filter { it.isNotEmpty() },
    mlScore = mlScore,
    isReported = isReported
)
