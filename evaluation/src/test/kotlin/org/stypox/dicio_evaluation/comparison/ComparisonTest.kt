package org.stypox.dicio_evaluation.comparison

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.compose.any
import io.kotest.matchers.string.shouldHaveLength
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.stypox.dicio_evaluation.benchmark.Strategy
import org.stypox.dicio_evaluation.benchmark.match

fun test(comparison: Comparison, strategy: Strategy) {
    val matchBetter = match(
        userInput = comparison.betterUser,
        component = comparison.betterRef,
        scoringFunction = strategy.scoringFunction,
        pruningFunction = strategy.pruningFunction,
    )
    println("Match between \"${comparison.betterUser}\" and \"${
        comparison.betterRawRef}\": $matchBetter")

    val matchWorse = match(
        userInput = comparison.worseUser,
        component = comparison.worseRef,
        scoringFunction = strategy.scoringFunction,
        pruningFunction = strategy.pruningFunction,
    )
    println("Match between \"${comparison.worseUser}\" and \"${
        comparison.worseRawRef}\": $matchWorse")

    // compare with some margin to avoid floating point errors
    (matchBetter.score > matchWorse.score + 0.1f || matchBetter.score / matchWorse.score > 1.03)
        .shouldBeTrue()
}

class ComparisonTest : DescribeSpec({
    val comparisons = ComparisonTest::class.java.classLoader
        .getResourceAsStream("comparisons.json")!!
        .use {
            @OptIn(ExperimentalSerializationApi::class)
            Json.decodeFromStream<Map<String, RawComparisonItem>>(it)
        }
        .toComparisonList()

    describe("Comparison tests for all strategies") {
        Strategy.entries.forEach { strategy ->
            describe(strategy.toString()) {
                comparisons.forEach { comparison ->
                    it("${comparison.betterKey} vs ${comparison.worseKey}") {
                        test(comparison, strategy)
                    }
                }
            }
        }
    }
})
