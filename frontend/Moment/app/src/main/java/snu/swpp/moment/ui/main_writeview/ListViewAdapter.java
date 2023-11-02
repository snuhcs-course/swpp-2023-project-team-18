package snu.swpp.moment.ui.main_writeview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.List;
import java.util.Objects;
import snu.swpp.moment.R;
import snu.swpp.moment.utils.AnimationProvider;

public class ListViewAdapter extends BaseAdapter {

    private List<ListViewItem> items = null;
    private final Context context;

    private int size;
    private final AnimationProvider animationProvider;

    private final MutableLiveData<Boolean> waitingAiReplySwitch = new MutableLiveData<>();

    public ListViewAdapter(Context context, List<ListViewItem> items) {
        this.items = items;
        this.context = context;
        this.size = items.size();
        this.animationProvider = new AnimationProvider(context);
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
        TextView momentText = convertView.findViewById(R.id.listitem_userInput);
        TextView momentTimeText = convertView.findViewById(R.id.listitem_inputTime);
        TextView aiReplyText = convertView.findViewById(R.id.listitem_serverResponse);

        ListViewItem item = items.get(position);
        momentText.setText(item.getUserInput());
        momentTimeText.setText(item.getTimestampText());

        Log.d("ListViewAdapter",
            String.format("getView() called: position %d, size %d, isWaitingAiReply: %s", position,
                size, item.isWaitingAiReply()));

        if (item.isWaitingAiReply()) {
            // 애니메이션 표시 & bottom button 비활성화
            showWaitingAnimation(aiReplyText, true);
            waitingAiReplySwitch.setValue(true);
        } else {
            showWaitingAnimation(aiReplyText, false);
            aiReplyText.setText(item.getAiReply());

            if (position >= size) {
                // 새로 추가된 item인 경우: AI 답글 애니메이션 보여줌
                size = items.size();
                aiReplyText.startAnimation(animationProvider.fadeIn);

                if (getWaitingAiReplySwitch()) {
                    // AI 답글 대기 중이었던 경우: 대기 끝난 상태로 변경
                    waitingAiReplySwitch.setValue(false);
                }
            }
        }

        return convertView;
    }

    public boolean getWaitingAiReplySwitch() {
        return Objects.requireNonNullElse(waitingAiReplySwitch.getValue(), false);
    }

    public void observeWaitingAiReplySwitch(Observer<Boolean> observer) {
        waitingAiReplySwitch.observeForever(observer);
    }

    private void showWaitingAnimation(TextView textView, boolean activate) {
        if (activate) {
            // AI 답글 대기중 애니메이션 표시
            textView.setText("· · ·\nAI가 일기를 읽고 있어요");    // 가운뎃점
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setAlpha(0.5f);
            textView.clearAnimation();
            textView.startAnimation(animationProvider.fadeInOut);
        } else {
            // AI 답글 대기중 애니메이션 제거
            textView.setText("");
            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            textView.setAlpha(1.0f);
            textView.clearAnimation();
        }
    }
}








