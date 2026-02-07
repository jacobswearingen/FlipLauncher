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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // WiFi toggle
        val wifiSwitch = view.findViewById<Switch>(R.id.switchWifi)
        wifiSwitch?.isChecked = if (activity != null) isWifiEnabled() else false
        wifiSwitch?.setOnCheckedChangeListener { _, _ ->
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }

        // Bluetooth toggle
        val bluetoothSwitch = view.findViewById<Switch>(R.id.switchBluetooth)
        bluetoothSwitch?.isChecked = isBluetoothEnabled()
        bluetoothSwitch?.setOnCheckedChangeListener { _, _ ->
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        }

        // Cellular Data toggle
        val cellularSwitch = view.findViewById<Switch>(R.id.switchCellular)
        cellularSwitch?.isChecked = isCellularDataEnabled()
        cellularSwitch?.setOnCheckedChangeListener { _, _ ->
            val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
            startActivity(intent)
        }

        // Airplane Mode toggle
        val airplaneSwitch = view.findViewById<Switch>(R.id.switchAirplane)
        airplaneSwitch?.isChecked = isAirplaneModeOn()
        airplaneSwitch?.setOnCheckedChangeListener { _, _ ->
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            startActivity(intent)
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
        return try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter?.isEnabled == true
        } catch (e: Exception) {
            false
        }
    }

    private fun isCellularDataEnabled(): Boolean {
        return try {
            val cm = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val networkInfo = cm?.getNetworkInfo(android.net.ConnectivityManager.TYPE_MOBILE)
            networkInfo != null && networkInfo.isConnected
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
