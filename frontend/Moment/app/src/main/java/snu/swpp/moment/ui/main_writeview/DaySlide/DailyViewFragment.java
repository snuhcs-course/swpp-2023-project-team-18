package snu.swpp.moment.ui.main_writeview.DaySlide;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import snu.swpp.moment.R;
import snu.swpp.moment.databinding.DailyItemBinding;
import snu.swpp.moment.ui.main_writeview.ListView_Adapter;
import snu.swpp.moment.ui.main_writeview.ListView_Item;
public class DailyViewFragment extends Fragment {
    private DailyItemBinding binding;
    private List<ListView_Item> items;
    private ListView_Adapter mAdapter;
    private Button addButton, submitButton;
    private ListView listView;
    private EditText inputEditText;
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
        TextView textView = root.findViewById(R.id.date);
        textView.setText(formattedDate);
        items = new ArrayList<>();
        mAdapter = new ListView_Adapter(getContext(), items);
        listView.setAdapter(mAdapter);
        View footerView = LayoutInflater.from(getContext()).inflate(R.layout.listview_footer, listView, false);
        listView.addFooterView(footerView);
        addButton = footerView.findViewById(R.id.add_button);
        inputEditText = footerView.findViewById(R.id.inputEditText);
        submitButton = footerView.findViewById(R.id.submit_button);
        addButton.setOnClickListener(v -> {
            inputEditText.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
            inputEditText.setSingleLine(true);
        });
        submitButton.setOnClickListener(v -> {
            String text = inputEditText.getText().toString();
            if (!text.isEmpty()) {
                addItem(text);
                inputEditText.setText("");
                inputEditText.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
                addButton.setVisibility(View.VISIBLE);
            }
        });
    }
    private void addItem(String userInput) {
        String currentTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date());
        items.add(new ListView_Item(userInput, currentTime, ""));
        mAdapter.notifyDataSetChanged();
        listView.setSelection(items.size() - 1);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}