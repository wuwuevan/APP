package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.CommunityPost;
import com.example.myapplication.data.PostWithComments;
import com.example.myapplication.utils.TimeUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * 社区帖子列表适配器。
 */
public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder> {

    public interface OnPostActionListener {
        void onReply(@NonNull CommunityPost post);

        void onDelete(@NonNull CommunityPost post);
    }

    private final List<PostWithComments> posts = new ArrayList<>();
    private final OnPostActionListener listener;
    private long currentUserId = -1;

    public CommunityPostAdapter(@NonNull OnPostActionListener listener) {
        this.listener = listener;
    }

    public void setCurrentUser(long userId) {
        this.currentUserId = userId;
        notifyDataSetChanged();
    }

    public void submitList(@NonNull List<PostWithComments> newPosts) {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_post, parent, false);
        return new PostViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostWithComments postWithComments = posts.get(position);
        holder.bind(postWithComments, currentUserId);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatar;
        private final TextView tvAuthor;
        private final TextView tvTime;
        private final TextView tvTitle;
        private final TextView tvContent;
        private final TextView tvCommentCount;
        private final MaterialButton btnReply;
        private final MaterialButton btnDelete;
        private final RecyclerView rvComments;
        private final CommunityCommentAdapter commentAdapter;
        private final OnPostActionListener listener;

        PostViewHolder(@NonNull View itemView, @NonNull OnPostActionListener listener) {
            super(itemView);
            this.listener = listener;
            tvAvatar = itemView.findViewById(R.id.tv_post_avatar);
            tvAuthor = itemView.findViewById(R.id.tv_post_author);
            tvTime = itemView.findViewById(R.id.tv_post_time);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvContent = itemView.findViewById(R.id.tv_post_content);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            btnReply = itemView.findViewById(R.id.btn_reply);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            rvComments = itemView.findViewById(R.id.recycler_comments);
            rvComments.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvComments.setNestedScrollingEnabled(false);
            commentAdapter = new CommunityCommentAdapter();
            rvComments.setAdapter(commentAdapter);
        }

        void bind(@NonNull PostWithComments postWithComments, long currentUserId) {
            CommunityPost post = postWithComments.getPost();
            tvAuthor.setText(post.getAuthorName());
            tvTitle.setText(post.getTitle());
            tvContent.setText(post.getContent());
            tvTime.setText(TimeUtils.getRelativeTime(post.getCreatedAt()));
            tvAvatar.setText(getAvatarText(post.getAuthorName()));

            List<com.example.myapplication.data.CommunityComment> comments = postWithComments.getComments();
            commentAdapter.submitList(comments);
            int commentCount = comments.size();
            tvCommentCount.setText(itemView.getResources().getQuantityString(
                    R.plurals.community_comment_count, commentCount, commentCount));

            btnReply.setOnClickListener(v -> listener.onReply(post));

            boolean canDelete = post.getAuthorId() == currentUserId && currentUserId != -1;
            btnDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
            btnDelete.setOnClickListener(v -> listener.onDelete(post));
        }

        private String getAvatarText(String authorName) {
            if (authorName == null || authorName.isEmpty()) {
                return "?";
            }
            String trimmed = authorName.trim();
            if (trimmed.isEmpty()) {
                return "?";
            }
            return trimmed.substring(trimmed.length() - 1);
        }
    }
}
