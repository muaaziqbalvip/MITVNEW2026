package com.mitv.master.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitv.master.ui.theme.MitvRed
import com.mitv.master.ui.theme.MitvTextSecondary
import kotlinx.coroutines.delay

/**
 * Netflix-style splash: logo scales up from a small point with an
 * "overshoot" ease (EaseOutBack) — the same punchy pop-in feel as the
 * real Netflix intro — then a tagline fades in before handing off.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.3f) }
    var taglineAlpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = EaseOutBack)
        )
        taglineAlpha = 1f
        delay(750)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1A0508), Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "MITV",
                color = MitvRed,
                fontWeight = FontWeight.Black,
                fontSize = 60.sp,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(if (scale.value > 0.85f) 1f else scale.value)
            )
            Text(
                text = "Your channels. Your rules.",
                color = MitvTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .alpha(taglineAlpha)
            )
        }
    }
}
