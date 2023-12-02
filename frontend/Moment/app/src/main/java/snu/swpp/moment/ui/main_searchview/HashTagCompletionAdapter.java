package snu.swpp.moment.ui.main_searchview;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import snu.swpp.moment.R;

public class HashTagCompletionAdapter extends
    RecyclerView.Adapter<HashTagCompletionAdapter.Holder> {

    class Holder extends RecyclerView.ViewHolder {

        Button button;

        public Holder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.hashtag_complete_item);
        }
    }

    List<String> items;
    SearchViewModel searchViewModel;

    public HashTagCompletionAdapter(SearchViewModel searchViewModel) {
        this.searchViewModel = searchViewModel;
        items = new ArrayList<>();
    }

    public void setItems(List<String> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HashTagCompletionAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent,
        int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.search_hashtag_completion_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HashTagCompletionAdapter.Holder holder, int position) {
        Log.d("holder", items.get(position));
        holder.button.setText(items.get(position));
        Log.d("hashtag", searchViewModel.selectedHashtag.getValue());

        // set moment-original red and font
        Typeface typeface = ResourcesCompat.getFont(holder.itemView.getContext(),
            R.font.maruburi_regular);
        if (holder.button.getText().equals(searchViewModel.selectedHashtag.getValue())) {
            holder.button.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
            holder.button.setTypeface(typeface, Typeface.BOLD);
        } else {
            holder.button.setTextColor(Color.BLACK);
            holder.button.setTypeface(typeface, Typeface.NORMAL);
        }

        holder.button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("holder", holder.button.getText().toString());
                searchViewModel.search(holder.button.getText().toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
