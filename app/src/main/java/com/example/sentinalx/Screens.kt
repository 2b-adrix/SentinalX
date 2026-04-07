package com.example.sentinalx

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinalx.ui.theme.*

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardOutline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun PermissionItem(title: String, desc: String, isGranted: Boolean, onClick: () -> Unit) {
    SectionCard(modifier = Modifier.clickable { if (!isGranted) onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isGranted) Icons.Default.VerifiedUser else Icons.Default.ErrorOutline,
                null,
                tint = if (isGranted) SuccessGreen else DangerRed
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            if (!isGranted) {
                Text("GRANT", color = PrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RiskBadge(riskLevel: RiskLevel) {
    val color = when (riskLevel) {
        RiskLevel.HIGH -> DangerRed
        RiskLevel.MEDIUM -> WarningAmber
        RiskLevel.LOW -> SuccessGreen
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = riskLevel.name,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: UiState,
    onThreatClick: (ThreatEvent) -> Unit,
    onSettingsClick: () -> Unit,
    onSimulateScam: (String) -> Unit,
    onRunScan: () -> Unit,
    onScanNetwork: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SENTINELX", fontWeight = FontWeight.Black, letterSpacing = 3.sp, fontSize = 22.sp, color = Color.White)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(SuccessGreen))
                            Spacer(Modifier.width(6.dp))
                            Text("CORE ACTIVE", style = MaterialTheme.typography.labelSmall, color = PrimaryCyan, letterSpacing = 2.sp)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryCyan)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // Startup Grade "Holographic Shield"
            item {
                HolographicProtectionStatus(state, onRunScan)
            }

            // Global Threat Activity Map (Mock)
            item {
                NetworkIntegrityCard(state.networkIntegrityStatus, onScanNetwork)
            }

            // Global Threat Activity Map (Mock)
            item {
                GlobalThreatMap(state.globalThreatPings)
            }

            // Risk Trend Visualization
            item {
                RiskTrendDashboard(state.threatTrend)
            }

            // Real-time Intelligence Grid
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatMetric(Modifier.weight(1f), "THREATS BLOCKED", state.threats.count { it.riskLevel == RiskLevel.HIGH }.toString(), DangerRed)
                    StatMetric(Modifier.weight(1f), "SENSORY DATA", "${state.threats.size * 12}KB", PrimaryCyan)
                    StatMetric(Modifier.weight(1f), "UPTIME", "99.9%", SuccessGreen)
                }
            }

            // Live Activity Feed
            item {
                Column {
                    Text("LIVE RISK FEED", fontWeight = FontWeight.Black, fontSize = 12.sp, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(Modifier.height(12.dp))
                    if (state.threats.isEmpty()) {
                        EmptyStartupState()
                    } else {
                        state.threats.take(5).forEach { threat ->
                            ThreatItem(threat, state.isHistoryLocked) { onThreatClick(threat) }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Demo Controls - Floating Section
            if (state.isDemoModeEnabled) {
                item {
                    SectionCard {
                        Text("SIMULATION ENVIRONMENT", fontWeight = FontWeight.Black, color = PrimaryCyan, fontSize = 10.sp, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DemoButton("FINANCIAL", Modifier.weight(1f)) { onSimulateScam("BANK") }
                                DemoButton("JOB SCAM", Modifier.weight(1f)) { onSimulateScam("PHISHING") }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DemoButton("ID THEFT", Modifier.weight(1f)) { onSimulateScam("ID_THEFT") }
                                DemoButton("HOMOGRAPH", Modifier.weight(1f)) { onSimulateScam("HOMOGRAPH") }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DemoButton("REGIONAL", Modifier.weight(1f)) { onSimulateScam("HINGLISH") }
                                DemoButton("REWARD", Modifier.weight(1f)) { onSimulateScam("FRAUD") }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun NetworkIntegrityCard(status: NetworkStatus, onScan: () -> Unit) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (status.isSecure) Icons.Default.Wifi else Icons.Default.WifiOff,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = if (status.isSecure) SuccessGreen else DangerRed
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("NETWORK INTEGRITY", style = MaterialTheme.typography.labelSmall, color = PrimaryCyan, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (status.isScanning) "ANALYZING PACKET FLOW..." else status.ssid.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Black,
                    color = if (status.isSecure) Color.White else DangerRed
                )
            }
            
            if (status.isScanning) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryCyan, strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onScan) {
                    Icon(Icons.Default.Refresh, null, tint = PrimaryCyan)
                }
            }
        }
        
        if (status.threatDetected) {
            Spacer(Modifier.height(12.dp))
            status.activeThreats.forEach { threat ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Icon(Icons.Default.Warning, null, Modifier.size(12.dp), tint = DangerRed)
                    Spacer(Modifier.width(8.dp))
                    Text(threat, style = MaterialTheme.typography.labelSmall, color = DangerRed)
                }
            }
        }
    }
}

@Composable
fun GlobalThreatMap(pings: List<Offset>) {
    SectionCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, null, Modifier.size(16.dp), tint = PrimaryCyan)
                Spacer(Modifier.width(8.dp))
                Text("GLOBAL RISK TELEMETRY", style = MaterialTheme.typography.labelSmall, color = PrimaryCyan, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .drawWithContent {
                        drawContent()
                        // Draw grid lines
                        val gridColor = PrimaryCyan.copy(alpha = 0.05f)
                        val step = 40.dp.toPx()
                        for (i in 0 until (size.width / step).toInt()) {
                            drawLine(gridColor, Offset(i * step, 0f), Offset(i * step, size.height), 1f)
                        }
                        for (i in 0 until (size.height / step).toInt()) {
                            drawLine(gridColor, Offset(0f, i * step), Offset(size.width, i * step), 1f)
                        }
                    }
            ) {
                pings.forEach { ping ->
                    ThreatPing(ping.x, ping.y)
                }
                
                Text(
                    "REAL-TIME PING: ${pings.size} ACTIVE VECTORS",
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = PrimaryCyan.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun ThreatPing(x: Float, y: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "ping")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "alpha"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                onDrawBehind {
                    val center = Offset(size.width * x, size.height * y)
                    drawCircle(
                        color = DangerRed.copy(alpha = alpha * 0.4f),
                        radius = 20.dp.toPx() * scale,
                        center = center
                    )
                    drawCircle(
                        color = DangerRed,
                        radius = 4.dp.toPx(),
                        center = center
                    )
                }
            }
    )
}

@Composable
fun RiskTrendDashboard(trend: List<Int>) {
    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("RISK ANOMALY TREND", fontWeight = FontWeight.Black, color = PrimaryCyan, fontSize = 10.sp, letterSpacing = 1.sp)
                Text("LAST 7 DETECTIONS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Icon(Icons.Default.TrendingUp, null, tint = DangerRed, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.BottomStart) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                trend.forEach { score ->
                    val heightFactor = (score / 100f).coerceIn(0.1f, 1f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(heightFactor)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        if (score > 70) DangerRed else if (score > 40) WarningAmber else SuccessGreen,
                                        SurfaceDarker
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun HolographicProtectionStatus(state: UiState, onRunScan: () -> Unit) {
    val isEnabled = state.isProtectionEnabled
    val isScanning = state.scanProgress.isScanning
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radius by infiniteTransition.animateFloat(
        initialValue = 160f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "radius"
    )

    Box(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing background effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = if (isEnabled) SuccessGreen.copy(alpha = 0.05f) else WarningAmber.copy(alpha = 0.05f),
                radius = radius * 1.5f
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { if (!isScanning) onRunScan() }) {
                CircularProgressIndicator(
                    progress = { if (isScanning) state.scanProgress.progress else state.globalHealthScore / 100f },
                    modifier = Modifier.size(160.dp),
                    color = if (isScanning) PrimaryCyan else (if (isEnabled) SuccessGreen else WarningAmber),
                    strokeWidth = 4.dp,
                    trackColor = (if (isEnabled) SuccessGreen.copy(0.1f) else WarningAmber.copy(0.1f))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isScanning) {
                        Text(
                            "${(state.scanProgress.progress * 100).toInt()}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryCyan
                        )
                        Text(
                            "SCANNING",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCyan.copy(0.7f),
                            letterSpacing = 1.sp
                        )
                    } else {
                        Text(
                            "${state.globalHealthScore}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isEnabled) Color.White else WarningAmber
                        )
                        Text(
                            "HEALTH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) PrimaryCyan else WarningAmber.copy(0.7f),
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                if (isScanning) "DEEP FORENSIC SCAN IN PROGRESS" else (if (isEnabled) "CORE PROTECTION ACTIVE" else "SYSTEM VULNERABLE"),
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontSize = 14.sp,
                color = if (isScanning) PrimaryCyan else (if (isEnabled) SuccessGreen else WarningAmber)
            )
            if (isScanning) {
                Text(
                    state.scanProgress.currentApp.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                ScanningTicker(isEnabled)
            }
            
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                StatMetric(Modifier, "INTERCEPTIONS", state.activeThreatInterceptions.toString(), PrimaryCyan)
                StatMetric(Modifier, "INTEGRITY", if (state.systemIntegrityCheck) "SECURE" else "FAIL", if (state.systemIntegrityCheck) SuccessGreen else DangerRed)
            }
        }
    }
}

@Composable
fun ScanningTicker(isEnabled: Boolean) {
    val phrases = listOf(
        "SCANNING NOTIFICATION STACK...",
        "ANALYZING ON-SCREEN NODES...",
        "CHECKING URL REPUTATION...",
        "HEURISTIC ENGINE V2.4 RUNNING...",
        "LATENCY: 14MS",
        "ALL SYSTEMS NOMINAL"
    )
    var index by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(isEnabled) {
        if (isEnabled) {
            while(true) {
                delay(3000)
                index = (index + 1) % phrases.size
            }
        }
    }

    AnimatedContent(
        targetState = if (isEnabled) phrases[index] else "SERVICE DISCONNECTED",
        transitionSpec = {
            slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
        },
        label = "ticker"
    ) { text ->
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun StatMetric(modifier: Modifier, label: String, value: String, color: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, CardOutline, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 0.5.sp)
    }
}

@Composable
fun EmptyStartupState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, CardOutline, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SECURE", fontWeight = FontWeight.Black, color = SuccessGreen.copy(0.3f), fontSize = 24.sp, letterSpacing = 4.sp)
            Text("NO ANOMALIES DETECTED", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
fun DemoButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, PrimaryCyan.copy(0.3f)),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Text(label, fontSize = 10.sp, color = PrimaryCyan, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, color: Color) {
    SectionCard(modifier = modifier) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
        Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ThreatItem(threat: ThreatEvent, isLocked: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(
            1.dp, 
            if (threat.riskLevel == RiskLevel.HIGH) DangerRed.copy(0.4f) else CardOutline
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape)
                    .background(if (threat.riskLevel == RiskLevel.HIGH) DangerRed.copy(0.1f) else SurfaceDarker),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isLocked) Icons.Default.Lock else {
                        when (threat.category) {
                            "Job Scam" -> Icons.Default.Work
                            "KYC/Bank Fraud" -> Icons.Default.AccountBalance
                            "Payment Scam" -> Icons.Default.Payments
                            "Phishing Link" -> Icons.Default.Link
                            else -> Icons.Default.GppMaybe
                        }
                    },
                    contentDescription = null,
                    tint = if (threat.riskLevel == RiskLevel.HIGH) DangerRed else WarningAmber,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isLocked) "SENSITIVE THREAT DATA" else threat.appName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    RiskBadge(threat.riskLevel)
                }
                Text(
                    if (isLocked) "Biometric verification required to view" else threat.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    threat: ThreatEvent, 
    onIgnoreApp: (String) -> Unit,
    onReport: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Threat Intelligence Report", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    TextButton(onClick = { 
                        onIgnoreApp(threat.appName)
                        onBack()
                    }) {
                        Text("IGNORE APP", color = WarningAmber, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Confidence & Risk Scores
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ScoreIndicator(Modifier.weight(1f), "RISK SCORE", threat.riskScore, if (threat.riskLevel == RiskLevel.HIGH) DangerRed else WarningAmber)
                ScoreIndicator(Modifier.weight(1f), "ML CERTAINTY", (threat.confidenceScore), PrimaryCyan)
            }

            // Trust Level Banner
            TrustLevelBanner(threat.trustLevel, threat.confidenceScore)

            SectionCard {
                Text("Intercepted Content", style = MaterialTheme.typography.labelSmall, color = PrimaryCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(threat.message, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
            }

            SectionCard {
                Text("Detection Heuristics", style = MaterialTheme.typography.labelSmall, color = PrimaryCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                threat.reasons.forEach { reason ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                        Icon(Icons.Default.TrackChanges, null, Modifier.size(14.dp), tint = DangerRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(reason, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(0.05f)),
                border = BorderStroke(1.dp, SuccessGreen.copy(0.2f))
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SentinelX Advice", fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(threat.advice, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                }
            }

            // Reporting Section
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cloud Intelligence", style = MaterialTheme.typography.labelSmall, color = PrimaryCyan, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (threat.isReported) "Reported to Global Network" else "Share this threat to help others",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    
                    if (threat.isReported) {
                        Icon(Icons.Default.CloudDone, null, tint = SuccessGreen)
                    } else {
                        Button(
                            onClick = onReport,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.3f)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, null, tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REPORT", color = PrimaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreIndicator(modifier: Modifier, label: String, score: Int, color: Color) {
    SectionCard(modifier = modifier, content = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.size(80.dp),
                    color = color,
                    strokeWidth = 6.dp,
                    trackColor = color.copy(alpha = 0.1f)
                )
                Text("$score%", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        }
    })
}

@Composable
fun TrustLevelBanner(level: TrustLevel, confidence: Int) {
    val (color, label, icon) = when (level) {
        TrustLevel.DANGEROUS -> Triple(DangerRed, "High Confidence Threat", Icons.Default.Dangerous)
        TrustLevel.SUSPICIOUS -> Triple(WarningAmber, "Moderate Confidence", Icons.Default.ReportProblem)
        TrustLevel.SAFE -> Triple(SuccessGreen, "Safe Verified Content", Icons.Default.CheckCircle)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold, color = color)
                Text(
                    "Analysis based on $confidence% model certainty",
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: UiState,
    onToggleProtection: () -> Unit,
    onToggleDemoMode: () -> Unit,
    onToggleHistoryLock: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onRemoveIgnoredPackage: (String) -> Unit,
    onExportForensics: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SentinelX Configuration") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("System Permissions", fontWeight = FontWeight.Bold, color = PrimaryCyan, fontSize = 12.sp) }
            
            item {
                PermissionItem(
                    title = "Notification Access",
                    desc = "Required to intercept incoming scams",
                    isGranted = PermissionUtils.isNotificationServiceEnabled(context),
                    onClick = { PermissionUtils.openNotificationAccessSettings(context) }
                )
            }

            item {
                PermissionItem(
                    title = "Display Over Other Apps",
                    desc = "Required to show instant danger alerts",
                    isGranted = PermissionUtils.canDrawOverlays(context),
                    onClick = { PermissionUtils.openOverlaySettings(context) }
                )
            }

            item {
                PermissionItem(
                    title = "Accessibility Protection",
                    desc = "Monitor on-screen text for real-time risk analysis",
                    isGranted = PermissionUtils.isAccessibilityServiceEnabled(context),
                    onClick = { PermissionUtils.openAccessibilitySettings(context) }
                )
            }

            item { Text("Core Security", fontWeight = FontWeight.Bold, color = PrimaryCyan, fontSize = 12.sp) }

            item {
                SectionCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Real-time Protection", fontWeight = FontWeight.Bold)
                            Text("Active heuristic message scanning", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Switch(checked = state.isProtectionEnabled, onCheckedChange = { onToggleProtection() })
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = CardOutline)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Vault Lock", fontWeight = FontWeight.Bold)
                            Text("Biometric protection for threat history", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Switch(checked = state.isHistoryLocked, onCheckedChange = { onToggleHistoryLock(it) })
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = CardOutline)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Simulation Lab", fontWeight = FontWeight.Bold)
                            Text("Enable developer attack triggers", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Switch(checked = state.isDemoModeEnabled, onCheckedChange = { onToggleDemoMode() })
                    }
                }
            }

            if (state.ignoredPackages.isNotEmpty()) {
                item { Text("Ignore List", fontWeight = FontWeight.Bold, color = PrimaryCyan, fontSize = 12.sp) }
                items(state.ignoredPackages.toList()) { pkg ->
                    SectionCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(pkg, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            IconButton(onClick = { onRemoveIgnoredPackage(pkg) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = DangerRed)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onClearHistory,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DangerRed.copy(0.3f))
                    ) {
                        Text("Purge Analysis History", color = DangerRed, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onExportForensics,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.3f)),
                        enabled = !state.isForensicExporting
                    ) {
                        if (state.isForensicExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PrimaryCyan, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp), tint = PrimaryCyan)
                            Spacer(Modifier.width(8.dp))
                            Text("Export Forensic Intel (JSON)", color = PrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForensicReportScreen(
    results: List<ScanResult>,
    onUninstall: (String) -> Unit,
    onIgnore: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Infection Intel", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        if (results.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Shield, null, Modifier.size(64.dp), tint = SuccessGreen.copy(alpha = 0.2f))
                    Spacer(Modifier.height(16.dp))
                    Text("NO THREATS FOUND", fontWeight = FontWeight.Black, color = SuccessGreen, letterSpacing = 2.sp)
                    Text("Your system integrity is verified.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "DISCOVERED ANOMALIES (${results.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = DangerRed,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                items(results) { result ->
                    InfectionItem(result, onUninstall, onIgnore)
                }
                
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun InfectionItem(
    result: ScanResult,
    onUninstall: (String) -> Unit,
    onIgnore: (String) -> Unit
) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(DangerRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.BugReport, null, tint = DangerRed, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(result.appName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(result.packageName, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(
                Modifier.clip(RoundedCornerShape(4.dp)).background(DangerRed.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("RISK: ${result.riskScore}", color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text("DETECTION SIGNALS", style = MaterialTheme.typography.labelSmall, color = PrimaryCyan, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        result.flags.forEach { flag ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Icon(Icons.Default.RadioButtonChecked, null, Modifier.size(8.dp), tint = WarningAmber)
                Spacer(Modifier.width(8.dp))
                Text(flag, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onUninstall(result.packageName) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, DangerRed.copy(0.3f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.DeleteForever, null, Modifier.size(14.dp), tint = DangerRed)
                Spacer(Modifier.width(4.dp))
                Text("ELIMINATE", color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            
            OutlinedButton(
                onClick = { onIgnore(result.packageName) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CardOutline),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("WHITELIST", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
