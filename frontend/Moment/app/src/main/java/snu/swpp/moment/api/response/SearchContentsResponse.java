package snu.swpp.moment.api.response;

import java.util.List;

public class SearchContentsResponse {
    private List<SearchEntry> searchEntries;

    public List<SearchEntry> getSearchentries() {
        return searchEntries;
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
