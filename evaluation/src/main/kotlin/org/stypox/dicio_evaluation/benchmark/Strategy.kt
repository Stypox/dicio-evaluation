package org.stypox.dicio_evaluation.benchmark

import org.stypox.dicio_evaluation.component.MatchResult
import java.util.function.Function
import kotlin.math.min
import kotlin.math.pow

@Suppress("EnumEntryName")
enum class Strategy(
    val scoringFunction: (stats: MatchResult) -> Float,
    val pruningFunction: (MutableList<MatchResult>) -> Unit,
    val estimateOptionCount: (userInputLength: Int, refLength: Int) -> Long,
) {
    `Linear, balanced, 2 vs -1`(
        scoringLinear(2.0f, -1.0f, 2.0f, -1.0f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    `Linear, balanced, 2 vs -1_1`(
        scoringLinear(2.0f, -1.1f, 2.0f, -1.1f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    `Linear, balanced, 2 vs -1_3`(
        scoringLinear(2.0f, -1.3f, 2.0f, -1.3f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    `Linear, balanced, 2 vs -0_8`(
        scoringLinear(2.0f, -0.8f, 2.0f, -0.8f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    `Linear, more ref`(
        scoringLinear(2.0f, -1.1f, 3.0f, -1.65f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    `Linear, more user`(
        scoringLinear(3.0f, -1.65f, 2.0f, -1.1f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    `Linear, only ref`(
        scoringLinear(0.0f, 0.0f, 2.0f, -1.0f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    `Linear, only user`(
        scoringLinear(2.0f, -1.0f, 0.0f, 0.0f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    `Ratio, weightExp = 0_5, no pruning`(
        scoringRatio(0.5f),
        ::pruningNone,
        ::pruningNoneEstimate,
    ),
    `Ratio, weightExp = 0_5, pruning best half`(
        scoringRatio(0.5f),
        ::pruningBestHalf,
        ::pruningBestHalfEstimate,
    ),
    `Ratio, weightExp = 0_5`(
        scoringRatio(0.5f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    `Ratio, weightExp = 0_9`(
        scoringRatio(0.9f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    `Ratio, weightExp = 1_0`(
        scoringRatio(1.0f),
        ::pruningBest,
        ::pruningBestEstimate,
    ),
    `Sum of ratios, weightExp = 0_5`(
        scoringSumOfRatios(1.0f, 0.5f),
        ::pruningBest,
        ::pruningBestEstimate
    ),
    `Sum of ratios, weightExp = 1_0`(
        scoringSumOfRatios(1.0f, 1.0f),
        ::pruningBest,
        ::pruningBestEstimate
    ),
    `Product of ratios, weightExp = 0_5`(
        scoringProductOfRatios(1.0f, 0.5f),
        ::pruningBest,
        ::pruningBestEstimate
    ),
    `Product of ratios, weightExp = 1_0`(
        scoringProductOfRatios(1.0f, 1.0f),
        ::pruningBest,
        ::pruningBestEstimate
    ),
    `Intersection over union, weightExp = 0_5`(
        scoringIntersectionOverUnion(1.0f, 0.5f),
        ::pruningBest,
        ::pruningBestEstimate
    ),
    `Intersection over union, weightExp = 1_0`(
        scoringIntersectionOverUnion(1.0f, 1.0f),
        ::pruningBest,
        ::pruningBestEstimate
    )

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

fun scoringLinear(um: Float, uw: Float, rm: Float, rw: Float) = { stats: MatchResult ->
    um * stats.userMatched +
            uw * stats.userWeight +
            rm * stats.refMatched +
            rw * stats.refWeight
}

fun scoringRatio(weightExp: Float) = { stats: MatchResult ->
    val denominator = stats.userWeight + stats.refWeight
    if (denominator <= 0.0f) {
        0.0f
    } else {
        (stats.userMatched + stats.refMatched) / denominator.pow(weightExp)
    }
}

fun scoringSumOfRatios(userMultiplier: Float, weightExp: Float) = { stats: MatchResult ->
    if (stats.userWeight <= 0.0f || stats.refWeight <= 0.0f) {
        0.0f
    } else {
        userMultiplier * stats.userMatched / stats.userWeight.pow(weightExp) +
                stats.refMatched / stats.refWeight.pow(weightExp)
    }
}

fun scoringProductOfRatios(userExp: Float, weightExp: Float) = { stats: MatchResult ->
    if (stats.userWeight <= 0.0f || stats.refWeight <= 0.0f) {
        0.0f
    } else {
        stats.userMatched.pow(userExp) *
                stats.refMatched /
                stats.userWeight.pow(userExp * weightExp) /
                stats.refWeight.pow(weightExp)
    }
}

fun scoringIntersectionOverUnion(userMultiplier: Float, weightExp: Float) = { stats: MatchResult ->
    if (stats.userMatched <= 0.0f || stats.refMatched <= 0.0f) {
        0.0f
    } else {
        1.0f / (1 +
                stats.userWeight.pow(weightExp) / stats.userMatched / userMultiplier +
                stats.refWeight.pow(weightExp) / stats.refMatched)
    }
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
