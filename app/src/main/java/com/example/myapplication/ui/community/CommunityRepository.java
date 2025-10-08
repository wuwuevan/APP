package com.example.myapplication.ui.community;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.myapplication.data.CommunityComment;
import com.example.myapplication.data.CommunityPost;
import com.example.myapplication.data.PostWithComments;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * 社区数据仓库，使用 SharedPreferences 存储帖子与回复，确保页面在没有数据库的情况下也能稳定运行。
 */
public class CommunityRepository {

    public enum SortOption {
        NEWEST,
        OLDEST,
        MOST_REPLIED
    }

    private static final String PREF_NAME = "community_repository";
    private static final String KEY_POSTS = "posts";
    private static final String KEY_COMMENTS = "comments";
    private static final String KEY_NEXT_POST_ID = "next_post_id";
    private static final String KEY_NEXT_COMMENT_ID = "next_comment_id";

    private final SharedPreferences sharedPreferences;
    private final List<CommunityPost> posts = new ArrayList<>();
    private final List<CommunityComment> comments = new ArrayList<>();

    private int nextPostId = 1;
    private int nextCommentId = 1;

    public CommunityRepository(@NonNull Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadFromStorage();
    }

    /**
     * 确保本地存在默认示例数据。
     */
    public void ensureSeedData() {
        if (!posts.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();

        CommunityPost welcome = createSeedPost("社区管家", "欢迎来到康复社区",
                "这里是大家交流康复心得、互相鼓励的温馨角落，欢迎分享你的故事和问题。",
                now - 3 * 24 * 60 * 60 * 1000L);
        CommunityPost rehabTips = createSeedPost("康复专家李医生", "术后第2周的注意事项",
                "保持关节适度活动、按时进行冰敷，若出现明显肿胀要及时反馈给医生。",
                now - 2 * 24 * 60 * 60 * 1000L);
        CommunityPost successStory = createSeedPost("王阿姨", "分享一下我的康复小成就",
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

        persist();
    }

    @NonNull
    public List<PostWithComments> getPosts(String keyword, SortOption sortOption) {
        String trimmedKeyword = keyword == null ? "" : keyword.trim();
        List<PostWithComments> result = new ArrayList<>();
        for (CommunityPost post : posts) {
            if (matchesKeyword(post, trimmedKeyword)) {
                List<CommunityComment> postComments = getCommentsInternal(post.getId());
                result.add(new PostWithComments(post, postComments));
            }
        }
        sortPosts(result, sortOption);
        return result;
    }

    @NonNull
    public CommunityPost createPost(long authorId, @NonNull String authorName, @NonNull String title,
                                    @NonNull String content) {
        CommunityPost post = new CommunityPost(authorId, authorName, title, content,
                System.currentTimeMillis());
        post.setId(nextPostId++);
        posts.add(post);
        persist();
        return post;
    }

    public CommunityComment addComment(int postId, long authorId, @NonNull String authorName,
                                       @NonNull String content) {
        CommunityComment comment = new CommunityComment(postId, authorId, authorName, content,
                System.currentTimeMillis());
        comment.setId(nextCommentId++);
        comments.add(comment);
        persist();
        return comment;
    }

    public void deletePost(@NonNull CommunityPost post) {
        Iterator<CommunityPost> postIterator = posts.iterator();
        boolean removed = false;
        while (postIterator.hasNext()) {
            if (postIterator.next().getId() == post.getId()) {
                postIterator.remove();
                removed = true;
                break;
            }
        }
        if (!removed) {
            return;
        }

        Iterator<CommunityComment> commentIterator = comments.iterator();
        while (commentIterator.hasNext()) {
            if (commentIterator.next().getPostId() == post.getId()) {
                commentIterator.remove();
            }
        }
        persist();
    }

    private boolean matchesKeyword(@NonNull CommunityPost post, @NonNull String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return true;
        }
        String lowerKeyword = keyword.toLowerCase(Locale.getDefault());
        return containsIgnoreCase(post.getTitle(), lowerKeyword)
                || containsIgnoreCase(post.getContent(), lowerKeyword)
                || containsIgnoreCase(post.getAuthorName(), lowerKeyword);
    }

    private boolean containsIgnoreCase(String source, String targetLower) {
        if (TextUtils.isEmpty(source)) {
            return false;
        }
        return source.toLowerCase(Locale.getDefault()).contains(targetLower);
    }

    private List<CommunityComment> getCommentsInternal(int postId) {
        List<CommunityComment> result = new ArrayList<>();
        for (CommunityComment comment : comments) {
            if (comment.getPostId() == postId) {
                result.add(comment);
            }
        }
        result.sort(Comparator.comparingLong(CommunityComment::getCreatedAt));
        return result;
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
        posts.sort(comparator);
    }

    private CommunityPost createSeedPost(@NonNull String author, @NonNull String title,
                                         @NonNull String content, long createdAt) {
        CommunityPost post = new CommunityPost(0, author, title, content, createdAt);
        post.setId(nextPostId++);
        posts.add(post);
        return post;
    }

    private void addSeedComment(int postId, @NonNull String author, @NonNull String content, long createdAt) {
        CommunityComment comment = new CommunityComment(postId, 0, author, content, createdAt);
        comment.setId(nextCommentId++);
        comments.add(comment);
    }

    private void loadFromStorage() {
        posts.clear();
        comments.clear();
        nextPostId = sharedPreferences.getInt(KEY_NEXT_POST_ID, 1);
        nextCommentId = sharedPreferences.getInt(KEY_NEXT_COMMENT_ID, 1);

        String postsJson = sharedPreferences.getString(KEY_POSTS, null);
        if (!TextUtils.isEmpty(postsJson)) {
            try {
                JSONArray postsArray = new JSONArray(postsJson);
                for (int i = 0; i < postsArray.length(); i++) {
                    JSONObject postObject = postsArray.getJSONObject(i);
                    CommunityPost post = parsePost(postObject);
                    posts.add(post);
                    nextPostId = Math.max(nextPostId, post.getId() + 1);
                }
            } catch (JSONException e) {
                posts.clear();
                nextPostId = 1;
            }
        }

        String commentsJson = sharedPreferences.getString(KEY_COMMENTS, null);
        if (!TextUtils.isEmpty(commentsJson)) {
            try {
                JSONArray commentsArray = new JSONArray(commentsJson);
                for (int i = 0; i < commentsArray.length(); i++) {
                    JSONObject commentObject = commentsArray.getJSONObject(i);
                    CommunityComment comment = parseComment(commentObject);
                    comments.add(comment);
                    nextCommentId = Math.max(nextCommentId, comment.getId() + 1);
                }
            } catch (JSONException e) {
                comments.clear();
                nextCommentId = 1;
            }
        }
    }

    private void persist() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_POSTS, postsToJson());
        editor.putString(KEY_COMMENTS, commentsToJson());
        editor.putInt(KEY_NEXT_POST_ID, nextPostId);
        editor.putInt(KEY_NEXT_COMMENT_ID, nextCommentId);
        editor.apply();
    }

    private String postsToJson() {
        JSONArray array = new JSONArray();
        for (CommunityPost post : posts) {
            array.put(postToJson(post));
        }
        return array.toString();
    }

    private String commentsToJson() {
        JSONArray array = new JSONArray();
        for (CommunityComment comment : comments) {
            array.put(commentToJson(comment));
        }
        return array.toString();
    }

    private JSONObject postToJson(CommunityPost post) {
        JSONObject object = new JSONObject();
        try {
            object.put("id", post.getId());
            object.put("authorId", post.getAuthorId());
            object.put("authorName", post.getAuthorName());
            object.put("title", post.getTitle());
            object.put("content", post.getContent());
            object.put("createdAt", post.getCreatedAt());
        } catch (JSONException ignored) {
        }
        return object;
    }

    private JSONObject commentToJson(CommunityComment comment) {
        JSONObject object = new JSONObject();
        try {
            object.put("id", comment.getId());
            object.put("postId", comment.getPostId());
            object.put("authorId", comment.getAuthorId());
            object.put("authorName", comment.getAuthorName());
            object.put("content", comment.getContent());
            object.put("createdAt", comment.getCreatedAt());
        } catch (JSONException ignored) {
        }
        return object;
    }

    private CommunityPost parsePost(JSONObject object) throws JSONException {
        CommunityPost post = new CommunityPost();
        post.setId(object.getInt("id"));
        post.setAuthorId(object.optLong("authorId", 0));
        post.setAuthorName(object.optString("authorName", ""));
        post.setTitle(object.optString("title", ""));
        post.setContent(object.optString("content", ""));
        post.setCreatedAt(object.optLong("createdAt", System.currentTimeMillis()));
        return post;
    }

    private CommunityComment parseComment(JSONObject object) throws JSONException {
        CommunityComment comment = new CommunityComment();
        comment.setId(object.getInt("id"));
        comment.setPostId(object.optInt("postId", -1));
        comment.setAuthorId(object.optLong("authorId", 0));
        comment.setAuthorName(object.optString("authorName", ""));
        comment.setContent(object.optString("content", ""));
        comment.setCreatedAt(object.optLong("createdAt", System.currentTimeMillis()));
        return comment;
    }
}
