package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchContentsResponse {

    @SerializedName("searchentries")
    private List<SearchEntry> searchEntries;

    public List<SearchEntry> getSearchentries() {
        return searchEntries;
    }

    public void setSearchEntries(
        List<SearchEntry> searchEntries) {
        this.searchEntries = searchEntries;
    }

    public static class SearchEntry {

        private String title;
        private Long created_at;
        private String emotion;
        private String content;
        private int id;
        private int field;

        // Getters and setters for these fields
        public String getTitle() {
            return title;
        }

        public Long getCreated_at() {
            return created_at;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setCreated_at(Long created_at) {
            this.created_at = created_at;
        }

        public void setEmotion(String emotion) {
            this.emotion = emotion;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setField(int field) {
            this.field = field;
        }

        public String getEmotion() {
            return emotion;
        }

        public String getContent() {
            return content;
        }

        public int getId() {
            return id;
        }

        public int getField() {
            return field;
        }

    }
}
