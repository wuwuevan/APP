package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.RecentChat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentChatAdapter extends RecyclerView.Adapter<RecentChatAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(RecentChat chat);
    }

    public interface OnItemDeleteListener {
        void onDelete(RecentChat chat);
    }

    private List<RecentChat> chatList;
    private OnItemClickListener listener;
    private OnItemDeleteListener deleteListener;

    public RecentChatAdapter(List<RecentChat> chatList, OnItemClickListener listener, OnItemDeleteListener deleteListener) {
        this.chatList = chatList;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_recent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(chatList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLastMessage, tvTime;
        View ivDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_chat_title);
            tvLastMessage = itemView.findViewById(R.id.tv_chat_last_message);
            tvTime = itemView.findViewById(R.id.tv_chat_time);
            ivDelete = itemView.findViewById(R.id.iv_delete_chat);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(chatList.get(getAdapterPosition()));
                }
            });
            ivDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(chatList.get(getAdapterPosition()));
                }
            });
        }

        void bind(RecentChat chat) {
            tvTitle.setText(chat.getFirstMessage());
            tvLastMessage.setText(chat.getLastMessage());
            String time = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new Date(chat.getLastTimestamp()));
            tvTime.setText(time);
        }
    }
}
