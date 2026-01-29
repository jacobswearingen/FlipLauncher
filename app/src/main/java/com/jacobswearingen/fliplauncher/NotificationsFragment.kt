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

class NotificationsFragment : Fragment(R.layout.fragment_notifications), KeyEventHandler {

    private var selectedNotificationIndex = 0
    private lateinit var listView: ListView
    private lateinit var adapter: NotificationAdapter
    private val notifications = mutableListOf<StatusBarNotification>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.notificationList)
        adapter = NotificationAdapter()
        listView.adapter = adapter
        refreshNotifications()
        setupListViewListeners()
    }

    private fun refreshNotifications() {
        notifications.clear()
        notifications.addAll(
            NotificationService.getActiveNotifications()
                .sortedByDescending { it.postTime }
        )
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
                    navController.popBackStack()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Cannot perform action", Toast.LENGTH_SHORT).show()
                }
            } else {
                requireContext().packageManager.getLaunchIntentForPackage(sbn.packageName)
                    ?.let {
                        startActivity(it)
                        navController.popBackStack()
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

    private inner class NotificationAdapter : BaseAdapter() {
        override fun getCount() = notifications.size
        override fun getItem(position: Int) = notifications[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val v = convertView ?: LayoutInflater.from(parent?.context ?: requireContext())
                .inflate(R.layout.item_notification_list, parent, false)
            val sbn = getItem(position)

            v.findViewById<ImageView>(R.id.appIcon).apply {
                try {
                    setImageDrawable(requireContext().packageManager.getApplicationIcon(sbn.packageName))
                } catch (_: Exception) {
                    setImageResource(android.R.drawable.sym_def_app_icon)
                }
            }

            with(sbn.notification.extras) {
                val title = getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                v.findViewById<TextView>(R.id.notificationText).text = "$title: $text"
            }

            // Format and show timestamp in 12-hour format (e.g., 3:45 PM)
            val timestampView = v.findViewById<TextView>(R.id.notificationTimestamp)
            val relativeTime = android.text.format.DateUtils.getRelativeTimeSpanString(
                sbn.postTime,
                System.currentTimeMillis(),
                android.text.format.DateUtils.MINUTE_IN_MILLIS,
                android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE
            )
            timestampView.text = relativeTime

            return v
        }
    }
}