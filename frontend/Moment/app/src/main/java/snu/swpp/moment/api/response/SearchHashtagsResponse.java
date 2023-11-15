package snu.swpp.moment.api.response;

import java.util.List;

public class SearchHashtagsResponse {
    private List<SearchEntry> searchentries;

    // Getter and setter for searchentries
    public List<SearchEntry> getSearchentries() {
        return searchentries;
    }

    public static class SearchEntry {
        private String title;
        private String content;
        private long created_at;
        private String emotion;
        private int id;

        // Getters and setters for these fields
        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public long getCreated_at() {
            return created_at;
        }

        public String getEmotion() {
            return emotion;
        }

        public int getId() {
            return id;
        }
    }
}
