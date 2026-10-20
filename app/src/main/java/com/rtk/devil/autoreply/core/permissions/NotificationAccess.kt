package com.rtk.devil.autoreply.core.permissions

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object NotificationAccess {

    fun isGranted(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        val expected = ComponentName(
            context,
            "com.rtk.devil.autoreply.integration.notificationreply." +
                "TelegramNotificationListener"
        )

        return enabled.split(':').any { entry ->
            val component = ComponentName.unflattenFromString(entry)
            component == expected
        }
    }

    fun isAnyNotificationAccessGranted(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        return !TextUtils.isEmpty(enabled)
    }
}
