package com.example.fliplauncher

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
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {
    private var inAppListView = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textViewTime = findViewById<TextView>(R.id.textViewTime)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = sdf.format(Date())
        textViewTime.text = currentTime
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
            val textViewTime = findViewById<TextView>(R.id.textViewTime)
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTime = sdf.format(Date())
            textViewTime.text = currentTime
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
        val apps = getAllInstalledPackages()
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
                                    android.R.layout.simple_list_item_1,
                                    parent,
                                    false
                                )
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    val appLabel = info.applicationInfo.loadLabel(packageManager)
                    val appIcon = info.applicationInfo.loadIcon(packageManager)
                    textView.text = appLabel
                    textView.setCompoundDrawablesWithIntrinsicBounds(
                        appIcon,
                        null,
                        null,
                        null
                    )
                    return view
                }
            }
        listView.adapter = adapter
    }
}
