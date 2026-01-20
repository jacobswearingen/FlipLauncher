package com.jacobswearingen.fliplauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class AppListFragment : Fragment(R.layout.fragment_app_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pm = requireContext().packageManager
        val apps = getInstalledApps(pm).sortedBy {
            it.loadLabel(pm).toString().lowercase()
        }
        val listView = view.findViewById<ListView>(R.id.appList)
        val adapter = object : BaseAdapter() {
            override fun getCount() = apps.size
            override fun getItem(position: Int) = apps[position]
            override fun getItemId(position: Int) = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val info = apps[position]
                val v = convertView ?: LayoutInflater.from(parent?.context ?: requireContext())
                    .inflate(R.layout.app_list_item, parent, false)
                val labelView = v.findViewById<TextView>(R.id.appLabel)
                labelView.text = info.loadLabel(pm)
                val icon = try {
                    info.loadIcon(pm)
                } catch (e: Exception) {
                    requireContext().getDrawable(android.R.drawable.sym_def_app_icon)
                }
                labelView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
                return v
            }
        }
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val info = apps[position]
            requireContext().packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
                ?.let { startActivity(it) }
        }
    }

    private fun getInstalledApps(pm: PackageManager): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
    }
}