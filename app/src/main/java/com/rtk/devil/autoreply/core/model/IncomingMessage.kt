package com.rtk.devil.autoreply.core.model

data class IncomingMessage(
    val platform: String,
    val conversationId: String,
    val senderId: String?,
    val text: String,
    val receivedAtMillis: Long,
    val isNewContact: Boolean = false
)
