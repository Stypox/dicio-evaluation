package org.stypox.dicio_evaluation.context

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.EqualityMatcherResult
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlin.math.abs

fun ctx(userInput: String) = MatchContext(userInput, { 0.0 }, {})

fun beEqualToPlusOrMinus(vararg expected: Float) = object : Matcher<FloatArray> {
    override fun test(value: FloatArray): MatcherResult {
        if (expected.size != value.size) {
            return EqualityMatcherResult.invoke(
                passed = false,
                actual = value,
                expected = expected,
                failureMessageFn = { "items have different lengths (expected size = ${
                    expected.size}, actual size = ${value.size})" },
                negatedFailureMessageFn = { "arrays are the same" }
            )
        }

        for (i in expected.indices) {
            if (abs(expected[i] - value[i]) > 0.0001f) {
                return EqualityMatcherResult.invoke(
                    passed = false,
                    actual = value,
                    expected = expected,
                    failureMessageFn = { "arrays differ at position $i (expected = ${
                        expected[i]}, actual = ${value[i]})" },
                    negatedFailureMessageFn = { "arrays are the same" }
                )
            }
        }

        return EqualityMatcherResult.invoke(
            passed = true,
            actual = value,
            expected = expected,
            failureMessageFn = { "arrays are different" },
            negatedFailureMessageFn = { "arrays are the same" }
        )
    }
}

class TokenizersTest : DescribeSpec({
    describe("splitWords") {
        it("empty") {
            splitWords(ctx(""))
                .shouldBe(listOf())
        }
        it("blank") {
            splitWords(ctx(" \n\t 0,-7"))
                .shouldBe(listOf())
        }
        it("whitespace") {
            splitWords(ctx(" hello how\nare \t  you "))
                .shouldBe(listOf(
                    WordToken(1, 6, "hello"),
                    WordToken(7, 10, "how"),
                    WordToken(11, 14, "are"),
                    WordToken(18, 21, "you"),
                ))
        }
        it("punctuation") {
            splitWords(ctx("¿hello, .org!?"))
                .shouldBe(listOf(
                    WordToken(1, 6, "hello"),
                    WordToken(9, 12, "org"),
                ))
        }
        it("digits") {
            splitWords(ctx("he110 w0rld"))
                .shouldBe(listOf(
                    WordToken(0, 2, "he"),
                    WordToken(6, 7, "w"),
                    WordToken(8, 11, "rld"),
                ))
        }
        it("case") {
            splitWords(ctx("HeLLo WoRlD"))
                .shouldBe(listOf(
                    WordToken(0, 5, "hello"),
                    WordToken(6, 11, "world"),
                ))
        }
    }

    describe("cumulativeWeight") {
        it("empty") {
            cumulativeWeight(ctx(""))
                .should(beEqualToPlusOrMinus(0.0f))
        }
        it("whitespace") {
            cumulativeWeight(ctx(" \n\t "))
                .should(beEqualToPlusOrMinus(0.0f, 0.0f, 0.0f, 0.0f, 0.0f))
        }
        it("punctuation") {
            cumulativeWeight(ctx(".,;-'"))
                .should(beEqualToPlusOrMinus(0.0f, 0.05f, 0.1f, 0.15f, 0.2f, 0.25f))
        }
        it("digits") {
            cumulativeWeight(ctx("0123"))
                .should(beEqualToPlusOrMinus(0.0f, 0.1f, 0.2f, 0.3f, 0.4f))
        }
        it("mixed") {
            cumulativeWeight(ctx(" \n.\t 0,-7"))
                .should(beEqualToPlusOrMinus(0.0f, 0.0f, 0.0f, 0.05f, 0.05f, 0.05f, 0.15f, 0.2f,
                    0.25f, 0.35f))
        }
        it("words") {
            cumulativeWeight(ctx("hello guys"))
                .should(beEqualToPlusOrMinus(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f, 1.0f, 1.25f, 1.5f,
                    1.75f, 2.0f))
        }
        it("long word") {
            cumulativeWeight(ctx("internationalisation"))
                .should(beEqualToPlusOrMinus(0.0f, 0.05f, 0.1f, 0.15f, 0.2f, 0.25f, 0.3f, 0.35f,
                    0.4f, 0.45f, 0.5f, 0.55f, 0.6f, 0.65f, 0.7f, 0.75f, 0.8f, 0.85f, 0.9f, 0.95f,
                    1.0f))
        }
        it("mixed words") {
            cumulativeWeight(ctx("\nLala\thello, gu2ys!!"))
                .should(beEqualToPlusOrMinus(0.0f, 0.0f, 0.25f, 0.5f, 0.75f, 1.0f, 1.0f, 1.2f, 1.4f,
                    1.6f, 1.8f, 2.0f, 2.05f, 2.05f, 2.55f, 3.05f, 3.15f, 3.65f, 4.15f, 4.20f,
                    4.25f))
        }
    }

    describe("cumulativeWhitespace") {
        it("only whitespace") {
            cumulativeWhitespace(ctx(" \n\t "))
                .shouldBe(arrayOf(0, 1, 2, 3, 4))
        }
        it("no whitespace") {
            cumulativeWhitespace(ctx("Hello!"))
                .shouldBe(arrayOf(0, 0, 0, 0, 0, 0, 0))
        }
        it("mixed") {
            cumulativeWhitespace(ctx("\nHello, gu0ys\t!"))
                .shouldBe(arrayOf(0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3))
        }
    }
})
