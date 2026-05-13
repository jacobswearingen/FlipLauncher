package com.jacobswearingen.fliplauncher;

import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class AppInfo {
    public final ResolveInfo resolveInfo;
    public final String label;
    public final Drawable icon;

    public AppInfo(ResolveInfo resolveInfo, String label, Drawable icon) {
        this.resolveInfo = resolveInfo;
        this.label = label;
        this.icon = icon;
    }

    public String getPackageName() {
        return resolveInfo.activityInfo.packageName;
    }
}
