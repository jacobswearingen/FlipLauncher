package com.jacobswearingen.fliplauncher;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsFragment extends Fragment implements KeyEventHandler {

    private SharedPreferences prefs;

    // Loaded once per fragment view
    private List<String> appPackages;
    private String[] appLabels;

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = requireContext().getSharedPreferences(LauncherPrefs.PREFS, 0);

        // D-pad hotkey rows — load apps async, then wire up each row
        loadInstalledAppsAsync(view);
    }

    private void loadInstalledAppsAsync(View view) {
        new Thread(() -> {
            PackageManager pm = requireContext().getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
            Collections.sort(infos, (a, b) -> a.loadLabel(pm).toString()
                    .compareToIgnoreCase(b.loadLabel(pm).toString()));

            appPackages = new ArrayList<>();
            List<String> labelList = new ArrayList<>();
            appPackages.add(LauncherPrefs.DEST_NONE);
            labelList.add("None");
            for (ResolveInfo info : infos) {
                appPackages.add(info.activityInfo.packageName);
                labelList.add(info.loadLabel(pm).toString());
            }
            appLabels = labelList.toArray(new String[0]);

            view.post(() -> {
                if (!isAdded()) return;
                setupHotkeyRow(view, R.id.rowHotkeyLeft, R.id.textHotkeyLeftApp, LauncherPrefs.KEY_HOTKEY_DPAD_LEFT);
                setupHotkeyRow(view, R.id.rowHotkeyRight, R.id.textHotkeyRightApp, LauncherPrefs.KEY_HOTKEY_DPAD_RIGHT);
                setupHotkeyRow(view, R.id.rowHotkeyUp, R.id.textHotkeyUpApp, LauncherPrefs.KEY_HOTKEY_DPAD_UP);
            });
        }).start();
    }

    private void setupHotkeyRow(View parent, int rowId, int subtitleId, String prefKey) {
        TextView subtitle = parent.findViewById(subtitleId);
        updateHotkeySubtitle(subtitle, prefs.getString(prefKey, LauncherPrefs.DEST_NONE));
        parent.findViewById(rowId).setOnClickListener(v -> showAppPickerDialog(subtitle, prefKey));
    }

    private void showAppPickerDialog(TextView subtitle, String prefKey) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Choose app")
                .setItems(appLabels, (dialog, which) -> {
                    String pkg = appPackages.get(which);
                    prefs.edit().putString(prefKey, pkg).apply();
                    updateHotkeySubtitle(subtitle, pkg);
                })
                .show();
    }

    private void updateHotkeySubtitle(TextView subtitle, String pkg) {
        if (LauncherPrefs.DEST_NONE.equals(pkg)) {
            subtitle.setText("None");
            return;
        }
        PackageManager pm = requireContext().getPackageManager();
        try {
            CharSequence label = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0));
            subtitle.setText(label);
        } catch (PackageManager.NameNotFoundException e) {
            subtitle.setText(pkg);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            NavHostFragment.findNavController(this).popBackStack();
            return true;
        }
        return false;
    }
}
