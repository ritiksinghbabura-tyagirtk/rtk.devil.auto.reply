package com.rtk.devil.autoreply.integration.telegrambot

data class BotUpdate(
    val updateId: Long,
    val chatId: Long,
    val text: String,
    val senderIsBot: Boolean
)
