package com.jacobswearingen.fliplauncher;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AppListViewModel extends AndroidViewModel {
    private static final String PREFS_NAME = "applist_prefs";
    private static final String KEY_HIDDEN_APPS = "hidden_apps";

    private final MutableLiveData<List<LauncherActivityInfo>> _filteredApps = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Map<String, Drawable>> _iconCache = new MutableLiveData<>(Collections.emptyMap());
    private final MutableLiveData<Set<String>> _hiddenPackages = new MutableLiveData<>(Collections.emptySet());
    private final MutableLiveData<Boolean> _showingHidden = new MutableLiveData<>(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Accessed only on executor thread
    private List<LauncherActivityInfo> allApps = Collections.emptyList();
    private Set<String> hiddenApps;
    private boolean showingHidden = false;

    private final LauncherApps launcherApps;
    private final SharedPreferences prefs;

    private final LauncherApps.Callback launcherCallback = new LauncherApps.Callback() {
        @Override
        public void onPackageAdded(String packageName, UserHandle user) { loadApps(); }
        @Override
        public void onPackageRemoved(String packageName, UserHandle user) { loadApps(); }
        @Override
        public void onPackageChanged(String packageName, UserHandle user) { loadApps(); }
        @Override
        public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) { loadApps(); }
        @Override
        public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) { loadApps(); }
    };

    public AppListViewModel(@NonNull Application application) {
        super(application);
        prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        hiddenApps = new HashSet<>(prefs.getStringSet(KEY_HIDDEN_APPS, Collections.emptySet()));
        _hiddenPackages.setValue(new HashSet<>(hiddenApps));
        launcherApps = (LauncherApps) application.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        launcherApps.registerCallback(launcherCallback);
        loadApps();
    }

    @Override
    protected void onCleared() {
        launcherApps.unregisterCallback(launcherCallback);
        executor.shutdown();
        super.onCleared();
    }

    public LiveData<List<LauncherActivityInfo>> getFilteredApps() {
        return _filteredApps;
    }

    public LiveData<Map<String, Drawable>> getIconCache() {
        return _iconCache;
    }

    public LiveData<Set<String>> getHiddenPackages() {
        return _hiddenPackages;
    }

    public LiveData<Boolean> getShowingHidden() {
        return _showingHidden;
    }

    public void toggleShowHidden() {
        executor.execute(() -> {
            showingHidden = !showingHidden;
            applyFilter();
        });
    }

    public void toggleHidden(String packageName) {
        executor.execute(() -> {
            if (!hiddenApps.add(packageName)) {
                hiddenApps.remove(packageName);
            }
            prefs.edit().putStringSet(KEY_HIDDEN_APPS, hiddenApps).apply();
            _hiddenPackages.postValue(new HashSet<>(hiddenApps));
            applyFilter();
        });
    }

    private void loadApps() {
        executor.execute(() -> {
            List<LauncherActivityInfo> activities = launcherApps.getActivityList(null, Process.myUserHandle());
            activities.sort((a, b) -> a.getLabel().toString().toLowerCase()
                    .compareTo(b.getLabel().toString().toLowerCase()));
            Map<String, Drawable> cache = new HashMap<>(activities.size());
            for (LauncherActivityInfo info : activities) {
                try {
                    cache.put(info.getApplicationInfo().packageName, info.getBadgedIcon(0));
                } catch (Exception e) {
                    cache.put(info.getApplicationInfo().packageName,
                            getApplication().getPackageManager().getDefaultActivityIcon());
                }
            }
            allApps = activities;
            _iconCache.postValue(Collections.unmodifiableMap(cache));
            applyFilter();
        });
    }

    /** Always called from the executor thread. */
    private void applyFilter() {
        List<LauncherActivityInfo> filtered = allApps.stream()
                .filter(info -> hiddenApps.contains(info.getApplicationInfo().packageName) == showingHidden)
                .collect(Collectors.toList());
        _filteredApps.postValue(filtered);
        _showingHidden.postValue(showingHidden);
    }
}

