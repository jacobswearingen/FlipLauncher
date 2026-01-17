package com.jacobswearingen.fliplauncher

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val title = sbn.notification.extras.getString("android.title") ?: ""
        val text = sbn.notification.extras.getString("android.text") ?: ""
        NotificationData.addNotification("$title: $text")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationData.removeNotification(sbn.key)
    }
}
