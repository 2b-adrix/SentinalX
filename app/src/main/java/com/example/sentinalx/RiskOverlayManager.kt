package com.example.sentinalx

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import com.example.sentinalx.R

class RiskOverlayManager(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    fun showScamAlert(threat: AnalysisResult, content: String) {
        if (!PermissionUtils.canDrawOverlays(context)) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        
        // Using a basic layout - this requires res/layout/overlay_scam.xml
        // For simplicity in this prompt, I'll assume we are injecting a view or using a ComposeView if needed, 
        // but traditionally WindowManager uses XML views.
        overlayView = inflater.inflate(R.layout.overlay_scam, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            y = 100
        }

        overlayView?.findViewById<TextView>(R.id.tvThreatCategory)?.text = "⚠️ ${threat.category} Detected"
        overlayView?.findViewById<TextView>(R.id.tvThreatReason)?.text = threat.reasons.firstOrNull() ?: "Suspicious content identified"
        overlayView?.findViewById<Button>(R.id.btnDismiss)?.setOnClickListener { dismiss() }

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismiss() {
        try {
            overlayView?.let { windowManager?.removeView(it) }
            overlayView = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
