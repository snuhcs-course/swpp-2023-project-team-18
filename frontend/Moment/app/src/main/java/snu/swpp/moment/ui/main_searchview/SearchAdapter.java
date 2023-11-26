package snu.swpp.moment.ui.main_searchview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import snu.swpp.moment.MainActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.utils.CalendarUtilsKt;

public class SearchAdapter extends BaseAdapter {

    private List<SearchEntryState> items;
    private final MainActivity context;

    private int size;


    public SearchAdapter(MainActivity context, List<SearchEntryState> items) {
        this.items = items;
        this.context = context;
        this.size = items.size();
    }

    public void setData(List<SearchEntryState> data) {
        this.items = data;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public SearchEntryState getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.search_item, parent, false);
        }
        TextView titleView = convertView.findViewById(R.id.search_title);
        ImageView emotionView = convertView.findViewById(R.id.search_emotion);
        TextView contentView = convertView.findViewById(R.id.search_content);
        TextView dateView = convertView.findViewById(R.id.search_date);

        SearchEntryState item = items.get(position);
        titleView.setText(item.title);
        contentView.setText(item.content);
        dateView.setText(item.createdAt.toString());
        emotionView.setImageDrawable(
            context.getDrawable(CalendarUtilsKt.convertEmotionImage(item.emotion)));

        convertView.setOnClickListener(v -> context.navigateToWriteViewPage(item.createdAt));
        Log.d("SearchAdapter",
            String.format("getView() called: position %d, size %d", position,
                size));

        return convertView;
    }
}
