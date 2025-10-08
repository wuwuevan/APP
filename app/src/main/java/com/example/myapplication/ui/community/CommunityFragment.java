package com.example.myapplication.ui.community;

import android.content.Context;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.adapter.CommunityPostAdapter;
import com.example.myapplication.data.CommunityComment;
import com.example.myapplication.data.CommunityPost;
import com.example.myapplication.ui.community.CommunityViewModel.SortOption;
import com.example.myapplication.utils.SharedPreferencesUtil;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

/**
 * 社区页面，实现帖子浏览、搜索、排序、回复等基础功能。
 */
public class CommunityFragment extends Fragment {

    @Nullable
    private Context appContext;

    private TextInputEditText etSearch;
    private ChipGroup chipGroupSort;
    private RecyclerView rvPosts;
    private LinearLayout layoutEmptyState;
    private ExtendedFloatingActionButton fabCreatePost;

    private CommunityPostAdapter postAdapter;
    private SharedPreferencesUtil sharedPreferencesUtil;
    private CommunityViewModel viewModel;

    private SortOption currentSortOption = SortOption.NEWEST;
    private String currentKeyword = "";
    private TextWatcher searchWatcher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context.getApplicationContext();
        initDependencies(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);
        if (sharedPreferencesUtil == null) {
            initDependencies(view.getContext());
        }
        viewModel = new ViewModelProvider(this).get(CommunityViewModel.class);
        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupSortChips();
        setupFab();
        observePosts();
        loadPosts();
        return view;
    }

    private void initDependencies(@NonNull Context context) {
        Context safeContext = context.getApplicationContext() != null
                ? context.getApplicationContext() : context;
        sharedPreferencesUtil = new SharedPreferencesUtil(safeContext);
    }

    private void initViews(@NonNull View view) {
        etSearch = view.findViewById(R.id.et_search_post);
        chipGroupSort = view.findViewById(R.id.chip_group_sort);
        rvPosts = view.findViewById(R.id.recycler_posts);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        fabCreatePost = view.findViewById(R.id.fab_create_post);
    }

    private void setupRecyclerView() {
        rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
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

    private void observePosts() {
        if (viewModel == null) {
            return;
        }
        viewModel.getPostsLiveData().observe(getViewLifecycleOwner(), posts -> {
            if (postAdapter != null) {
                postAdapter.submitList(posts != null ? posts : new ArrayList<>());
            }
            boolean isEmpty = posts == null || posts.isEmpty();
            if (layoutEmptyState != null) {
                layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            }
            if (rvPosts != null) {
                rvPosts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void setupSearch() {
        searchWatcher = new TextWatcher() {
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
        };
        etSearch.addTextChangedListener(searchWatcher);
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
        if (!isAdded()) {
            return;
        }
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_dialog_create_post, null, false);
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
                        showToast(R.string.community_title_required);
                        return;
                    }
                    if (content.isEmpty()) {
                        if (tilContent != null) {
                            tilContent.setError(getString(R.string.community_content_required));
                        }
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
        if (sharedPreferencesUtil == null || viewModel == null) {
            return;
        }
        Context context = getSafeContext();
        if (context == null) {
            return;
        }
        long userId = sharedPreferencesUtil.getCurrentUserId();
        String username = sharedPreferencesUtil.getCurrentUsername();
        if (username == null || username.isEmpty()) {
            username = context.getString(R.string.community_anonymous_user);
        }
        viewModel.createPost(userId, username, title, content);
        showToast(R.string.community_post_published);
        loadPosts();
    }

    private void showReplyDialog(@NonNull CommunityPost post) {
        if (!isAdded()) {
            return;
        }
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.view_dialog_reply_post, null, false);
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
        if (sharedPreferencesUtil == null || viewModel == null) {
            return;
        }
        Context context = getSafeContext();
        if (context == null) {
            return;
        }
        long userId = sharedPreferencesUtil.getCurrentUserId();
        String username = sharedPreferencesUtil.getCurrentUsername();
        if (username == null || username.isEmpty()) {
            username = context.getString(R.string.community_anonymous_user);
        }
        CommunityComment comment = viewModel.addComment(post.getId(), userId, username, content);
        if (comment != null) {
            showToast(R.string.community_reply_success);
            loadPosts();
        }
    }

    private void showDeleteDialog(@NonNull CommunityPost post) {
        if (!isAdded()) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(R.string.community_delete_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    long userId = sharedPreferencesUtil != null
                            ? sharedPreferencesUtil.getCurrentUserId() : -1;
                    if (viewModel != null && userId != -1
                            && viewModel.deletePost(post, userId)) {
                        showToast(R.string.community_post_deleted);
                        loadPosts();
                    } else {
                        showToast(R.string.community_delete_not_allowed);
                    }
                })
                .show();
    }

    private void loadPosts() {
        if (viewModel == null) {
            return;
        }
        viewModel.loadPosts(currentKeyword, currentSortOption);
    }

    @Nullable
    private Context getSafeContext() {
        if (isAdded()) {
            return requireContext();
        }
        return appContext;
    }

    private void showToast(int messageResId) {
        Context context = getSafeContext();
        if (context == null) {
            return;
        }
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (etSearch != null && searchWatcher != null) {
            etSearch.removeTextChangedListener(searchWatcher);
        }
        if (rvPosts != null) {
            rvPosts.setAdapter(null);
        }
        searchWatcher = null;
        etSearch = null;
        chipGroupSort = null;
        rvPosts = null;
        layoutEmptyState = null;
        fabCreatePost = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        sharedPreferencesUtil = null;
        appContext = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAdapterUser();
        loadPosts();
    }

    private void updateAdapterUser() {
        if (postAdapter == null) {
            return;
        }
        long userId = sharedPreferencesUtil != null
                ? sharedPreferencesUtil.getCurrentUserId() : -1;
        postAdapter.setCurrentUser(userId);
    }
}
