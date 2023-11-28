package snu.swpp.moment.utils.userguide_dialog;

import android.content.Context;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import snu.swpp.moment.R;

public class CustomDialogFactory {

    public static CustomDialog createDialog(Context context, int destinationId) {
        CustomDialog dialog = new CustomDialog(context);

        dialog.setView(destinationId); // 뷰 설정
        if(destinationId == R.id.StatView){
            //set gif image works
            ImageView hashCloudGif = dialog.findViewById(R.id.user_guide_statview_image3);
            Glide.with(context).load(R.drawable.gif_user_guide_hashcloud).into(hashCloudGif);
        }
        dialog.setCustomAnimations(); // 애니메이션 설정
        dialog.setCustomSize(); // 사이즈 설정
        dialog.setCloseButton(R.id.btn_close); // 닫기(이해했어요) 버튼 설정

        return dialog;
    }
}
