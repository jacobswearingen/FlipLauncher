package com.jacobswearingen.fliplauncher;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class NotificationService extends NotificationListenerService {
    private static NotificationService instance = null;

    public interface NotificationListener {
        void onNotificationsChanged();
    }

    private static final Set<NotificationListener> listeners = new HashSet<>();

    public static void registerListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public static void unregisterListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (NotificationListener listener : listeners) {
            listener.onNotificationsChanged();
        }
    }

    public static List<StatusBarNotification> getActiveNotificationsList() {
        NotificationService service = instance;
        if (service == null) {
            return Collections.emptyList();
        }
        StatusBarNotification[] activeNotifications = service.getActiveNotifications();
        if (activeNotifications == null) {
            return Collections.emptyList();
        }
        List<StatusBarNotification> list = new ArrayList<>(activeNotifications.length);
        Collections.addAll(list, activeNotifications);
        return list;
    }

    public static void cancelNotificationByKey(String key) {
        NotificationService service = instance;
        if (service == null) return;
        StatusBarNotification[] activeNotifications = service.getActiveNotifications();
        if (activeNotifications == null) return;
        for (StatusBarNotification sbn : activeNotifications) {
            if (sbn.getKey().equals(key) && isNotificationCancelable(sbn)) {
                service.cancelNotification(key);
                break;
            }
        }
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
        for (StatusBarNotification sbn : activeNotifications) {
            if (isNotificationCancelable(sbn)) {
                service.cancelNotification(sbn.getKey());
            }
        }
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
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        notifyListeners();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        notifyListeners();
    }
}
