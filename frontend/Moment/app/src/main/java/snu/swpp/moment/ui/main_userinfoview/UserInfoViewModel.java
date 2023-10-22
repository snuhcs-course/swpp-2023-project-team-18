package snu.swpp.moment.ui.main_userinfoview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserInfoViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public UserInfoViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}