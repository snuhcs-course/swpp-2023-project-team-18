package snu.swpp.moment.ui.main_statview

import snu.swpp.moment.data.model.StoryModel
import snu.swpp.moment.utils.EmotionMap
import snu.swpp.moment.utils.TimeConverter
import snu.swpp.moment.utils.convertEmotionKoreanText
import java.time.Duration
import java.time.LocalDate

/*class ScoreEntry(  val daysFromToday:Int ,
                   val score:Int){
    companion object{
        fun fromStoryModels(stories:MutableList<StoryModel>,today:LocalDate):List<ScoreEntry>{
            val scoreEntries :MutableList<ScoreEntry> = mutableListOf()
            for(story in stories){
                val days = Duration.between(TimeConverter.convertDateToLocalDate(story.createdAt).atStartOfDay(),today.atStartOfDay()).toDays()
                scoreEntries.add(ScoreEntry(days.toInt(),story.score))
            }
            return scoreEntries


        }
    }

}
class EmotionEntry(val emotion:String,val count:Int){
    companion object{
        fun fromStoryModels(stories:MutableList<StoryModel>,today:LocalDate):List<EmotionEntry>{
            val emotionEntries :MutableList<EmotionEntry> = mutableListOf()
            for(story in stories){
                Em
            }
            return scoreEntries


        }
    }
}*/
class StatState(
    val hashtagCounts: Map<String, Int>,
    val scoresBydateOffset: Map<Int, Int>,
    val emotionCounts: Map<String, Int>,
    val error: Exception?
) {
    companion object {
        @JvmStatic
        fun withError(error: Exception) = StatState(
            error = error,
            scoresBydateOffset = mapOf(),
            hashtagCounts = mapOf(),
            emotionCounts = mapOf()
        )

        fun fromStoryModels(stories: MutableList<StoryModel>, today: LocalDate): StatState {
            return StatState(
                scoresBydateOffset = storiesToScores(stories, today),
                hashtagCounts = storiesToHashtags(stories),
                emotionCounts = storiesToEmotions(stories),
                error = null
            )


        }

        private fun storiesToScores(stories: List<StoryModel>, today: LocalDate): Map<Int, Int> {
            val scores: MutableMap<Int, Int> = mutableMapOf()
            for (story in stories) {
                val days = Duration.between(
                    TimeConverter.convertDateToLocalDate(story.createdAt).atStartOfDay(),
                    today.atStartOfDay()
                ).toDays()
                scores[days.toInt()] = story.score
            }
            return scores
        }

        private fun storiesToEmotions(stories: List<StoryModel>): Map<String, Int> {
            val emotions: MutableMap<String, Int> = mutableMapOf()
            for (story in stories) {
                if (story.emotionInt == EmotionMap.INVALID_EMOTION) continue
                emotions.compute(convertEmotionKoreanText(story.emotionInt)) { _, existingValue ->
                    existingValue?.plus(
                        1
                    ) ?: 1
                }

            }
            return emotions
        }

        private fun storiesToHashtags(stories: List<StoryModel>): Map<String, Int> {
            val hashtags: MutableMap<String, Int> = mutableMapOf()
            for (story in stories) {
                for (hashtag in story.hashtags) {
                    hashtags.compute(hashtag.content) { _, existingValue ->
                        existingValue?.plus(
                            1
                        ) ?: 1
                    }
                }

            }
            return hashtags
        }
    }
}