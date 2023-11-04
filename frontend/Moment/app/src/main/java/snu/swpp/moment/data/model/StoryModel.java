package snu.swpp.moment.data.model;

import java.util.Date;
import java.util.List;
import snu.swpp.moment.utils.TimeConverter;

public class StoryModel {

    private final int id;
    private final String emotion;
    private final int score;
    private final String title;
    private final String content;
    private final List<HashtagModel> hashtags;
    private final Date createdAt;
    private final boolean isPointCompleted;
    private boolean isEmpty;

    public StoryModel(int id, String emotion, int score, String title, String content,
        List<HashtagModel> hashtags, Long createdAt, boolean isPointCompleted) {
        this.id = id;
        this.emotion = emotion;
        this.score = score;
        this.title = title;
        this.content = content;
        this.hashtags = hashtags;
        this.createdAt = TimeConverter.convertTimestampToDate(createdAt);
        this.isPointCompleted = isPointCompleted;
        this.isEmpty = false;
    }

    public int getId() {
        return id;
    }

    public String getEmotion() {
        return emotion;
    }

    public int getScore() {
        return score;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<HashtagModel> getHashtags() {
        return hashtags;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean getIsPointCompleted() {
        return isPointCompleted;
    }

    public static StoryModel empty() {
        StoryModel empty = new StoryModel(
            -1,
            "invalid",
            3,
            "",
            "",
            null,
            0L,
            false
        );
        empty.isEmpty = true;
        return empty;
    }
}
