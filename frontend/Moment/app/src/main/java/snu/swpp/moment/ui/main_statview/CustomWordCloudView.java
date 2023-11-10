package snu.swpp.moment.ui.main_statview;

import android.content.Context;
import android.util.AttributeSet;
import net.alhazmy13.wordcloud.WordCloudView;

public class CustomWordCloudView extends WordCloudView {

    /**
     * Instantiates a new Word cloud view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public CustomWordCloudView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        loadUrl("javascript:Android.calculateContentDimensions(document.body.scrollWidth, document.body.scrollHeight);");

    }
}
