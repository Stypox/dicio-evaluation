package org.stypox.dicio_evaluation.benchmark

import org.stypox.dicio_evaluation.component.MatchResult
import kotlin.math.min
import kotlin.math.pow

enum class Strategy(
    val scoringFunction: (stats: MatchResult) -> Double,
    val pruningFunction: (MutableList<MatchResult>) -> Unit,
    val estimateOptionCount: (userInputLength: Int, refLength: Int) -> Long,
) {
    LINEAR(
        ::scoringLinear,
        pruningBest(::scoringLinear),
        ::pruningBestEstimate,
    ),
    RATIO_PRUNING_NONE(
        ::scoringWeightedRatio,
        ::pruningNone,
        ::pruningNoneEstimate,
    ),
    RATIO_PRUNING_BEST_HALF(
        ::scoringWeightedRatio,
        pruningBestHalf(::scoringWeightedRatio),
        ::pruningBestHalfEstimate,
    ),
    RATIO_PRUNING_BEST(
        ::scoringWeightedRatio,
        pruningBest(::scoringWeightedRatio),
        ::pruningBestEstimate,
    ),
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

/**
 * The bruteforce algorithm produces O((u+1) * binom(u+r, u)) options
 */
fun pruningNoneEstimate(userInputLength: Int, refLength: Int): Long {
    var result = (userInputLength + 1).toLong()

    for (i in 1..refLength) {
        result *= userInputLength + i
        result /= i
    }

    return result
}

fun pruningBest(scoringFunction: (stats: MatchResult) -> Double): (MutableList<MatchResult>) -> Unit = { options: MutableList<MatchResult> ->
    val best = options.maxBy(scoringFunction)
    options.clear()
    options.add(best)
}

fun pruningBestEstimate(userInputLength: Int, refLength: Int): Long {
    return (userInputLength + 1).toLong()
}

fun pruningBestHalf(scoringFunction: (stats: MatchResult) -> Double): (MutableList<MatchResult>) -> Unit = { options: MutableList<MatchResult> ->
    options.sortByDescending(scoringFunction)
    for (i in 0..min(options.size - 16, options.size / 2)) {
        options.removeLast()
    }
}

fun pruningBestHalfEstimate(userInputLength: Int, refLength: Int): Long {
    // rough estimate
    var result = pruningNoneEstimate(userInputLength, refLength)
    for (i in 0..refLength-2) {
        result /= 2
    }
    return result
}
