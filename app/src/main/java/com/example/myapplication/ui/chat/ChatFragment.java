package com.example.myapplication.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

/**
 * 咨询页面，用于显示医生列表和在线咨询
 */
public class ChatFragment extends Fragment {

    private Button btnStartChat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        btnStartChat = root.findViewById(R.id.btn_start_chat);

        btnStartChat.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            // Pass a generic name for the assistant
            intent.putExtra("doctor_name", "智能健康助手");
            startActivity(intent);
        });

        return root;
    }
} 