package com.jacobswearingen.fliplauncher;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AppListFragment extends Fragment implements KeyEventHandler {
    private static final int GRID_COLUMN_COUNT = 3;

    private RecyclerView appListView;
    private TextView toggleLabel;
    private AppListViewModel viewModel;
    private AppListAdapter adapter;
    private PackageManager packageManager;
    private Set<String> hiddenPackages = Collections.emptySet();
    private Map<String, Drawable> iconCache = Collections.emptyMap();

    public AppListFragment() {
        super(R.layout.fragment_app_list);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageManager = requireContext().getPackageManager();
        viewModel = new ViewModelProvider(requireActivity()).get(AppListViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appListView = view.findViewById(R.id.appListView);
        toggleLabel = view.findViewById(R.id.textViewToggleLayout);
        appListView.setHasFixedSize(true);
        appListView.setLayoutManager(new GridLayoutManager(requireContext(), GRID_COLUMN_COUNT));
        adapter = new AppListAdapter();
        appListView.setAdapter(adapter);

        viewModel.getFilteredApps().observe(getViewLifecycleOwner(), apps -> {
            adapter.submitList(apps);
            focusFirstItem();
        });
        viewModel.getIconCache().observe(getViewLifecycleOwner(), cache ->
                iconCache = cache != null ? cache : Collections.emptyMap());
        viewModel.getHiddenPackages().observe(getViewLifecycleOwner(), pkgs ->
                hiddenPackages = pkgs != null ? pkgs : Collections.emptySet());
        viewModel.getShowingHidden().observe(getViewLifecycleOwner(), isShowingHidden -> {
            if (toggleLabel != null) {
                toggleLabel.setText(isShowingHidden ? "Normal" : "Hidden");
            }
        });
    }

    private void focusFirstItem() {
        if (appListView == null) return;
        appListView.post(() -> {
            appListView.scrollToPosition(0);
            RecyclerView.ViewHolder holder = appListView.findViewHolderForAdapterPosition(0);
            if (holder != null) holder.itemView.requestFocus();
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
            showAppMenu();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT) {
            viewModel.toggleShowHidden();
            return true;
        }
        return false;
    }

    private void showAppMenu() {
        View focused = appListView.getFocusedChild();
        if (focused == null) return;
        int pos = appListView.getChildAdapterPosition(focused);
        if (pos == RecyclerView.NO_POSITION) return;
        LauncherActivityInfo info = adapter.getAppItem(pos);
        if (info == null) return;
        String pkg = info.getApplicationInfo().packageName;
        boolean isHidden = hiddenPackages.contains(pkg);
        new AlertDialog.Builder(requireContext())
                .setTitle(info.getLabel())
                .setItems(new String[]{isHidden ? "Unhide" : "Hide"}, (dialog, which) ->
                        viewModel.toggleHidden(pkg))
                .show();
    }

    private final class AppListAdapter extends ListAdapter<LauncherActivityInfo, AppListAdapter.AppViewHolder> {
        @Nullable
        public LauncherActivityInfo getAppItem(int position) {
            if (position < 0 || position >= getCurrentList().size()) return null;
            return getItem(position);
        }

        AppListAdapter() {
            super(new DiffUtil.ItemCallback<LauncherActivityInfo>() {
                @Override
                public boolean areItemsTheSame(@NonNull LauncherActivityInfo oldItem, @NonNull LauncherActivityInfo newItem) {
                    return oldItem.getApplicationInfo().packageName.equals(newItem.getApplicationInfo().packageName);
                }

                @Override
                public boolean areContentsTheSame(@NonNull LauncherActivityInfo oldItem, @NonNull LauncherActivityInfo newItem) {
                    return oldItem.getLabel().toString().equals(newItem.getLabel().toString());
                }
            });
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
            LauncherActivityInfo info = getItem(position);
            CharSequence label = info.getLabel();
            holder.label.setText(label);
            holder.icon.setContentDescription(label);
            Drawable icon = iconCache.get(info.getApplicationInfo().packageName);
            holder.icon.setImageDrawable(icon != null ? icon : info.getBadgedIcon(0));
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
                        Intent launchIntent = packageManager.getLaunchIntentForPackage(
                                getItem(pos).getApplicationInfo().packageName);
                        if (launchIntent != null) {
                            startActivity(launchIntent);
                            NavHostFragment.findNavController(AppListFragment.this)
                                    .popBackStack(R.id.mainFragment, false);
                        }
                    } catch (Exception ignored) {
                    }
                });
            }
        }
    }
}

