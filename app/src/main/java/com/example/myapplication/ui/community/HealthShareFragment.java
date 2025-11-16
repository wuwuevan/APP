package com.example.myapplication.ui.community;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.HealthShareAdapter;
import com.example.myapplication.data.HealthShareEntry;
import com.example.myapplication.utils.SharedPreferencesUtil;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 独立的健康数据共享墙页面，支持本地增删改查操作。
 */
public class HealthShareFragment extends Fragment {

    private static final String KEY_HEALTH_ENTRIES = "community_health_entries_json";
    private static final String KEY_NEXT_HEALTH_ID = "community_next_health_id";

    private RecyclerView rvHealthShare;
    private TextView tvEmptyState;
    private Button btnAddHealthShare;
    private HealthShareAdapter healthShareAdapter;
    private SharedPreferencesUtil sharedPreferencesUtil;

    private final ArrayList<HealthShareEntry> healthEntries = new ArrayList<>();
    private int nextHealthEntryId = 1;
    private boolean seeded = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sharedPreferencesUtil = new SharedPreferencesUtil(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health_share, container, false);
        initViews(view);
        setupRecyclerView(view.getContext());
        restoreState();
        seedHealthDataIfNeeded();
        loadHealthEntries();
        return view;
    }

    private void initViews(@NonNull View view) {
        rvHealthShare = view.findViewById(R.id.recycler_health_data);
        tvEmptyState = view.findViewById(R.id.tv_health_empty_state);
        btnAddHealthShare = view.findViewById(R.id.btn_add_health_share);
        if (btnAddHealthShare != null) {
            btnAddHealthShare.setOnClickListener(v -> showHealthShareDialog(null));
        }
    }

    private void setupRecyclerView(@NonNull Context context) {
        if (rvHealthShare == null) {
            return;
        }
        rvHealthShare.setLayoutManager(new LinearLayoutManager(context));
        rvHealthShare.setHasFixedSize(true);
        healthShareAdapter = new HealthShareAdapter(new HealthShareAdapter.OnHealthEntryActionListener() {
            @Override
            public void onEdit(@NonNull HealthShareEntry entry) {
                if (canModifyEntry(entry)) {
                    showHealthShareDialog(entry);
                } else {
                    showToast(R.string.community_health_edit_not_allowed);
                }
            }

            @Override
            public void onDelete(@NonNull HealthShareEntry entry) {
                if (canModifyEntry(entry)) {
                    showDeleteHealthEntryDialog(entry);
                } else {
                    showToast(R.string.community_health_edit_not_allowed);
                }
            }
        });
        rvHealthShare.setAdapter(healthShareAdapter);
        updateAdapterUser();
    }

    private void showHealthShareDialog(@Nullable HealthShareEntry entry) {
        if (entry != null && !canModifyEntry(entry)) {
            showToast(R.string.community_health_edit_not_allowed);
            return;
        }
        if (!isAdded()) {
            return;
        }
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_dialog_health_share, null, false);
        EditText inputUsername = dialogView.findViewById(R.id.input_health_username);
        EditText inputMetric = dialogView.findViewById(R.id.input_health_metric);
        EditText inputValue = dialogView.findViewById(R.id.input_health_value);
        EditText inputNote = dialogView.findViewById(R.id.input_health_note);

        if (entry != null) {
            if (inputUsername != null) {
                inputUsername.setText(entry.getAuthorName());
            }
            if (inputMetric != null) {
                inputMetric.setText(entry.getMetricName());
            }
            if (inputValue != null) {
                inputValue.setText(entry.getMetricValue());
            }
            if (inputNote != null) {
                inputNote.setText(entry.getNote());
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(entry == null
                        ? R.string.community_health_share_dialog_title
                        : R.string.community_health_share_dialog_edit_title)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(entry == null ? R.string.publish : R.string.save, null)
                .create();

        dialog.setOnShowListener(d -> {
            View positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setOnClickListener(v -> {
                    String metric = inputMetric != null && inputMetric.getText() != null
                            ? inputMetric.getText().toString().trim() : "";
                    if (metric.isEmpty()) {
                        showToast(R.string.community_health_metric_required);
                        return;
                    }
                    String value = inputValue != null && inputValue.getText() != null
                            ? inputValue.getText().toString().trim() : "";
                    if (value.isEmpty()) {
                        showToast(R.string.community_health_value_required);
                        return;
                    }
                    String note = inputNote != null && inputNote.getText() != null
                            ? inputNote.getText().toString().trim() : "";
                    String username = inputUsername != null && inputUsername.getText() != null
                            ? inputUsername.getText().toString().trim() : "";
                    saveHealthEntry(entry, username, metric, value, note);
                    dialog.dismiss();
                });
            }
        });

        dialog.show();
    }

    private void saveHealthEntry(@Nullable HealthShareEntry original,
                                 @NonNull String username,
                                 @NonNull String metric,
                                 @NonNull String value,
                                 @NonNull String note) {
        String resolvedUsername = username;
        if (resolvedUsername.isEmpty()) {
            resolvedUsername = sharedPreferencesUtil != null
                    ? sharedPreferencesUtil.getCurrentUsername() : "";
        }
        if (resolvedUsername == null || resolvedUsername.isEmpty()) {
            resolvedUsername = getString(R.string.community_anonymous_user);
        }
        long userId = sharedPreferencesUtil != null
                ? sharedPreferencesUtil.getCurrentUserId() : -1L;
        long now = System.currentTimeMillis();
        if (original == null) {
            HealthShareEntry entry = new HealthShareEntry(userId, resolvedUsername,
                    metric, value, note.isEmpty() ? null : note, now);
            entry.setId(nextHealthEntryId++);
            healthEntries.add(0, entry);
        } else {
            original.setAuthorId(userId);
            original.setAuthorName(resolvedUsername);
            original.setMetricName(metric);
            original.setMetricValue(value);
            original.setNote(note.isEmpty() ? null : note);
            original.setUpdatedAt(now);
        }
        persistState();
        loadHealthEntries();
        showToast(R.string.community_health_saved);
    }

    private void showDeleteHealthEntryDialog(@NonNull HealthShareEntry entry) {
        if (!isAdded()) {
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(R.string.community_delete_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteHealthEntry(entry))
                .show();
    }

    private void deleteHealthEntry(@NonNull HealthShareEntry entry) {
        boolean removed = healthEntries.removeIf(e -> e.getId() == entry.getId());
        if (removed) {
            persistState();
            loadHealthEntries();
            showToast(R.string.community_health_deleted);
        }
    }

    private void loadHealthEntries() {
        if (healthShareAdapter == null) {
            return;
        }
        ArrayList<HealthShareEntry> snapshot = new ArrayList<>(healthEntries);
        snapshot.sort((o1, o2) -> Long.compare(o2.getUpdatedAt(), o1.getUpdatedAt()));
        healthShareAdapter.submitList(snapshot);
        if (tvEmptyState != null && rvHealthShare != null) {
            boolean empty = snapshot.isEmpty();
            tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvHealthShare.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    private void persistState() {
        if (sharedPreferencesUtil == null) {
            return;
        }
        JSONArray healthArray = new JSONArray();
        for (HealthShareEntry entry : healthEntries) {
            JSONObject object = new JSONObject();
            try {
                object.put("id", entry.getId());
                object.put("authorId", entry.getAuthorId());
                object.put("authorName", entry.getAuthorName());
                object.put("metricName", entry.getMetricName());
                object.put("metricValue", entry.getMetricValue());
                object.put("note", entry.getNote());
                object.put("updatedAt", entry.getUpdatedAt());
                healthArray.put(object);
            } catch (JSONException ignored) {
                // skip single row error
            }
        }
        sharedPreferencesUtil.putString(KEY_HEALTH_ENTRIES, healthArray.toString());
        sharedPreferencesUtil.putInt(KEY_NEXT_HEALTH_ID, nextHealthEntryId);
    }

    private void restoreState() {
        if (sharedPreferencesUtil == null) {
            return;
        }
        healthEntries.clear();
        String healthJson = sharedPreferencesUtil.getString(KEY_HEALTH_ENTRIES, "");
        int maxHealthId = 0;
        if (!healthJson.isEmpty()) {
            try {
                JSONArray healthArray = new JSONArray(healthJson);
                for (int i = 0; i < healthArray.length(); i++) {
                    JSONObject object = healthArray.optJSONObject(i);
                    if (object == null) {
                        continue;
                    }
                    HealthShareEntry entry = new HealthShareEntry(
                            object.optLong("authorId", -1L),
                            object.optString("authorName", ""),
                            object.optString("metricName", ""),
                            object.optString("metricValue", ""),
                            object.optString("note", null),
                            object.optLong("updatedAt", System.currentTimeMillis()));
                    int id = object.optInt("id", 0);
                    if (id <= 0) {
                        id = maxHealthId + 1;
                    }
                    entry.setId(id);
                    maxHealthId = Math.max(maxHealthId, id);
                    if (entry.getNote() != null && entry.getNote().isEmpty()) {
                        entry.setNote(null);
                    }
                    healthEntries.add(entry);
                }
            } catch (JSONException ignored) {
                healthEntries.clear();
            }
        }
        int storedNextHealthId = sharedPreferencesUtil.getInt(KEY_NEXT_HEALTH_ID, -1);
        if (storedNextHealthId <= 0) {
            storedNextHealthId = maxHealthId + 1;
        }
        nextHealthEntryId = Math.max(storedNextHealthId, maxHealthId + 1);
        seeded = !healthEntries.isEmpty();
    }

    private void seedHealthDataIfNeeded() {
        if (seeded && !healthEntries.isEmpty()) {
            return;
        }
        if (!healthEntries.isEmpty()) {
            return;
        }
        seeded = true;
        long now = System.currentTimeMillis();
        addSeedHealthEntry("王阿姨", "血压", "118/76 mmHg",
                "术后第三周保持稳定，很开心！", now - 5 * 60 * 60 * 1000L);
        addSeedHealthEntry("康复专家李医生", "步数", "6200 步",
                "今天的处方步行目标已经完成，继续加油。", now - 8 * 60 * 60 * 1000L);
        addSeedHealthEntry("陈先生", "血糖", "5.4 mmol/L",
                "早餐前数据，饮食调整起作用了。", now - 12 * 60 * 60 * 1000L);
        persistState();
    }

    private void addSeedHealthEntry(@NonNull String author,
                                    @NonNull String metric,
                                    @NonNull String value,
                                    @NonNull String note,
                                    long updatedAt) {
        HealthShareEntry entry = new HealthShareEntry(0, author, metric, value, note, updatedAt);
        entry.setId(nextHealthEntryId++);
        healthEntries.add(entry);
    }

    private void updateAdapterUser() {
        if (healthShareAdapter == null) {
            return;
        }
        long userId = sharedPreferencesUtil != null
                ? sharedPreferencesUtil.getCurrentUserId() : -1L;
        healthShareAdapter.setCurrentUserId(userId);
    }

    private boolean canModifyEntry(@NonNull HealthShareEntry entry) {
        long userId = sharedPreferencesUtil != null
                ? sharedPreferencesUtil.getCurrentUserId() : -1L;
        return entry.getAuthorId() == userId;
    }

    private void showToast(int messageResId) {
        if (!isAdded()) {
            return;
        }
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAdapterUser();
        loadHealthEntries();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rvHealthShare = null;
        tvEmptyState = null;
        btnAddHealthShare = null;
        healthShareAdapter = null;
    }
}
