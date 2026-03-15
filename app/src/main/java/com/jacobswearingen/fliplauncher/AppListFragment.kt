
package com.jacobswearingen.fliplauncher

import android.os.Bundle
import android.content.Context
import android.view.*
import android.widget.TextView
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil

class AppListFragment : Fragment(R.layout.fragment_app_list), KeyEventHandler {
    private var showingGrid = false
    private lateinit var recyclerView: RecyclerView
    private val viewModel: AppListViewModel by viewModels()
    private lateinit var adapter: AppListAdapter
    private val prefs by lazy {
        requireContext().getSharedPreferences("applist_prefs", Context.MODE_PRIVATE)
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_SOFT_RIGHT || keyCode == 48) {
            showingGrid = !showingGrid
            prefs.edit().putBoolean(KEY_SHOWING_GRID, showingGrid).apply()
            adapter.notifyDataSetChanged()
            updateLayoutManager()
            return true
        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.appRecycler)
        val pm = requireContext().packageManager
        adapter = AppListAdapter(pm) { info ->
            pm.getLaunchIntentForPackage(info.activityInfo.packageName)?.let {
                startActivity(it)
                findNavController().popBackStack(R.id.mainFragment, false)
            }
        }
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        showingGrid = prefs.getBoolean(KEY_SHOWING_GRID, false)
        updateLayoutManager()
        viewModel.apps.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }
    companion object {
        private const val KEY_SHOWING_GRID = "showing_grid"
    }

    private fun updateLayoutManager() {
        recyclerView.layoutManager = GridLayoutManager(requireContext(), if (showingGrid) 3 else 1)
    }

    private inner class AppListAdapter(
        private val pm: android.content.pm.PackageManager,
        private val onClick: (android.content.pm.ResolveInfo) -> Unit
    ) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {
        private var apps: List<android.content.pm.ResolveInfo> = emptyList()

        fun submitList(newApps: List<android.content.pm.ResolveInfo>) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = apps.size
                override fun getNewListSize() = newApps.size
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    apps[oldItemPosition].activityInfo.packageName == newApps[newItemPosition].activityInfo.packageName
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    apps[oldItemPosition] == newApps[newItemPosition]
            })
            apps = newApps
            diffResult.dispatchUpdatesTo(this)
        }

        override fun getItemViewType(position: Int) = if (showingGrid) 1 else 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutId = if (viewType == 1) R.layout.item_app_grid else R.layout.item_app_list
            val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val info = apps[position]
            holder.label.text = info.loadLabel(pm)
            val icon = try { info.loadIcon(pm) } catch (_: Exception) {
                ContextCompat.getDrawable(requireContext(), android.R.drawable.sym_def_app_icon)
            }
            holder.icon.setImageDrawable(icon)
            holder.itemView.setOnClickListener { onClick(info) }
        }

        override fun getItemCount() = apps.size

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            holder.icon.setImageDrawable(null)
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val label: TextView = view.findViewById(R.id.appLabel)
            val icon: ImageView = view.findViewById(R.id.appIcon)
        }
    }
}