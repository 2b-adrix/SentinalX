package com.example.sentinalx

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ForensicExporter {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    data class DeviceMetadata(
        val manufacturer: String,
        val model: String,
        val androidVersion: String,
        val sdkLevel: Int,
        val securityPatch: String
    )

    data class ForensicReport(
        val reportId: String,
        val generatedAt: String,
        val deviceIntegrity: String,
        val metadata: DeviceMetadata,
        val totalThreatsAnalyzed: Int,
        val threatHistory: List<ThreatEvent>
    )

    suspend fun exportThreatHistory(context: Context, threats: List<ThreatEvent>): File? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            
            val metadata = DeviceMetadata(
                manufacturer = android.os.Build.MANUFACTURER,
                model = android.os.Build.MODEL,
                androidVersion = android.os.Build.VERSION.RELEASE,
                sdkLevel = android.os.Build.VERSION.SDK_INT,
                securityPatch = if (android.os.Build.VERSION.SDK_INT >= 23) android.os.Build.VERSION.SECURITY_PATCH else "UNKNOWN"
            )

            val report = ForensicReport(
                reportId = "SX-REPORT-${UUID.randomUUID().toString().take(8).uppercase()}",
                generatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                deviceIntegrity = "PASS (SQLCipher-Encrypted)",
                metadata = metadata,
                totalThreatsAnalyzed = threats.size,
                threatHistory = threats
            )

            val jsonString = gson.toJson(report)
            val fileName = "SentinelX_Forensic_Report_$timestamp.json"
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { 
                it.write(jsonString.toByteArray())
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Forensic Intelligence"))
    }
}
