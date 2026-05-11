package com.jacobswearingen.fliplauncher;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NotificationService extends NotificationListenerService {
    private static NotificationService instance = null;
    private static final MutableLiveData<Void> changes = new MutableLiveData<>();

    public static LiveData<Void> getChanges() { return changes; }

    public static List<StatusBarNotification> getActiveNotificationsList() {
        NotificationService service = instance;
        if (service == null) return Collections.emptyList();
        StatusBarNotification[] activeNotifications = service.getActiveNotifications();
        if (activeNotifications == null) return Collections.emptyList();
        return Arrays.asList(activeNotifications);
    }

    public static void cancelNotificationByKey(String key) {
        NotificationService service = instance;
        if (service == null) return;
        service.cancelNotification(key);
    }

    private static boolean isNotificationCancelable(StatusBarNotification sbn) {
        int flags = sbn.getNotification().flags;
        boolean isOngoing = (flags & Notification.FLAG_ONGOING_EVENT) != 0;
        boolean isNoClear = (flags & Notification.FLAG_NO_CLEAR) != 0;
        return !isOngoing && !isNoClear;
    }

    public static void clearAllSystemNotifications() {
        NotificationService service = instance;
        if (service == null) return;
        StatusBarNotification[] activeNotifications = service.getActiveNotifications();
        if (activeNotifications == null) return;
        Arrays.stream(activeNotifications)
                .filter(NotificationService::isNotificationCancelable)
                .forEach(sbn -> service.cancelNotification(sbn.getKey()));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (instance == this) instance = null;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        changes.postValue(null);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        changes.setValue(null);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        changes.setValue(null);
    }
}
