package snu.swpp.moment.ui.main_searchview;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.SearchRepository;
import snu.swpp.moment.ui.main_searchview.SearchViewModel;

public class SearchViewModelFactory implements ViewModelProvider.Factory {

    private final AuthenticationRepository authenticationRepository;
    private final SearchRepository searchRepository;

    public SearchViewModelFactory(AuthenticationRepository authenticationRepository,
        SearchRepository searchRepository) {
        this.authenticationRepository = authenticationRepository;
        this.searchRepository = searchRepository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SearchViewModel.class)) {
            return (T) new SearchViewModel(authenticationRepository, searchRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel Class");
    }
}
