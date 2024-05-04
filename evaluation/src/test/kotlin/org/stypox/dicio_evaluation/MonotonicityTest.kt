package org.stypox.dicio_evaluation

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import org.stypox.dicio_evaluation.benchmark.MatchInfo
import org.stypox.dicio_evaluation.benchmark.match
import org.stypox.dicio_evaluation.benchmark.pruningBest
import org.stypox.dicio_evaluation.benchmark.scoringLinear
import org.stypox.dicio_evaluation.benchmark.scoringWeightedRatio
import org.stypox.dicio_evaluation.benchmark.stringToComponent
import org.stypox.dicio_evaluation.component.MatchResult

fun parseMatch(user: String, rawRef: String, scoringFunction: (stats: MatchResult) -> Double): MatchInfo {
    return match(
        userInput = user,
        component = stringToComponent(rawRef),
        scoringFunction = scoringFunction,
        pruningFunction = pruningBest(scoringFunction),
    )
}

fun test(u0: String, r0: String, u1: String, r1: String, u2: String, r2: String,
         scoringFunction: (stats: MatchResult) -> Double) {
    val match1 = parseMatch(u1, r1, scoringFunction)
    val match2 = parseMatch(u2, r2, scoringFunction)
    println("Match 1 (\"$u1\" vs \"$r1\"): $match1")
    println("Match 2 (\"$u2\" vs \"$r2\"): $match2\n")
    val comparison12 = match1.score > match2.score

    val match01 = parseMatch("$u0 $u1", "$r0 $r1", scoringFunction)
    val match02 = parseMatch("$u0 $u2", "$r0 $r2", scoringFunction)
    println("Match 0 + 1 (\"$u0 $u1\" vs \"$r0 $r1\"): $match01")
    println("Match 0 + 2 (\"$u0 $u2\" vs \"$r0 $r2\"): $match02\n\n")
    val comparison0102 = match01.score > match02.score

    // If the two comparisons are not equal, it means that prepending $u0 to user and
    // $r0 to rawRef, in the exact same way for the two matches, does not preserve the
    // order of the scores of the matches. This implies that it is not possible, for
    // this specific scoring function, to prune results by always only keeping the one
    // with the best score at each step, since doing so wouldn't lead to the optimal
    // result.
    comparison12 shouldBeEqual comparison0102
}

class MonotonicityTest : DescribeSpec({
    describe("check which scoring functions would fail under greedy pruning") {
        listOf(
            Pair(scoringLinear(2.0, -1.0, 2.0, -1.0), "scoringLinear 2, -1, 2, -1"),
            Pair(scoringWeightedRatio(0.9), "scoringWeightedRatio 0.9"),
            Pair(scoringWeightedRatio(0.5), "scoringWeightedRatio 0.5"),
        ).forEach { (scoringFunction, describeName) ->
            describe(describeName) {
                it("should break scoringWeightedRatio(0.9)") {
                    test("e e", "f f", "c", "c", "a b c", "d b c", scoringFunction)
                }
                it("should break scoringWeightedRatio(0.5)") {
                    test("g h h", "g i i", "c", "c", "a b b b c", "a d d d c", scoringFunction)
                }
            }
        }
    }
})
