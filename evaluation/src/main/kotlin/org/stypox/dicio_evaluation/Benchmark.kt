package org.stypox.dicio_evaluation

import org.stypox.dicio_evaluation.component.Component
import org.stypox.dicio_evaluation.component.MatchResult
import org.stypox.dicio_evaluation.context.MatchContext
import org.stypox.dicio_evaluation.context.cumulativeWeight
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.time.measureTimedValue

fun match(
    userInput: String,
    component: Component,
    scoringFunction: (MatchResult) -> Double,
    pruningFunction: (MutableList<MatchResult>) -> Unit,
): MatchInfo {
    val (res, time) = measureTimedValue {
        component.setupCache(userInput.length)
        val ctx = MatchContext(userInput, scoringFunction, pruningFunction)
        val cumulativeWeight = ctx.getOrTokenize("cumulativeWeight", ::cumulativeWeight)

        val options = ArrayList<MatchResult>()
        for (start in 0..userInput.length) {
            val skippedWordsWeight = cumulativeWeight[start] - cumulativeWeight[0]
            options.addAll(
                component.matchCached(0, userInput.length, ctx)
                    .map { it.copy(userWeight = it.userWeight + skippedWordsWeight) }
            )
        }

        val bestResult = options.maxBy(scoringFunction)
        return@measureTimedValue Pair(options.size, bestResult)
    }

    return MatchInfo(
        options = res.first,
        score = scoringFunction(res.second),
        result = res.second,
        time = time,
    )
}

fun benchmark(f: () -> Unit): Duration {
    val timeSource = TimeSource.Monotonic

    // warmup phase
    val warmupMark = timeSource.markNow().plus(1.seconds)
    while (warmupMark.hasNotPassedNow()) {
        f()
    }

    // benchmark phase
    val startMark = timeSource.markNow()
    val endMark = startMark.plus(2.seconds)
    var times = 0
    while (endMark.hasNotPassedNow()) {
        f()
        times += 1
    }

    return startMark.elapsedNow() / times
}
