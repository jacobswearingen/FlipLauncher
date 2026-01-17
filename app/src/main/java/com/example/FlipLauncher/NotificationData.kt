package com.jacobswearingen.fliplauncher

object NotificationData {
    val notifications = mutableListOf<String>()

    fun addNotification(text: String) {
        notifications.add(text)
    }

    fun removeNotification(key: String) {
        // Optional: implement removal logic
    }
}
