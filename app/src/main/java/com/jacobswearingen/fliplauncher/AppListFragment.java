package com.jacobswearingen.fliplauncher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AppListFragment extends Fragment implements KeyEventHandler {
    private static final int GRID_COLUMN_COUNT = 3;
    private static final String KEY_HIDDEN_APPS = "hidden_apps";

    private RecyclerView appListView;
    private TextView toggleLabel;
    private AppListViewModel viewModel;
    private SharedPreferences prefs;
    private AppListAdapter adapter;
    private PackageManager packageManager;
    private List<AppInfo> allApps = Collections.emptyList();
    private Set<String> hiddenApps = new HashSet<>();
    private boolean showingHidden = false;

    public AppListFragment() {
        super(R.layout.fragment_app_list);
    }

    private void loadHiddenApps() {
        hiddenApps = new HashSet<>(prefs.getStringSet(KEY_HIDDEN_APPS, Collections.emptySet()));
    }

    private void setHiddenState(String pkg, boolean hide) {
        if (hide) hiddenApps.add(pkg);
        else hiddenApps.remove(pkg);
        prefs.edit().putStringSet(KEY_HIDDEN_APPS, hiddenApps).apply();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences("applist_prefs", Context.MODE_PRIVATE);
        packageManager = requireContext().getPackageManager();
        viewModel = new ViewModelProvider(this).get(AppListViewModel.class);
        loadHiddenApps();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appListView = view.findViewById(R.id.appListView);
        toggleLabel = view.findViewById(R.id.textViewToggleLayout);
        updateToggleLabel();
        appListView.setHasFixedSize(true);
        appListView.setLayoutManager(new GridLayoutManager(requireContext(), GRID_COLUMN_COUNT));
        adapter = new AppListAdapter();
        appListView.setAdapter(adapter);
        viewModel.getApps().observe(getViewLifecycleOwner(), apps -> {
            allApps = apps != null ? apps : Collections.<AppInfo>emptyList();
            refreshList();
        });
    }

    private void refreshList() {
        if (adapter == null) return;
        List<AppInfo> displayed = allApps.stream()
                .filter(info -> hiddenApps.contains(info.getPackageName()) == showingHidden)
                .collect(Collectors.toList());
        adapter.setApps(displayed);
        focusFirstItem();
    }

    private void focusFirstItem() {
        if (appListView == null) return;
        appListView.post(() -> {
            appListView.scrollToPosition(0);
            RecyclerView.ViewHolder holder = appListView.findViewHolderForAdapterPosition(0);
            if (holder != null) holder.itemView.requestFocus();
        });
    }

    private void updateToggleLabel() {
        if (toggleLabel == null) return;
        toggleLabel.setText(showingHidden ? "Normal" : "Hidden");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
            showAppMenu();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT) {
            showingHidden = !showingHidden;
            updateToggleLabel();
            refreshList();
            return true;
        }
        return false;
    }

    private void showAppMenu() {
        View focused = appListView.getFocusedChild();
        if (focused == null) return;
        int pos = appListView.getChildAdapterPosition(focused);
        if (pos == RecyclerView.NO_POSITION) return;
        AppInfo info = adapter.getItem(pos);
        if (info == null) return;
        String pkg = info.getPackageName();
        boolean isHidden = hiddenApps.contains(pkg);
        new AlertDialog.Builder(requireContext())
                .setTitle(info.label)
                .setItems(new String[]{isHidden ? "Unhide" : "Hide"}, (dialog, which) -> {
                    setHiddenState(pkg, !isHidden);
                    refreshList();
                })
                .show();
    }

    private final class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
        private List<AppInfo> apps = new ArrayList<>();

        void setApps(List<AppInfo> newApps) {
            apps = new ArrayList<>(newApps);
            notifyDataSetChanged();
        }

        @Nullable
        AppInfo getItem(int position) {
            if (position < 0 || position >= apps.size()) return null;
            return apps.get(position);
        }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app_grid, parent, false);
            return new AppViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            AppInfo info = apps.get(position);
            holder.label.setText(info.label);
            holder.icon.setContentDescription(info.label);
            holder.icon.setImageDrawable(info.icon);
        }

        @Override
        public int getItemCount() {
            return apps.size();
        }

        final class AppViewHolder extends RecyclerView.ViewHolder {
            final TextView label;
            final ImageView icon;

            AppViewHolder(@NonNull View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.appLabel);
                icon = itemView.findViewById(R.id.appIcon);
                itemView.setOnClickListener(v -> {
                    int pos = getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return;
                    try {
                        Intent launchIntent = packageManager.getLaunchIntentForPackage(apps.get(pos).getPackageName());
                        if (launchIntent != null) {
                            startActivity(launchIntent);
                            NavHostFragment.findNavController(AppListFragment.this).popBackStack(R.id.mainFragment, false);
                        }
                    } catch (Exception ignored) {
                    }
                });
            }
        }
    }
}
