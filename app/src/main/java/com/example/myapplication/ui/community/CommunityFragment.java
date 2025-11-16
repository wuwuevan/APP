package com.example.myapplication.ui.community;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.CommunityPostAdapter;
import com.example.myapplication.adapter.HealthShareAdapter;
import com.example.myapplication.data.CommunityComment;
import com.example.myapplication.data.CommunityPost;
import com.example.myapplication.data.HealthShareEntry;
import com.example.myapplication.data.PostWithComments;
import com.example.myapplication.utils.SharedPreferencesUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 社区页面，提供最基础的浏览、搜索、排序、发帖、回复与删除功能。
 */
public class CommunityFragment extends Fragment {

    private EditText etSearch;
    private Spinner spinnerSort;
    private RecyclerView rvPosts;
    private TextView tvEmptyState;
    private FloatingActionButton fabCreatePost;
    private RecyclerView rvHealthShare;
    private TextView tvHealthEmptyState;
    private Button btnAddHealthShare;

    private CommunityPostAdapter postAdapter;
    private HealthShareAdapter healthShareAdapter;
    private SharedPreferencesUtil sharedPreferencesUtil;

    private static final String KEY_POSTS = "community_posts_json";
    private static final String KEY_COMMENTS = "community_comments_json";
    private static final String KEY_NEXT_POST_ID = "community_next_post_id";
    private static final String KEY_NEXT_COMMENT_ID = "community_next_comment_id";
    private static final String KEY_HEALTH_ENTRIES = "community_health_entries_json";
    private static final String KEY_NEXT_HEALTH_ID = "community_next_health_id";

    private SortOption currentSortOption = SortOption.NEWEST;
    private String currentKeyword = "";
    private TextWatcher searchWatcher;

    private final ArrayList<CommunityPost> posts = new ArrayList<>();
    private final ArrayList<CommunityComment> comments = new ArrayList<>();
    private final ArrayList<HealthShareEntry> healthEntries = new ArrayList<>();
    private boolean seeded = false;
    private boolean healthSeeded = false;
    private int nextPostId = 1;
    private int nextCommentId = 1;
    private int nextHealthEntryId = 1;

    private enum SortOption {
        NEWEST,
        OLDEST,
        MOST_REPLIED
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sharedPreferencesUtil = new SharedPreferencesUtil(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);
        initViews(view);
        setupRecyclerView(view.getContext());
        setupHealthShareSection(view.getContext());
        setupSearch();
        setupSortSpinner();
        setupFab();
        restoreState();
        seedDataIfNeeded();
        seedHealthDataIfNeeded();
        loadPosts();
        loadHealthEntries();
        return view;
    }

    private void initViews(@NonNull View view) {
        etSearch = view.findViewById(R.id.et_search_post);
        spinnerSort = view.findViewById(R.id.spinner_sort);
        rvPosts = view.findViewById(R.id.recycler_posts);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        fabCreatePost = view.findViewById(R.id.fab_create_post);
        rvHealthShare = view.findViewById(R.id.recycler_health_data);
        tvHealthEmptyState = view.findViewById(R.id.tv_health_empty_state);
        btnAddHealthShare = view.findViewById(R.id.btn_add_health_share);
    }

    private void setupRecyclerView(@NonNull Context context) {
        rvPosts.setLayoutManager(new LinearLayoutManager(context));
        rvPosts.setHasFixedSize(true);
        postAdapter = new CommunityPostAdapter(new CommunityPostAdapter.OnPostActionListener() {
            @Override
            public void onReply(@NonNull CommunityPost post) {
                showReplyDialog(post);
            }

            @Override
            public void onDelete(@NonNull CommunityPost post) {
                showDeleteDialog(post);
            }
        });
        updateAdapterUser();
        rvPosts.setAdapter(postAdapter);
    }

    private void setupHealthShareSection(@NonNull Context context) {
        if (rvHealthShare == null) {
            return;
        }
        rvHealthShare.setLayoutManager(new LinearLayoutManager(context));
        rvHealthShare.setNestedScrollingEnabled(false);
        healthShareAdapter = new HealthShareAdapter(new HealthShareAdapter.OnHealthEntryActionListener() {
            @Override
            public void onEdit(@NonNull HealthShareEntry entry) {
                showHealthShareDialog(entry);
            }

            @Override
            public void onDelete(@NonNull HealthShareEntry entry) {
                showDeleteHealthEntryDialog(entry);
            }
        });
        rvHealthShare.setAdapter(healthShareAdapter);
        if (btnAddHealthShare != null) {
            btnAddHealthShare.setOnClickListener(v -> showHealthShareDialog(null));
        }
    }

