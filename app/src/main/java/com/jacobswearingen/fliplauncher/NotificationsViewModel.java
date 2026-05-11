
package com.jacobswearingen.fliplauncher;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.service.notification.StatusBarNotification;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationsViewModel extends AndroidViewModel {
    private final MutableLiveData<List<StatusBarNotification>> notificationsLiveData = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Observer<Void> changesObserver = ignored -> loadNotifications();

    public LiveData<List<StatusBarNotification>> getNotifications() {
        return notificationsLiveData;
    }

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        NotificationService.getChanges().observeForever(changesObserver);
        loadNotifications();
    }

    @Override
    protected void onCleared() {
        NotificationService.getChanges().removeObserver(changesObserver);
        executor.shutdownNow();
        super.onCleared();
    }

    private void loadNotifications() {
        executor.execute(() -> {
            NotificationManager nm = getApplication().getSystemService(NotificationManager.class);
            List<StatusBarNotification> notifications = NotificationService.getActiveNotificationsList();
            List<StatusBarNotification> sorted = new ArrayList<>(notifications);
            sorted.sort(
                Comparator.comparingInt((StatusBarNotification sbn) -> -getImportance(nm, sbn))
                    .thenComparing(sbn -> Objects.requireNonNullElse(sbn.getNotification().getChannelId(), ""))
                    .thenComparing(Comparator.comparingLong(StatusBarNotification::getPostTime).reversed())
            );
            notificationsLiveData.postValue(sorted);
        });
    }

    private int getImportance(NotificationManager nm, StatusBarNotification sbn) {
        String channelId = sbn.getNotification().getChannelId();
        if (channelId != null && nm != null) {
            NotificationChannel channel = nm.getNotificationChannel(channelId);
            if (channel != null) return channel.getImportance();
        }
        return NotificationManager.IMPORTANCE_DEFAULT;
    }

    public void cancelNotification(String key) {
        NotificationService.cancelNotificationByKey(key);
    }

    public void clearAllNotifications() {
        NotificationService.clearAllSystemNotifications();
    }
}
