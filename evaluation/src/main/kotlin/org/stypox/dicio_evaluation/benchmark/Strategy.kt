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
        scoringLinear(2.0, -1.0, 2.0, -1.0),
        pruningBest(scoringLinear(2.0, -1.0, 2.0, -1.0)),
        ::pruningBestEstimate,
    ),
    RATIO_PRUNING_NONE(
        scoringWeightedRatio(0.9),
        ::pruningNone,
        ::pruningNoneEstimate,
    ),
    RATIO_PRUNING_BEST_HALF(
        scoringWeightedRatio(0.9),
        pruningBestHalf(scoringWeightedRatio(0.9)),
        ::pruningBestHalfEstimate,
    ),
    RATIO_PRUNING_BEST(
        scoringWeightedRatio(0.9),
        pruningBest(scoringWeightedRatio(0.9)),
        ::pruningBestEstimate,
    ),
}

fun scoringWeightedRatio(denominatorExp: Double) = { stats: MatchResult ->
    val denominator = stats.userWeight + stats.refWeight
    if (denominator == 0.0f) {
        0.0
    } else {
        (stats.userMatched + stats.refMatched) /
                denominator.toDouble().pow(denominatorExp)
    }
}

fun scoringLinear(um: Double, uw: Double, rm: Double, rw: Double) = { stats: MatchResult ->
    um * stats.userMatched +
            uw * stats.userWeight +
            rm * stats.refMatched +
            rw * stats.refWeight
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
