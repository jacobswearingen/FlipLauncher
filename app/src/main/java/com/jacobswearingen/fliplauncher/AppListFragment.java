package com.jacobswearingen.fliplauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListFragment extends Fragment implements KeyEventHandler {
    private static final int GRID_COLUMN_COUNT = 3;

    private RecyclerView appListView;
    private AppListViewModel viewModel;
    private SharedPreferences prefs;
    private static final String KEY_SHOWING_GRID = "showing_grid";
    private AppListAdapter adapter;
    private PackageManager packageManager;

    private boolean isShowingGrid() {
        return prefs.getBoolean(KEY_SHOWING_GRID, true);
    }
    private void setShowingGrid(boolean value) {
        prefs.edit().putBoolean(KEY_SHOWING_GRID, value).apply();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences("applist_prefs", Context.MODE_PRIVATE);
        packageManager = requireContext().getPackageManager();
        viewModel = new ViewModelProvider(this).get(AppListViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appListView = view.findViewById(R.id.appListView);
        appListView.setHasFixedSize(true);
        updateLayoutManager();
        adapter = new AppListAdapter();
        appListView.setAdapter(adapter);
        viewModel.getApps().observe(getViewLifecycleOwner(), this::populateAndFocus);
        populateAndFocus(viewModel.getApps().getValue());
    }

    private void populateAndFocus(List<ResolveInfo> apps) {
        populateApps(apps);
        focusFirstItem();
    }

    private void focusFirstItem() {
        if (appListView == null) {
            return;
        }
        appListView.post(() -> {
            appListView.scrollToPosition(0);
            View firstItem = getFirstItemView();
            if (firstItem != null) {
                firstItem.requestFocus();
            }
        });
    }

    private View getFirstItemView() {
        if (appListView == null || adapter == null || adapter.getItemCount() == 0) {
            return null;
        }
        RecyclerView.ViewHolder firstHolder = appListView.findViewHolderForAdapterPosition(0);
        if (firstHolder != null) {
            return firstHolder.itemView;
        }
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT) {
            setShowingGrid(!isShowingGrid());
            updateLayoutManager();
            populateAndFocus(viewModel.getApps().getValue());
            return true;
        }
        return false;
    }

    private void populateApps(List<ResolveInfo> apps) {
        if (adapter == null) {
            return;
        }
        adapter.setApps(apps == null ? Collections.emptyList() : apps);
    }

    private void updateLayoutManager() {
        if (appListView == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager;
        if (isShowingGrid()) {
            layoutManager = new GridLayoutManager(requireContext(), GRID_COLUMN_COUNT);
        } else {
            layoutManager = new LinearLayoutManager(requireContext());
        }
        appListView.setLayoutManager(layoutManager);
    }

    private final class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
        private final List<ResolveInfo> apps = new ArrayList<>();

        void setApps(List<ResolveInfo> newApps) {
            apps.clear();
            apps.addAll(newApps);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return isShowingGrid() ? R.layout.item_app_grid : R.layout.item_app_list;
        }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new AppViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            holder.bind(apps.get(position));
        }

        @Override
        public int getItemCount() {
            return apps.size();
        }

        final class AppViewHolder extends RecyclerView.ViewHolder {
            private final TextView label;
            private final ImageView icon;

            AppViewHolder(@NonNull View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.appLabel);
                icon = itemView.findViewById(R.id.appIcon);
            }

            void bind(ResolveInfo info) {
                CharSequence labelText = info.loadLabel(packageManager);
                label.setText(labelText);
                try {
                    icon.setImageDrawable(info.loadIcon(packageManager));
                    icon.setContentDescription(labelText);
                } catch (Exception e) {
                    icon.setImageDrawable(ContextCompat.getDrawable(requireContext(), android.R.drawable.sym_def_app_icon));
                    icon.setContentDescription(labelText);
                }

                itemView.setOnClickListener(v -> {
                    try {
                        Intent launchIntent = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName);
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
