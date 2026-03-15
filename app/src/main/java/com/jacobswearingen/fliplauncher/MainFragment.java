package com.jacobswearingen.fliplauncher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainFragment extends Fragment implements KeyEventHandler {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeUpdater;

    public MainFragment() {
        super(R.layout.fragment_main);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        updateTimeViews(view);
        startMinuteUpdater(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        View v = getView();
        if (v != null) updateTimeViews(v);
    }

    private void updateTimeViews(@NonNull View view) {
        Date now = new Date();
        TextView timeView = view.findViewById(R.id.textViewTime);
        TextView ampmView = view.findViewById(R.id.textViewAmPm);
        TextView dateView = view.findViewById(R.id.textViewDate);
        if (timeView != null)
            timeView.setText(new SimpleDateFormat("h:mm", Locale.getDefault()).format(now));
        if (ampmView != null)
            ampmView.setText(new SimpleDateFormat("a", Locale.getDefault()).format(now));
        if (dateView != null)
            dateView.setText(new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(now));
    }

    private void startMinuteUpdater(@NonNull View view) {
        if (timeUpdater != null) handler.removeCallbacks(timeUpdater);
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                updateTimeViews(view);
                Calendar now = Calendar.getInstance();
                int msUntilNextMinute = (60 - now.get(Calendar.SECOND)) * 1000 - now.get(Calendar.MILLISECOND);
                handler.postDelayed(this, msUntilNextMinute);
            }
        };
        handler.post(timeUpdater);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT || keyCode == 139) {
            NavHostFragment.findNavController(this).navigate(R.id.notificationsFragment);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == 28) {
            NavHostFragment.findNavController(this).navigate(R.id.appListFragment);
            return true;
        } else if ((keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9)
                || keyCode == KeyEvent.KEYCODE_STAR || keyCode == KeyEvent.KEYCODE_POUND) {
            String digit;
            if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                digit = String.valueOf(keyCode - KeyEvent.KEYCODE_0);
            } else if (keyCode == KeyEvent.KEYCODE_STAR) {
                digit = "*";
            } else if (keyCode == KeyEvent.KEYCODE_POUND) {
                digit = "#";
            } else {
                digit = "";
            }
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + digit));
            dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(dialIntent);
            return true;
        }
        return false;
    }
}
