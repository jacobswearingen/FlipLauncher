
package com.jacobswearingen.fliplauncher;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.service.notification.StatusBarNotification;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationsViewModel extends AndroidViewModel implements NotificationService.NotificationListener {
    private final MutableLiveData<List<StatusBarNotification>> notificationsLiveData = new MutableLiveData<>();

    public LiveData<List<StatusBarNotification>> getNotifications() {
        return notificationsLiveData;
    }

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        NotificationService.registerListener(this);
        loadNotifications();
    }

    @Override
    protected void onCleared() {
        NotificationService.unregisterListener(this);
        super.onCleared();
    }

    @Override
    public void onNotificationsChanged() {
        loadNotifications();
    }

    private void loadNotifications() {
        new Thread(() -> {
            NotificationManager nm = getApplication().getSystemService(NotificationManager.class);
            List<StatusBarNotification> notifications = NotificationService.getActiveNotificationsList();
            List<StatusBarNotification> sorted = new ArrayList<>(notifications);
            Collections.sort(sorted, new Comparator<StatusBarNotification>() {
                @Override
                public int compare(StatusBarNotification a, StatusBarNotification b) {
                    int priorityA = a.getNotification().priority;
                    int priorityB = b.getNotification().priority;
                    if (priorityA != priorityB) return Integer.compare(priorityB, priorityA);

                    String channelA = safeChannelId(a);
                    String channelB = safeChannelId(b);
                    int cmp = channelA.compareTo(channelB);
                    if (cmp != 0) return cmp;

                    int importanceA = getImportance(nm, a);
                    int importanceB = getImportance(nm, b);
                    if (importanceA != importanceB) return Integer.compare(importanceB, importanceA);

                    return Long.compare(b.getPostTime(), a.getPostTime());
                }

                private String safeChannelId(StatusBarNotification sbn) {
                    String id = sbn.getNotification().getChannelId();
                    return id != null ? id : "";
                }

                private int getImportance(NotificationManager nm, StatusBarNotification sbn) {
                    String channelId = sbn.getNotification().getChannelId();
                    if (channelId != null && nm != null) {
                        NotificationChannel channel = nm.getNotificationChannel(channelId);
                        if (channel != null) return channel.getImportance();
                    }
                    return sbn.getNotification().priority;
                }
            });
            notificationsLiveData.postValue(sorted);
        }).start();
    }

    public void cancelNotification(String key) {
        NotificationService.cancelNotificationByKey(key);
    }

    public void clearAllNotifications() {
        NotificationService.clearAllSystemNotifications();
    }
}
