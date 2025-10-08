package com.example.myapplication.ui.community;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.CommunityComment;
import com.example.myapplication.data.CommunityCommentDao;
import com.example.myapplication.data.CommunityPost;
import com.example.myapplication.data.CommunityPostDao;
import com.example.myapplication.data.PostWithComments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 社区数据仓库，封装帖子与回复的数据库操作。
 */
public class CommunityRepository {

    public enum SortOption {
        NEWEST,
        OLDEST,
        MOST_REPLIED
    }

    private final CommunityPostDao postDao;
    private final CommunityCommentDao commentDao;

    public CommunityRepository(@NonNull Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.postDao = database.communityPostDao();
        this.commentDao = database.communityCommentDao();
    }

    /**
     * 确保数据库中存在默认的示例帖子。
     */
    public void ensureSeedData() {
        if (postDao.getPostCount() > 0) {
            return;
        }

        long now = System.currentTimeMillis();

        CommunityPost welcomePost = new CommunityPost(0, "社区管家",
                "欢迎来到康复社区",
                "这里是大家交流康复心得、互相鼓励的温馨角落，欢迎分享你的故事和问题。",
                now - 3 * 24 * 60 * 60 * 1000L);
        int welcomeId = (int) postDao.insert(welcomePost);
        welcomePost.setId(welcomeId);

        CommunityPost rehabTips = new CommunityPost(0, "康复专家李医生",
                "术后第2周的注意事项",
                "保持关节适度活动、按时进行冰敷，若出现明显肿胀要及时反馈给医生。",
                now - 2 * 24 * 60 * 60 * 1000L);
        int rehabTipsId = (int) postDao.insert(rehabTips);
        rehabTips.setId(rehabTipsId);

        CommunityPost successStory = new CommunityPost(0, "王阿姨",
                "分享一下我的康复小成就",
                "在坚持了三周的康复训练后，现在已经可以独立上下楼梯啦！感谢大家的鼓励。",
                now - 20 * 60 * 60 * 1000L);
        int successStoryId = (int) postDao.insert(successStory);
        successStory.setId(successStoryId);

        commentDao.insert(new CommunityComment(welcomeId, 0, "陈先生",
                "谢谢提醒，我刚入群就看到这么暖心的欢迎帖！", now - 2 * 24 * 60 * 60 * 1000L));
        commentDao.insert(new CommunityComment(welcomeId, 0, "赵护士",
                "有任何问题都可以@我，我们会尽快解答~", now - 36 * 60 * 60 * 1000L));
        commentDao.insert(new CommunityComment(rehabTipsId, 0, "杨女士",
                "请问冰敷一次大概多久合适？", now - 30 * 60 * 60 * 1000L));
        commentDao.insert(new CommunityComment(rehabTipsId, 0, "康复专家李医生",
                "一次15-20分钟即可，记得间隔至少40分钟再进行下一次。", now - 28 * 60 * 60 * 1000L));
        commentDao.insert(new CommunityComment(successStoryId, 0, "刘先生",
                "太棒了！我也在努力，希望能像您一样。", now - 18 * 60 * 60 * 1000L));
    }

    /**
     * 获取筛选后的帖子列表。
     */
    @NonNull
    public List<PostWithComments> getPosts(String keyword, SortOption sortOption) {
        List<PostWithComments> posts;
        if (TextUtils.isEmpty(keyword)) {
            posts = postDao.getAllPostsWithComments();
        } else {
            String pattern = "%" + keyword + "%";
            posts = postDao.searchPostsWithComments(pattern);
        }

        if (posts == null) {
            posts = new ArrayList<>();
        }

        sortPosts(posts, sortOption);
        return posts;
    }

    private void sortPosts(@NonNull List<PostWithComments> posts, SortOption sortOption) {
        Comparator<PostWithComments> comparator;
        switch (sortOption) {
            case OLDEST:
                comparator = Comparator.comparingLong(p -> p.getPost().getCreatedAt());
                break;
            case MOST_REPLIED:
                comparator = (p1, p2) -> {
                    int diff = p2.getComments().size() - p1.getComments().size();
                    if (diff != 0) {
                        return diff;
                    }
                    return Long.compare(p2.getPost().getCreatedAt(), p1.getPost().getCreatedAt());
                };
                break;
            case NEWEST:
            default:
                comparator = (p1, p2) -> Long.compare(p2.getPost().getCreatedAt(), p1.getPost().getCreatedAt());
                break;
        }
        Collections.sort(posts, comparator);
    }

    /**
     * 创建一条新的帖子。
     */
    @NonNull
    public CommunityPost createPost(long authorId, @NonNull String authorName, @NonNull String title,
                                    @NonNull String content) {
        CommunityPost post = new CommunityPost(authorId, authorName, title, content, System.currentTimeMillis());
        int id = (int) postDao.insert(post);
        post.setId(id);
        return post;
    }

    /**
     * 为帖子添加回复。
     */
    public CommunityComment addComment(int postId, long authorId, @NonNull String authorName,
                                       @NonNull String content) {
        CommunityComment comment = new CommunityComment(postId, authorId, authorName, content, System.currentTimeMillis());
        int id = (int) commentDao.insert(comment);
        comment.setId(id);
        return comment;
    }

    /**
     * 删除帖子（仅作者可操作）。
     */
    public void deletePost(@NonNull CommunityPost post) {
        postDao.delete(post);
    }
}
