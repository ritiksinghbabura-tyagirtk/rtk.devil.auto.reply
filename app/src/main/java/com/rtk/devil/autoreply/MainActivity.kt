package com.rtk.devil.autoreply

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.rtk.devil.autoreply.feature.dashboard.RTKDevilApp

private val RTKBackground = Color(0xFF0B0D12)
private val RTKSurface = Color(0xFF171A23)
private val RTKAccent = Color(0xFF39F28B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RTKTheme {
                RTKDevilApp()
            }
        }
    }
}

@Composable
private fun RTKTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = RTKAccent,
            secondary = Color(0xFF45D7FF),
            background = RTKBackground,
            surface = RTKSurface,
            onSurfaceVariant = Color(0xFFB8F8D2),
            surfaceVariant = Color(0xFF292B36),
            outline = Color(0xFF52665A),
            onBackground = Color(0xFFF4F4F8),
            onSurface = Color(0xFFF4F4F8)
        ),
        content = content
    )
}
