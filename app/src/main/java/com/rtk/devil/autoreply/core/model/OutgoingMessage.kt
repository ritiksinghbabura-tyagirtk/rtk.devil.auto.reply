package com.rtk.devil.autoreply.core.model

data class OutgoingMessage(
    val platform: String,
    val conversationId: String,
    val text: String
)
