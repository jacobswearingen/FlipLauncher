package com.jacobswearingen.fliplauncher;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class ShortcutsFragment extends Fragment {

    public ShortcutsFragment() {
        super(R.layout.fragment_shortcuts);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupShortcut(view, R.id.itemWifi, R.drawable.wifi_enabled, R.drawable.wifi_disabled,
                "Wi-Fi", new Intent(Settings.ACTION_WIFI_SETTINGS), this::isWifiEnabled);
        setupShortcut(view, R.id.itemBluetooth, R.drawable.bluetooth_enabled, R.drawable.bluetooth_disabled,
                "Bluetooth", new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), this::isBluetoothEnabled);
        setupShortcut(view, R.id.itemCellular, R.drawable.cell_data, R.drawable.cell_data,
                "Cellular Data", new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS), this::isCellularDataEnabled);
        setupShortcut(view, R.id.itemAirplane, R.drawable.airplanemode_active, R.drawable.airplanemode_inactive,
                "Airplane Mode", new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS), this::isAirplaneModeOn);

        // Settings — navigates within the app
        View settingsItem = view.findViewById(R.id.itemLauncherSettings);
        ((TextView) settingsItem.findViewById(R.id.shortcutLabel)).setText("Settings");
        ImageView settingsIcon = settingsItem.findViewById(R.id.shortcutIcon);
        settingsIcon.setImageResource(R.drawable.settings);
        GradientDrawable settingsBg = new GradientDrawable();
        settingsBg.setShape(GradientDrawable.OVAL);
        settingsBg.setColor(Color.parseColor("#2196F3"));
        settingsIcon.setBackground(settingsBg);
        settingsItem.setOnClickListener(v ->
                NavHostFragment.findNavController(ShortcutsFragment.this).navigate(R.id.settingsFragment));

        // Hide unused extra slot
        View extraItem = view.findViewById(R.id.itemExtra1);
        if (extraItem != null) extraItem.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view == null) return;
        updateShortcut(view, R.id.itemWifi, R.drawable.wifi_enabled, R.drawable.wifi_disabled, this::isWifiEnabled);
        updateShortcut(view, R.id.itemBluetooth, R.drawable.bluetooth_enabled, R.drawable.bluetooth_disabled, this::isBluetoothEnabled);
        updateShortcut(view, R.id.itemCellular, R.drawable.cell_data, R.drawable.cell_data, this::isCellularDataEnabled);
        updateShortcut(view, R.id.itemAirplane, R.drawable.airplanemode_active, R.drawable.airplanemode_inactive, this::isAirplaneModeOn);
    }

    private interface StatusChecker {
        boolean isEnabled();
    }

    private void setupShortcut(View parent, int itemId, int enabledRes, int disabledRes,
                               String label, Intent intent, StatusChecker checker) {
        View item = parent.findViewById(itemId);
        TextView text = item.findViewById(R.id.shortcutLabel);
        text.setText(label);
        item.setOnClickListener(v -> parent.getContext().startActivity(intent));
        updateShortcut(parent, itemId, enabledRes, disabledRes, checker);
    }

    private void updateShortcut(View parent, int itemId, int enabledRes, int disabledRes,
                                StatusChecker checker) {
        View item = parent.findViewById(itemId);
        ImageView icon = item.findViewById(R.id.shortcutIcon);
        boolean enabled = checker.isEnabled();
        icon.setImageResource(enabled ? enabledRes : disabledRes);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor(enabled ? "#2196F3" : "#BDBDBD"));
        icon.setBackground(bg);
    }

    private boolean isWifiEnabled() {
        try {
            if (getActivity() == null) return false;
            WifiManager wm = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return wm != null && wm.isWifiEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isBluetoothEnabled() {
        try {
            if (getActivity() == null) return false;
            BluetoothManager bm = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            return bm != null && bm.getAdapter() != null && bm.getAdapter().isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("deprecation") // getAllNetworks() checks if data is enabled regardless of active route
    private boolean isCellularDataEnabled() {
        try {
            if (getActivity() == null) return false;
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            for (Network network : cm.getAllNetworks()) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                if (caps != null
                        && caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isAirplaneModeOn() {
        try {
            if (getActivity() == null) return false;
            return Settings.Global.getInt(getActivity().getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        } catch (Exception e) {
            return false;
        }
    }
}
