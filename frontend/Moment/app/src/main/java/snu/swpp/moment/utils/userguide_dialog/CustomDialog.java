package snu.swpp.moment.utils.userguide_dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import snu.swpp.moment.R;

public class CustomDialog extends Dialog {

    public CustomDialog(@NonNull Context context) {
        super(context);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    public void setView(int destinationId) {
        if (destinationId == R.id.WriteView) {
            setContentView(R.layout.user_guide_writeview);
        } else if (destinationId == R.id.MonthView) {
            setContentView(R.layout.user_guide_monthview);
        } else if (destinationId == R.id.StatView) {
            setContentView(R.layout.user_guide_statview);
            ImageView hashCloudGif = findViewById(R.id.user_guide_statview_image3);
            Glide.with(getContext())
                    .load(R.drawable.user_guide_hashcloud_gif) // 로드할 GIF
                    .placeholder(R.drawable.user_guide_statview_hashcloud) // 로딩 중에 표시될 임시 이미지
                    .into(hashCloudGif); // 이미지를 표시할 ImageView
        } else if (destinationId == R.id.SearchView) {
            setContentView(R.layout.user_guide_searchview);
        }
    }

    // 다이얼로그의 애니메이션 설정
    public void setCustomAnimations() {
        View dialogContent = findViewById(android.R.id.content);
        if (dialogContent != null) {
            Animation fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
            dialogContent.startAnimation(fadeInAnimation);
        }
    }

    // 다이얼로그의 크기를 설정
    public void setCustomSize() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        if (window != null) {
            lp.copyFrom(window.getAttributes());
            // Dialog의 너비와 높이를 화면의 90%와 80%로 설정
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            int dialogWidth = (int) (displayMetrics.widthPixels * 0.9);
            int dialogHeight = (int) (displayMetrics.heightPixels * 0.8);
            lp.width = dialogWidth;
            lp.height = dialogHeight;
            window.setAttributes(lp);
        }
    }

    // 이해했어요 버튼 설정
    public void setCloseButton(int closeButtonId) {
        Button closeButton = findViewById(closeButtonId);
        if (closeButton != null) {
            closeButton.setActivated(true); // 버튼 활성화
            closeButton.setOnClickListener(v -> dismiss()); // 클릭 리스너 설정
        }
    }
}
