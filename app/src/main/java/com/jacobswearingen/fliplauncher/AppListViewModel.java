package com.jacobswearingen.fliplauncher;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppListViewModel extends AndroidViewModel {
    private final MutableLiveData<List<ResolveInfo>> _apps = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    public LiveData<List<ResolveInfo>> getApps() { return _apps; }

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
            List<ResolveInfo> result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result = pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0));
            } else {
                result = pm.queryIntentActivities(intent, 0);
            }
            result.sort((a, b) -> {
                CharSequence la = a.loadLabel(pm);
                CharSequence lb = b.loadLabel(pm);
                return String.valueOf(la).toLowerCase().compareTo(String.valueOf(lb).toLowerCase());
            });
            _apps.postValue(result);
        });
    }
}
