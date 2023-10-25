package snu.swpp.moment.ui.main_writeview;

import java.util.ArrayList;
import snu.swpp.moment.data.model.MomentPair;

public class MomentUiState {

    private final int error;
    private final ArrayList<MomentPair> momentPairsList;

    public MomentUiState(int error, ArrayList<MomentPair> list) {
        this.error = error;
        this.momentPairsList = list;
    }

    public int getError() {
        return error;
    }

    public ArrayList<MomentPair> getMomentPairsList() {
        return momentPairsList;
    }

    public int getMomentPairsListSize() {
        return momentPairsList.size();
    }

    public void addMomentPair(MomentPair momentPair) {
        momentPairsList.add(momentPair);
    }

}
