package com.rtk.devil.autoreply.domain.automation

enum class RuleType {
    WELCOME,
    EVERY_MESSAGE,
    KEYWORD
}

data class AutomationRule(
    val id: String,
    val name: String,
    val type: RuleType,
    val enabled: Boolean,
    val replyText: String,
    val priority: Int = 100
)
