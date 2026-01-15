package com.example.fliplauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Build
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Load the layout first
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

    private fun getInstalledApps(): List<ResolveInfo> {
        val intent =
                Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }

        val apps =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.queryIntentActivities(
                            intent,
                            PackageManager.ResolveInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION") packageManager.queryIntentActivities(intent, 0)
                }
        return apps
    }

    private fun showAllApps() {
        val apps = getInstalledApps()
        val gridView = findViewById<GridView>(R.id.appGrid)
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
                        textView.text = info.loadLabel(packageManager)
                        textView.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                info.loadIcon(packageManager),
                                null,
                                null
                        )
                        return view
                    }
                }
        gridView.adapter = adapter
    }
}
