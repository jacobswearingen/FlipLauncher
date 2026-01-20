package com.jacobswearingen.fliplauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class NotificationsFragment : Fragment(R.layout.fragment_notifications),
    KeyEventHandler, NotificationData.Listener {

    private var selectedNotificationIndex = 0
    private lateinit var listView: ListView
    private lateinit var adapter: NotificationAdapter
    private var notifications: MutableList<NotificationEntry> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.notificationList)
        notifications.clear()
        notifications.addAll(NotificationData.getAll())
        adapter = NotificationAdapter()
        listView.adapter = adapter
        setupListViewListeners()
        NotificationData.addListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        NotificationData.removeListener(this)
    }

    override fun onNotificationDataChanged() {
        notifications.clear()
        notifications.addAll(NotificationData.getAll())
        adapter.notifyDataSetChanged()
        if (notifications.isNotEmpty()) {
            selectedNotificationIndex = selectedNotificationIndex.coerceAtMost(notifications.size - 1)
            listView.setSelection(selectedNotificationIndex)
        }
    }

    private fun setupListViewListeners() {
        listView.setOnItemClickListener { _, _, position, _ ->
            val entry = notifications[position]
            requireContext().packageManager.getLaunchIntentForPackage(entry.packageName)
                ?.let { startActivity(it) }
                ?: Toast.makeText(requireContext(), "Cannot launch app", Toast.LENGTH_SHORT).show()
        }

        listView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedNotificationIndex = position
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_SOFT_LEFT || keyCode == 139) {
            val position = listView.selectedItemPosition
            if (position in notifications.indices) {
                val key = NotificationData.getKeyAt(position)
                if (key != null) {
                    NotificationService.cancelNotificationByKey(key)
                    // No need to call onNotificationDataChanged or setupList, listener will update UI
                }
            }
            return true
        }
        return false
    }

    inner class NotificationAdapter : BaseAdapter() {
        override fun getCount() = notifications.size
        override fun getItem(position: Int) = notifications[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val v = convertView ?: LayoutInflater.from(parent?.context ?: requireContext())
                .inflate(R.layout.notification_list_item, parent, false)
            v.findViewById<ImageView>(R.id.appIcon).apply {
                try {
                    setImageDrawable(requireContext().packageManager.getApplicationIcon(getItem(position).packageName))
                } catch (_: Exception) {
                    setImageResource(android.R.drawable.sym_def_app_icon)
                }
            }
            v.findViewById<TextView>(R.id.notificationText).text = getItem(position).text
            return v
        }
    }
}