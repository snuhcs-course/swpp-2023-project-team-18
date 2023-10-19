package snu.swpp.moment.ui.main_writeview;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;
import snu.swpp.moment.R;
public class ListView_Adapter extends BaseAdapter {
    private List<ListView_Item> items = null;
    private Context context;
    public ListView_Adapter(Context context, List<ListView_Item> items) {
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
    public ListView_Item getItem(int position) {
        return items.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listview_item, parent, false);
        }
        TextView userInput = convertView.findViewById(R.id.listitem_userInput);
        TextView inputTime = convertView.findViewById(R.id.listitem_inputTime);
        TextView serverResponse = convertView.findViewById(R.id.listitem_serverResponse);
        ListView_Item item = items.get(position);
        userInput.setText(item.getUserInput());
        inputTime.setText(item.getInputTime());
        serverResponse.setText(item.getServerResponse().isEmpty() ? "Waiting for server response..." : item.getServerResponse());
        serverResponse.setText("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        return convertView;
    }
}