    private void setupSearch() {
        searchWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // no-op
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentKeyword = s != null ? s.toString().trim() : "";
                loadPosts();
            }
        };
        etSearch.addTextChangedListener(searchWatcher);
    }

    private void setupSortSpinner() {
        Context context = requireContext();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.community_sort_newest));
        adapter.add(getString(R.string.community_sort_oldest));
        adapter.add(getString(R.string.community_sort_most_replied));
        spinnerSort.setAdapter(adapter);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentSortOption = SortOption.NEWEST;
                } else if (position == 1) {
                    currentSortOption = SortOption.OLDEST;
                } else {
                    currentSortOption = SortOption.MOST_REPLIED;
                }
                loadPosts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });
        spinnerSort.setSelection(0);
    }

    private void setupFab() {
        fabCreatePost.setOnClickListener(v -> showCreatePostDialog());
    }

    private void showCreatePostDialog() {
        if (!isAdded()) {
            return;
        }
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_dialog_create_post, null, false);
        EditText inputTitle = dialogView.findViewById(R.id.input_post_title);
        EditText inputContent = dialogView.findViewById(R.id.input_post_content);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.community_create_post)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.publish, null)
                .create();

        dialog.setOnShowListener(d -> {
            View positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setOnClickListener(v -> {
                    String title = inputTitle != null && inputTitle.getText() != null
                            ? inputTitle.getText().toString().trim() : "";
                    String content = inputContent != null && inputContent.getText() != null
                            ? inputContent.getText().toString().trim() : "";
                    if (title.isEmpty()) {
                        showToast(R.string.community_title_required);
                        return;
                    }
                    if (content.isEmpty()) {
                        showToast(R.string.community_content_required);
                        return;
                    }
                    publishPost(title, content);
                    dialog.dismiss();
                });
            }
        });

        dialog.show();
    }

    private void showHealthShareDialog(@Nullable HealthShareEntry entry) {
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
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    deleteHealthEntry(entry);
                })
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

    private void publishPost(@NonNull String title, @NonNull String content) {
        long userId = sharedPreferencesUtil != null
                ? sharedPreferencesUtil.getCurrentUserId() : -1;
        String username = sharedPreferencesUtil != null
                ? sharedPreferencesUtil.getCurrentUsername() : "";
        if (username == null || username.isEmpty()) {
            username = getString(R.string.community_anonymous_user);
        }
        CommunityPost post = new CommunityPost(userId, username, title, content,
                System.currentTimeMillis());
        post.setId(nextPostId++);
        posts.add(post);
        persistState();
        showToast(R.string.community_post_published);
        loadPosts();
    }

    private void showReplyDialog(@NonNull CommunityPost post) {
        if (!isAdded()) {
            return;
        }
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_dialog_reply_post, null, false);
        EditText inputReply = dialogView.findViewById(R.id.input_reply_content);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.community_reply_title)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.send, null)
                .create();

        dialog.setOnShowListener(d -> {
            View positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setOnClickListener(v -> {
                    String content = inputReply != null && inputReply.getText() != null
                            ? inputReply.getText().toString().trim() : "";
                    if (content.isEmpty()) {
                        showToast(R.string.community_reply_required);
                        return;
                    }
                    sendReply(post, content);
                    dialog.dismiss();
                });
            }
        });

        dialog.show();
    }

    private void sendReply(@NonNull CommunityPost post, @NonNull String content) {
        long userId = sharedPreferencesUtil != null
                ? sharedPreferencesUtil.getCurrentUserId() : -1;
        String username = sharedPreferencesUtil != null
                ? sharedPreferencesUtil.getCurrentUsername() : "";
        if (username == null || username.isEmpty()) {
            username = getString(R.string.community_anonymous_user);
        }
        CommunityComment comment = new CommunityComment(post.getId(), userId, username,
                content, System.currentTimeMillis());
        comment.setId(nextCommentId++);
        comments.add(comment);
        persistState();
        showToast(R.string.community_reply_success);
        loadPosts();
    }

    private void showDeleteDialog(@NonNull CommunityPost post) {
        if (!isAdded()) {
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(R.string.community_delete_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    long userId = sharedPreferencesUtil != null
                            ? sharedPreferencesUtil.getCurrentUserId() : -1;
                    if (userId != -1 && deletePost(post, userId)) {
                        showToast(R.string.community_post_deleted);
                        loadPosts();
                    } else {
                        showToast(R.string.community_delete_not_allowed);
                    }
                })
                .show();
    }

    private boolean deletePost(@NonNull CommunityPost post, long requesterId) {
        if (post.getAuthorId() != requesterId) {
            return false;
        }
        boolean removed = posts.removeIf(p -> p.getId() == post.getId());
        if (!removed) {
            return false;
        }
        for (int i = comments.size() - 1; i >= 0; i--) {
            if (comments.get(i).getPostId() == post.getId()) {
                comments.remove(i);
            }
        }
        persistState();
        return true;
    }

    private void loadPosts() {
        if (rvPosts == null || tvEmptyState == null) {
            return;
        }
        ArrayList<PostWithComments> snapshot = new ArrayList<>();
        for (CommunityPost post : posts) {
            if (matchesKeyword(post, currentKeyword)) {
                snapshot.add(new PostWithComments(post, collectComments(post.getId())));
            }
        }
        sortPosts(snapshot, currentSortOption);
        if (postAdapter != null) {
            postAdapter.submitList(snapshot);
        }
        boolean isEmpty = snapshot.isEmpty();
        rvPosts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void loadHealthEntries() {
        if (healthShareAdapter == null) {
            return;
        }
        ArrayList<HealthShareEntry> snapshot = new ArrayList<>(healthEntries);
        snapshot.sort((o1, o2) -> Long.compare(o2.getUpdatedAt(), o1.getUpdatedAt()));
        healthShareAdapter.submitList(snapshot);
        if (tvHealthEmptyState != null && rvHealthShare != null) {
            boolean empty = snapshot.isEmpty();
            tvHealthEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvHealthShare.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    private ArrayList<CommunityComment> collectComments(int postId) {
        ArrayList<CommunityComment> result = new ArrayList<>();
        for (CommunityComment comment : comments) {
            if (comment.getPostId() == postId) {
                result.add(comment);
            }
        }
        result.sort((c1, c2) -> Long.compare(c1.getCreatedAt(), c2.getCreatedAt()));
        return result;
    }

    private boolean matchesKeyword(@NonNull CommunityPost post, @NonNull String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        String lowerKeyword = keyword.toLowerCase(Locale.getDefault());
        return containsIgnoreCase(post.getTitle(), lowerKeyword)
                || containsIgnoreCase(post.getContent(), lowerKeyword)
                || containsIgnoreCase(post.getAuthorName(), lowerKeyword);
    }

    private boolean containsIgnoreCase(String source, String lowerKeyword) {
        if (source == null || source.isEmpty()) {
            return false;
        }
        return source.toLowerCase(Locale.getDefault()).contains(lowerKeyword);
    }

    private void sortPosts(@NonNull ArrayList<PostWithComments> data, SortOption option) {
        switch (option) {
            case OLDEST:
                data.sort((p1, p2) -> Long.compare(p1.getPost().getCreatedAt(),
                        p2.getPost().getCreatedAt()));
                break;
            case MOST_REPLIED:
                data.sort((p1, p2) -> {
                    int diff = p2.getComments().size() - p1.getComments().size();
                    if (diff != 0) {
                        return diff;
                    }
                    return Long.compare(p2.getPost().getCreatedAt(), p1.getPost().getCreatedAt());
                });
                break;
            case NEWEST:
            default:
                data.sort((p1, p2) -> Long.compare(p2.getPost().getCreatedAt(),
                        p1.getPost().getCreatedAt()));
                break;
        }
    }

    private void persistState() {
        if (sharedPreferencesUtil == null) {
            return;
        }
        JSONArray postArray = new JSONArray();
        for (CommunityPost post : posts) {
            JSONObject object = new JSONObject();
            try {
                object.put("id", post.getId());
                object.put("authorId", post.getAuthorId());
                object.put("authorName", safeString(post.getAuthorName()));
                object.put("title", safeString(post.getTitle()));
                object.put("content", safeString(post.getContent()));
                object.put("createdAt", post.getCreatedAt());
                postArray.put(object);
            } catch (JSONException ignored) {
                // 忽略单条写入异常，继续保存其他数据
            }
        }

        JSONArray commentArray = new JSONArray();
        for (CommunityComment comment : comments) {
            JSONObject object = new JSONObject();
            try {
                object.put("id", comment.getId());
                object.put("postId", comment.getPostId());
                object.put("authorId", comment.getAuthorId());
                object.put("authorName", safeString(comment.getAuthorName()));
                object.put("content", safeString(comment.getContent()));
                object.put("createdAt", comment.getCreatedAt());
                commentArray.put(object);
            } catch (JSONException ignored) {
                // 忽略单条写入异常，继续保存其他数据
            }
        }

        JSONArray healthArray = new JSONArray();
        for (HealthShareEntry entry : healthEntries) {
            JSONObject object = new JSONObject();
            try {
                object.put("id", entry.getId());
                object.put("authorId", entry.getAuthorId());
                object.put("authorName", safeString(entry.getAuthorName()));
                object.put("metricName", safeString(entry.getMetricName()));
                object.put("metricValue", safeString(entry.getMetricValue()));
                object.put("note", safeString(entry.getNote()));
                object.put("updatedAt", entry.getUpdatedAt());
                healthArray.put(object);
            } catch (JSONException ignored) {
                // 忽略单条写入异常
            }
        }

        sharedPreferencesUtil.putString(KEY_POSTS, postArray.toString());
        sharedPreferencesUtil.putString(KEY_COMMENTS, commentArray.toString());
        sharedPreferencesUtil.putString(KEY_HEALTH_ENTRIES, healthArray.toString());
        sharedPreferencesUtil.putInt(KEY_NEXT_POST_ID, nextPostId);
        sharedPreferencesUtil.putInt(KEY_NEXT_COMMENT_ID, nextCommentId);
        sharedPreferencesUtil.putInt(KEY_NEXT_HEALTH_ID, nextHealthEntryId);
    }

    private void restoreState() {
        if (sharedPreferencesUtil == null) {
            return;
        }
        posts.clear();
        comments.clear();
        healthEntries.clear();

        int maxPostId = 0;
        int maxCommentId = 0;
        int maxHealthId = 0;

        String postJson = sharedPreferencesUtil.getString(KEY_POSTS, "");
        if (!postJson.isEmpty()) {
            try {
                JSONArray postArray = new JSONArray(postJson);
                for (int i = 0; i < postArray.length(); i++) {
                    JSONObject object = postArray.optJSONObject(i);
                    if (object == null) {
                        continue;
                    }
                    CommunityPost post = new CommunityPost(
                            object.optLong("authorId", -1L),
                            safeString(object.optString("authorName", "")),
                            safeString(object.optString("title", "")),
                            safeString(object.optString("content", "")),
                            object.optLong("createdAt", System.currentTimeMillis())
                    );
                    int id = object.optInt("id", 0);
                    if (id <= 0) {
                        id = maxPostId + 1;
                    }
                    post.setId(id);
                    maxPostId = Math.max(maxPostId, id);
                    posts.add(post);
                }
            } catch (JSONException ignored) {
                posts.clear();
            }
        }

        String commentJson = sharedPreferencesUtil.getString(KEY_COMMENTS, "");
        if (!commentJson.isEmpty()) {
            try {
                JSONArray commentArray = new JSONArray(commentJson);
                for (int i = 0; i < commentArray.length(); i++) {
                    JSONObject object = commentArray.optJSONObject(i);
                    if (object == null) {
                        continue;
                    }
                    CommunityComment comment = new CommunityComment(
                            object.optInt("postId", 0),
                            object.optLong("authorId", -1L),
                            safeString(object.optString("authorName", "")),
                            safeString(object.optString("content", "")),
                            object.optLong("createdAt", System.currentTimeMillis())
                    );
                    int id = object.optInt("id", 0);
                    if (id <= 0) {
                        id = maxCommentId + 1;
                    }
                    comment.setId(id);
                    maxCommentId = Math.max(maxCommentId, id);
                    comments.add(comment);
                }
            } catch (JSONException ignored) {
                comments.clear();
            }
        }

        String healthJson = sharedPreferencesUtil.getString(KEY_HEALTH_ENTRIES, "");
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
                            safeString(object.optString("authorName", "")),
                            safeString(object.optString("metricName", "")),
                            safeString(object.optString("metricValue", "")),
                            safeString(object.optString("note", "")),
                            object.optLong("updatedAt", System.currentTimeMillis())
                    );
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

        int storedNextPostId = sharedPreferencesUtil.getInt(KEY_NEXT_POST_ID, -1);
        int storedNextCommentId = sharedPreferencesUtil.getInt(KEY_NEXT_COMMENT_ID, -1);
        int storedNextHealthId = sharedPreferencesUtil.getInt(KEY_NEXT_HEALTH_ID, -1);
        if (storedNextPostId <= 0) {
            storedNextPostId = maxPostId + 1;
        }
        if (storedNextCommentId <= 0) {
            storedNextCommentId = maxCommentId + 1;
        }
        if (storedNextHealthId <= 0) {
            storedNextHealthId = maxHealthId + 1;
        }
        nextPostId = Math.max(storedNextPostId, maxPostId + 1);
        nextCommentId = Math.max(storedNextCommentId, maxCommentId + 1);
        nextHealthEntryId = Math.max(storedNextHealthId, maxHealthId + 1);
        seeded = !posts.isEmpty();
        healthSeeded = !healthEntries.isEmpty();
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private void seedDataIfNeeded() {
        if (seeded && !posts.isEmpty()) {
            return;
        }
        if (!posts.isEmpty()) {
            return;
        }
        seeded = true;
        long now = System.currentTimeMillis();

        CommunityPost welcome = addSeedPost("社区管家", "欢迎来到康复社区",
                "这里是大家交流康复心得、互相鼓励的温馨角落，欢迎分享你的故事和问题。",
                now - 3 * 24 * 60 * 60 * 1000L);
        CommunityPost rehabTips = addSeedPost("康复专家李医生", "术后第2周的注意事项",
                "保持关节适度活动、按时进行冰敷，若出现明显肿胀要及时反馈给医生。",
                now - 2 * 24 * 60 * 60 * 1000L);
        CommunityPost successStory = addSeedPost("王阿姨", "分享一下我的康复小成就",
                "在坚持了三周的康复训练后，现在已经可以独立上下楼梯啦！感谢大家的鼓励。",
                now - 20 * 60 * 60 * 1000L);

        addSeedComment(welcome.getId(), "陈先生",
                "谢谢提醒，我刚入群就看到这么暖心的欢迎帖！", now - 2 * 24 * 60 * 60 * 1000L);
        addSeedComment(welcome.getId(), "赵护士",
                "有任何问题都可以@我，我们会尽快解答~", now - 36 * 60 * 60 * 1000L);
        addSeedComment(rehabTips.getId(), "杨女士",
                "请问冰敷一次大概多久合适？", now - 30 * 60 * 60 * 1000L);
        addSeedComment(rehabTips.getId(), "康复专家李医生",
                "一次15-20分钟即可，记得间隔至少40分钟再进行下一次。", now - 28 * 60 * 60 * 1000L);
        addSeedComment(successStory.getId(), "刘先生",
                "太棒了！我也在努力，希望能像您一样。", now - 18 * 60 * 60 * 1000L);
        persistState();
    }

    private void seedHealthDataIfNeeded() {
        if (healthSeeded && !healthEntries.isEmpty()) {
            return;
        }
        if (!healthEntries.isEmpty()) {
            return;
        }
        healthSeeded = true;
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

    private CommunityPost addSeedPost(@NonNull String author, @NonNull String title,
                                      @NonNull String content, long createdAt) {
        CommunityPost post = new CommunityPost(0, author, title, content, createdAt);
        post.setId(nextPostId++);
        posts.add(post);
        return post;
    }

    private void addSeedComment(int postId, @NonNull String author, @NonNull String content,
                                long createdAt) {
        CommunityComment comment = new CommunityComment(postId, 0, author, content, createdAt);
        comment.setId(nextCommentId++);
        comments.add(comment);
    }

    private void showToast(int messageResId) {
        if (!isAdded()) {
            return;
        }
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show();
    }

    private void updateAdapterUser() {
        if (postAdapter == null) {
            return;
        }
        long userId = sharedPreferencesUtil != null
                ? sharedPreferencesUtil.getCurrentUserId() : -1;
        postAdapter.setCurrentUser(userId);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAdapterUser();
        loadPosts();
        loadHealthEntries();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (etSearch != null && searchWatcher != null) {
            etSearch.removeTextChangedListener(searchWatcher);
        }
        rvPosts.setAdapter(null);
        searchWatcher = null;
        etSearch = null;
        spinnerSort = null;
        rvPosts = null;
        tvEmptyState = null;
        fabCreatePost = null;
        rvHealthShare = null;
        tvHealthEmptyState = null;
        btnAddHealthShare = null;
        healthShareAdapter = null;
    }
}
