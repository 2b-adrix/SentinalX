package com.example.sentinalx

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.sentinalx.ui.theme.*
import kotlinx.coroutines.delay

import androidx.fragment.app.FragmentActivity
import android.widget.Toast

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Repository with Persistence
        ThreatRepository.init(this)
        RiskAnalyzer.init(this)

        setContent {
            DigitalRiskShieldTheme {
                val viewModel: MainViewModel = viewModel()
                val state by viewModel.uiState.collectAsState()
                val navController = rememberNavController()
                val view = LocalView.current

                LaunchedEffect(state.lastDetectedThreat) {
                    if (state.lastDetectedThreat != null) {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                }

                Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                state = state,
                                onThreatClick = { threat -> 
                                    if (state.isHistoryLocked) {
                                        BiometricHelper.showBiometricPrompt(
                                            activity = this@MainActivity,
                                            onSuccess = {
                                                viewModel.setHistoryLocked(false)
                                                navController.navigate("detail/${threat.id}")
                                            },
                                            onError = { error ->
                                                Toast.makeText(this@MainActivity, "Access Denied: $error", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    } else {
                                        navController.navigate("detail/${threat.id}")
                                    }
                                },
                                onSettingsClick = { navController.navigate("settings") },
                                onSimulateScam = { type -> 
                                    viewModel.simulateScam(type) 
                                    viewModel.toggleTestOverlay(true)
                                },
                                onRunScan = { 
                                    viewModel.runForensicDeepScan(this@MainActivity) {
                                        navController.navigate("forensic_report")
                                    }
                                },
                                onScanNetwork = {
                                    viewModel.scanNetworkIntegrity()
                                }
                            )
                        }
                        composable("forensic_report") {
                            ForensicReportScreen(
                                results = state.scanProgress.results,
                                onUninstall = { packageName ->
                                    val intent = Intent(Intent.ACTION_DELETE).apply {
                                        data = Uri.parse("package:$packageName")
                                    }
                                    startActivity(intent)
                                },
                                onIgnore = { packageName ->
                                    viewModel.ignorePackage(packageName)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("detail/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")
                            val threat = state.threats.find { it.id == id }
                            threat?.let {
                                DetailScreen(
                                    threat = it, 
                                    onIgnoreApp = { pkg -> viewModel.ignorePackage(pkg) },
                                    onReport = { viewModel.reportThreat(it) },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable("settings") {
                            SettingsScreen(
                                state = state,
                                onToggleProtection = { viewModel.toggleProtection() },
                                onToggleDemoMode = { viewModel.toggleDemoMode() },
                                onToggleHistoryLock = { viewModel.setHistoryLocked(it) },
                                onClearHistory = { viewModel.clearHistory() },
                                onRemoveIgnoredPackage = { pkg -> viewModel.removeIgnoredPackage(pkg) },
                                onExportForensics = { viewModel.exportForensicReport(this@MainActivity) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // Global Alert Overlay
                    AlertOverlay(
                        show = state.showTestOverlay,
                        threat = state.lastDetectedThreat,
                        onDismiss = { viewModel.toggleTestOverlay(false) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertOverlay(
    show: Boolean,
    threat: ThreatEvent?,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "alpha"
    )

    AnimatedVisibility(
        visible = show && threat != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        if (threat != null) {
            val isIdTheft = threat.category == "Identity Theft Risk"
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isIdTheft) Color(0xFF1A0000) else SurfaceDark)
                    .border(
                        2.dp, 
                        if (isIdTheft) DangerRed.copy(alpha = alpha) else DangerRed.copy(0.3f), 
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(DangerRed.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isIdTheft) Icons.Default.PrivacyTip else Icons.Default.Security, 
                            null, 
                            tint = DangerRed,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (isIdTheft) "SENSITIVE DATA BREACH PREVENTED" else "CRITICAL THREAT INTERCEPTED",
                            fontWeight = FontWeight.Black,
                            color = DangerRed,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            threat.category.uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            if (isIdTheft) "Unauthorized attempt to capture credentials detected on-screen." 
                            else "SentinelX heuristic engine identified malicious intent in real-time.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }
                
                LaunchedEffect(show) {
                    if (show) {
                        delay(6000)
                        onDismiss()
                    }
                }
            }
        }
    }
}
