package com.jacobswearingen.fliplauncher

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ListView
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {
    private var inAppListView = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateTimeViews()
    }

    private fun updateTimeViews() {
        val sdf = SimpleDateFormat("h:mm", Locale.getDefault())
        val ampm = SimpleDateFormat("a", Locale.getDefault())
        val currentDate = Date()
        findViewById<TextView>(R.id.textViewTime).text = sdf.format(currentDate)
        findViewById<TextView>(R.id.textViewAmPm).text = ampm.format(currentDate)
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            showAllApps()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (inAppListView) {
            setContentView(R.layout.activity_main)
            updateTimeViews()
            inAppListView = false
        } else {
            super.onBackPressed()
        }
    }

    private fun getInstalledApps(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val launcherApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION") packageManager.queryIntentActivities(intent, 0)
        }

        // To get all installed packages (including those without launcher icons):
        // val packages = packageManager.getInstalledPackages(0)
        // This returns List<PackageInfo>, not ResolveInfo.

        return launcherApps // or use getInstalledPackages if you want all apps, but you'll need to adapt your adapter code.
    }

    private fun getAllInstalledPackages(): List<PackageInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION") packageManager.getInstalledPackages(0)
        }
    }

    private fun showAllApps() {
        setContentView(R.layout.activity_app_list)
        inAppListView = true
        val apps = getInstalledApps().sortedBy { it.loadLabel(packageManager).toString().lowercase(Locale.getDefault()) }
        val listView = findViewById<ListView>(R.id.appList)
        val adapter =
            object : BaseAdapter() {
                override fun getCount() = apps.size
                override fun getItem(position: Int) = apps[position]
                override fun getItemId(position: Int) = position.toLong()
                override fun getView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup?
                ): View {
                    val info = apps[position]
                    val view =
                        convertView
                            ?: LayoutInflater.from(this@MainActivity)
                                .inflate(
                                    R.layout.app_list_item,
                                    parent,
                                    false
                                )
                    val iconView = view.findViewById<ImageView>(R.id.appIcon)
                    val labelView = view.findViewById<TextView>(R.id.appLabel)
                    iconView.setImageDrawable(info.loadIcon(packageManager))
                    labelView.text = info.loadLabel(packageManager)
                    return view
                }
            }
        listView.adapter = adapter
        // Launch app on item click
        listView.setOnItemClickListener { _, _, position, _ ->
            val info = apps[position]
            val launchIntent = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }
    }
}
