package com.jacobswearingen.fliplauncher

import android.app.Notification
import android.service.notification.StatusBarNotification
import androidx.navigation.fragment.findNavController
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class NotificationsFragment : Fragment(R.layout.fragment_notifications),
    KeyEventHandler {

    private var selectedNotificationIndex = 0
    private lateinit var listView: ListView
    private lateinit var adapter: NotificationAdapter
    private var notifications: MutableList<StatusBarNotification> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.notificationList)
        adapter = NotificationAdapter()
        listView.adapter = adapter
        refreshNotifications()
        setupListViewListeners()
    }

    // No longer need to remove listener

    private fun refreshNotifications() {
        notifications.clear()
        notifications.addAll(NotificationService.getActiveNotifications())
        adapter.notifyDataSetChanged()
        if (notifications.isNotEmpty()) {
            selectedNotificationIndex = selectedNotificationIndex.coerceAtMost(notifications.size - 1)
            listView.setSelection(selectedNotificationIndex)
        }
    }

    private fun setupListViewListeners() {
        listView.setOnItemClickListener { _, _, position, _ ->
            val sbn = notifications[position]
            val intent = sbn.notification.contentIntent
            val navController = findNavController()
            if (intent != null) {
                try {
                    intent.send()
                    navController.popBackStack() // Go back to main fragment
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Cannot perform action", Toast.LENGTH_SHORT).show()
                }
            } else {
                requireContext().packageManager.getLaunchIntentForPackage(sbn.packageName)
                    ?.let {
                        startActivity(it)
                        navController.popBackStack() // Go back to main fragment
                    }
                    ?: Toast.makeText(requireContext(), "Cannot launch app", Toast.LENGTH_SHORT).show()
            }
        }

        listView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedNotificationIndex = position
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        when (keyCode) {
            android.view.KeyEvent.KEYCODE_SOFT_LEFT, 139 -> {
                val position = listView.selectedItemPosition
                if (position in notifications.indices) {
                    val key = notifications[position].key
                    if (key != null) {
                        NotificationService.cancelNotificationByKey(key)
                        // UI will update via refreshNotifications()
                        refreshNotifications()
                    }
                }
                return true
            }
            android.view.KeyEvent.KEYCODE_SOFT_RIGHT, 48 -> {
                NotificationService.clearAllSystemNotifications()
                refreshNotifications()
                return true
            }
        }
        return false
    }

    inner class NotificationAdapter : BaseAdapter() {
        override fun getCount() = notifications.size
        override fun getItem(position: Int) = notifications[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val v = convertView ?: LayoutInflater.from(parent?.context ?: requireContext())
                .inflate(R.layout.item_notification_list, parent, false)
            val sbn = getItem(position)

            // Set app icon
            v.findViewById<ImageView>(R.id.appIcon).apply {
                try {
                    setImageDrawable(requireContext().packageManager.getApplicationIcon(sbn.packageName))
                } catch (_: Exception) {
                    setImageResource(android.R.drawable.sym_def_app_icon)
                }
            }

            // Use Notification.extras for title and text
            val extras = sbn.notification.extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            v.findViewById<TextView>(R.id.notificationText).text = "$title: $text"
            return v
        }
    }
}