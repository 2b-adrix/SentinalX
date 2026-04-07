package com.example.sentinalx

import android.util.Log
import java.net.URL
import java.util.regex.Pattern

object SafeBrowsingSimulator {
    
    // Local "Blacklist" of high-risk TLDs and suspicious domain patterns
    private val SUSPICIOUS_TLDS = listOf(".xyz", ".top", ".info", ".win", ".bid", ".club", ".online", ".site", ".tk", ".ml", ".ga", ".cf", ".gq")
    
    // Keywords often used in phishing URLs
    private val PHISHING_KEYWORDS = listOf("sbi", "hdfc", "bank", "update", "verify", "secure", "login", "paytm", "amazon", "flipkart", "win", "lottery", "gift")

    // RegEx for extracting URLs
    private val URL_PATTERN = Pattern.compile(
        "(https?://(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,})"
    )

    data class UrlCheckResult(
        val isSafe: Boolean,
        val riskScore: Int,
        val reasons: List<String>
    )

    fun checkUrl(text: String): UrlCheckResult {
        val matcher = URL_PATTERN.matcher(text)
        val reasons = mutableListOf<String>()
        var totalRisk = 0

        while (matcher.find()) {
            val urlString = matcher.group()
            val lowerUrl = urlString.lowercase()
            
            // 1. Check TLD
            if (SUSPICIOUS_TLDS.any { lowerUrl.endsWith(it) || lowerUrl.contains("$it/") }) {
                totalRisk += 30
                reasons.add("Suspicious Top-Level Domain (TLD) detected")
            }

            // 2. Phishing Keyword Check (Subdomain/Path)
            if (PHISHING_KEYWORDS.any { lowerUrl.contains(it) }) {
                // If it contains a keyword but isn't on the official domain
                val isOfficial = lowerUrl.contains("sbi.co.in") || lowerUrl.contains("hdfcbank.com") || 
                                 lowerUrl.contains("amazon.in") || lowerUrl.contains("flipkart.com")
                
                if (!isOfficial) {
                    totalRisk += 40
                    reasons.add("Potential brand impersonation in URL")
                }
            }

            // 3. Length check (long URLs often hide redirect chains)
            if (urlString.length > 75) {
                totalRisk += 15
                reasons.add("Abnormally long URL (possible redirect chain)")
            }

            // 4. HTTPS check
            if (lowerUrl.startsWith("http://")) {
                totalRisk += 25
                reasons.add("Insecure connection (HTTP instead of HTTPS)")
            }
        }

        return UrlCheckResult(
            isSafe = totalRisk < 50,
            riskScore = totalRisk.coerceIn(0, 100),
            reasons = reasons
        )
    }
}
