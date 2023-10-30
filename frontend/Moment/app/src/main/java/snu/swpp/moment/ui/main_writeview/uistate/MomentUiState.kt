package snu.swpp.moment.ui.main_writeview.uistate

import snu.swpp.moment.data.model.MomentPairModel

class MomentUiState(
    val error: Exception?,
    val momentPairList: List<MomentPairModel>,
) {
    companion object {
        @JvmStatic
        fun withError(error: Exception): MomentUiState {
            return MomentUiState(error, emptyList())
        }
    }
    
    fun getNumMoments(): Int {
        return momentPairList.size
    }
}