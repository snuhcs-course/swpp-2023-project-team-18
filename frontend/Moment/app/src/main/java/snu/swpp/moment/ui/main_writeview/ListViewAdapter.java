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
import snu.swpp.moment.utils.AnimationProvider;

public class ListViewAdapter extends BaseAdapter {

    private List<ListViewItem> items = null;
    private final Context context;

    private int size;
    private AnimationProvider animationProvider;

    public ListViewAdapter(Context context, List<ListViewItem> items) {
        this.items = items;
        this.context = context;
        this.size = items.size();
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
            setWaitingResponse(serverResponse);
        } else {
            if (position >= size) {
                size = items.size();
                showUpdatedResponse(item.getServerResponse(), serverResponse);
            } else {
                showServerResponse(item.getServerResponse(), serverResponse);
            }
        }

        return convertView;
    }

    private void showUpdatedResponse(String response, TextView textView) {
        textView.setText(response);
        textView.setGravity(Gravity.START);
        textView.setAlpha(1);
        textView.clearAnimation();
        textView.startAnimation(animationProvider.fadeIn);
    }


    private void showServerResponse(String response, TextView textView) {
        textView.setText(response);
        textView.setGravity(Gravity.START);
        textView.setAlpha(1);
        textView.clearAnimation();
    }

    private void setWaitingResponse(TextView textView) {
        textView.setText("\u00B7  \u00B7  \u00B7 \nAI가 일기를 읽고 있어요");    // 가운뎃점
        textView.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
        textView.setAlpha(0.5f);
        textView.clearAnimation();
        textView.startAnimation(animationProvider.fadeInOut);
    }
}








