package snu.swpp.moment.ui.main_searchview;

import java.time.LocalDate;
import snu.swpp.moment.api.response.SearchContentsResponse;
import snu.swpp.moment.api.response.SearchHashtagsResponse;
import snu.swpp.moment.utils.EmotionMap;
import snu.swpp.moment.utils.TimeConverter;

public class SearchEntryState {

    int id;
    int emotion;
    String title;
    String content;
    LocalDate createdAt;
    FieldType field;

    public SearchEntryState(int id, int emotion, String title, String content, LocalDate createdAt,
        FieldType field) {
        this.id = id;
        this.emotion = emotion;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.field = field;
    }

    public static SearchEntryState fromHashtagSearchEntry(
        SearchHashtagsResponse.SearchEntry entry) {
        return new SearchEntryState(entry.getId(),
            EmotionMap.getEmotionInt(entry.getEmotion()),
            entry.getTitle(),
            entry.getContent(),
            TimeConverter.convertDateToLocalDate(
                TimeConverter.convertTimestampToDate(entry.getCreated_at())),
            FieldType.INVALID);

    }

    public static SearchEntryState fromContentSearchEntry(
        SearchContentsResponse.SearchEntry entry) {
        return new SearchEntryState(entry.getId(),
            EmotionMap.getEmotionInt(entry.getEmotion()),
            entry.getTitle(),
            entry.getContent(),
            TimeConverter.convertDateToLocalDate(
                TimeConverter.convertTimestampToDate(entry.getCreated_at())),
            FieldType.fromInteger(entry.getField()));
    }

    public enum FieldType {
        INVALID(-1), STORY(0), MOMENT(1), TITLE(2);
        private final int id;

        FieldType(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }

        public static FieldType fromInteger(int x) {
            switch (x) {
                case 0:
                    return STORY;
                case 1:
                    return MOMENT;
                case 2:
                    return TITLE;
            }
            return INVALID;
        }
    }

}
