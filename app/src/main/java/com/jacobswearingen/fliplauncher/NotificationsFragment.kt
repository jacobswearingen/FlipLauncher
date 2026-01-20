package com.jacobswearingen.fliplauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private var selectedNotificationIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<ListView>(R.id.notificationList)
        val notifications = NotificationData.getAll()

        val adapter = object : BaseAdapter() {
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
        listView.adapter = adapter

        // Restore selection if needed
        if (notifications.isNotEmpty()) {
            selectedNotificationIndex = selectedNotificationIndex.coerceAtMost(notifications.size - 1)
            listView.setSelection(selectedNotificationIndex)
        }

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
}