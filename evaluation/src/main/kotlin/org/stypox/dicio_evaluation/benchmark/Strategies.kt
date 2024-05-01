package org.stypox.dicio_evaluation.benchmark

import org.stypox.dicio_evaluation.component.MatchResult
import kotlin.math.min
import kotlin.math.pow

enum class Strategies(
    val scoringFunction: (stats: MatchResult) -> Double,
    val pruningFunction: (MutableList<MatchResult>) -> Unit,
) {
    LINEAR(::scoringLinear, pruningBestScore(::scoringLinear)),
    RATIO_PRUNING_NONE(::scoringWeightedRatio, ::pruningNone),
    RATIO_PRUNING_BEST_HALF(::scoringWeightedRatio, pruningBestHalfScore(::scoringWeightedRatio)),
    RATIO_PRUNING_BEST(::scoringWeightedRatio, pruningBestScore(::scoringWeightedRatio)),
}

const val SCORING_WEIGHTED_RATIO_WEIGHT = 0.9
fun scoringWeightedRatio(stats: MatchResult): Double {
    val denominator = stats.userWeight + stats.refWeight
    if (denominator == 0.0f) {
        return 0.0
    }

    return (stats.userMatched + stats.refMatched) /
            denominator.toDouble().pow(SCORING_WEIGHTED_RATIO_WEIGHT)
}

const val SCORING_LINEAR_UM = 2.0
const val SCORING_LINEAR_UW = -1.0
const val SCORING_LINEAR_RM = 2.0
const val SCORING_LINEAR_RW = -1.0
fun scoringLinear(stats: MatchResult): Double {
    return SCORING_LINEAR_UM * stats.userMatched +
            SCORING_LINEAR_UW * stats.userWeight +
            SCORING_LINEAR_RM * stats.refMatched +
            SCORING_LINEAR_RW * stats.refWeight
}

fun pruningNone(@Suppress("UNUSED_PARAMETER") options: MutableList<MatchResult>) {
}

fun pruningBestScore(scoringFunction: (stats: MatchResult) -> Double): (MutableList<MatchResult>) -> Unit = { options: MutableList<MatchResult> ->
    val best = options.maxBy(scoringFunction)
    options.clear()
    options.add(best)
}

fun pruningBestHalfScore(scoringFunction: (stats: MatchResult) -> Double): (MutableList<MatchResult>) -> Unit = { options: MutableList<MatchResult> ->
    options.sortByDescending(scoringFunction)
    for (i in 0..min(options.size - 16, options.size / 2)) {
        options.removeLast()
    }
}
