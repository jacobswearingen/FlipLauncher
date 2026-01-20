package com.jacobswearingen.fliplauncher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check notification access
        if (!hasNotificationAccess()) {
            Toast.makeText(
                this,
                "Please enable notification access for FlipLauncher",
                Toast.LENGTH_LONG
            ).show()
            requestNotificationAccess()
        }

        // Setup Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    private fun hasNotificationAccess(): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(this)
        return enabledPackages.contains(packageName)
    }

    private fun requestNotificationAccess() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    // Optional: Delegate key events to fragments that implement KeyEventHandler
    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
        if (currentFragment is KeyEventHandler) {
            if (currentFragment.onKeyDown(keyCode, event)) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
