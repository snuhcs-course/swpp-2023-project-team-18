package snu.swpp.moment.ui.main_writeview.DaySlide;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import snu.swpp.moment.R;
import snu.swpp.moment.databinding.DailyItemBinding;
import snu.swpp.moment.ui.main_writeview.ListViewAdapter;
import snu.swpp.moment.ui.main_writeview.ListViewItem;
public class DailyViewFragment extends Fragment {
    private DailyItemBinding binding;
    private List<ListViewItem> items;
    private ListViewAdapter mAdapter;
    private ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //ViewGroup rootView = (ViewGroup) inflater.inflate(
        //      R.layout.daily_item, container, false);
        binding = DailyItemBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        System.out.println("#DEBUG: OnCreateView");
        // Initialize ListView and related components
        initializeListView(root);
        return root;
    }
    private void initializeListView(View root) {
        listView = root.findViewById(R.id.listview_list);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        String formattedDate = sdf.format(calendar.getTime());
        items = new ArrayList<>();
        mAdapter = new ListViewAdapter(getContext(), items);
        listView.setAdapter(mAdapter);
    }
    private void addItem(String userInput) {
        String currentTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date());
        items.add(new ListViewItem(userInput, currentTime, ""));
        mAdapter.notifyDataSetChanged();
        listView.setSelection(items.size() - 1);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}