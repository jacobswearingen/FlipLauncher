package com.jacobswearingen.fliplauncher

import android.app.Application
import android.service.notification.StatusBarNotification
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class NotificationsViewModel(application: Application) : AndroidViewModel(application), NotificationService.Companion.NotificationListener {
    private val _notifications = MutableLiveData<List<StatusBarNotification>>()
    val notifications: LiveData<List<StatusBarNotification>> = _notifications

    init {
        NotificationService.registerListener(this)
        loadNotifications()
    }

    override fun onCleared() {
        super.onCleared()
        NotificationService.unregisterListener(this)
    }

    override fun onNotificationsChanged() {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch(Dispatchers.Default) {
            val sorted = NotificationService.getActiveNotifications()
                .sortedWith(compareByDescending<StatusBarNotification> { it.notification.priority }
                    .thenByDescending { it.postTime })
            _notifications.postValue(sorted)
        }
    }

    fun cancelNotification(key: String) {
        NotificationService.cancelNotificationByKey(key)
    }

    fun clearAllNotifications() {
        NotificationService.clearAllSystemNotifications()
    }
}