package com.jacobswearingen.fliplauncher

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import com.jacobswearingen.fliplauncher.R

class ShortcutsFragment : Fragment(R.layout.fragment_shortcuts) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupShortcut(view, R.id.itemWifi, R.drawable.wifi_enabled, R.drawable.wifi_disabled, "Wi-Fi", Intent(Settings.ACTION_WIFI_SETTINGS)) { isWifiEnabled() }
        setupShortcut(view, R.id.itemBluetooth, R.drawable.bluetooth_enabled, R.drawable.bluetooth_disabled, "Bluetooth", Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) { isBluetoothEnabled() }
        setupShortcut(view, R.id.itemCellular, R.drawable.cell_data, R.drawable.cell_data, "Cellular Data", Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)) { isCellularDataEnabled() }
        setupShortcut(view, R.id.itemAirplane, R.drawable.airplanemode_active, R.drawable.airplanemode_inactive, "Airplane Mode", Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)) { isAirplaneModeOn() }
    }

    override fun onResume() {
        super.onResume()
        view?.let {
            updateShortcut(it, R.id.itemWifi, R.drawable.wifi_enabled, R.drawable.wifi_disabled) { isWifiEnabled() }
            updateShortcut(it, R.id.itemBluetooth, R.drawable.bluetooth_enabled, R.drawable.bluetooth_disabled) { isBluetoothEnabled() }
            updateShortcut(it, R.id.itemCellular, R.drawable.cell_data, R.drawable.cell_data) { isCellularDataEnabled() }
            updateShortcut(it, R.id.itemAirplane, R.drawable.airplanemode_active, R.drawable.airplanemode_inactive) { isAirplaneModeOn() }
        }
    }

    private fun setupShortcut(
        parent: View,
        itemId: Int,
        enabledRes: Int,
        disabledRes: Int,
        label: String,
        intent: Intent,
        isEnabled: () -> Boolean
    ) {
        val item = parent.findViewById<View>(itemId)
        val icon = item.findViewById<android.widget.ImageView>(R.id.shortcutIcon)
        val text = item.findViewById<android.widget.TextView>(R.id.shortcutLabel)
        text.text = label
        item.setOnClickListener { parent.context.startActivity(intent) }
        updateShortcut(parent, itemId, enabledRes, disabledRes, isEnabled)
    }

    private fun updateShortcut(
        parent: View,
        itemId: Int,
        enabledRes: Int,
        disabledRes: Int,
        isEnabled: () -> Boolean
    ) {
        val item = parent.findViewById<View>(itemId)
        val icon = item.findViewById<android.widget.ImageView>(R.id.shortcutIcon)
        val enabled = isEnabled()
        val iconRes = if (enabled) enabledRes else disabledRes
        val color = if (enabled) "#2196F3" else "#BDBDBD"
        icon.setImageResource(iconRes)
        val bg = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(android.graphics.Color.parseColor(color))
        }
        icon.background = bg
    }

    private fun isWifiEnabled(): Boolean {
        return try {
            val act = activity ?: return false
            val wifiManager = act.applicationContext.getSystemService(Context.WIFI_SERVICE)
            (wifiManager as? WifiManager)?.isWifiEnabled == true
        } catch (_: Exception) {
            false
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        return try {
            val bluetoothManager = (activity ?: return false).getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
            bluetoothManager?.adapter?.isEnabled == true
        } catch (_: Exception) {
            false
        }
    }

    private fun isCellularDataEnabled(): Boolean {
        return try {
            val cm = (activity ?: return false).getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val networks = cm?.allNetworks ?: return false
            networks.any { network ->
                cm.getNetworkCapabilities(network)?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) == true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun isAirplaneModeOn(): Boolean {
        return try {
            val act = activity ?: return false
            Settings.Global.getInt(act.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
        } catch (_: Exception) {
            false
        }
    }
}
