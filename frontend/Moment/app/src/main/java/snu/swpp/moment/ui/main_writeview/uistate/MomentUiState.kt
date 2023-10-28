package snu.swpp.moment.ui.main_writeview.uistate

import snu.swpp.moment.data.model.MomentPairModel

class MomentUiState(
    val error: Exception?,
    val momentPairList: List<MomentPairModel>,
) {
    fun getNumMoments(): Int {
        return momentPairList.size
    }
}