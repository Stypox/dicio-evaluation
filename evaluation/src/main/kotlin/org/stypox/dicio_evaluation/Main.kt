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

fun pruningNone(scoringFunction: (stats: Stats) -> Double, options: MutableList<Stats>) {
}

fun pruningBestScore(scoringFunction: (stats: Stats) -> Double, options: MutableList<Stats>) {
    val best = options.maxBy(scoringFunction)
    options.clear()
    options.add(best)
}

fun main() {
    val matcher = Matcher(
        scoringFunction = ::scoringF,
        pruningFunction = ::pruningNone,
        userWords = listOf("a", "b", "c", "e", "d", "g", "c", "d", "e"),
        refWords = listOf("a", "c", "d", "g", "e", "c", "d", "g", "e"),
    )

    println(matcher.match())
}
