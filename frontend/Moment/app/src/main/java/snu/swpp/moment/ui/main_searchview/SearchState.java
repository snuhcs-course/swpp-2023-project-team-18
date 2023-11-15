package snu.swpp.moment.ui.main_searchview;

import java.util.List;

public class SearchState {
    private final List<String> searchResults; // 검색 결과를 저장하는 리스트
    private final Exception error; // 오류 발생 시 저장하는 필드

    public SearchState(List<String> searchResults, Exception error) {
        this.searchResults = searchResults;
        this.error = error;
    }

    public List<String> getSearchResults() {
        return searchResults;
    }

    public Exception getError() {
        return error;
    }

    public static SearchState success(List<String> searchResults) {
        return new SearchState(searchResults, null);
    }

    public static SearchState failure(Exception error) {
        return new SearchState(null, error);
    }
}
