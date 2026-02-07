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
    private data class ShortcutIcon(
        val viewId: Int,
        val enabledRes: Int,
        val disabledRes: Int,
        val isEnabled: () -> Boolean,
        val onClickIntent: Intent
    )

    private val icons by lazy {
        return@lazy listOf(
            ShortcutIcon(
                R.id.iconWifi,
                R.drawable.wifi_enabled,
                R.drawable.wifi_disabled,
                { isWifiEnabled() },
                Intent(Settings.ACTION_WIFI_SETTINGS)
            ),
            ShortcutIcon(
                R.id.iconBluetooth,
                R.drawable.bluetooth_enabled,
                R.drawable.bluetooth_disabled,
                { isBluetoothEnabled() },
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            ),
            ShortcutIcon(
                R.id.iconCellular,
                R.drawable.cell_data,
                R.drawable.cell_data, // same icon, just color changes
                { isCellularDataEnabled() },
                Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
            ),
            ShortcutIcon(
                R.id.iconAirplane,
                R.drawable.airplanemode_active,
                R.drawable.airplanemode_active, // same icon, just color changes
                { isAirplaneModeOn() },
                Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        icons.forEach { icon ->
            val imageView = view.findViewById<android.widget.ImageView>(icon.viewId)
            imageView?.setOnClickListener { startActivity(icon.onClickIntent) }
        }
        updateAllIcons(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { updateAllIcons(it) }
    }

    private fun updateAllIcons(view: View) {
        icons.forEach { icon ->
            val imageView = view.findViewById<android.widget.ImageView>(icon.viewId)
            updateIcon(imageView, icon.enabledRes, icon.disabledRes, icon.isEnabled())
        }
    }

    private fun updateIcon(
        imageView: android.widget.ImageView?,
        enabledRes: Int,
        disabledRes: Int,
        enabled: Boolean
    ) {
        imageView?.let {
            val (iconRes, color) = if (enabled) {
                enabledRes to "#2196F3"
            } else {
                disabledRes to "#BDBDBD"
            }
            it.setImageResource(iconRes)
            val bg = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(android.graphics.Color.parseColor(color))
            }
            it.background = bg
        }
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
