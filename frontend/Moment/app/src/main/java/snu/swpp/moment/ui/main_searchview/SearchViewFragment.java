package snu.swpp.moment.ui.main_searchview;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import okhttp3.internal.Util;
import snu.swpp.moment.MainActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.data.factory.AuthenticationRepositoryFactory;
import snu.swpp.moment.data.factory.SearchRepositoryFactory;
import snu.swpp.moment.databinding.FragmentSearchviewBinding;
import snu.swpp.moment.ui.main_searchview.SearchViewModel.SearchType;
import snu.swpp.moment.utils.KeyboardUtils;

public class SearchViewFragment extends Fragment {

    private FragmentSearchviewBinding binding;
    private SearchViewModel searchViewModel;
    private Button hashtagButton;
    private Button contentButton;

    // For hashtag completion
    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;


    // 검색 모드를 나타내는 열거형
    private enum SearchMode {
        HASHTAG, CONTENT
    }

    private SearchMode currentSearchMode = SearchMode.HASHTAG;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        AuthenticationRepositoryFactory authenticationRepositoryFactory = new AuthenticationRepositoryFactory(
            context);
        SearchRepositoryFactory searchRepositoryFactory = new SearchRepositoryFactory();
        searchViewModel = new ViewModelProvider(this,
            new SearchViewModelFactory(
                authenticationRepositoryFactory.getRepository(),
                searchRepositoryFactory.getRepository()
            )
        ).get(SearchViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
        ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Include된 레이아웃에서 버튼 찾기
        hashtagButton = root.findViewById(R.id.search_hashtag_button);
        contentButton = root.findViewById(R.id.search_content_button);

        // 문자열 리소스를 사용하여 버튼 텍스트 설정
        hashtagButton.setText(getString(R.string.search_hashtag_button));
        contentButton.setText(getString(R.string.search_content_button));

        // Set up listeners for the buttons
        hashtagButton.setOnClickListener(v -> {
            currentSearchMode = SearchMode.HASHTAG;
            searchViewModel.setSearchType(SearchType.HASHTAG);
            updateSearchUI();
        });

        contentButton.setOnClickListener(v -> {
            currentSearchMode = SearchMode.CONTENT;
            searchViewModel.setSearchType(SearchType.CONTENT);
            updateSearchUI();
        });

        // binding.searchHashtagEdittext.addTextChangedListener과 같이 EditText를
        // TextWatcher을 통해 글자수가 0인 경우 검색 버튼이 눌리지 않도록 하는 로직
        binding.searchContentEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0)
                    binding.searchContentQueryButton.setEnabled(false);
                else {
                    binding.searchContentQueryButton.setEnabled(true);

                    binding.searchContentQueryButton.setOnClickListener(v -> {
                        String query;
                        query = binding.searchContentEdittext.getText().toString();
                        // 공백만 입력된 경우엔 API call 안함
                        if(query.trim().length()==0){
                            KeyboardUtils.hideSoftKeyboard(requireContext());
                        }
                        else{
                            searchViewModel.search(query);
                            KeyboardUtils.hideSoftKeyboard(requireContext());
                        }
                    });
                }
            }
        });


        SearchAdapter adapter = new SearchAdapter((MainActivity) getActivity(), new ArrayList<>());
        SearchAdapter hashtagSearchAdapter = new SearchAdapter((MainActivity) getActivity(),
            new ArrayList<>());
        binding.searchContentResult.setAdapter(adapter);
        binding.searchHashtagResult.setAdapter(hashtagSearchAdapter);

        searchViewModel.searchState.observe(getViewLifecycleOwner(), new Observer<SearchState>() {
            @Override
            public void onChanged(SearchState searchState) {
                if (searchViewModel.searchType.getValue() == SearchType.CONTENT) {
                    adapter.setData(searchState.searchEntries);
                    adapter.notifyDataSetChanged();
                    if(searchState.searchEntries.size()==0){
                        Log.d("SearchViewFragment", "여기");
                        // tr1. visibility 조정으로 해결이 되긴 하는데 View.VISIBLE 했을 때 다른 코드와 충돌날 까봐 걱정됨
                        displayNoResultsMessage(true);
                    }
                    else{
                        displayNoResultsMessage(false);
                    }
                } else {
                    // 여기서는 실시간으로 해시태그 검색결과가 없는것을 못띄워줌 - 추천된 해시태그를 눌러야 해당 entry수를 확인하고
                    displayNoResultsMessage(false);
                        hashtagSearchAdapter.setData(searchState.searchEntries);
                        hashtagSearchAdapter.notifyDataSetChanged();
                }
            }
        });

        // RecyclerView 설정
        RecyclerView hashtagCompletionRecyclerView = binding.hashtagCompleteList;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        hashtagCompletionRecyclerView.setLayoutManager(linearLayoutManager);

        HashTagCompletionAdapter hashTagCompletionAdapter = new HashTagCompletionAdapter(
            searchViewModel);
        binding.hashtagCompleteList.setAdapter(hashTagCompletionAdapter);

        searchViewModel.hashtagCompletionState.observe(getViewLifecycleOwner(),
            hashtagCompletionState -> {
                hashTagCompletionAdapter.setItems(hashtagCompletionState.hashtags);
                hashTagCompletionAdapter.notifyDataSetChanged();

                // 여기서 실시간으로 바꿔줘야할
                if(hashtagCompletionState.hashtags.size() == 0) {
                    binding.searchHashtagResult.setVisibility(View.GONE);
                    displayNoResultsMessage(true);
                }
                else{
                    binding.searchHashtagResult.setVisibility(View.VISIBLE);
                    displayNoResultsMessage(false);
                }
            });

        searchViewModel.selectedHashtag.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                hashTagCompletionAdapter.notifyDataSetChanged();
            }
        });
        // Hashtag 자동 완성을 위한 로직

        binding.searchHashtagEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 필요한 경우 여기에 코드 추가
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 필요한 경우 여기에 코드 추가
            }

            private String lastQuery = ""; // 이전 텍스트를 저장할 변수

            @Override
            public void afterTextChanged(Editable s) {
                String currentText = s.toString().trim(); // 공백 제거

                // Handler를 사용하여 이전에 예약된 콜백을 취소
                searchHandler.removeCallbacks(searchRunnable);

                // 현재 텍스트가 비어 있지 않은 경우에만 API 호출
                if (!currentText.isEmpty()) {
                    // 현재 텍스트가 이전 텍스트와 다른 경우에만 로그를 기록하고 lastQuery 업데이트
                    if (!currentText.equals(lastQuery)) {
                        Log.d("SearchViewFragment", "Call Hashtag Completion API");
                        lastQuery = currentText; // 현재 텍스트를 이전 텍스트로 업데이트
                    }

                    // 새로운 콜백을 예약
                    searchRunnable = () -> searchViewModel.completeHashtag(currentText);
                    searchHandler.postDelayed(searchRunnable, 300); // 300ms 후에 실행
                }
                if (currentText.isEmpty()) {
                    hashTagCompletionAdapter.setItems(new ArrayList<>());
                    hashtagSearchAdapter.setData(new ArrayList<>());
                }

            }
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
            binding.searchHashtagEdittext.requestFocus();
            binding.searchContentQueryButton.setVisibility(View.GONE);
            binding.searchContentEdittext.setVisibility(View.GONE);
            binding.searchHashtagResult.setVisibility(View.VISIBLE);
            binding.hashtagCompleteList.setVisibility(View.VISIBLE);

            // Content 검색 결과 숨기기
            binding.searchContentResult.setVisibility(View.GONE);

            // 기타 UI 설정
            binding.searchContentNoResultText.setVisibility(View.GONE);
        } else {
            hashtagButton.setActivated(false);
            contentButton.setActivated(true);
            binding.searchHashtagEdittext.setVisibility(View.GONE);
            binding.searchContentEdittext.setVisibility(View.VISIBLE);
            binding.searchContentEdittext.requestFocus();
            binding.searchContentQueryButton.setVisibility(View.VISIBLE);
            binding.searchContentResult.setVisibility(View.VISIBLE);
            binding.hashtagCompleteList.setVisibility(View.GONE);

            // HashTag 검색 결과 숨기기
            binding.searchHashtagResult.setVisibility(View.GONE);

            // 기타 UI 설정
            binding.searchContentNoResultText.setVisibility(View.GONE);
        }
    }
    private void displayNoResultsMessage(boolean show) {
        if(show){
            binding.searchContentNoResultText.setVisibility(View.VISIBLE);
        }
        else{
            binding.searchContentNoResultText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
