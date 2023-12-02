package snu.swpp.moment.utils.userguide_dialog;

import android.content.Context;
import snu.swpp.moment.R;

public class CustomDialogFactory {

    public static CustomDialog createDialog(Context context, int destinationId) {
        CustomDialog dialog = new CustomDialog(context);

        dialog.setView(destinationId); // 뷰 설정
        dialog.setCustomAnimations(); // 애니메이션 설정
        dialog.setCustomSize(); // 사이즈 설정
        dialog.setCloseButton(R.id.btn_close); // 닫기(이해했어요) 버튼 설정

        return dialog;
    }
}
