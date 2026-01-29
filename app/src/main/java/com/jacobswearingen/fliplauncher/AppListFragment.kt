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
import androidx.navigation.fragment.findNavController

class AppListFragment : Fragment(R.layout.fragment_app_list) {

    private val pm by lazy { requireContext().packageManager }
    private val apps by lazy {
        getInstalledApps(pm).sortedBy { it.loadLabel(pm).toString().lowercase() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<ListView>(R.id.appList)
        listView.adapter = AppListAdapter()
        listView.setOnItemClickListener { _, _, position, _ ->
            val info = apps[position]
            pm.getLaunchIntentForPackage(info.activityInfo.packageName)?.let { startActivity(it) }
            findNavController().popBackStack()
        }
    }

    private fun getInstalledApps(pm: PackageManager): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
    }

    private inner class AppListAdapter : BaseAdapter() {
        private val defaultIcon by lazy {
            requireContext().getDrawable(android.R.drawable.sym_def_app_icon)
        }

        override fun getCount() = apps.size
        override fun getItem(position: Int) = apps[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val info = apps[position]
            val v = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.item_app_list, parent, false)
            v.findViewById<TextView>(R.id.appLabel).text = info.loadLabel(pm)
            val icon = try {
                info.loadIcon(pm)
            } catch (_: Exception) {
                defaultIcon
            }
            v.findViewById<ImageView>(R.id.appIcon).setImageDrawable(icon)
            return v
        }
    }
}