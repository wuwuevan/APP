package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.CommunityComment;
import com.example.myapplication.data.CommunityPost;
import com.example.myapplication.data.PostWithComments;
import com.example.myapplication.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单的社区帖子适配器，负责渲染帖子与回复。
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
        holder.bind(posts.get(position), currentUserId);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvAuthor;
        private final TextView tvTime;
        private final TextView tvTitle;
        private final TextView tvContent;
        private final TextView tvCommentCount;
        private final LinearLayout layoutComments;
        private final Button btnReply;
        private final Button btnDelete;
        private final OnPostActionListener listener;

        PostViewHolder(@NonNull View itemView, @NonNull OnPostActionListener listener) {
            super(itemView);
            this.listener = listener;
            tvAuthor = itemView.findViewById(R.id.tv_post_author);
            tvTime = itemView.findViewById(R.id.tv_post_time);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvContent = itemView.findViewById(R.id.tv_post_content);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            layoutComments = itemView.findViewById(R.id.layout_comments);
            btnReply = itemView.findViewById(R.id.btn_reply);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(@NonNull PostWithComments postWithComments, long currentUserId) {
            CommunityPost post = postWithComments.getPost();
            tvAuthor.setText(post.getAuthorName());
            tvTitle.setText(post.getTitle());
            tvContent.setText(post.getContent());
            tvTime.setText(TimeUtils.getRelativeTime(post.getCreatedAt()));

            List<CommunityComment> comments = postWithComments.getComments();
            int commentCount = comments.size();
            tvCommentCount.setText(itemView.getResources().getQuantityString(
                    R.plurals.community_comment_count, commentCount, commentCount));

            layoutComments.removeAllViews();
            if (commentCount == 0) {
                layoutComments.setVisibility(View.GONE);
            } else {
                LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                layoutComments.setVisibility(View.VISIBLE);
                for (CommunityComment comment : comments) {
                    View commentView = inflater.inflate(R.layout.item_community_comment, layoutComments, false);
                    TextView tvCommentAuthor = commentView.findViewById(R.id.tv_comment_author);
                    TextView tvCommentTime = commentView.findViewById(R.id.tv_comment_time);
                    TextView tvCommentContent = commentView.findViewById(R.id.tv_comment_content);
                    tvCommentAuthor.setText(comment.getAuthorName());
                    tvCommentContent.setText(comment.getContent());
                    tvCommentTime.setText(TimeUtils.getRelativeTime(comment.getCreatedAt()));
                    layoutComments.addView(commentView);
                }
            }

            btnReply.setOnClickListener(v -> listener.onReply(post));

            boolean canDelete = post.getAuthorId() == currentUserId && currentUserId != -1;
            btnDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
            btnDelete.setOnClickListener(v -> listener.onDelete(post));
        }
    }
}
