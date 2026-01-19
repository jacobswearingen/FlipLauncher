package com.jacobswearingen.fliplauncher

object NotificationData {
    val notifications = mutableMapOf<String, String>()

    fun addNotification(key: String, text: String) {
        notifications[key] = text
    }

    fun removeNotification(key: String) {
        notifications.remove(key)
    }

    fun getAll(): List<String> = notifications.values.toList()
}
