package snu.swpp.moment.ui.main_statview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import snu.swpp.moment.data.callback.StoryGetCallBack
import snu.swpp.moment.data.callback.TokenCallBack
import snu.swpp.moment.data.model.StoryModel
import snu.swpp.moment.data.repository.AuthenticationRepository
import snu.swpp.moment.data.repository.StoryRepository
import snu.swpp.moment.exception.UnauthorizedAccessException
import snu.swpp.moment.utils.EmotionMap
import snu.swpp.moment.utils.TimeConverter
import java.time.LocalDate

class StatViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {
    val today: MutableLiveData<LocalDate> = MutableLiveData(LocalDate.now())
    val stat: MutableLiveData<StatState> = MutableLiveData()

    // 기간을 표시하기 위한 변수
    val startDate: MutableLiveData<LocalDate> = MutableLiveData()
    val endDate: MutableLiveData<LocalDate> = MutableLiveData(LocalDate.now())

    // 버튼 타입을 나타내는 livedata
    val selectedButtonType: MutableLiveData<ButtonType> = MutableLiveData(ButtonType.WEEK)

    // 통계 평균값을 7일 30일
    val selectedPeriod: MutableLiveData<ButtonType> = MutableLiveData(ButtonType.WEEK)

    // 점수에 대한 통계 값들을 띄워주는 용도
    val highestScore: MutableLiveData<Int> = MutableLiveData()
    val lowestScore: MutableLiveData<Int> = MutableLiveData()
    val averageScore: MutableLiveData<Double> = MutableLiveData()

    fun getStats(todayDate: LocalDate, isMonth: Boolean) {
        //  val todayDate = TimeConverter.getToday()
        today.value = todayDate
        val startEndTimes =
            if (isMonth) TimeConverter.getRecentMonthTimestamps(todayDate) else TimeConverter.getRecentWeekTimestamps(
                todayDate
            )

        // 기간 표시를 위한 로직
        val periodStartDate = if (isMonth) {
            todayDate.minusDays(30)
        } else {
            todayDate.minusDays(7)
        }
        startDate.value = periodStartDate
        endDate.value = todayDate.minusDays(1)

        selectedPeriod.value = if (isMonth) ButtonType.MONTH else ButtonType.WEEK


        authenticationRepository.isTokenValid(object : TokenCallBack {
            override fun onSuccess() {
                val accessToken = authenticationRepository.token.accessToken
                storyRepository.getStory(accessToken, startEndTimes[0], startEndTimes[1],
                    object : StoryGetCallBack {
                        override fun onSuccess(storyList: MutableList<StoryModel>) {
                            stat.value = StatState.fromStoryModels(
                                storyList.filter { it.emotionInt != EmotionMap.INVALID_EMOTION },
                                todayDate
                            )


                            // score stat
                            val validScores = storyList.mapNotNull { story ->
                                if (story.emotionInt != EmotionMap.INVALID_EMOTION) story.score else null

                            }
                            if (validScores.isNotEmpty()) {
                                val sumOfValidScores = validScores.sum()
                                val countOfValidScores = validScores.count()

                                highestScore.postValue(validScores.maxOrNull() ?: 0)
                                lowestScore.postValue(validScores.minOrNull() ?: 0)
                                averageScore.postValue(sumOfValidScores.toDouble() / countOfValidScores)
                            } else {
                                // Set statistics to a default or error value if there are no valid scores
                                highestScore.postValue(0)
                                lowestScore.postValue(0)
                                averageScore.postValue(0.0)
                            }


                        }

                        override fun onFailure(error: Exception) {
                            stat.value = StatState.withError(error)
                        }
                    })
            }

            override fun onFailure() {
                stat.value = StatState.withError(UnauthorizedAccessException())
            }
        })

        // 버튼의 상태 업데이트,
        selectedButtonType.value = if (isMonth) ButtonType.MONTH else ButtonType.WEEK
    }

    enum class ButtonType {
        WEEK, MONTH
    }
}