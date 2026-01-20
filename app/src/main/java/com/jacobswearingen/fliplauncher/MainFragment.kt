package com.jacobswearingen.fliplauncher

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatDelegate
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.fragment.findNavController

class MainFragment : Fragment(R.layout.fragment_main), KeyEventHandler {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        updateTimeViews(view)
        // Add navigation or other logic here if needed

    }

    private fun updateTimeViews(view: View) = with(view) {
        val now = Date()
        findViewById<TextView>(R.id.textViewTime).text = SimpleDateFormat("h:mm", Locale.getDefault()).format(now)
        findViewById<TextView>(R.id.textViewAmPm).text = SimpleDateFormat("a", Locale.getDefault()).format(now)
        findViewById<TextView>(R.id.textViewDate).text = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(now)
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        when (keyCode) {
            android.view.KeyEvent.KEYCODE_SOFT_LEFT, 139 -> {
                // Navigate to NotificationsFragment
                findNavController().navigate(R.id.notificationsFragment)
                return true
            }
            android.view.KeyEvent.KEYCODE_DPAD_CENTER, 28 -> {
                // Navigate to AppListFragment
                findNavController().navigate(R.id.appListFragment)
                return true
            }
        }
        return false
    }
}
