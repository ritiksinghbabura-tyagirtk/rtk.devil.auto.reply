package com.rtk.devil.autoreply.feature.dashboard

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rtk.devil.autoreply.core.permissions.NotificationAccess
import com.rtk.devil.autoreply.data.local.AppSettings
import com.rtk.devil.autoreply.feature.settings.AppSettingsViewModel

private enum class RTKPage(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    WHATSAPP("WhatsApp", Icons.Filled.Chat),
    TELEGRAM("Telegram", Icons.AutoMirrored.Filled.Send),
    SETTINGS("Settings", Icons.Filled.Settings)
}

@Composable
fun RTKDevilApp() {
    val context = LocalContext.current
    val vm: AppSettingsViewModel = viewModel()
    val settings by vm.settings.collectAsState()
    var page by remember { mutableStateOf(RTKPage.HOME) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF39F28B),
            onPrimary = Color(0xFF062012),
            secondary = Color(0xFF45D7FF),
            onSecondary = Color(0xFF061820),
            tertiary = Color(0xFFB9A0FF),
            background = Color(0xFF101116),
            surface = Color(0xFF1B1C25),
            surfaceVariant = Color(0xFF292B36),
            onBackground = Color(0xFFF7F8FC),
            onSurface = Color(0xFFF7F8FC),
            onSurfaceVariant = Color(0xFFB8F8D2),
            primaryContainer = Color(0xFF16472D),
            onPrimaryContainer = Color(0xFFB8FFD3),
            secondaryContainer = Color(0xFF123747),
            onSecondaryContainer = Color(0xFFB9EDFF),
            outline = Color(0xFF52665A)
        )
    ) {
        Scaffold(
            containerColor = Color(0xFF101116),
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF191A22)
                ) {
                    RTKPage.entries.forEach { item ->
                        NavigationBarItem(
                            selected = page == item,
                            onClick = { page = item },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF39F28B),
                                selectedTextColor = Color(0xFF39F28B),
                                indicatorColor = Color(0xFF16472D),
                                unselectedIconColor = Color(0xFFB8F8D2),
                                unselectedTextColor = Color(0xFFB8F8D2)
                            ),
                            icon = { androidx.compose.material3.Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            ) },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFF101116))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF17271F)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "RTK Devil",
                            tint = Color(0xFF39F28B),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                "RTK Devil",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color(0xFFF7F5FF)
                            )
                            Text(
                                "Auto Reply Manager",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC5BED8)
                            )
                            Text(
                                "Manage your messaging automation",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA9A5B6)
                            )
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFF292832))

                when (page) {
                    RTKPage.HOME -> HomePage(
                        settings = settings,
                        notificationAccess = NotificationAccess.isGranted(context),
                        onWhatsApp = { vm.setWhatsappAutoReply(it) },
                        onTelegram = { vm.setTelegramNotificationEnabled(it) },
                        onBot = { vm.setTelegramBotAutoReply(it) },
                        onOpenSettings = { page = RTKPage.SETTINGS }
                    )

                    RTKPage.WHATSAPP -> WhatsAppPage(
                        settings = settings,
                        onEnabled = vm::setWhatsappAutoReply,
                        onWelcomeEnabled = vm::setWhatsappWelcomeEnabled,
                        onEveryMessage = vm::setWhatsappEveryMessage,
                        onWelcome1 = vm::setWhatsappWelcomeMessage1,
                        onWelcome2 = vm::setWhatsappWelcomeMessage2,
                        onCustomReply = vm::setWhatsappCustomReply
                    )

                    RTKPage.TELEGRAM -> TelegramPage(
                        settings = settings,
                        onNotificationEnabled = vm::setTelegramNotificationEnabled,
                        onWelcomeEnabled = vm::setTelegramWelcomeEnabled,
                        onEveryMessage = vm::setTelegramEveryMessage,
                        onWelcome1 = vm::setTelegramWelcomeMessage1,
                        onWelcome2 = vm::setTelegramWelcomeMessage2,
                        onCustomReply = vm::setTelegramCustomReply,
                        onBotEnabled = vm::setTelegramBotAutoReply
                    )

                    RTKPage.SETTINGS -> SettingsPage(
                        notificationAccess = NotificationAccess.isGranted(context),
                        onOpenNotificationSettings = {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            )
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "GitHub: github.com/ritiksinghbabura-tyagirtk",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun HomePage(
    settings: AppSettings,
    notificationAccess: Boolean,
    onWhatsApp: (Boolean) -> Unit,
    onTelegram: (Boolean) -> Unit,
    onBot: (Boolean) -> Unit,
    onOpenSettings: () -> Unit
) {
    SectionCard("Automation status") {
        ToggleRow(
            "WhatsApp Auto Reply",
            settings.whatsappAutoReplyEnabled,
            onWhatsApp
        )
        Text(
            if (settings.whatsappAutoReplyEnabled)
                "Enabled in settings; actual replies need Notification Access and a usable Reply action."
            else "Disabled",
            style = MaterialTheme.typography.bodySmall
        )
        HorizontalDivider()
        ToggleRow(
            "Telegram Notification Auto Reply",
            settings.telegramNotificationEnabled,
            onTelegram
        )
        ToggleRow(
            "Telegram Bot API Auto Reply",
            settings.telegramBotAutoReplyEnabled,
            onBot
        )
    }

    SectionCard("Permission status") {
        Text(
            if (notificationAccess)
                "Notification Access: Granted"
            else
                "Notification Access: Not granted"
        )
        Text(
            "Both notification-based modules require Android Notification Access.",
            style = MaterialTheme.typography.bodySmall
        )
        Button(onClick = onOpenSettings) {
            Text("Open Settings")
        }
    }

    SectionCard("Important") {
        Text("WhatsApp replies work only when its notification exposes a compatible Reply action.")
        Text("Telegram Bot API only handles messages sent to your bot, not every chat on your personal Telegram account.")
    }
}

@Composable
private fun WhatsAppPage(
    settings: AppSettings,
    onEnabled: (Boolean) -> Unit,
    onWelcomeEnabled: (Boolean) -> Unit,
    onEveryMessage: (Boolean) -> Unit,
    onWelcome1: (String) -> Unit,
    onWelcome2: (String) -> Unit,
    onCustomReply: (String) -> Unit
) {
    SectionCard("WhatsApp Auto Reply") {
        ToggleRow("Enable WhatsApp replies", settings.whatsappAutoReplyEnabled, onEnabled)
        ToggleRow("Send first welcome", settings.whatsappWelcomeEnabled, onWelcomeEnabled)
        ToggleRow("Reply to every eligible message", settings.whatsappEveryMessageEnabled, onEveryMessage)

        Text(
            "The two welcome fields are combined into one notification reply. Later replies use Custom Reply when enabled.",
            style = MaterialTheme.typography.bodySmall
        )

        TextEditor(
            "Welcome Message 1",
            settings.whatsappWelcomeMessage1,
            onWelcome1
        )
        TextEditor(
            "Welcome Message 2",
            settings.whatsappWelcomeMessage2,
            onWelcome2
        )
        TextEditor(
            "Custom Reply",
            settings.whatsappCustomReply,
            onCustomReply
        )
    }
}

@Composable
private fun TelegramPage(
    settings: AppSettings,
    onNotificationEnabled: (Boolean) -> Unit,
    onWelcomeEnabled: (Boolean) -> Unit,
    onEveryMessage: (Boolean) -> Unit,
    onWelcome1: (String) -> Unit,
    onWelcome2: (String) -> Unit,
    onCustomReply: (String) -> Unit,
    onBotEnabled: (Boolean) -> Unit
) {
    SectionCard("Telegram notification replies") {
        ToggleRow(
            "Enable notification replies",
            settings.telegramNotificationEnabled,
            onNotificationEnabled
        )
        ToggleRow(
            "Send first welcome",
            settings.telegramWelcomeEnabled,
            onWelcomeEnabled
        )
        ToggleRow(
            "Reply to every eligible message",
            settings.telegramEveryMessageEnabled,
            onEveryMessage
        )
        TextEditor("Welcome Message 1", settings.telegramWelcomeMessage1, onWelcome1)
        TextEditor("Welcome Message 2", settings.telegramWelcomeMessage2, onWelcome2)
        TextEditor("Custom Reply", settings.telegramCustomReply, onCustomReply)
    }

    SectionCard("Telegram Bot API") {
        ToggleRow(
            "Enable Bot API auto replies",
            settings.telegramBotAutoReplyEnabled,
            onBotEnabled
        )
        Text(
            "Configure your bot token in the existing Bot Control panel below. Never share your token in chat.",
            style = MaterialTheme.typography.bodySmall
        )
        BotControlCard()
    }
}

@Composable
private fun SettingsPage(
    notificationAccess: Boolean,
    onOpenNotificationSettings: () -> Unit
) {
    SectionCard("Notification Access") {
        Text(
            if (notificationAccess) "Status: Granted" else "Status: Not granted"
        )
        Button(onClick = onOpenNotificationSettings) {
            Text("Manage Notification Access")
        }
        Text(
            "Enable RTK Devil Auto Reply in Android's notification access screen. Return to the app after granting permission.",
            style = MaterialTheme.typography.bodySmall
        )
    }

    SectionCard("Privacy and limitations") {
        Text("Reply processing uses notification actions exposed by the messaging app.")
        Text("The app should not be force-stopped if you want background notification processing.")
        Text("Some notifications do not contain reply actions; those messages cannot be answered by this method.")
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B1C25)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF39F28B)
            )
            content()
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun TextEditor(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2
    )
}
