
package com.jacobswearingen.fliplauncher;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment implements KeyEventHandler {
    private int selectedNotificationIndex = 0;
    private ListView listView;
    private NotificationAdapter adapter;
    private NotificationsViewModel viewModel;
    private PackageManager packageManager;

    public NotificationsFragment() {
        super(R.layout.fragment_notifications);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        packageManager = requireContext().getPackageManager();
        listView = view.findViewById(R.id.notificationList);
        adapter = new NotificationAdapter();
        listView.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        viewModel.getNotifications().observe(getViewLifecycleOwner(), loadedNotifications -> {
            adapter.submitList(loadedNotifications);
            if (!loadedNotifications.isEmpty()) {
                selectedNotificationIndex = Math.min(selectedNotificationIndex, loadedNotifications.size() - 1);
                listView.setSelection(selectedNotificationIndex);
            }
        });
        setupListViewListeners();
    }

    private void setupListViewListeners() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            StatusBarNotification sbn = adapter.getItem(position);
            PendingIntent intent = sbn.getNotification().contentIntent;
            if (intent != null) {
                try {
                    intent.send();
                    NavHostFragment.findNavController(this).popBackStack(R.id.mainFragment, false);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Cannot perform action", Toast.LENGTH_SHORT).show();
                }
            } else {
                try {
                    Intent launchIntent = packageManager.getLaunchIntentForPackage(sbn.getPackageName());
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                        NavHostFragment.findNavController(this).popBackStack(R.id.mainFragment, false);
                    } else {
                        Toast.makeText(requireContext(), "Cannot launch app", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Cannot launch app", Toast.LENGTH_SHORT).show();
                }
            }
        });

        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedNotificationIndex = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_SOFT_LEFT) {
            int position = listView.getSelectedItemPosition();
            StatusBarNotification sbn = adapter.getItemOrNull(position);
            if (sbn != null) {
                viewModel.cancelNotification(sbn.getKey());
            }
            return true;
        } else if (keyCode == android.view.KeyEvent.KEYCODE_SOFT_RIGHT) {
            viewModel.clearAllNotifications();
            return true;
        }
        return false;
    }

    private class NotificationAdapter extends BaseAdapter {
        private List<StatusBarNotification> items = Collections.emptyList();
        private final Map<String, Drawable> iconCache = new HashMap<>();

        void submitList(List<StatusBarNotification> newList) {
            items = newList != null ? newList : Collections.emptyList();
            notifyDataSetChanged();
        }

        StatusBarNotification getItemOrNull(int position) {
            return (position >= 0 && position < items.size()) ? items.get(position) : null;
        }

        @Override
        public int getCount() { return items.size(); }

        @Override
        public StatusBarNotification getItem(int position) { return items.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;
            if (v == null) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_list, parent, false);
                holder = new ViewHolder(v);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            StatusBarNotification sbn = getItem(position);
            holder.appIcon.setImageDrawable(getAppIcon(sbn.getPackageName()));
            Notification notification = sbn.getNotification();
            CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "");
            CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT, "");
            holder.notificationText.setText(title + ": " + text);
            holder.notificationTimestamp.setText(DateUtils.getRelativeTimeSpanString(
                    sbn.getPostTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            ));
            return v;
        }

        private Drawable getAppIcon(String packageName) {
            Drawable cachedIcon = iconCache.get(packageName);
            if (cachedIcon != null) {
                return cachedIcon;
            }
            Drawable icon;
            try {
                icon = packageManager.getApplicationIcon(packageName);
            } catch (Exception e) {
                icon = androidx.core.content.ContextCompat.getDrawable(requireContext(), android.R.drawable.sym_def_app_icon);
            }
            iconCache.put(packageName, icon);
            return icon;
        }

        private static final class ViewHolder {
            private final ImageView appIcon;
            private final TextView notificationText;
            private final TextView notificationTimestamp;

            private ViewHolder(View itemView) {
                appIcon = itemView.findViewById(R.id.appIcon);
                notificationText = itemView.findViewById(R.id.notificationText);
                notificationTimestamp = itemView.findViewById(R.id.notificationTimestamp);
            }
        }
    }
}
