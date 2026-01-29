package com.jacobswearingen.fliplauncher

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.lifecycle.AndroidViewModel

class AppListViewModel(application: Application) : AndroidViewModel(application) {
    private var _apps: List<ResolveInfo>? = null

    fun getApps(pm: PackageManager): List<ResolveInfo> {
        if (_apps == null) {
            val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            _apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, 0)
            }
        }
        return _apps!!
    }
}