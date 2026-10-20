package com.rtk.devil.autoreply.feature.dashboard

import com.rtk.devil.autoreply.feature.settings.AppSettingsViewModel
import com.rtk.devil.autoreply.core.permissions.NotificationAccess
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF0B0D12)
private val Panel = Color(0xFF171A23)
private val Accent = Color(0xFF9B7BFF)
private val Muted = Color(0xFFA4A8B7)
private val Green = Color(0xFF55D6BE)

private enum class Page(val title: String) {
    HOME("Dashboard"),
    RULES("Automation"),
    MESSAGES("Messages"),
    SETTINGS("Settings")
}

@Composable
fun RTKDevilApp() {
    var page by remember { mutableStateOf(Page.HOME) }

    val context = LocalContext.current
    val settingsViewModel: AppSettingsViewModel = viewModel()
    val settings by settingsViewModel.settings.collectAsState()

    var notificationAccessGranted by remember {
        mutableStateOf(NotificationAccess.isGranted(context))
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(containerColor = Panel) {
                Page.entries.forEach { item ->
                    NavigationBarItem(
                        selected = page == item,
                        onClick = { page = item },
                        icon = {
                            Text(
                                when (item) {
                                    Page.HOME -> "⌂"
                                    Page.RULES -> "⚡"
                                    Page.MESSAGES -> "✉"
                                    Page.SETTINGS -> "⚙"
                                },
                                fontSize = 20.sp
                            )
                        },
                        label = { Text(item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Accent,
                            selectedTextColor = Accent,
                            indicatorColor = Accent.copy(alpha = 0.16f),
                            unselectedTextColor = Muted
                        )
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(padding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 22.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Header()
            }

            item {
                BotControlCard()
            }

            when (page) {
                Page.HOME -> {
                    item {
                        HeroCard(
                            enabled = settings.autoReplyEnabled,
                            onToggle = settingsViewModel::setAutoReply
                        )
                    }
                    item {
                        SectionTitle("YOUR AUTOMATION")
                    }
                    item {
                        SettingCard(
                            title = "Welcome Message",
                            subtitle = "Configure your greeting",
                            checked = settings.welcomeEnabled,
                            onCheckedChange = settingsViewModel::setWelcomeEnabled
                        )
                    }
                    item {
                        SettingCard(
                            title = "Every Message",
                            subtitle = "Reply to eligible incoming messages",
                            checked = settings.everyMessageEnabled,
                            onCheckedChange = settingsViewModel::setEveryMessageEnabled
                        )
                    }
                    item {
                        StatusCard()
                    }
                    item {
                        Text(
                            "More controls are available in Automation " +
                                "and Settings.",
                            color = Muted,
                            fontSize = 13.sp
                        )
                    }
                }

                Page.RULES -> {
                    item {
                        SectionTitle("AUTOMATION RULES")
                    }
                    item {
                        SettingCard(
                            title = "Welcome Message",
                            subtitle = "Greeting for eligible new contacts",
                            checked = settings.welcomeEnabled,
                            onCheckedChange = settingsViewModel::setWelcomeEnabled
                        )
                    }
                    item {
                        SettingCard(
                            title = "Every Message",
                            subtitle = "General automatic reply rule",
                            checked = settings.everyMessageEnabled,
                            onCheckedChange = settingsViewModel::setEveryMessageEnabled
                        )
                    }
                    item {
                        InfoCard(
                            "Rule engine",
                            "Rules are currently local UI settings. " +
                                "Message processing will be connected " +
                                "in a later implementation step."
                        )
                    }
                }

                Page.MESSAGES -> {
                    item {
                        SectionTitle("WELCOME MESSAGE")
                    }
                    item {
                        InfoCard(
                            "Personalise your reply",
                            "Write the message you want to use " +
                                "when the rule is triggered."
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = settings.welcomeMessage,
                            onValueChange = settingsViewModel::setWelcomeMessage,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Welcome message") },
                            minLines = 5,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Accent,
                                unfocusedBorderColor = Muted,
                                focusedLabelColor = Accent,
                                cursorColor = Accent
                            )
                        )
                        Text(
                            "${settings.welcomeMessage.length} characters",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                    item {
                        Text(
                            "Changes are saved locally on this device.",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                }

                Page.SETTINGS -> {
                    item {
                        SectionTitle("APP SETTINGS")
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Panel
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Notification Access",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    if (notificationAccessGranted) {
                                        "Permission enabled on this device."
                                    } else {
                                        "Permission is not enabled yet."
                                    },
                                    color = if (notificationAccessGranted) Green else Muted,
                                    fontSize = 13.sp
                                )
                                Button(
                                    onClick = {
                                        context.startActivity(
                                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Open Notification Access Settings")
                                }
                                OutlinedButton(
                                    onClick = {
                                        notificationAccessGranted =
                                            NotificationAccess.isGranted(context)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Refresh Permission Status")
                                }
                            }
                        }
                    }
                    item {
                        InfoCard(
                            "Automation settings",
                            "Auto Reply: ${if (settings.autoReplyEnabled) "ON" else "OFF"}\n" +
                                "Welcome Message: ${if (settings.welcomeEnabled) "ON" else "OFF"}\n" +
                                "Every Message: ${if (settings.everyMessageEnabled) "ON" else "OFF"}"
                        )
                    }
                    item {
                        InfoCard(
                            "Privacy",
                            "Settings are stored locally on this device. " +
                                "This app does not ask for your Telegram password."
                        )
                    }
                    item {
                        InfoCard(
                            "Integration status",
                            "Notification listener is registered. " +
                                "Automatic reply processing is not implemented yet."
                        )
                    }
                    item {
                        InfoCard(
                            "About",
                            "RTK DEVIL AUTO REPLY\n" +
                                "Modular Android automation project."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Accent.copy(alpha = 0.18f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "😈",
                modifier = Modifier.padding(14.dp),
                fontSize = 28.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                "RTK DEVIL",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "AUTO REPLY",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                "Automation, your way.",
                color = Muted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun HeroCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "AUTOMATION STATUS",
                        color = Muted,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        if (enabled) "Enabled locally" else "Ready to configure",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Green,
                        uncheckedThumbColor = Muted
                    )
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            Text(
                if (enabled) {
                    "Setting is saved. Automatic reply engine is not implemented yet."
                } else {
                    "Configure your rules, then connect a supported messaging adapter."
                },
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            Surface(
                color = if (enabled) Green.copy(alpha = 0.12f)
                else Accent.copy(alpha = 0.12f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    if (enabled) "●  UI ENABLED"
                    else "●  SETUP REQUIRED",
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),
                    color = if (enabled) Green else Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        color = Muted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp
    )
}

@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    subtitle,
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun StatusCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "INTEGRATION STATUS",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            StatusLine("Telegram account", "Not connected")
            StatusLine("Reply engine", "Not implemented")
            StatusLine("Local settings", "DataStore")
        }
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            color = Color.White,
            fontSize = 13.sp
        )
        Text(
            value,
            color = Muted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun InfoCard(title: String, description: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                description,
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}
