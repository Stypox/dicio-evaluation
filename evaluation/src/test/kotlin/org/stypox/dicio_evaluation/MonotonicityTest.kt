package org.stypox.dicio_evaluation

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import org.stypox.dicio_evaluation.benchmark.MatchInfo
import org.stypox.dicio_evaluation.benchmark.Strategy
import org.stypox.dicio_evaluation.benchmark.match
import org.stypox.dicio_evaluation.benchmark.stringToComponent

fun parseMatch(user: String, rawRef: String, strategy: Strategy): MatchInfo {
    return match(
        userInput = user,
        component = stringToComponent(rawRef),
        scoringFunction = strategy.scoringFunction,
        pruningFunction = strategy.pruningFunction,
    )
}

class MonotonicityTest : DescribeSpec({
    describe("check which scoring functions would fail under greedy pruning") {
        Strategy.entries.forEach { strategy ->
            it(strategy.toString()) {
                val match1 = parseMatch("c",     "c", strategy)
                val match2 = parseMatch("a b c", "d b c", strategy)
                print("Match 1: $match1")
                print("Match 2: $match2")
                val comparison12 = match1.score > match2.score

                val match01 = parseMatch("e e c",     "f f c", strategy)
                val match02 = parseMatch("e e a b c", "f f d b c", strategy)
                print("Match 0 + 1: $match01")
                print("Match 0 + 2: $match02")
                val comparison0102 = match01.score > match02.score

                // If the two comparisons are not equal, it means that prepending "e e" to user and
                // "f f" to rawRef, in the exact same way for the two matches, does not preserve the
                // order of the scores of the matches. This implies that it is not possible, for
                // this specific scoring function, to prune results by always only keeping the one
                // with the best score at each step, since doing so wouldn't lead to the optimal
                // result.
                comparison12 shouldBeEqual comparison0102
            }
        }
    }
})
