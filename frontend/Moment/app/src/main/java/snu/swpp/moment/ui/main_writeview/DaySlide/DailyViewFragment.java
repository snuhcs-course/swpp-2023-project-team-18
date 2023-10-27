package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import snu.swpp.moment.R;
import snu.swpp.moment.databinding.DailyItemBinding;
import snu.swpp.moment.ui.main_writeview.ListViewAdapter;
import snu.swpp.moment.ui.main_writeview.ListViewItem;

public class DailyViewFragment extends Fragment {

    private DailyItemBinding binding;
    private List<ListViewItem> listViewItems;
    private ListViewAdapter listViewAdapter;

    private ListFooterContainer listFooterContainer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        binding = DailyItemBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize ListView and related components
        initializeListView(root);

        return root;
    }

    private void initializeListView(View root) {
        listViewItems = new ArrayList<>();

        // TODO: moment GET API 호출해서 listViewItems 채우기

        listViewAdapter = new ListViewAdapter(getContext(), listViewItems);
        binding.dailyMomentList.setAdapter(listViewAdapter);
        View footerView = LayoutInflater.from(getContext())
            .inflate(R.layout.listview_footer, null, false);
        binding.dailyMomentList.addFooterView(footerView);

        // list footer 관리 객체 초기화
        listFooterContainer = new ListFooterContainer(footerView);
    }

    private void addItem(String userInput) {
        String currentTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date());
        listViewItems.add(new ListViewItem(userInput, currentTime, ""));
        listViewAdapter.notifyDataSetChanged();
        binding.dailyMomentList.setSelection(listViewItems.size() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}