package org.stypox.dicio_evaluation
import kotlin.math.pow

const val SCORING_F_PARAM = 0.9
fun scoringF(stats: Stats): Double {
    val denominator = stats.userWeight + stats.refWeight
    if (denominator == 0) {
        return 0.0
    }

    return (stats.userMatched + stats.refMatched) / denominator.toDouble().pow(SCORING_F_PARAM)
}

fun main() {
    val matcher = Matcher(
        scoringFunction = ::scoringF,
        userWords = listOf("a", "b", "c", "e", "d", "g", "c", "d", "e"),
        refWords = listOf("a", "c", "d", "g", "e", "c", "d", "g", "e"),
    )

    println(matcher.match())
}
