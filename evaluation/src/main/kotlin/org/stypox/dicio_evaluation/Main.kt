package org.stypox.dicio_evaluation
import org.stypox.dicio_evaluation.component.Component
import org.stypox.dicio_evaluation.component.CompositeComponent
import kotlin.math.min
import kotlin.math.pow
import org.stypox.dicio_evaluation.component.MatchResult
import org.stypox.dicio_evaluation.context.MatchContext
import org.stypox.dicio_evaluation.component.WordComponent
import org.stypox.dicio_evaluation.context.cumulativeWeight
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.time.measureTimedValue

const val SCORING_F_PARAM = 0.9
fun scoringF(stats: MatchResult): Double {
    val denominator = stats.userWeight + stats.refWeight
    if (denominator == 0.0f) {
        return 0.0
    }

    return (stats.userMatched + stats.refMatched) / denominator.toDouble().pow(SCORING_F_PARAM)
}

const val SCORING_G_UM = 2.0
const val SCORING_G_UW = -1.0
const val SCORING_G_RM = 2.0
const val SCORING_G_RW = -1.0
fun scoringG(stats: MatchResult): Double {
    return SCORING_G_UM * stats.userMatched +
            SCORING_G_UW * stats.userWeight +
            SCORING_G_RM * stats.refMatched +
            SCORING_G_RW * stats.refWeight
}

fun pruningNone(options: MutableList<MatchResult>) {
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

fun match(
    userInput: String,
    component: Component,
    scoringFunction: (MatchResult) -> Double,
    pruningFunction: (MutableList<MatchResult>) -> Unit,
): MatchInfo {
    val (options, time) = measureTimedValue {
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
        return@measureTimedValue options
    }

    val bestResult = options.maxBy(scoringFunction)
    return MatchInfo(
        options = options.size,
        score = scoringFunction(bestResult),
        result = bestResult,
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

fun main() {
    val time = benchmark {
        val component = CompositeComponent(listOf(
            WordComponent("aaaaaaaa", 1.0f),
            WordComponent("cccccccc", 1.0f),
            WordComponent("dddddddd", 1.0f),
            WordComponent("gggggggg", 1.0f),
            WordComponent("eeeeeeee", 1.0f),
            WordComponent("cccccccc", 1.0f),
            WordComponent("dddddddd", 1.0f),
            WordComponent("gggggggg", 1.0f),
            WordComponent("eeeeeeee", 1.0f)
        ))

        val info = match(
            arrayOf(
                "aaaaaaaa",
                "bbbbbbbb",
                "cccccccc",
                "eeeeeeee",
                "dddddddd",
                "gggggggg",
                "cccccccc",
                "dddddddd",
                "eeeeeeee",
            ).joinToString(separator = " "),
            component,
            scoringFunction = ::scoringG,
            pruningFunction = pruningBestScore(::scoringG),
        )

        println(info)
    }

    println("Time: $time")
}
