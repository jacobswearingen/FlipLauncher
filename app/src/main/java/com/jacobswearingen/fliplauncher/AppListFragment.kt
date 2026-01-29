package com.jacobswearingen.fliplauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.ImageView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

class AppListFragment : Fragment(R.layout.fragment_app_list) {

    private val viewModel: AppListViewModel by viewModels()
    private lateinit var adapter: AppListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<ListView>(R.id.appList)
        val pm = requireContext().packageManager
        adapter = AppListAdapter(pm)
        listView.adapter = adapter

        viewModel.apps.observe(viewLifecycleOwner) { loadedApps ->
            adapter.submitList(loadedApps)
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val info = adapter.getItem(position)
            pm.getLaunchIntentForPackage(info.activityInfo.packageName)?.let {
                startActivity(it)
                findNavController().popBackStack(R.id.mainFragment, false)
            }
        }
    }

    private inner class AppListAdapter(
        private val pm: android.content.pm.PackageManager
    ) : BaseAdapter() {
        private var apps: List<android.content.pm.ResolveInfo> = emptyList()
        private val defaultIcon by lazy {
            requireContext().getDrawable(android.R.drawable.sym_def_app_icon)
        }

        fun submitList(newApps: List<android.content.pm.ResolveInfo>) {
            apps = newApps
            notifyDataSetChanged()
        }

        override fun getCount() = apps.size
        override fun getItem(position: Int) = apps[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val holder: ViewHolder
            val view: View

            if (convertView == null) {
                view = LayoutInflater.from(parent?.context).inflate(R.layout.item_app_list, parent, false)
                holder = ViewHolder(
                    view.findViewById(R.id.appLabel),
                    view.findViewById(R.id.appIcon)
                )
                view.tag = holder
            } else {
                view = convertView
                holder = view.tag as ViewHolder
            }

            val info = apps[position]
            holder.label.text = info.loadLabel(pm)
            val icon = try {
                info.loadIcon(pm)
            } catch (_: Exception) {
                defaultIcon
            }
            holder.icon.setImageDrawable(icon)
            return view
        }
    }

    private data class ViewHolder(
        val label: TextView,
        val icon: ImageView
    )
}