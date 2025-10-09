package com.example.myapplication.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.MessageAdapter;
import com.example.myapplication.db.MessageDao;
import com.example.myapplication.model.ChatMessage;
import com.example.myapplication.model.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String DEEPSEEK_MODEL = "deepseek-chat";
    private static final String DEEPSEEK_SYSTEM_PROMPT =
            "You are an empathetic Chinese-speaking medical consultation assistant developed for the Smart Health app. " +
            "Provide reliable health guidance based on public medical knowledge, remind users to consult licensed doctors " +
            "for diagnoses or emergencies, and keep responses concise and easy to understand.";
    private static final String DEEPSEEK_API_KEY = "sk-ea93f13ea85f42c8b5e81f8105385794";

    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();

    private RecyclerView recyclerView;
    private EditText etMessage;
    private Button btnSend;
    private MessageAdapter messageAdapter;
    private List<ChatMessage> chatMessages;
    private String doctorName;
    private Toolbar toolbar;
    private MessageDao messageDao;

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

        messageDao = new MessageDao(this);

        chatMessages = new ArrayList<>();
        messageAdapter = new MessageAdapter(chatMessages);

        // Load history messages
        List<Message> history = messageDao.getMessagesByDoctor(doctorName);
        for (Message msg : history) {
            String time = formatTimestamp(msg.getTimestamp());
            String type = msg.isSent() ? "USER" : "BOT";
            chatMessages.add(new ChatMessage(msg.isSent() ? 1 : 0, type, msg.getContent(), time));
        }

        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
            }
        });

        // Add a welcome message only if there is no history
        if (history.isEmpty()) {
            addBotMessage("您好，这里是我们开发的智能医疗咨询助手DeepSeek AI，有任何健康问题都可以告诉我，我会尽力为您提供参考建议。", true);
        }
    }

    private void sendMessage(String messageText) {
        long timestamp = System.currentTimeMillis();
        String currentTime = formatTimestamp(timestamp);
        // User's message
        ChatMessage userMessage = new ChatMessage(1, "USER", messageText, currentTime);
        chatMessages.add(userMessage);
        messageAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);
        etMessage.setText("");

        // Save to database
        messageDao.saveMessage(new Message(messageText, timestamp, true, doctorName));

        int placeholderPosition = addBotMessage("DeepSeek 助手机器人正在思考，请稍候……", false);
        requestDeepSeekResponse(messageText, placeholderPosition);
    }

    private int addBotMessage(String messageText, boolean persist) {
        long timestamp = System.currentTimeMillis();
        String currentTime = formatTimestamp(timestamp);
        ChatMessage botMessage = new ChatMessage(0, "BOT", messageText, currentTime);
        chatMessages.add(botMessage);
        int position = chatMessages.size() - 1;
        messageAdapter.notifyItemInserted(position);
        recyclerView.scrollToPosition(position);

        if (persist) {
            messageDao.saveMessage(new Message(messageText, timestamp, false, doctorName));
        }

        return position;
    }

    private void updateBotMessage(int position, String newContent, boolean persist) {
        if (position < 0 || position >= chatMessages.size()) {
            return;
        }

        long timestamp = System.currentTimeMillis();
        String currentTime = formatTimestamp(timestamp);
        ChatMessage botMessage = chatMessages.get(position);
        botMessage.setContent(newContent);
        botMessage.setSendTime(currentTime);
        messageAdapter.notifyItemChanged(position);
        recyclerView.scrollToPosition(position);

        if (persist) {
            messageDao.saveMessage(new Message(newContent, timestamp, false, doctorName));
        }
    }

    private void requestDeepSeekResponse(String userMessage, int placeholderPosition) {
        JSONArray messagesArray = new JSONArray();
        try {
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", DEEPSEEK_SYSTEM_PROMPT);
            messagesArray.put(systemMessage);

            JSONObject userMessageObject = new JSONObject();
            userMessageObject.put("role", "user");
            userMessageObject.put("content", userMessage);
            messagesArray.put(userMessageObject);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to build DeepSeek request", e);
            handleDeepSeekError(placeholderPosition, "构造 DeepSeek 请求失败：" + e.getMessage());
            return;
        }

        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("model", DEEPSEEK_MODEL);
            requestBodyJson.put("messages", messagesArray);
            requestBodyJson.put("temperature", 0.3);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create DeepSeek payload", e);
            handleDeepSeekError(placeholderPosition, "生成 DeepSeek 请求内容失败：" + e.getMessage());
            return;
        }

        final int messagePosition = placeholderPosition;
        final String requestBodyString = requestBodyJson.toString();

        networkExecutor.execute(() -> {
            HttpsURLConnection connection = null;
            try {
                URL url = new URL(DEEPSEEK_API_URL);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + DEEPSEEK_API_KEY);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream();
                     OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                    writer.write(requestBodyString);
                    writer.flush();
                }

                int responseCode = connection.getResponseCode();
                InputStream inputStream = responseCode >= 200 && responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                StringBuilder responseBuilder = new StringBuilder();
                if (inputStream != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                    }
                }

                String responseBody = responseBuilder.toString();

                if (responseCode < 200 || responseCode >= 300) {
                    Log.e(TAG, "DeepSeek API error: " + responseCode + " " + responseBody);
                    String errorMessage = TextUtils.isEmpty(responseBody)
                            ? "DeepSeek 响应失败(" + responseCode + ")"
                            : "DeepSeek 响应失败(" + responseCode + ")：" + responseBody;
                    handleDeepSeekError(messagePosition, errorMessage);
                    return;
                }

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (jsonResponse.has("error")) {
                        JSONObject error = jsonResponse.getJSONObject("error");
                        String errorMessage = error.optString("message", "未知错误");
                        handleDeepSeekError(messagePosition, "DeepSeek 返回错误：" + errorMessage);
                        return;
                    }

                    JSONArray choices = jsonResponse.optJSONArray("choices");
                    if (choices == null || choices.length() == 0) {
                        handleDeepSeekError(messagePosition, "DeepSeek 未返回任何结果，请稍后重试。");
                        return;
                    }

                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.optJSONObject("message");
                    String content = "";
                    if (message != null) {
                        content = message.optString("content", "");
                    }

                    if (TextUtils.isEmpty(content)) {
                        content = firstChoice.optString("text", "");
                    }

                    if (TextUtils.isEmpty(content)) {
                        handleDeepSeekError(messagePosition, "DeepSeek 返回内容为空。");
                        return;
                    }

                    final String botReply = content.trim();
                    runOnUiThread(() -> updateBotMessage(messagePosition, botReply, true));
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse DeepSeek response", e);
                    handleDeepSeekError(messagePosition, "解析 DeepSeek 响应失败：" + e.getMessage());
                }
            } catch (IOException e) {
                Log.e(TAG, "DeepSeek request failed", e);
                handleDeepSeekError(messagePosition, "DeepSeek 请求失败：" + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void handleDeepSeekError(int placeholderPosition, String errorMessage) {
        final int messagePosition = placeholderPosition;
        final String botReply = errorMessage;
        runOnUiThread(() -> updateBotMessage(messagePosition, botReply, true));
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkExecutor.shutdownNow();
    }
}
