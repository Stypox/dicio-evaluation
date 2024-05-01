package org.stypox.dicio_evaluation.example

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.stypox.dicio_evaluation.benchmark.Strategies
import org.stypox.dicio_evaluation.benchmark.benchmark
import org.stypox.dicio_evaluation.benchmark.match
import org.stypox.dicio_evaluation.benchmark.optionCountBruteforce
import java.io.FileInputStream
import java.nio.file.Paths
import kotlin.math.max
import kotlin.time.measureTimedValue

fun test(examples: List<Example>, doBenchmark: Boolean, limitOptionCountBruteforce: Long) {
    for (example in examples) {
        for (userInput in example.user) {
            for ((component, ref) in example.ref) {
                println("User input: $userInput")
                println("Reference: $ref")
                val optionCountBruteforce = optionCountBruteforce(
                    userInput.length, ref.count { it == ' ' } + 1)
                println("Option count when bruteforcing: $optionCountBruteforce")

                for (strategy in Strategies.entries) {
                    if (
                        strategy.isBruteforce &&
                        optionCountBruteforce > limitOptionCountBruteforce
                    ) {
                        println("$strategy: skipped")
                        continue
                    }

                    val funToBenchmark = {
                        match(
                            userInput = userInput,
                            component = component,
                            scoringFunction = strategy.scoringFunction,
                            pruningFunction = strategy.pruningFunction,
                        )
                    }

                    val (result, time) = if (doBenchmark)
                        benchmark(funToBenchmark)
                    else
                        measureTimedValue(funToBenchmark)

                    println("$strategy: $time, ${result.options} options, score ${
                        "%.2f".format(result.score)}, result ${result.result}")
                }
                println()
            }
        }
    }
}

class ExampleTest : StringSpec({
    val examples = ExampleTest::class.java.classLoader
        .getResourceAsStream("examples.json")!!
        .use {
            @OptIn(ExperimentalSerializationApi::class)
            Json.decodeFromStream<List<RawExample>>(it)
        }.map {
            it.toExample()
        }


    "run benchmark, including strategies that take some time".config(enabled = false) {
        test(examples, true, Long.MAX_VALUE)
    }

    "run benchmark, but skip strategies that would take too much time" {
        test(examples, true, 1000000)
    }

    "run once, including strategies that take some time".config(enabled = false) {
        test(examples, true, Long.MAX_VALUE)
    }

    "run once, but skip strategies that would take too much time" {
        test(examples, true, 1000000)
    }
})
