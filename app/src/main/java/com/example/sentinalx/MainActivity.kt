package com.example.sentinalx

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.sentinalx.ui.theme.DigitalRiskShieldTheme
import com.example.sentinalx.ui.theme.DangerRed
import com.example.sentinalx.ui.theme.DarkBackground
import com.example.sentinalx.ui.theme.SurfaceDark
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
                                onSimulateScam = { type -> viewModel.simulateScam(type) },
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
    AnimatedVisibility(
        visible = show && threat != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        if (threat != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DangerRed.copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, null, tint = DangerRed)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "CRITICAL THREAT BLOCKED",
                            fontWeight = FontWeight.Black,
                            color = DangerRed,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            threat.category,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            "SentinelX heuristic engine identified a malicious intent in a incoming message.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
                
                LaunchedEffect(show) {
                    if (show) {
                        delay(4000)
                        onDismiss()
                    }
                }
            }
        }
    }
}
