package snu.swpp.moment.api.response;

import java.util.List;

public class SearchHashtagsResponse {
    private List<SearchEntry> searchentries;

    public void setSearchentries(
        List<SearchEntry> searchentries) {
        this.searchentries = searchentries;
    }

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

        public void setTitle(String title) {
            this.title = title;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setCreated_at(long created_at) {
            this.created_at = created_at;
        }

        public void setEmotion(String emotion) {
            this.emotion = emotion;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
