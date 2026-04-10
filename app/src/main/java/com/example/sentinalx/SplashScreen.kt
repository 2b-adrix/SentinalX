package com.example.sentinalx

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animation sequence
        this.launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
        this.launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
        
        // Wait and then proceed
        delay(3000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)), // Matches the hero background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // The Hero Illustration
            Image(
                painter = painterResource(id = R.drawable.sentinel_hero),
                contentDescription = "SentinelX Logo",
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(350.dp)
                    .scale(scale.value)
                    .alpha(alpha.value),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Loading State or Subtext
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "INITIALIZING HEURISTIC ENGINES...",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF3B82F6).copy(alpha = alpha.value * 0.7f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Bottom Footer
        Text(
            text = "V 2.1.0 | SECURE CONNECT",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(alpha.value * 0.4f),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            letterSpacing = 3.sp
        )
    }
}
