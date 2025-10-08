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
import com.example.myapplication.data.CommunityComment;
import com.example.myapplication.data.CommunityPost;
import com.example.myapplication.data.PostWithComments;
import com.example.myapplication.utils.SharedPreferencesUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 社区页面，提供最基础的浏览、搜索、排序、发帖、回复与删除功能。
 */
public class CommunityFragment extends Fragment {

    private EditText etSearch;
    private Spinner spinnerSort;
    private RecyclerView rvPosts;
    private TextView tvEmptyState;
    private FloatingActionButton fabCreatePost;

    private CommunityPostAdapter postAdapter;
    private SharedPreferencesUtil sharedPreferencesUtil;

    private SortOption currentSortOption = SortOption.NEWEST;
    private String currentKeyword = "";
    private TextWatcher searchWatcher;

    private final ArrayList<CommunityPost> posts = new ArrayList<>();
    private final ArrayList<CommunityComment> comments = new ArrayList<>();
    private boolean seeded = false;
    private int nextPostId = 1;
    private int nextCommentId = 1;

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
        setupSearch();
        setupSortSpinner();
        setupFab();
        seedDataIfNeeded();
        loadPosts();
        return view;
    }

    private void initViews(@NonNull View view) {
        etSearch = view.findViewById(R.id.et_search_post);
        spinnerSort = view.findViewById(R.id.spinner_sort);
        rvPosts = view.findViewById(R.id.recycler_posts);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        fabCreatePost = view.findViewById(R.id.fab_create_post);
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
        return true;
    }

    private void loadPosts() {
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
    }
}
