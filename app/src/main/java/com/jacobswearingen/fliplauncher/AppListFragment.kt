package com.jacobswearingen.fliplauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

class AppListFragment : Fragment(R.layout.fragment_app_list), KeyEventHandler {
    private var showingGrid = false
    private var recyclerView: RecyclerView? = null
    private val viewModel: AppListViewModel by viewModels()
    private lateinit var adapter: AppListAdapter

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_SOFT_RIGHT || keyCode == 48) {
            showingGrid = !showingGrid
            adapter.setGridMode(showingGrid)
            updateLayoutManager()
            return true
        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.appRecycler)
        val pm = requireContext().packageManager
        adapter = AppListAdapter(pm, showingGrid) { info ->
            pm.getLaunchIntentForPackage(info.activityInfo.packageName)?.let {
                startActivity(it)
                findNavController().popBackStack(R.id.mainFragment, false)
            }
        }
        recyclerView?.adapter = adapter
        updateLayoutManager()

        viewModel.apps.observe(viewLifecycleOwner) { loadedApps ->
            adapter.submitList(loadedApps)
        }
    }

    private fun updateLayoutManager() {
        recyclerView?.layoutManager = GridLayoutManager(requireContext(), if (showingGrid) 3 else 1)
    }

    private inner class AppListAdapter(
        private val pm: android.content.pm.PackageManager,
        private var gridMode: Boolean,
        private val onClick: (android.content.pm.ResolveInfo) -> Unit
    ) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {
        private var apps: List<android.content.pm.ResolveInfo> = emptyList()
        private val defaultIcon by lazy {
            requireContext().getDrawable(android.R.drawable.sym_def_app_icon)
        }

        fun setGridMode(isGrid: Boolean) {
            if (gridMode != isGrid) {
                gridMode = isGrid
                notifyDataSetChanged()
            }
        }

        fun submitList(newApps: List<android.content.pm.ResolveInfo>) {
            apps = newApps
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return if (gridMode) 1 else 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutId = if (viewType == 1) R.layout.item_app_grid else R.layout.item_app_list
            val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val info = apps[position]
            holder.label.text = info.loadLabel(pm)
            val icon = try {
                info.loadIcon(pm)
            } catch (_: Exception) {
                defaultIcon
            }
            holder.icon.setImageDrawable(icon)
            holder.itemView.setOnClickListener { onClick(info) }
        }

        override fun getItemCount() = apps.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val label: TextView = view.findViewById(R.id.appLabel)
            val icon: ImageView = view.findViewById(R.id.appIcon)
        }
    }
}