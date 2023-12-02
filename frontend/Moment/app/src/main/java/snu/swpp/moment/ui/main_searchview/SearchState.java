package snu.swpp.moment.ui.main_searchview;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import snu.swpp.moment.api.response.SearchContentsResponse;
import snu.swpp.moment.api.response.SearchHashtagsResponse;

public class SearchState {

    List<SearchEntryState> searchEntries;
    Throwable error;
    boolean noResults; // 검색 결과 없음 상태

    public SearchState(List<SearchEntryState> searchEntries, Throwable error) {
        this.searchEntries = searchEntries;
        this.error = error;
    }

    public static SearchState withError(Throwable e) {
        return new SearchState(null, e);
    }

    public static SearchState fromSearchContentsResponse(SearchContentsResponse response) {

        if (response.getSearchEntries().size()==0) {
            // Handle the null case, e.g., return an empty list or a specific error state.
            return new SearchState(Collections.emptyList(), null);
        }
        return new SearchState(
            response.getSearchEntries().stream()
                .map(entry -> SearchEntryState.fromContentSearchEntry(entry))
                .collect(Collectors.toList()),
            null
        );

    }

    public static SearchState fromSearchHashtagsResponse(SearchHashtagsResponse response) {
        if (response.getSearchentries().size()==0) {
            // Handle the null case, e.g., return an empty list or a specific error state.
            return new SearchState(Collections.emptyList(), null);
        }
        return new SearchState(
            response.getSearchentries().stream()
                .map(entry -> SearchEntryState.fromHashtagSearchEntry(entry))
                .collect(Collectors.toList()),
            null
        );
    }

    //검색 결과가 없는경우
    public static SearchState withNoResult(){
        Log.d("SearchState",
                "State content withNoResult");
        return new SearchState(new ArrayList<>(), null);
    }
}
