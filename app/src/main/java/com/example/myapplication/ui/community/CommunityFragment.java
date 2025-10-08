package com.example.myapplication.ui.community;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.CommunityPostAdapter;
import com.example.myapplication.data.CommunityComment;
import com.example.myapplication.data.CommunityPost;
import com.example.myapplication.data.PostWithComments;
import com.example.myapplication.ui.community.CommunityRepository.SortOption;
import com.example.myapplication.utils.SharedPreferencesUtil;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 社区页面，实现帖子浏览、搜索、排序、回复等基础功能。
 */
public class CommunityFragment extends Fragment {

    private TextInputEditText etSearch;
    private ChipGroup chipGroupSort;
    private RecyclerView rvPosts;
    private LinearLayout layoutEmptyState;
    private ExtendedFloatingActionButton fabCreatePost;

    private CommunityPostAdapter postAdapter;
    private CommunityRepository repository;
    private SharedPreferencesUtil sharedPreferencesUtil;

    private SortOption currentSortOption = SortOption.NEWEST;
    private String currentKeyword = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);
        initDependencies(view.getContext());
        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupSortChips();
        setupFab();
        loadPosts();
        return view;
    }

    private void initDependencies(@NonNull android.content.Context context) {
        sharedPreferencesUtil = new SharedPreferencesUtil(context);
        repository = new CommunityRepository(context);
        repository.ensureSeedData();
    }

    private void initViews(@NonNull View view) {
        etSearch = view.findViewById(R.id.et_search_post);
        chipGroupSort = view.findViewById(R.id.chip_group_sort);
        rvPosts = view.findViewById(R.id.recycler_posts);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        fabCreatePost = view.findViewById(R.id.fab_create_post);
    }

    private void setupRecyclerView() {
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
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
        long userId = sharedPreferencesUtil.getCurrentUserId();
        postAdapter.setCurrentUser(userId);
        rvPosts.setAdapter(postAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentKeyword = s != null ? s.toString().trim() : "";
                loadPosts();
            }
        });
    }

    private void setupSortChips() {
        chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) {
                return;
            }
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_sort_newest) {
                currentSortOption = SortOption.NEWEST;
            } else if (checkedId == R.id.chip_sort_oldest) {
                currentSortOption = SortOption.OLDEST;
            } else if (checkedId == R.id.chip_sort_most_replied) {
                currentSortOption = SortOption.MOST_REPLIED;
            }
            loadPosts();
        });
    }

    private void setupFab() {
        fabCreatePost.setOnClickListener(v -> showCreatePostDialog());
    }

    private void showCreatePostDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_create_post, null, false);
        TextInputLayout tilTitle = dialogView.findViewById(R.id.til_post_title);
        TextInputLayout tilContent = dialogView.findViewById(R.id.til_post_content);
        TextInputEditText etTitle = dialogView.findViewById(R.id.et_post_title);
        TextInputEditText etContent = dialogView.findViewById(R.id.et_post_content);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.community_create_post)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.publish, null)
                .create();

        dialog.setOnShowListener(d -> {
            View positive = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setOnClickListener(v -> {
                    String title = etTitle != null && etTitle.getText() != null
                            ? etTitle.getText().toString().trim() : "";
                    String content = etContent != null && etContent.getText() != null
                            ? etContent.getText().toString().trim() : "";
                    if (tilTitle != null) {
                        tilTitle.setError(null);
                    }
                    if (tilContent != null) {
                        tilContent.setError(null);
                    }
                    if (title.isEmpty()) {
                        if (tilTitle != null) {
                            tilTitle.setError(getString(R.string.community_title_required));
                        }
                        Toast.makeText(getContext(), R.string.community_title_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (content.isEmpty()) {
                        if (tilContent != null) {
                            tilContent.setError(getString(R.string.community_content_required));
                        }
                        Toast.makeText(getContext(), R.string.community_content_required, Toast.LENGTH_SHORT).show();
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
        long userId = sharedPreferencesUtil.getCurrentUserId();
        String username = sharedPreferencesUtil.getCurrentUsername();
        if (username == null || username.isEmpty()) {
            username = getString(R.string.community_anonymous_user);
        }
        repository.createPost(userId, username, title, content);
        Toast.makeText(getContext(), R.string.community_post_published, Toast.LENGTH_SHORT).show();
        loadPosts();
    }

    private void showReplyDialog(@NonNull CommunityPost post) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_reply_post, null, false);
        TextInputLayout tilReply = dialogView.findViewById(R.id.til_reply_content);
        TextInputEditText etReply = dialogView.findViewById(R.id.et_reply_content);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.community_reply_title)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.send, null)
                .create();

        dialog.setOnShowListener(d -> {
            View positive = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setOnClickListener(v -> {
                    String content = etReply != null && etReply.getText() != null
                            ? etReply.getText().toString().trim() : "";
                    if (tilReply != null) {
                        tilReply.setError(null);
                    }
                    if (content.isEmpty()) {
                        if (tilReply != null) {
                            tilReply.setError(getString(R.string.community_reply_required));
                        }
                        Toast.makeText(getContext(), R.string.community_reply_required, Toast.LENGTH_SHORT).show();
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
        long userId = sharedPreferencesUtil.getCurrentUserId();
        String username = sharedPreferencesUtil.getCurrentUsername();
        if (username == null || username.isEmpty()) {
            username = getString(R.string.community_anonymous_user);
        }
        CommunityComment comment = repository.addComment(post.getId(), userId, username, content);
        if (comment != null) {
            Toast.makeText(getContext(), R.string.community_reply_success, Toast.LENGTH_SHORT).show();
            loadPosts();
        }
    }

    private void showDeleteDialog(@NonNull CommunityPost post) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(R.string.community_delete_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    repository.deletePost(post);
                    Toast.makeText(getContext(), R.string.community_post_deleted, Toast.LENGTH_SHORT).show();
                    loadPosts();
                })
                .show();
    }

    private void loadPosts() {
        List<PostWithComments> posts = repository.getPosts(currentKeyword, currentSortOption);
        if (posts == null) {
            posts = new ArrayList<>();
        }
        postAdapter.submitList(posts);
        layoutEmptyState.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
        rvPosts.setVisibility(posts.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
