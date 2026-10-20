package com.rtk.devil.autoreply.integration.telegrambot

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class TelegramBotIdentity(
    val id: Long,
    val username: String,
    val firstName: String
)

class TelegramBotApi {

    fun verifyToken(token: String): TelegramBotIdentity {
        val clean = token.trim()
        if (!TOKEN_PATTERN.matches(clean)) {
            throw IllegalArgumentException(
                "Token format looks invalid. Check the token from BotFather."
            )
        }

        val response = request(clean, "getMe", null)
        if (!response.optBoolean("ok", false)) {
            val code = response.optInt("error_code", 0)
            val description = response.optString(
                "description", "Telegram rejected the request."
            )
            throw IOException(
                if (code == 401) {
                    "Telegram rejected this token. Check it in BotFather."
                } else {
                    "Telegram API error ($code): ${safeDescription(description)}"
                }
            )
        }

        val result = response.optJSONObject("result")
            ?: throw IOException("Telegram returned an unexpected response.")

        return TelegramBotIdentity(
            id = result.optLong("id"),
            username = result.optString("username", ""),
            firstName = result.optString("first_name", "")
        )
    }

    fun getUpdates(
        token: String,
        offset: Long,
        timeoutSeconds: Int = 20
    ): List<BotUpdate> {
        val body = JSONObject()
            .put("offset", offset)
            .put("timeout", timeoutSeconds)
            .put("allowed_updates", JSONArray().put("message"))

        val response = request(token, "getUpdates", body)
        if (!response.optBoolean("ok", false)) {
            val code = response.optInt("error_code", 0)
            val description = safeDescription(
                response.optString("description", "Unknown Telegram error")
            )
            throw IOException("Telegram polling error ($code): $description")
        }

        val results = response.optJSONArray("result") ?: JSONArray()
        return buildList {
            for (i in 0 until results.length()) {
                val update = results.optJSONObject(i) ?: continue
                val message = update.optJSONObject("message") ?: continue
                val chat = message.optJSONObject("chat") ?: continue
                val sender = message.optJSONObject("from")
                val text = message.optString("text", "")
                if (text.isBlank()) continue

                add(
                    BotUpdate(
                        updateId = update.optLong("update_id", -1L),
                        chatId = chat.optLong("id"),
                        text = text,
                        senderIsBot = sender?.optBoolean("is_bot", false) ?: false
                    )
                )
            }
        }
    }

    fun sendMessage(token: String, chatId: Long, text: String) {
        if (text.isBlank()) return
        val body = JSONObject()
            .put("chat_id", chatId)
            .put("text", text.take(4096))

        val response = request(token, "sendMessage", body)
        if (!response.optBoolean("ok", false)) {
            val code = response.optInt("error_code", 0)
            val description = safeDescription(
                response.optString("description", "Unknown Telegram error")
            )
            throw IOException("Telegram send error ($code): $description")
        }
    }

    private fun request(
        token: String,
        method: String,
        body: JSONObject?
    ): JSONObject {
        val connection = (URL("$API_ROOT/bot$token/$method")
            .openConnection() as HttpURLConnection).apply {
            requestMethod = if (body == null) "GET" else "POST"
            connectTimeout = 12_000
            // Telegram long polling may wait for up to 20 seconds.
            readTimeout = if (method == "getUpdates") 35_000 else 15_000
            useCaches = false
            if (body != null) {
                doOutput = true
                setRequestProperty(
                    "Content-Type", "application/json; charset=utf-8"
                )
            }
        }

        try {
            if (body != null) {
                connection.outputStream.use {
                    it.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (text.isBlank()) {
                throw IOException("Telegram returned HTTP $code without a response.")
            }
            return JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    private fun safeDescription(value: String): String =
        value.replace(Regex("[\\r\\n]+"), " ").take(180)

    companion object {
        private const val API_ROOT = "https://api.telegram.org"
        private val TOKEN_PATTERN = Regex("[0-9]{5,}:[A-Za-z0-9_-]{20,}")
    }
}
