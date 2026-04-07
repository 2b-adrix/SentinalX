package com.example.sentinalx

import android.content.Context
import java.util.regex.Pattern

object RiskAnalyzer {
    
    private val URL_PATTERN = Pattern.compile(
        "(https?://(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?://(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})"
    )

    private val SUSPICIOUS_URL_SHORTENERS = listOf("bit.ly", "t.co", "tinyurl.com", "goo.gl", "is.gd", "buff.ly", "adf.ly")

    private var classifier: ScamClassifier? = null

    fun init(context: Context) {
        classifier = ScamClassifier(context)
    }

    fun analyze(text: String): AnalysisResult {
        val mlScore = classifier?.classify(text) ?: 0f
        var riskScore = (mlScore * 100).toInt()
        val reasons = mutableListOf<String>()
        
        if (mlScore > 0.6f) {
            reasons.add("On-device ML detected suspicious sentiment pattern")
        }

        val lowerText = text.lowercase()
        var category = "Unclassified"
        var signals = 0

        fun addSignal(score: Int, reason: String) {
            riskScore += score
            reasons.add(reason)
            signals++
        }

        // 1. Urgency Heuristics (Lower weight unless combined)
        val urgencyKeywords = listOf("urgent", "immediately", "action required", "last chance", "expire soon", "blocked")
        if (urgencyKeywords.any { lowerText.contains(it) }) {
            addSignal(15, "Uses high-pressure urgency language")
        }

        // 2. Financial/Payment Scams
        val paymentKeywords = listOf("processing fee", "gst charges", "delivery fee", "claim prize", "lottery", "won ₹", "reward money")
        if (paymentKeywords.any { lowerText.contains(it) }) {
            addSignal(40, "Requests suspicious upfront payment or 'fees'")
            category = "Payment Scam"
        }

        // 3. KYC / Bank Fraud
        val bankKeywords = listOf(
            "kyc expired", "pan card update", "account suspended", "verify details", 
            "netbanking", "official-bank-update", "adhar update", "pancard pending"
        )
        if (bankKeywords.any { lowerText.contains(it) }) {
            addSignal(45, "Impersonates bank or official KYC verification")
            category = if (category == "Unclassified") "KYC/Bank Fraud" else category
        }

        // 4. Job Scams (Hinglish/Common Patterns)
        val jobKeywords = listOf(
            "part-time job", "work from home", "daily salary", "no experience needed", 
            "telegram job", "earn money daily", "paytm cash", "ghar baithe paise"
        )
        if (jobKeywords.any { lowerText.contains(it) }) {
            addSignal(35, "Promises unrealistic employment returns")
            category = if (category == "Unclassified") "Job Scam" else category
        }

        // 5. Technical Phishing Signals (URL Analysis)
        val urlAnalysis = SafeBrowsingSimulator.checkUrl(text)
        if (urlAnalysis.riskScore > 0) {
            addSignal(urlAnalysis.riskScore, urlAnalysis.reasons.joinToString(", "))
        }

        val matcher = URL_PATTERN.matcher(text)
        while (matcher.find()) {
            val url = matcher.group()
            val lowerUrl = url.lowercase()
            
            if (SUSPICIOUS_URL_SHORTENERS.any { lowerUrl.contains(it) }) {
                addSignal(30, "Contains obfuscated short-URL")
            }
            
            // Check for homograph attacks or non-ASCII characters in suspicious URLs
            if (url.any { it.code > 127 }) {
                addSignal(40, "Detected non-standard characters in link (possible Homograph attack)")
            }

            // Flag URLs using IP addresses instead of domains
            if (url.matches(".*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*".toRegex())) {
                addSignal(35, "Direct IP address used instead of domain name")
            }

            category = if (category == "Unclassified") "Phishing Link" else category
        }

        // 6. Negative Signals (Trusted Contexts)
        val trustedKeywords = listOf("otp is", "verification code", "receipt", "your order", "subscribed", "official")
        if (trustedKeywords.any { lowerText.contains(it) }) {
            riskScore -= 20
        }

        // 7. Accessibility False Positive Mitigation
        // If the text is very short (e.g. just navigation items), reduce score
        if (text.length < 20) {
            riskScore -= 30
        }

        val finalRiskScore = riskScore.coerceIn(0, 100)
        
        val riskLevel = when {
            finalRiskScore >= 70 -> RiskLevel.HIGH
            finalRiskScore >= 40 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        // Confidence logic: More signals = Higher confidence
        val confidenceScore = (signals * 25).coerceIn(10, 100)
        
        val trustLevel = when {
            riskLevel == RiskLevel.HIGH && confidenceScore >= 50 -> TrustLevel.DANGEROUS
            riskLevel == RiskLevel.LOW && confidenceScore >= 60 -> TrustLevel.SAFE
            else -> TrustLevel.SUSPICIOUS
        }

        return AnalysisResult(
            riskScore = finalRiskScore,
            riskLevel = riskLevel,
            confidenceScore = confidenceScore,
            trustLevel = trustLevel,
            reasons = if (reasons.isEmpty()) listOf("Content appears legitimate or insufficient data") else reasons,
            category = category,
            mlScore = mlScore
        )
    }
}
