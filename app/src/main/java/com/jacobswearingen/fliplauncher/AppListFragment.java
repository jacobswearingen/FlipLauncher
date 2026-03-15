package com.jacobswearingen.fliplauncher;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.List;

public class AppListFragment extends Fragment implements KeyEventHandler {
    private ViewGroup appContainer;
    private AppListViewModel viewModel;
    private SharedPreferences prefs;
    private static final String KEY_SHOWING_GRID = "showing_grid";

    private boolean isShowingGrid() {
        return prefs.getBoolean(KEY_SHOWING_GRID, false);
    }
    private void setShowingGrid(boolean value) {
        prefs.edit().putBoolean(KEY_SHOWING_GRID, value).apply();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences("applist_prefs", Context.MODE_PRIVATE);
        viewModel = new ViewModelProvider(this).get(AppListViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appContainer = view.findViewById(R.id.appContainer);
        viewModel.getApps().observe(getViewLifecycleOwner(), this::populateAndFocus);
        populateAndFocus(viewModel.getApps().getValue());
    }

    private void populateAndFocus(List<ResolveInfo> apps) {
        populateApps(apps);
        focusFirstItem();
    }

    private void focusFirstItem() {
        appContainer.post(() -> {
            View firstItem = getFirstItemView();
            if (firstItem != null) firstItem.requestFocus();
        });
    }

    private View getFirstItemView() {
        if (appContainer.getChildCount() > 0) {
            ViewGroup firstRow = (ViewGroup) appContainer.getChildAt(0);
            if (firstRow != null && firstRow.getChildCount() > 0) {
                return firstRow.getChildAt(0);
            }
        }
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT || keyCode == 48) {
            setShowingGrid(!isShowingGrid());
            populateAndFocus(viewModel.getApps().getValue());
            return true;
        }
        return false;
    }

    private void populateApps(List<ResolveInfo> apps) {
        if (appContainer == null) return;
        appContainer.removeAllViews();
        List<ResolveInfo> appList = (apps == null) ? java.util.Collections.emptyList() : apps;
        boolean grid = isShowingGrid();
        int columns = grid ? 3 : 1;
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        PackageManager pm = requireContext().getPackageManager();
        int rowCount = (int) Math.ceil(appList.size() / (float) columns);
        for (int row = 0; row < rowCount; row++) {
            LinearLayout rowLayout = new LinearLayout(requireContext());
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            for (int col = 0; col < columns; col++) {
                int index = row * columns + col;
                if (index < appList.size()) {
                    addAppItem(rowLayout, appList.get(index), inflater, pm, grid);
                } else if (grid) {
                    addGridSpacer(rowLayout);
                }
            }
            appContainer.addView(rowLayout);
        }
    }

    private void addAppItem(LinearLayout rowLayout, ResolveInfo info, LayoutInflater inflater, PackageManager pm, boolean isGrid) {
        View itemView = inflater.inflate(isGrid ? R.layout.item_app_grid : R.layout.item_app_list, rowLayout, false);
        if (isGrid) {
            // Set layout_weight so all columns are evenly distributed
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            itemView.setLayoutParams(params);
        }
        TextView label = itemView.findViewById(R.id.appLabel);
        ImageView icon = itemView.findViewById(R.id.appIcon);
        if (label != null) label.setText(info.loadLabel(pm));
        try {
            if (icon != null) icon.setImageDrawable(info.loadIcon(pm));
        } catch (Exception e) {
            if (icon != null) icon.setImageDrawable(ContextCompat.getDrawable(requireContext(), android.R.drawable.sym_def_app_icon));
        }
        itemView.setOnClickListener(v -> {
            try {
                Intent launchIntent = pm.getLaunchIntentForPackage(info.activityInfo.packageName);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    NavHostFragment.findNavController(AppListFragment.this).popBackStack(R.id.mainFragment, false);
                }
            } catch (Exception ignored) {}
        });
        rowLayout.addView(itemView);
    }

    private void addGridSpacer(LinearLayout rowLayout) {
        View spacer = new View(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0, 1f);
        spacer.setLayoutParams(params);
        rowLayout.addView(spacer);
    }
}
