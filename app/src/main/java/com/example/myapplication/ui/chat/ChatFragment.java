package com.example.myapplication.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.adapter.RecentChatAdapter;
import com.example.myapplication.db.MessageDao;
import com.example.myapplication.model.RecentChat;
import java.util.ArrayList;
import java.util.List;

/**
 * 咨询页面，用于显示医生列表和在线咨询
 */
public class ChatFragment extends Fragment {

    private Button btnStartChat;
    private RecyclerView rvRecentChats;
    private RecentChatAdapter chatAdapter;
    private List<RecentChat> recentChats;
    private MessageDao messageDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        btnStartChat = root.findViewById(R.id.btn_start_chat);
        rvRecentChats = root.findViewById(R.id.rv_recent_chats);

        messageDao = new MessageDao(getContext());
        recentChats = new ArrayList<>();
        chatAdapter = new RecentChatAdapter(recentChats, chat -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("doctor_name", chat.getDoctorName());
            startActivity(intent);
        }, chat -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setMessage("确定要删除这条对话吗？")
                    .setPositiveButton("确定", (d, which) -> {
                        messageDao.deleteMessagesByDoctor(chat.getDoctorName());
                        loadChats();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        rvRecentChats.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentChats.setAdapter(chatAdapter);

        btnStartChat.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            // Use a unique conversation id so each consultation appears separately
            String conversationId = "智能健康助手_" + System.currentTimeMillis();
            intent.putExtra("doctor_name", conversationId);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChats();
    }

    private void loadChats() {
        recentChats.clear();
        recentChats.addAll(messageDao.getRecentChats());
        chatAdapter.notifyDataSetChanged();
    }
}
