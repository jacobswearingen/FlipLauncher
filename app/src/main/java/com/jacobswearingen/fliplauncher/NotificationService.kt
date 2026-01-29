package com.jacobswearingen.fliplauncher

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationService : NotificationListenerService() {
    companion object {
        private var instance: NotificationService? = null

        interface NotificationListener {
            fun onNotificationsChanged()
        }
        private val listeners = mutableSetOf<NotificationListener>()

        fun registerListener(listener: NotificationListener) {
            listeners.add(listener)
        }

        fun unregisterListener(listener: NotificationListener) {
            listeners.remove(listener)
        }

        private fun notifyListeners() {
            listeners.forEach { it.onNotificationsChanged() }
        }

        @JvmStatic
        fun getActiveNotifications(): List<StatusBarNotification> {
            return instance?.activeNotifications?.toList() ?: emptyList()
        }

        fun cancelNotificationByKey(key: String) {
            val service = instance ?: return
            val sbn = service.activeNotifications?.find { it.key == key }
            if (sbn != null && isNotificationCancelable(sbn)) {
                service.cancelNotification(key)
            }
        }

        private fun isNotificationCancelable(sbn: StatusBarNotification): Boolean {
            val flags = sbn.notification.flags
            val isOngoing = flags and Notification.FLAG_ONGOING_EVENT != 0
            val isNoClear = flags and Notification.FLAG_NO_CLEAR != 0
            return !isOngoing && !isNoClear
        }

        fun clearAllSystemNotifications() {
            instance?.activeNotifications?.forEach { sbn ->
                if (isNotificationCancelable(sbn)) {
                    instance?.cancelNotification(sbn.key)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        notifyListeners()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        notifyListeners()
    }
}
