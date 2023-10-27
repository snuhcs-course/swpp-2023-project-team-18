package snu.swpp.moment.data.model;

import java.util.Date;
import snu.swpp.moment.utils.TimeConverter;

public class Story {

    private final int id;
    private final String emotion;
    private final int score;
    private final String title;
    private final String content;
    private final Date createdAt;

    public Story(int id, String emotion, int score, String title, String content, Long createdAt) {
        this.id = id;
        this.emotion = emotion;
        this.score = score;
        this.title = title;
        this.content = content;
        this.createdAt = TimeConverter.convertLongToDate(createdAt);
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

    public Date getCreatedAt() {
        return createdAt;
    }
}
