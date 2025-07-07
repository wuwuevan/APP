package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.ChatMessage;

import java.util.List;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    
    private List<ChatMessage> messageList;
    
    public MessageAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if ("USER".equals(message.getSenderType())) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageHolder) holder).bind(message);
        } else {
            ((ReceivedMessageHolder) holder).bind(message);
        }

        // Add animation
        setAnimation(holder.itemView, position);
    }
    
    @Override
    public int getItemCount() {
        return messageList.size();
    }
    
    // 更新消息列表
    public void setMessages(List<ChatMessage> messages) {
        this.messageList = messages;
        notifyDataSetChanged();
    }
    
    // 添加新消息
    public void addMessage(ChatMessage message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }
    
    // 发送消息的ViewHolder
    private static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        
        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message);
        }
        
        void bind(ChatMessage message) {
            messageText.setText(message.getContent());
        }
    }
    
    // 接收消息的ViewHolder
    private static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        
        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message);
        }
        
        void bind(ChatMessage message) {
            messageText.setText(message.getContent());
        }
    }

    private int lastPosition = -1;

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't animated already, animate it
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.fade_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
} 