package com.example.myapplication.ui.gait;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于展示最近步态事件的适配器。
 */
public class GaitEventAdapter extends RecyclerView.Adapter<GaitEventAdapter.GaitEventViewHolder> {

    private final List<GaitDataGenerator.GaitEvent> events = new ArrayList<>();

    @NonNull
    @Override
    public GaitEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gait_event, parent, false);
        return new GaitEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GaitEventViewHolder holder, int position) {
        GaitDataGenerator.GaitEvent event = events.get(position);
        holder.title.setText(event.title);
        holder.subtitle.setText(event.subtitle);
        holder.description.setText(event.description);
        holder.icon.setImageResource(event.iconRes);
        holder.status.setText(event.statusLabel);
        int statusColor = ContextCompat.getColor(holder.status.getContext(), event.statusColorRes);
        holder.status.setTextColor(statusColor);
        ColorStateList tint = ColorStateList.valueOf(ColorUtils.setAlphaComponent(statusColor, 56));
        ViewCompat.setBackgroundTintList(holder.status, tint);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<GaitDataGenerator.GaitEvent> newEvents) {
        events.clear();
        if (newEvents != null) {
            events.addAll(newEvents);
        }
        notifyDataSetChanged();
    }

    static class GaitEventViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title;
        final TextView subtitle;
        final TextView description;
        final TextView status;

        GaitEventViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_event_icon);
            title = itemView.findViewById(R.id.tv_event_title);
            subtitle = itemView.findViewById(R.id.tv_event_subtitle);
            description = itemView.findViewById(R.id.tv_event_description);
            status = itemView.findViewById(R.id.tv_event_status);
        }
    }
}
