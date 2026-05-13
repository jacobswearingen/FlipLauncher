package com.jacobswearingen.fliplauncher;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppListViewModel extends AndroidViewModel {
    private final MutableLiveData<List<AppInfo>> _apps = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final BroadcastReceiver packageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadApps();
        }
    };

    public AppListViewModel(@NonNull Application application) {
        super(application);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        application.registerReceiver(packageReceiver, filter);
        loadApps();
    }

    @Override
    protected void onCleared() {
        getApplication().unregisterReceiver(packageReceiver);
        executor.shutdownNow();
        super.onCleared();
    }

    public LiveData<List<AppInfo>> getApps() {
        return _apps;
    }

    private void loadApps() {
        executor.execute(() -> {
            final PackageManager pm = getApplication().getPackageManager();
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                resolveInfos = pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0));
            } else {
                resolveInfos = pm.queryIntentActivities(intent, 0);
            }
            List<AppInfo> result = new ArrayList<>(resolveInfos.size());
            for (ResolveInfo info : resolveInfos) {
                String label = info.loadLabel(pm).toString();
                Drawable icon;
                try {
                    icon = info.loadIcon(pm);
                } catch (Exception e) {
                    icon = pm.getDefaultActivityIcon();
                }
                result.add(new AppInfo(info, label, icon));
            }
            result.sort((a, b) -> a.label.toLowerCase().compareTo(b.label.toLowerCase()));
            _apps.postValue(result);
        });
    }
}

