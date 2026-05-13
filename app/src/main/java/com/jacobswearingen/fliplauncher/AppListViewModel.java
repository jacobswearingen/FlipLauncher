package com.jacobswearingen.fliplauncher;

import android.app.Application;
import android.content.Intent;
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
    public LiveData<List<AppInfo>> getApps() { return _apps; }

    public AppListViewModel(@NonNull Application application) {
        super(application);
        loadApps();
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
        super.onCleared();
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
            // Pre-load labels once so sorting doesn't call IPC repeatedly
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
