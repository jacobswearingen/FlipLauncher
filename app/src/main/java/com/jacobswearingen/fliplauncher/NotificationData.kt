package com.jacobswearingen.fliplauncher

data class NotificationEntry(val text: String, val packageName: String)

object NotificationData {
    private val notifications = mutableMapOf<String, NotificationEntry>()

    fun addNotification(key: String, text: String, packageName: String) {
        notifications[key] = NotificationEntry(text, packageName)
        notifyListeners()
    }

    fun removeNotification(key: String) {
        notifications.remove(key)
        notifyListeners()
    }

    fun getAll(): List<NotificationEntry> = notifications.values.toList()

    fun getKeyAt(index: Int): String? {
        return notifications.keys.elementAtOrNull(index)
    }

    fun clearAll() {
        notifications.clear()
        notifyListeners()
    }

    interface Listener {
        fun onNotificationDataChanged()
    }

    private val listeners = mutableSetOf<Listener>()

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.onNotificationDataChanged() }
    }
}
