package org.stypox.dicio_evaluation
import org.stypox.dicio_evaluation.component.Component
import org.stypox.dicio_evaluation.component.CompositeComponent
import kotlin.math.min
import kotlin.math.pow
import org.stypox.dicio_evaluation.component.MatchResult
import org.stypox.dicio_evaluation.component.MatchContext
import org.stypox.dicio_evaluation.component.WordComponent
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

fun pruningNone(scoringFunction: (stats: MatchResult) -> Double, options: MutableList<MatchResult>) {
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
        val ctx = MatchContext(userInput, scoringFunction, pruningFunction)
        val options = ArrayList<MatchResult>()
        for (start in 0..userInput.length) {
            val skippedWordsWeight = start * 0.1f
            options.addAll(
                component.match(0, userInput.length, ctx)
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

fun main() {
    val component = CompositeComponent(listOf(
        WordComponent("a", 1.0f),
        WordComponent("c", 1.0f),
        WordComponent("d", 1.0f),
        WordComponent("g", 1.0f),
        WordComponent("e", 1.0f),
        WordComponent("c", 1.0f),
        WordComponent("d", 1.0f),
        WordComponent("g", 1.0f),
        WordComponent("e", 1.0f)
    ))

    val info = match(
        "a b c e d g c d e",
        component,
        scoringFunction = ::scoringG,
        pruningFunction = pruningBestScore(::scoringG),
    )

    println(info)
}
