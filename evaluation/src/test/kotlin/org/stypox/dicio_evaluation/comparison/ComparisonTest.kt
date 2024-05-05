package org.stypox.dicio_evaluation.comparison

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.floats.shouldNotBeNaN
import io.kotest.matchers.shouldNotBe
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
    println("Better match between \"${comparison.betterUser}\" and \"${
        comparison.betterRawRef}\": $matchBetter")

    val matchWorse = match(
        userInput = comparison.worseUser,
        component = comparison.worseRef,
        scoringFunction = strategy.scoringFunction,
        pruningFunction = strategy.pruningFunction,
    )
    println("Worse match between \"${comparison.worseUser}\" and \"${
        comparison.worseRawRef}\": $matchWorse")

    matchBetter.score
        .shouldNotBeNaN()
        .shouldNotBe(Float.NEGATIVE_INFINITY)
        .shouldNotBe(Float.POSITIVE_INFINITY)
    matchWorse.score
        .shouldNotBeNaN()
        .shouldNotBe(Float.NEGATIVE_INFINITY)
        .shouldNotBe(Float.POSITIVE_INFINITY)

    // compare with some margin to avoid floating point errors
    (matchBetter.score > matchWorse.score + 0.1f ||
            (matchBetter.score >= matchWorse.score && matchWorse.score == 0.0f) ||
            matchBetter.score / matchWorse.score > 1.03)
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
                    it("${comparison.betterKey} vs ${comparison.worseKey}")
                        .config(enabled = comparison.estimateOptionCount(strategy) < 2000000) {
                        test(comparison, strategy)
                    }
                }
            }
        }
    }
})
