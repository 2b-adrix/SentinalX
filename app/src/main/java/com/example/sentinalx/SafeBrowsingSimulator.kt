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

    // Homograph detector: checks for look-alike characters (e.g., 'o' vs '0', 'l' vs '1')
    private val HOMOGRAPH_MAP = mapOf(
        '0' to 'o', '1' to 'l', '3' to 'e', '4' to 'a', '5' to 's', '7' to 't', '8' to 'b'
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

            // 2. Phishing Keyword Check (Subdomain/Path) & Homograph Detection
            val domain = getDomainName(lowerUrl)
            val isLookAlike = containsLookAlikes(domain)
            
            if (PHISHING_KEYWORDS.any { lowerUrl.contains(it) } || isLookAlike) {
                // If it contains a keyword but isn't on the official domain
                val isOfficial = lowerUrl.contains("sbi.co.in") || lowerUrl.contains("hdfcbank.com") || 
                                 lowerUrl.contains("amazon.in") || lowerUrl.contains("flipkart.com") ||
                                 lowerUrl.contains("google.com") || lowerUrl.contains("microsoft.com")
                
                if (!isOfficial) {
                    totalRisk += 40
                    reasons.add("Potential brand impersonation in URL")
                    
                    if (isLookAlike) {
                        totalRisk += 25
                        reasons.add("Visual deception detected (Look-alike characters in domain)")
                    }
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

            // 5. IP Address check
            if (urlString.matches(".*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*".toRegex())) {
                totalRisk += 35
                reasons.add("Direct IP address used instead of domain name")
            }
        }

        return UrlCheckResult(
            isSafe = totalRisk < 50,
            riskScore = totalRisk.coerceIn(0, 100),
            reasons = reasons
        )
    }

    private fun getDomainName(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val domain = uri.host ?: ""
            if (domain.startsWith("www.")) domain.substring(4) else domain
        } catch (e: Exception) {
            url.split("/").getOrNull(2) ?: ""
        }
    }

    private fun containsLookAlikes(domain: String): Boolean {
        // Simple heuristic: if a domain contains both 'l' and '1', or 'o' and '0'
        // Or if it uses numbers where characters usually go in brand names
        val hasNumbers = domain.any { it.isDigit() }
        if (!hasNumbers) return false

        // Check for common brand substitutions
        val substitutions = listOf("sbi", "amazon", "google", "bank")
        return substitutions.any { brand ->
            val fuzzyBrand = brand.map { char -> 
                HOMOGRAPH_MAP.entries.find { it.value == char }?.key ?: char 
            }.joinToString("")
            domain.contains(fuzzyBrand) && domain != brand
        }
    }
}
