package org.stypox.dicio_evaluation
import kotlin.math.min
import kotlin.math.pow

const val SCORING_F_PARAM = 0.9
fun scoringF(stats: Stats): Double {
    val denominator = stats.userWeight + stats.refWeight
    if (denominator == 0) {
        return 0.0
    }

    return (stats.userMatched + stats.refMatched) / denominator.toDouble().pow(SCORING_F_PARAM)
}

const val SCORING_G_UM = 2.0
const val SCORING_G_UW = -1.0
const val SCORING_G_RM = 2.0
const val SCORING_G_RW = -1.0
fun scoringG(stats: Stats): Double {
    return SCORING_G_UM * stats.userMatched +
            SCORING_G_UW * stats.userWeight +
            SCORING_G_RM * stats.refMatched +
            SCORING_G_RW * stats.refWeight
}

fun pruningNone(scoringFunction: (stats: Stats) -> Double, options: MutableList<Stats>) {
}

fun pruningBestScore(scoringFunction: (stats: Stats) -> Double, options: MutableList<Stats>) {
    val best = options.maxBy(scoringFunction)
    options.clear()
    options.add(best)
}

fun pruningBestHalfScore(scoringFunction: (stats: Stats) -> Double, options: MutableList<Stats>) {
    options.sortByDescending(scoringFunction)
    for (i in 0..min(options.size - 16, options.size / 2)) {
        options.removeLast()
    }
}

fun main() {
    val matcher = Matcher(
        scoringFunction = ::scoringG,
        pruningFunction = ::pruningBestScore,
        userWords = listOf("a", "b", "c", "e", "d", "g", "c", "d", "e"),
        refWords = listOf("a", "c", "d", "g", "e", "c", "d", "g", "e"),
    )

    println(matcher.match())
}
