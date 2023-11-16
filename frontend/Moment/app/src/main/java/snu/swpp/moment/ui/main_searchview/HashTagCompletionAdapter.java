package snu.swpp.moment.ui.main_searchview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import snu.swpp.moment.R;

public class HashTagCompletionAdapter extends RecyclerView.Adapter<HashTagCompletionAdapter.Holder>{


    class Holder extends RecyclerView.ViewHolder{
        Button button;

        public Holder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.hashtag_complete_item);
        }
    }
    List<String> items;
    SearchViewModel searchViewModel;
    public HashTagCompletionAdapter(SearchViewModel searchViewModel){
        this.searchViewModel = searchViewModel;
        items = new ArrayList<>();
    }
    public void setItems(List<String> items){
        this.items = items;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public HashTagCompletionAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.search_hashtag_completion_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull HashTagCompletionAdapter.Holder holder, int position) {
        Log.d("holder",items.get(position));
        holder.button.setText(items.get(position));
        holder.button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchViewModel.search(holder.button.getText().toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

/*public class HashTagCompletionAdapter extends BaseAdapter {
    private List<String> items;
    private final Context context;
    private SearchViewModel viewModel;

    private int size;

    public HashTagCompletionAdapter(SearchViewModel viewModel, Context context, List<String> items) {
        this.viewModel = viewModel;
        this.items = items;
        this.context = context;
        this.size = items.size();
    }
    public void setData(List<String> data){
        this.items = data;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public String getItem(int position) {
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
            convertView = layoutInflater.inflate(R.layout.search_hashtag_completion_item, parent, false);
        }

        String item = items.get(position);
        Button button = convertView.findViewById(R.id.hashtag_complete_item);
        button.setText(item);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        Log.d("SearchAdapter",
                String.format("getView() called: position %d, size %d", position,
                        size));

        return convertView;
    }

}

*/