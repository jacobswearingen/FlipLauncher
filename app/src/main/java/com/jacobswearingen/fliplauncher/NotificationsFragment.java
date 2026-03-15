
package com.jacobswearingen.fliplauncher;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
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
import java.util.List;

public class NotificationsFragment extends Fragment implements KeyEventHandler {
    private int selectedNotificationIndex = 0;
    private ListView listView;
    private NotificationAdapter adapter;
    private NotificationsViewModel viewModel;

    public NotificationsFragment() {
        super(R.layout.fragment_notifications);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                    Intent launchIntent = requireContext().getPackageManager().getLaunchIntentForPackage(sbn.getPackageName());
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
        if (keyCode == android.view.KeyEvent.KEYCODE_SOFT_LEFT || keyCode == 139) {
            int position = listView.getSelectedItemPosition();
            StatusBarNotification sbn = adapter.getItemOrNull(position);
            if (sbn != null) {
                viewModel.cancelNotification(sbn.getKey());
            }
            return true;
        } else if (keyCode == android.view.KeyEvent.KEYCODE_SOFT_RIGHT || keyCode == 48) {
            viewModel.clearAllNotifications();
            return true;
        }
        return false;
    }

    private class NotificationAdapter extends BaseAdapter {
        private List<StatusBarNotification> items = Collections.emptyList();

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
            View v = convertView != null ? convertView : LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_list, parent, false);
            StatusBarNotification sbn = getItem(position);
            ImageView appIcon = v.findViewById(R.id.appIcon);
            try {
                appIcon.setImageDrawable(requireContext().getPackageManager().getApplicationIcon(sbn.getPackageName()));
            } catch (Exception e) {
                appIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            }
            Notification notification = sbn.getNotification();
            CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE, "");
            CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT, "");
            TextView notificationText = v.findViewById(R.id.notificationText);
            notificationText.setText(title + ": " + text);
            TextView notificationTimestamp = v.findViewById(R.id.notificationTimestamp);
            notificationTimestamp.setText(DateUtils.getRelativeTimeSpanString(
                    sbn.getPostTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            ));
            return v;
        }
    }
}
