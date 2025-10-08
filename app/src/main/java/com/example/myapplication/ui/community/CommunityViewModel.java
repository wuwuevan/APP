package com.example.myapplication.ui.community;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.data.CommunityComment;
import com.example.myapplication.data.CommunityPost;
import com.example.myapplication.data.PostWithComments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * 负责管理社区帖子与回复的简单内存数据源，提供筛选、排序等功能。
 */
public class CommunityViewModel extends ViewModel {

    public enum SortOption {
        NEWEST,
        OLDEST,
        MOST_REPLIED
    }

    private final MutableLiveData<List<PostWithComments>> postsLiveData =
            new MutableLiveData<>(new ArrayList<>());

    private final List<CommunityPost> posts = new ArrayList<>();
    private final List<CommunityComment> comments = new ArrayList<>();

    private int nextPostId = 1;
    private int nextCommentId = 1;
    private boolean seeded = false;

    private String lastKeyword = "";
    private SortOption lastSortOption = SortOption.NEWEST;

    public CommunityViewModel() {
        ensureSeedData();
        publish();
    }

    public LiveData<List<PostWithComments>> getPostsLiveData() {
        return postsLiveData;
    }

    public void loadPosts(String keyword, SortOption sortOption) {
        lastKeyword = keyword == null ? "" : keyword.trim();
        lastSortOption = sortOption == null ? SortOption.NEWEST : sortOption;
        publish();
    }

    @NonNull
    public CommunityPost createPost(long authorId, @NonNull String authorName,
                                    @NonNull String title, @NonNull String content) {
        CommunityPost post = new CommunityPost(authorId, authorName, title, content,
                System.currentTimeMillis());
        post.setId(nextPostId++);
        posts.add(post);
        publish();
        return post;
    }

    public CommunityComment addComment(int postId, long authorId, @NonNull String authorName,
                                       @NonNull String content) {
        CommunityPost target = findPostById(postId);
        if (target == null) {
            return null;
        }
        CommunityComment comment = new CommunityComment(postId, authorId, authorName, content,
                System.currentTimeMillis());
        comment.setId(nextCommentId++);
        comments.add(comment);
        publish();
        return comment;
    }

    public boolean deletePost(@NonNull CommunityPost post, long requesterId) {
        if (post.getAuthorId() != requesterId) {
            return false;
        }
        boolean removed = false;
        Iterator<CommunityPost> postIterator = posts.iterator();
        while (postIterator.hasNext()) {
            if (postIterator.next().getId() == post.getId()) {
                postIterator.remove();
                removed = true;
                break;
            }
        }
        if (!removed) {
            return false;
        }
        Iterator<CommunityComment> commentIterator = comments.iterator();
        while (commentIterator.hasNext()) {
            if (commentIterator.next().getPostId() == post.getId()) {
                commentIterator.remove();
            }
        }
        publish();
        return true;
    }

    private void publish() {
        List<PostWithComments> snapshot = new ArrayList<>();
        for (CommunityPost post : posts) {
            if (matchesKeyword(post, lastKeyword)) {
                snapshot.add(new PostWithComments(post, collectComments(post.getId())));
            }
        }
        sortPosts(snapshot, lastSortOption);
        postsLiveData.setValue(snapshot);
    }

    private void ensureSeedData() {
        if (seeded || !posts.isEmpty()) {
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

    private CommunityPost findPostById(int postId) {
        for (CommunityPost post : posts) {
            if (post.getId() == postId) {
                return post;
            }
        }
        return null;
    }

    private List<CommunityComment> collectComments(int postId) {
        List<CommunityComment> result = new ArrayList<>();
        for (CommunityComment comment : comments) {
            if (comment.getPostId() == postId) {
                result.add(comment);
            }
        }
        result.sort(Comparator.comparingLong(CommunityComment::getCreatedAt));
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

    private boolean containsIgnoreCase(String source, String targetLower) {
        if (source == null || source.isEmpty()) {
            return false;
        }
        return source.toLowerCase(Locale.getDefault()).contains(targetLower);
    }

    private void sortPosts(@NonNull List<PostWithComments> data, SortOption sortOption) {
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
                comparator = (p1, p2) -> Long.compare(p2.getPost().getCreatedAt(),
                        p1.getPost().getCreatedAt());
                break;
        }
        data.sort(comparator);
    }
}
