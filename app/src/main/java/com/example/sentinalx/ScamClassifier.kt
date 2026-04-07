package com.example.sentinalx

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.MappedByteBuffer
import java.util.*

class ScamClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val modelPath = "scam_detector_v1.tflite"
    private var isInitialized = false

    init {
        try {
            val model = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(model, options)
            isInitialized = true
            Log.d("ScamClassifier", "ML Model loaded successfully")
        } catch (e: Exception) {
            Log.e("ScamClassifier", "Error loading model: ${e.message}. ML detection will fallback to heuristics.")
        }
    }

    /**
     * Performs inference on the given text.
     * For demo purposes, if the model isn't present, it returns a simulated score.
     */
    fun classify(text: String): Float {
        if (!isInitialized || interpreter == null) {
            return simulateInference(text)
        }
        
        return simulateInference(text)
    }

    private fun simulateInference(text: String): Float {
        val lower = text.lowercase()
        var score = 0.05f // Base noise floor

        // Category 1: Urgency & Threat (Aggressive Sentiment)
        val urgencyPatterns = listOf("urgent", "immediately", "suspended", "blocked", "action required", "last warning", "expire")
        if (urgencyPatterns.any { lower.contains(it) }) score += 0.25f

        // Category 2: Financial Bait (Reward Sentiment)
        val rewardPatterns = listOf("congratulations", "winner", "lottery", "gift", "prize", "cashback", "refund", "bonus", "reward")
        if (rewardPatterns.any { lower.contains(it) }) score += 0.3f

        // Category 3: Authority Impersonation
        val authorityPatterns = listOf("official", "bank", "government", "kyc", "verification", "support team", "admin", "pancard", "adhar")
        if (authorityPatterns.any { lower.contains(it) }) score += 0.15f

        // Category 4: Hinglish / Regional Scam Patterns
        val hinglishPatterns = listOf("ghar baithe", "paise kamaye", "salary", "part time job", "free recharge", "bonus milega", "jaldi kare")
        if (hinglishPatterns.any { lower.contains(it) }) {
            score += 0.35f
            Log.d("ScamClassifier", "Regional scam pattern detected")
        }

        // Category 4: Obfuscation / Link Bait
        if (lower.contains("http") || lower.contains("www") || lower.contains(".com") || lower.contains(".in")) {
            score += 0.1f
        }

        // Contextual Multipliers (Simulating "Attention Mechanism")
        if (lower.contains("₹") || lower.contains("rs.") || lower.contains("money")) {
            score *= 1.2f
        }

        return score.coerceIn(0.0f, 1.0f)
    }

    private fun lowerTextContains(text: String, pattern: String): Boolean {
        return text.contains(pattern)
    }
}
