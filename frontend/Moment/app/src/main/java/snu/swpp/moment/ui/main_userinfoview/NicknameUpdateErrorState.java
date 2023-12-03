package snu.swpp.moment.ui.main_userinfoview;

public class NicknameUpdateErrorState {

    private final Exception error;

    public NicknameUpdateErrorState(Exception error) {
        this.error = error;
    }

    public Exception getError() {
        return error;
    }
}
