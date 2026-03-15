package com.jacobswearingen.fliplauncher;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppListViewModel extends AndroidViewModel {
    private final MutableLiveData<List<ResolveInfo>> _apps = new MutableLiveData<>();
    public LiveData<List<ResolveInfo>> getApps() { return _apps; }

    public AppListViewModel(@NonNull Application application) {
        super(application);
        loadApps();
    }

    private void loadApps() {
        new Thread(() -> {
            final PackageManager pm = getApplication().getPackageManager();
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result = pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0));
            } else {
                result = pm.queryIntentActivities(intent, 0);
            }
            if (result == null) {
                result = Collections.emptyList();
            } else {
                result.sort((a, b) -> {
                    CharSequence la = a.loadLabel(pm);
                    CharSequence lb = b.loadLabel(pm);
                    return String.valueOf(la).toLowerCase().compareTo(String.valueOf(lb).toLowerCase());
                });
            }
            _apps.postValue(result);
        }).start();
    }
}
