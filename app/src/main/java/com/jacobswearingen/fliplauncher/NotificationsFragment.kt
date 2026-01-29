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
import androidx.fragment.app.viewModels
import android.graphics.drawable.Drawable


class NotificationsFragment : Fragment(R.layout.fragment_notifications), KeyEventHandler {
    private var selectedNotificationIndex = 0
    private lateinit var listView: ListView
    private lateinit var adapter: NotificationAdapter
    private val viewModel: NotificationsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.notificationList)
        adapter = NotificationAdapter()
        listView.adapter = adapter

        viewModel.notifications.observe(viewLifecycleOwner) { loadedNotifications ->
            adapter.submitList(loadedNotifications)
            if (loadedNotifications.isNotEmpty()) {
                selectedNotificationIndex = selectedNotificationIndex.coerceAtMost(loadedNotifications.size - 1)
                listView.setSelection(selectedNotificationIndex)
            }
        }
        setupListViewListeners()
    }

    private fun setupListViewListeners() {
        listView.setOnItemClickListener { _, _, position, _ ->
            val sbn = adapter.getItem(position)
            val intent = sbn.notification.contentIntent
            val navController = findNavController()
            if (intent != null) {
                try {
                    intent.send()
                    navController.popBackStack(R.id.mainFragment, false)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Cannot perform action", Toast.LENGTH_SHORT).show()
                }
            } else {
                requireContext().packageManager.getLaunchIntentForPackage(sbn.packageName)
                    ?.let {
                        startActivity(it)
                        navController.popBackStack(R.id.mainFragment, false)
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
                val sbn = adapter.getItemOrNull(position)
                val key = sbn?.key
                if (key != null) {
                    viewModel.cancelNotification(key)
                }
                return true
            }
            android.view.KeyEvent.KEYCODE_SOFT_RIGHT, 48 -> {
                viewModel.clearAllNotifications()
                return true
            }
        }
        return false
    }

    private inner class NotificationAdapter : BaseAdapter() {
        private var items: List<StatusBarNotification> = emptyList()

        fun submitList(newList: List<StatusBarNotification>) {
            items = newList
            notifyDataSetChanged()
        }

        fun getItemOrNull(position: Int): StatusBarNotification? =
            if (position in items.indices) items[position] else null

        override fun getCount() = items.size
        override fun getItem(position: Int) = items[position]
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

            v.findViewById<TextView>(R.id.notificationTimestamp).text =
                android.text.format.DateUtils.getRelativeTimeSpanString(
                    sbn.postTime,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.MINUTE_IN_MILLIS,
                    android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE
                )

            return v
        }
    }
    private data class ViewHolder(
        val appIcon: ImageView,
        val notificationText: TextView,
        val notificationTimestamp: TextView
    )
}