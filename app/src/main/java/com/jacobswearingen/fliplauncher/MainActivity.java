package com.jacobswearingen.fliplauncher;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_FUNCTION) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.popBackStack(R.id.mainFragment, false);
            return true;
        }
        Fragment current = getCurrentFragment();
        if (current instanceof KeyEventHandler) {
            if (((KeyEventHandler) current).onKeyDown(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Nullable
    private Fragment getCurrentFragment() {
        Fragment navHost = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHost == null) return null;
        List<Fragment> fragments = navHost.getChildFragmentManager().getFragments();
        return fragments.isEmpty() ? null : fragments.get(0);
    }
}
