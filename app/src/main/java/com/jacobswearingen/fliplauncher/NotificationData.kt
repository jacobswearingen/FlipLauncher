package com.jacobswearingen.fliplauncher

data class NotificationEntry(val text: String, val packageName: String)

object NotificationData {
    private val notifications = mutableMapOf<String, NotificationEntry>()

    fun addNotification(key: String, text: String, packageName: String) {
        notifications[key] = NotificationEntry(text, packageName)
    }

    fun removeNotification(key: String) {
        notifications.remove(key)
    }

    fun getAll(): List<NotificationEntry> = notifications.values.toList()

    fun getKeyAt(index: Int): String? {
        return notifications.keys.elementAtOrNull(index)
    }

    fun clearAll() {
        notifications.clear()
    }
}
