package com.jacobswearingen.fliplauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var inMainView = true

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateTimeViews()

        if (!hasNotificationAccess()) {
            Toast.makeText(
                            this,
                            "Please enable notification access for FlipLauncher",
                            Toast.LENGTH_LONG
                    )
                    .show()
            requestNotificationAccess()
        }
    }

    private fun hasNotificationAccess(): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(this)
        return enabledPackages.contains(packageName)
    }

    private fun requestNotificationAccess() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    private fun updateTimeViews() {
        val sdf = SimpleDateFormat("h:mm", Locale.getDefault())
        val ampm = SimpleDateFormat("a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val currentDate = Date()
        findViewById<TextView>(R.id.textViewTime).text = sdf.format(currentDate)
        findViewById<TextView>(R.id.textViewAmPm).text = ampm.format(currentDate)
        findViewById<TextView>(R.id.textViewDate).text = dateFormat.format(currentDate)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                showAllApps() // Show app grid
                true
            }
            KeyEvent.KEYCODE_SOFT_LEFT, 139 -> { // Handle SOFT_LEFT key
                showNotificationsView() // Show notifications view
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun showNotificationsView() {
        setContentView(R.layout.activity_notifications)
        inMainView = false
        val listView = findViewById<ListView>(R.id.notificationList)

        val notifications = NotificationData.getAll()
        val adapter = object : BaseAdapter() {
            override fun getCount() = notifications.size
            override fun getItem(position: Int) = notifications[position]
            override fun getItemId(position: Int) = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = convertView ?: LayoutInflater.from(this@MainActivity)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.text = notifications[position].text
                return view
            }
        }

        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val entry = notifications[position]
            val launchIntent = packageManager.getLaunchIntentForPackage(entry.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                Toast.makeText(this, "Cannot launch app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        if (!inMainView) {
            setContentView(R.layout.activity_main)
            updateTimeViews()
            inMainView = true
        } else {
            super.onBackPressed()
        }
    }

    private fun getInstalledApps(): List<ResolveInfo> {
        val intent =
                Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val launcherApps =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.queryIntentActivities(
                            intent,
                            PackageManager.ResolveInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION") packageManager.queryIntentActivities(intent, 0)
                }
        return launcherApps
    }

    private fun showAllApps() {
        setContentView(R.layout.activity_app_list)
        inMainView = false
        val apps =
                getInstalledApps().sortedBy {
                    it.loadLabel(packageManager).toString().lowercase(Locale.getDefault())
                }
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
                                                .inflate(R.layout.app_list_item, parent, false)
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
            val launchIntent =
                    packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }
    }
}
