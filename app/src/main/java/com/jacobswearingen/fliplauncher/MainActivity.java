package com.jacobswearingen.fliplauncher;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!hasNotificationAccess()) {
            Toast.makeText(
                    this,
                    "Please enable notification access for FlipLauncher",
                    Toast.LENGTH_LONG
            ).show();
            requestNotificationAccess();
        }
    }

    private boolean hasNotificationAccess() {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
    }

    private void requestNotificationAccess() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_FUNCTION) {
            androidx.navigation.NavController navController =
                    androidx.navigation.Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.popBackStack(R.id.mainFragment, false);
            return true;
        }
        androidx.fragment.app.Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        androidx.fragment.app.Fragment currentFragment = null;
        if (navHostFragment != null && !navHostFragment.getChildFragmentManager().getFragments().isEmpty()) {
            currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        }
        if (currentFragment instanceof KeyEventHandler) {
            if (((KeyEventHandler) currentFragment).onKeyDown(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
