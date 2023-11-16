package snu.swpp.moment.ui.main_searchview;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.security.GeneralSecurityException;

import snu.swpp.moment.R;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.SearchRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.data.source.SearchRemoteDataSource;
import snu.swpp.moment.data.source.StoryRemoteDataSource;
import snu.swpp.moment.databinding.FragmentSearchviewBinding;
import snu.swpp.moment.ui.main_statview.SearchViewModelFactory;

public class SearchViewFragment extends Fragment {


    private FragmentSearchviewBinding binding;
    private SearchViewModel searchViewModel;
    private Button hashtagButton;
    private Button contentButton;

    // 검색 모드를 나타내는 열거형
    private enum SearchMode {
        HASHTAG, CONTENT
    }

    private SearchMode currentSearchMode = SearchMode.HASHTAG;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        AuthenticationRepository authenticationRepository = null;
        try {
            authenticationRepository = AuthenticationRepository.getInstance(context);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SearchRepository searchRepository = new SearchRepository(new SearchRemoteDataSource());
        searchViewModel = new ViewModelProvider(this, new SearchViewModelFactory(authenticationRepository, searchRepository ))
                .get(SearchViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Include된 레이아웃에서 버튼 찾기
        hashtagButton = root.findViewById(R.id.search_hashtag_button);
        contentButton = root.findViewById(R.id.search_content_button);

        // Set up listeners for the buttons
        hashtagButton.setOnClickListener(v -> {
            currentSearchMode = SearchMode.HASHTAG;
            updateSearchUI();
        });

        contentButton.setOnClickListener(v -> {
            currentSearchMode = SearchMode.CONTENT;
            updateSearchUI();
        });

        // Listener for the searchContentQueryButton
        binding.searchContentQueryButton.setOnClickListener(v -> {
            String query;
            if (currentSearchMode == SearchMode.HASHTAG) {
                query = binding.searchHashtagEdittext.getText().toString();
                searchViewModel.setSearchType(SearchViewModel.SearchType.HASHTAG);
            } else {
                query = binding.searchContentEdittext.getText().toString();
                searchViewModel.setSearchType(SearchViewModel.SearchType.CONTENT);
            }
            searchViewModel.search(query);
        });

        // 초기 상태 설정
        updateSearchUI();

        return root;
    }

    private void updateSearchUI() {
        if (currentSearchMode == SearchMode.HASHTAG) {
            hashtagButton.setActivated(true);
            contentButton.setActivated(false);
            binding.searchHashtagEdittext.setVisibility(View.VISIBLE);
            binding.searchContentQueryButton.setVisibility(View.GONE);
            binding.searchContentEdittext.setVisibility(View.GONE);
        } else {
            hashtagButton.setActivated(false);
            contentButton.setActivated(true);
            binding.searchHashtagEdittext.setVisibility(View.GONE);
            binding.searchContentEdittext.setVisibility(View.VISIBLE);
            binding.searchContentQueryButton.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
