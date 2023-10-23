package snu.swpp.moment.ui.main_writeview;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;
import snu.swpp.moment.R;

public class ListViewAdapter extends BaseAdapter {

    private List<ListViewItem> items = null;
    private Context context;

    public ListViewAdapter(Context context, List<ListViewItem> items) {
        this.items = items;
        this.context = context;
    }

    public void notifyChange() {
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ListViewItem getItem(int position) {
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
            convertView = layoutInflater.inflate(R.layout.listview_item, parent, false);
        }
        TextView userInput = convertView.findViewById(R.id.listitem_userInput);
        TextView inputTime = convertView.findViewById(R.id.listitem_inputTime);
        TextView serverResponse = convertView.findViewById(R.id.listitem_serverResponse);
        ListViewItem item = items.get(position);
        userInput.setText(item.getUserInput());
        inputTime.setText(item.getInputTime());
        if (item.getServerResponse().isEmpty()) {
            serverResponse.setText("\u2026\n\nAI가 일기를 읽고 있어요");
            serverResponse.setGravity(Gravity.CENTER);
            serverResponse.setAlpha(0.5f);
        } else {
            serverResponse.setText(item.getServerResponse());
            serverResponse.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
            serverResponse.setAlpha(1);
        }
        return convertView;
    }
}








