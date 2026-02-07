package com.jacobswearingen.fliplauncher


import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import android.bluetooth.BluetoothAdapter
import android.widget.Toast
import com.jacobswearingen.fliplauncher.R

class ShortcutsFragment : Fragment(R.layout.fragment_shortcuts) {
    private var wifiIcon: android.widget.ImageView? = null
    private var bluetoothIcon: android.widget.ImageView? = null
    private var cellularIcon: android.widget.ImageView? = null
    private var airplaneIcon: android.widget.ImageView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // WiFi icon clickable
        wifiIcon = view.findViewById(R.id.iconWifi)
        bluetoothIcon = view.findViewById(R.id.iconBluetooth)
        cellularIcon = view.findViewById(R.id.iconCellular)
        airplaneIcon = view.findViewById(R.id.iconAirplane)

        updateAllIcons()

        wifiIcon?.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }
        bluetoothIcon?.setOnClickListener {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        }
        cellularIcon?.setOnClickListener {
            val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
            startActivity(intent)
        }
        airplaneIcon?.setOnClickListener {
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateAllIcons()
    }

    private fun updateAllIcons() {
        updateWifiIcon(wifiIcon, isWifiEnabled())
        updateBluetoothIcon(bluetoothIcon, isBluetoothEnabled())
        updateCellularIcon(cellularIcon, isCellularDataEnabled())
        updateAirplaneIcon(airplaneIcon, isAirplaneModeOn())
    }

    private fun updateCellularIcon(cellularIcon: android.widget.ImageView?, enabled: Boolean) {
        cellularIcon?.let {
            if (enabled) {
                it.setImageResource(R.drawable.cell_data)
                val bg = android.graphics.drawable.GradientDrawable()
                bg.shape = android.graphics.drawable.GradientDrawable.OVAL
                bg.setColor(android.graphics.Color.parseColor("#2196F3"))
                it.background = bg
            } else {
                it.setImageResource(R.drawable.cell_data)
                val bg = android.graphics.drawable.GradientDrawable()
                bg.shape = android.graphics.drawable.GradientDrawable.OVAL
                bg.setColor(android.graphics.Color.parseColor("#BDBDBD"))
                it.background = bg
            }
        }
    }

    private fun updateAirplaneIcon(airplaneIcon: android.widget.ImageView?, enabled: Boolean) {
        airplaneIcon?.let {
            if (enabled) {
                it.setImageResource(R.drawable.airplanemode_active)
                val bg = android.graphics.drawable.GradientDrawable()
                bg.shape = android.graphics.drawable.GradientDrawable.OVAL
                bg.setColor(android.graphics.Color.parseColor("#2196F3"))
                it.background = bg
            } else {
                it.setImageResource(R.drawable.airplanemode_active)
                val bg = android.graphics.drawable.GradientDrawable()
                bg.shape = android.graphics.drawable.GradientDrawable.OVAL
                bg.setColor(android.graphics.Color.parseColor("#BDBDBD"))
                it.background = bg
            }
        }
    }

    private fun updateBluetoothIcon(bluetoothIcon: android.widget.ImageView?, enabled: Boolean) {
        bluetoothIcon?.let {
            if (enabled) {
                it.setImageResource(R.drawable.bluetooth_enabled)
                val bg = android.graphics.drawable.GradientDrawable()
                bg.shape = android.graphics.drawable.GradientDrawable.OVAL
                bg.setColor(android.graphics.Color.parseColor("#2196F3"))
                it.background = bg
            } else {
                it.setImageResource(R.drawable.bluetooth_disabled)
                val bg = android.graphics.drawable.GradientDrawable()
                bg.shape = android.graphics.drawable.GradientDrawable.OVAL
                bg.setColor(android.graphics.Color.parseColor("#BDBDBD"))
                it.background = bg
            }
        }
    }

    private fun updateWifiIcon(wifiIcon: android.widget.ImageView?, enabled: Boolean) {
        wifiIcon?.let {
            val context = it.context
            if (enabled) {
                it.setImageResource(R.drawable.wifi_enabled)
                val bg = android.graphics.drawable.GradientDrawable()
                bg.shape = android.graphics.drawable.GradientDrawable.OVAL
                bg.setColor(android.graphics.Color.parseColor("#2196F3"))
                it.background = bg
            } else {
                it.setImageResource(R.drawable.wifi_disabled)
                val bg = android.graphics.drawable.GradientDrawable()
                bg.shape = android.graphics.drawable.GradientDrawable.OVAL
                bg.setColor(android.graphics.Color.parseColor("#BDBDBD"))
                it.background = bg
            }
        }
    }

    private fun isWifiEnabled(): Boolean {
        return try {
            val act = activity ?: return false
            val wifiManager = act.applicationContext.getSystemService(Context.WIFI_SERVICE)
            (wifiManager as? WifiManager)?.isWifiEnabled == true
        } catch (e: Exception) {
            false
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        // Use BluetoothManager for API 18+
        return try {
            val context = activity ?: return false
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
            val bluetoothAdapter = bluetoothManager?.adapter
            bluetoothAdapter?.isEnabled == true
        } catch (e: Exception) {
            false
        }
    }

    private fun isCellularDataEnabled(): Boolean {
        // Use NetworkCapabilities for modern Android
        return try {
            val context = activity ?: return false
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val networks = cm?.allNetworks ?: return false
            for (network in networks) {
                val caps = cm.getNetworkCapabilities(network)
                if (caps != null && caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun isAirplaneModeOn(): Boolean {
        return try {
            val act = activity ?: return false
            Settings.Global.getInt(act.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
        } catch (e: Exception) {
            false
        }
    }
}
