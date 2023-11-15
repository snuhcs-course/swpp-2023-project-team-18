package snu.swpp.moment.api.response;

import java.util.List;

public class SearchEntriesGetResponse {
    private List<SearchEntry> searchEntries;

    public List<SearchEntry> getSearchentries() {
        return searchEntries;
    }

    public static class SearchEntry {
        private String title;
        private String created_at;
        private String emotion;
        private String content;
        private String moment;
        private int field;

        // Getters and setters for these fields
        public String getTitle() {
            return title;
        }

        public String getCreated_at() {
            return created_at;
        }

        public String getEmotion() {
            return emotion;
        }

        public String getContent() {
            return content;
        }

        public String getMoment() {
            return moment;
        }

        public int getField() {
            return field;
        }

    }
}
