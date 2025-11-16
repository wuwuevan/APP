package com.example.myapplication.adapter;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.HealthShareEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView 适配器：展示社区中的健康数据分享。
 */
public class HealthShareAdapter extends RecyclerView.Adapter<HealthShareAdapter.HealthShareViewHolder> {

    public interface OnHealthEntryActionListener {
        void onEdit(@NonNull HealthShareEntry entry);

        void onDelete(@NonNull HealthShareEntry entry);
    }

    private final ArrayList<HealthShareEntry> data = new ArrayList<>();
    private final OnHealthEntryActionListener listener;

    public HealthShareAdapter(@NonNull OnHealthEntryActionListener listener) {
        this.listener = listener;
    }

    public void submitList(@NonNull List<HealthShareEntry> entries) {
        data.clear();
        data.addAll(entries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HealthShareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_health_share, parent, false);
        return new HealthShareViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthShareViewHolder holder, int position) {
        HealthShareEntry entry = data.get(position);
        holder.bind(entry, listener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class HealthShareViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvUserName;
        private final TextView tvUpdatedAt;
        private final TextView tvMetricName;
        private final TextView tvMetricValue;
        private final TextView tvNote;
        private final TextView btnEdit;
        private final TextView btnDelete;

        HealthShareViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_health_user);
            tvUpdatedAt = itemView.findViewById(R.id.tv_health_time);
            tvMetricName = itemView.findViewById(R.id.tv_health_metric);
            tvMetricValue = itemView.findViewById(R.id.tv_health_value);
            tvNote = itemView.findViewById(R.id.tv_health_note);
            btnEdit = itemView.findViewById(R.id.btn_health_edit);
            btnDelete = itemView.findViewById(R.id.btn_health_delete);
        }

        void bind(@NonNull HealthShareEntry entry,
                  @NonNull OnHealthEntryActionListener listener) {
            tvUserName.setText(entry.getAuthorName());
            CharSequence timeText = DateUtils.getRelativeTimeSpanString(entry.getUpdatedAt(),
                    System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            tvUpdatedAt.setText(timeText);
            tvMetricName.setText(entry.getMetricName());
            tvMetricValue.setText(entry.getMetricValue());
            if (TextUtils.isEmpty(entry.getNote())) {
                tvNote.setVisibility(View.GONE);
            } else {
                tvNote.setVisibility(View.VISIBLE);
                tvNote.setText(entry.getNote());
            }
            btnEdit.setOnClickListener(v -> listener.onEdit(entry));
            btnDelete.setOnClickListener(v -> listener.onDelete(entry));
        }
    }
}
