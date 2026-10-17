package com.growthscore.demo.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Blue = Color(0xFF4265D6); val Navy = Color(0xFF17254A); val PaleBlue = Color(0xFFF3F6FF)
val Green = Color(0xFF168561); val Orange = Color(0xFFD97E18); val Red = Color(0xFFC94252)
private val Scheme = lightColorScheme(
    primary = Blue, onPrimary = Color.White, primaryContainer = Color(0xFFE8EEFF), onPrimaryContainer = Navy,
    secondary = Green, secondaryContainer = Color(0xFFE3F5EE), onSecondaryContainer = Color(0xFF103D31),
    background = Color(0xFFF8F9FD), onBackground = Navy, surface = Color.White, surfaceVariant = Color(0xFFF0F2F8),
    onSurface = Navy, onSurfaceVariant = Color(0xFF5C667D), outline = Color(0xFFCBD2E0), error = Red
)
private val GrowthTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 38.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 29.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 23.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
)
@Composable fun GrowthTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = Scheme,
    typography = GrowthTypography,
    shapes = Shapes(
        extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        large = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
    ),
    content = content
)
