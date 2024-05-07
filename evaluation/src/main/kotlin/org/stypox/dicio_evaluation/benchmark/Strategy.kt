package org.stypox.dicio_evaluation.benchmark

import org.stypox.dicio_evaluation.component.MatchResult
import java.util.function.Function
import kotlin.math.min
import kotlin.math.pow

enum class Strategy(
    val scoringFunction: (stats: MatchResult) -> Float,
    val pruningFunction: (MutableList<MatchResult>) -> Unit,
    val estimateOptionCount: (userInputLength: Int, refLength: Int) -> Long,
) {
    LINEAR_A(
        scoringLinear(2.0f, -1.0f, 2.0f, -1.0f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    LINEAR_B(
        scoringLinear(2.0f, -1.1f, 2.0f, -1.1f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    LINEAR_C(
        scoringLinear(2.0f, -1.3f, 2.0f, -1.3f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    LINEAR_D(
        scoringLinear(2.0f, -0.8f, 2.0f, -0.8f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    LINEAR_MORE_REF(
        scoringLinear(2.0f, -1.1f, 3.0f, -1.65f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    LINEAR_MORE_USER(
        scoringLinear(3.0f, -1.65f, 2.0f, -1.1f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    LINEAR_ONLY_REF(
        scoringLinear(0.0f, 0.0f, 2.0f, -1.0f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    LINEAR_ONLY_USER(
        scoringLinear(2.0f, -1.0f, 0.0f, 0.0f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    RATIO_0_5_PRUNING_NONE(
        scoringWeightedRatio(0.5f),
        ::pruningNone,
        ::pruningNoneEstimate,
    ),
    RATIO_0_5_PRUNING_BEST_HALF(
        scoringWeightedRatio(0.5f),
        ::pruningBestHalf,
        ::pruningBestHalfEstimate,
    ),
    RATIO_0_5_PRUNING_BEST(
        scoringWeightedRatio(0.5f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    RATIO_0_9_PRUNING_BEST(
        scoringWeightedRatio(0.9f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),

    ;

    /**
     * Alternative constructor where the pruning function is derived from the scoring function.
     * Using [java.util.function.Function] to avoid "Platform declaration clash", i.e. to make the
     * two constructor have a different signature.
     */
    constructor(
        scoringFunction: (stats: MatchResult) -> Float,
        pruningFunction: Function<(stats: MatchResult) -> Float, (MutableList<MatchResult>) -> Unit>,
        estimateOptionCount: (userInputLength: Int, refLength: Int) -> Long,
    ) : this(scoringFunction, pruningFunction.apply(scoringFunction), estimateOptionCount)
}

fun scoringWeightedRatio(denominatorExp: Float) = { stats: MatchResult ->
    val denominator = stats.userWeight + stats.refWeight
    if (denominator == 0.0f) {
        0.0f
    } else {
        (stats.userMatched + stats.refMatched) / denominator.pow(denominatorExp)
    }
}

fun scoringLinear(um: Float, uw: Float, rm: Float, rw: Float) = { stats: MatchResult ->
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

fun pruningBest(scoringFunction: (stats: MatchResult) -> Float): (MutableList<MatchResult>) -> Unit = { options: MutableList<MatchResult> ->
    val best = options.maxBy(scoringFunction)
    options.clear()
    options.add(best)
}

fun pruningBestEstimate(userInputLength: Int, refLength: Int): Long {
    return (userInputLength + 1).toLong()
}

fun pruningBestHalf(scoringFunction: (stats: MatchResult) -> Float): (MutableList<MatchResult>) -> Unit = { options: MutableList<MatchResult> ->
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
