package snu.swpp.moment.ui.main_writeview;

import java.util.ArrayList;
import snu.swpp.moment.data.model.MomentPairModel;

public class MomentUiState {

    private final int error;
    private final ArrayList<MomentPairModel> momentPairsList;

    public MomentUiState(int error, ArrayList<MomentPairModel> list) {
        this.error = error;
        this.momentPairsList = list;
    }

    public int getError() {
        return error;
    }

    public ArrayList<MomentPairModel> getMomentPairsList() {
        return momentPairsList;
    }

    public int getMomentPairsListSize() {
        return momentPairsList.size();
    }

    public void addMomentPair(MomentPairModel momentPair) {
        momentPairsList.add(momentPair);
    }

}
