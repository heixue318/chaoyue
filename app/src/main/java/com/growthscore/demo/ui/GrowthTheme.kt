package com.growthscore.demo.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Blue = Color(0xFF4F6FE8); val Navy = Color(0xFF15285C); val PaleBlue = Color(0xFFF1F5FF)
val Green = Color(0xFF35AC7A); val Orange = Color(0xFFF1A44C); val Red = Color(0xFFE35B67)
private val Scheme = lightColorScheme(primary = Blue, secondary = Green, background = Color.White, surface = Color.White, onSurface = Navy)
@Composable fun GrowthTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = Scheme, typography = Typography(), content = content)
