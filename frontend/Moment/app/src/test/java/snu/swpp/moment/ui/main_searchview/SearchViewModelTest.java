package snu.swpp.moment.ui.main_searchview;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import snu.swpp.moment.api.response.SearchContentsResponse;
import snu.swpp.moment.api.response.SearchHashtagsResponse;
import snu.swpp.moment.api.response.SearchHashtagsResponse.SearchEntry;
import snu.swpp.moment.data.callback.SearchEntriesGetCallBack;
import snu.swpp.moment.data.callback.SearchHashTagCompleteCallBack;
import snu.swpp.moment.data.callback.SearchHashTagGetCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.TokenModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.SearchRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.data.source.SearchRemoteDataSource;
import snu.swpp.moment.data.source.StoryRemoteDataSource;
import snu.swpp.moment.ui.main_searchview.SearchEntryState.FieldType;
import snu.swpp.moment.ui.main_searchview.SearchViewModel.SearchType;
import snu.swpp.moment.ui.main_statview.StatViewModel;

@RunWith(MockitoJUnitRunner.class)
public class SearchViewModelTest extends TestCase {
    private SearchViewModel viewModel;

    @Mock
    private SearchRemoteDataSource searchRemoteDataSourceDataSource;

    @Spy
    @InjectMocks
    private AuthenticationRepository authenticationRepository;


    @Spy
    @InjectMocks
    private SearchRepository searchRepository;
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        doAnswer(invocation -> {
            TokenCallBack callback = (TokenCallBack) invocation.getArguments()[0];
            callback.onSuccess();
            return null;
        }).when(authenticationRepository).isTokenValid(any());
        doReturn(new TokenModel("access", "refresh"))
            .when(authenticationRepository).getToken();

        viewModel = new SearchViewModel(authenticationRepository, searchRepository);
    }

    @Test
    public void hashtagCompleteTest(){
        doAnswer(invocation -> {
            SearchHashTagCompleteCallBack callBack = (SearchHashTagCompleteCallBack) invocation.getArguments()[2];
            callBack.onSuccess(Arrays.asList("h1","h2","h3"));
            return null;
        }).when(searchRemoteDataSourceDataSource).getCompleteHashTagList(anyString(),anyString(),any());
        viewModel.selectedHashtag.observeForever(s -> {});
        viewModel.completeHashtag("h");
        assertEquals(viewModel.hashtagCompletionState.getValue().hashtags,Arrays.asList("h1","h2","h3"));
    }
    @Test
    public void hashtagSearchTest(){
        doAnswer(invocation -> {
            SearchHashTagGetCallBack callBack = (SearchHashTagGetCallBack) invocation.getArguments()[2];
            SearchHashtagsResponse response = new SearchHashtagsResponse();
            SearchHashtagsResponse.SearchEntry entry = new SearchEntry();
            entry.setContent("c1");
            entry.setId(2);
            entry.setCreated_at(2);
            entry.setTitle("t1");
            entry.setEmotion("excited1");
            SearchHashtagsResponse.SearchEntry entry2 = new SearchEntry();
            entry2.setContent("c2");
            entry2.setId(1);
            entry2.setCreated_at(1);
            entry2.setTitle("t2");
            entry2.setEmotion("excited2");
            response.setSearchentries(Arrays.asList(entry,entry2));

            callBack.onSuccess(response);
            return null;
        }).when(searchRemoteDataSourceDataSource).getHashtagSearchList(anyString(),anyString(),any());
        viewModel.searchState.observeForever(s -> {});
        viewModel.setSearchType(SearchType.HASHTAG);
        viewModel.search("t");
        SearchState state = viewModel.searchState.getValue();
        SearchEntryState state1 = state.searchEntries.get(0);
        SearchEntryState state2 = state.searchEntries.get(1);
        assertEquals(state1.content,"c1");
        assertEquals(state1.title,"t1");
        assertEquals(state1.emotion,0);
        assertEquals(state1.id,2);
        assertEquals(state2.content,"c2");
        assertEquals(state2.title,"t2");
        assertEquals(state2.emotion,1);
        assertEquals(state2.id,1);



    }
    @Test
    public void contentSearchTest(){
        doAnswer(invocation -> {
            SearchEntriesGetCallBack callBack = (SearchEntriesGetCallBack) invocation.getArguments()[2];
            SearchContentsResponse response = new SearchContentsResponse();
            SearchContentsResponse.SearchEntry entry = new SearchContentsResponse.SearchEntry();
            entry.setContent("c1");
            entry.setId(2);
            entry.setCreated_at(2L);
            entry.setTitle("t1");
            entry.setEmotion("excited1");
            entry.setField(1);
            SearchContentsResponse.SearchEntry entry2 = new SearchContentsResponse.SearchEntry();
            entry2.setContent("c2");
            entry2.setId(1);
            entry2.setCreated_at(1L);
            entry2.setTitle("t2");
            entry2.setEmotion("excited2");
            entry2.setField(2);
            response.setSearchEntries(Arrays.asList(entry,entry2));

            callBack.onSuccess(response);
            return null;
        }).when(searchRemoteDataSourceDataSource).getContentSearchList(anyString(),anyString(),any());
        viewModel.searchState.observeForever(s -> {});
        viewModel.setSearchType(SearchType.CONTENT);
        viewModel.search("t");
        SearchState state = viewModel.searchState.getValue();
        SearchEntryState state1 = state.searchEntries.get(0);
        SearchEntryState state2 = state.searchEntries.get(1);
        assertEquals(state1.content,"c1");
        assertEquals(state1.title,"t1");
        assertEquals(state1.emotion,0);
        assertEquals(state1.id,2);
        assertEquals(state1.field, FieldType.MOMENT);
        assertEquals(state2.content,"c2");
        assertEquals(state2.title,"t2");
        assertEquals(state2.emotion,1);
        assertEquals(state2.id,1);
        assertEquals(state2.field,FieldType.TITLE);
    }


}
