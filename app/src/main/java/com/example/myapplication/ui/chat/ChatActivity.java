package com.example.myapplication.ui.chat;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.MessageAdapter;
import com.example.myapplication.db.MessageDao;
import com.example.myapplication.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private Button btnSend;
    private MessageAdapter messageAdapter;
    private List<ChatMessage> chatMessages;
    private String doctorName;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        recyclerView = findViewById(R.id.recycler_chat);
        toolbar = findViewById(R.id.toolbar);

        // 设置自定义工具栏
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        doctorName = getIntent().getStringExtra("doctor_name");
        if (doctorName == null || doctorName.isEmpty()) {
            doctorName = "智能健康助手";
        }
        setTitle(doctorName);

        chatMessages = new ArrayList<>();
        messageAdapter = new MessageAdapter(chatMessages);

        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });

        // Add a welcome message from the assistant
        addAutoReply("您好！我是您的智能健康助手，请问有什么可以帮助您的吗？");
    }

    private void sendMessage(String messageText) {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        // User's message
        ChatMessage userMessage = new ChatMessage(1, "USER", messageText, currentTime);
        chatMessages.add(userMessage);
        messageAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);
        etMessage.setText("");

        // Auto-reply from the assistant
        addAutoReply("感谢您的提问，我会尽快为您解答。");
    }

    private void addAutoReply(String messageText) {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        // Assistant's message
        ChatMessage botMessage = new ChatMessage(0, "BOT", messageText, currentTime);
        chatMessages.add(botMessage);
        messageAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}