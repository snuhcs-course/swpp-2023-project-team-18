package snu.swpp.moment.ui.main_searchview;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import java.util.List;
import java.util.Objects;
import snu.swpp.moment.MainActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.ui.main_writeview.slideview.ListViewItem;
import snu.swpp.moment.utils.AnimationProvider;

public class SearchAdapter extends BaseAdapter {

    private  List<SearchEntryState> items;
    private final MainActivity context;

    private int size;


    public SearchAdapter(MainActivity context, List<SearchEntryState> items) {
        this.items = items;
        this.context = context;
        this.size = items.size();
    }
    public void setData(List<SearchEntryState> data){
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
        emotionView.setImageDrawable(context.getDrawable(R.drawable.icon_sun_cloud));

        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                context.navigateToWriteViewPage(item.createdAt);
            }
        });
        Log.d("SearchAdapter",
            String.format("getView() called: position %d, size %d", position,
                size));





        return convertView;
    }






}
