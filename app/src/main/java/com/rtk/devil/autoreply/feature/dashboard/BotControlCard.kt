package com.rtk.devil.autoreply.feature.dashboard

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.rtk.devil.autoreply.feature.settings.BotConnectionViewModel
import com.rtk.devil.autoreply.integration.telegrambot.TelegramBotService
import kotlinx.coroutines.delay

private val BotPanel = Color(0xFF171A23)
private val BotAccent = Color(0xFF9B7BFF)
private val BotMuted = Color(0xFFA4A8B7)
private val BotGreen = Color(0xFF55D6BE)

@Composable
internal fun BotControlCard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val connectionModel: BotConnectionViewModel = viewModel()
    val connection by connectionModel.state.collectAsState()

    var token by remember { mutableStateOf("") }
    var runtimeStatus by remember { mutableStateOf("STOPPED") }
    var runtimeDetail by remember { mutableStateOf("Bot has not been started") }

    LaunchedEffect(Unit) {
        connectionModel.verifySavedToken()
        while (true) {
            val prefs = context.getSharedPreferences(
                TelegramBotService.STATUS_PREFS, Context.MODE_PRIVATE
            )
            runtimeStatus = prefs.getString(
                TelegramBotService.KEY_STATUS, "STOPPED"
            ) ?: "STOPPED"
            runtimeDetail = prefs.getString(
                TelegramBotService.KEY_DETAIL, "Bot has not been started"
            ) ?: "Bot has not been started"
            delay(2000)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BotPanel)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().then(Modifier),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "TELEGRAM BOT CONTROL",
                    color = BotAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Bot token",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Paste token from BotFather") },
                    singleLine = true
                )
                if (connection.checking) {
                    CircularProgressIndicator()
                }
                Text(
                    connection.status,
                    color = if (connection.connected) BotGreen else BotMuted,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            connectionModel.verifyAndSave(token)
                            token = ""
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !connection.checking
                    ) {
                        Text("Verify & Save")
                    }
                    OutlinedButton(
                        onClick = { connectionModel.clearToken() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Remove Token")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "SERVICE: $runtimeStatus",
                    color = if (runtimeStatus == "RUNNING") BotGreen else BotMuted,
                    fontWeight = FontWeight.Bold
                )
                Text(runtimeDetail, color = BotMuted, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { TelegramBotService.start(context) },
                        modifier = Modifier.weight(1f),
                        enabled = connection.tokenConfigured
                    ) {
                        Text("START BOT")
                    }
                    OutlinedButton(
                        onClick = { TelegramBotService.stop(context) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("STOP BOT")
                    }
                }
                Text(
                    "The bot only replies to messages sent to this bot account. " +
                        "Keep Android battery restrictions in mind.",
                    color = BotMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
