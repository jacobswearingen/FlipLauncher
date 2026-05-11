package com.jacobswearingen.fliplauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainFragment extends Fragment implements KeyEventHandler {
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("h:mm");
    private final DateTimeFormatter amPmFormat = DateTimeFormatter.ofPattern("a");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEE, MMM d");
    private final BroadcastReceiver timeTick = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            View v = getView();
            if (v != null) updateTimeViews(v);
        }
    };

    public MainFragment() {
        super(R.layout.fragment_main);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateTimeViews(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        View v = getView();
        if (v != null) updateTimeViews(v);
        requireContext().registerReceiver(timeTick, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(timeTick);
    }

    private void updateTimeViews(@NonNull View view) {
        LocalDateTime now = LocalDateTime.now();
        TextView timeView = view.findViewById(R.id.textViewTime);
        TextView ampmView = view.findViewById(R.id.textViewAmPm);
        TextView dateView = view.findViewById(R.id.textViewDate);
        if (timeView != null) timeView.setText(timeFormat.format(now));
        if (ampmView != null) ampmView.setText(amPmFormat.format(now));
        if (dateView != null) dateView.setText(dateFormat.format(now));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
            NavHostFragment.findNavController(this).navigate(R.id.notificationsFragment);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT) {
            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            NavHostFragment.findNavController(this).navigate(R.id.appListFragment);
            return true;
        } else if ((keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9)
                || keyCode == KeyEvent.KEYCODE_STAR || keyCode == KeyEvent.KEYCODE_POUND) {
            String digit;
            if (keyCode == KeyEvent.KEYCODE_STAR) {
                digit = "*";
            } else if (keyCode == KeyEvent.KEYCODE_POUND) {
                digit = "#";
            } else {
                digit = String.valueOf(keyCode - KeyEvent.KEYCODE_0);
            }
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + digit));
            dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(dialIntent);
            return true;
        }
        return false;
    }

}
