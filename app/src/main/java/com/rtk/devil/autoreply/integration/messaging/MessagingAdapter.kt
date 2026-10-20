package com.rtk.devil.autoreply.integration.messaging

import com.rtk.devil.autoreply.core.model.IncomingMessage
import com.rtk.devil.autoreply.core.model.OutgoingMessage
import kotlinx.coroutines.flow.Flow

data class AdapterCapabilities(
    val canObserveMessages: Boolean = false,
    val canReplyToMessages: Boolean = false,
    val supportsAllChats: Boolean = false
)

sealed interface ConnectionResult {
    data object Connected : ConnectionResult
    data class Failed(val reason: String) : ConnectionResult
}

sealed interface SendResult {
    data object Sent : SendResult
    data class Unsupported(val reason: String) : SendResult
    data class Failed(val reason: String) : SendResult
}

interface MessagingAdapter {
    val id: String
    val displayName: String
    val capabilities: AdapterCapabilities

    fun observeIncomingMessages(): Flow<IncomingMessage>

    suspend fun connect(): ConnectionResult

    suspend fun disconnect()

    suspend fun sendReply(message: OutgoingMessage): SendResult
}
