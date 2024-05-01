package org.stypox.dicio_evaluation
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.stypox.dicio_evaluation.benchmark.RawData
import org.stypox.dicio_evaluation.benchmark.Strategies
import org.stypox.dicio_evaluation.benchmark.benchmark
import org.stypox.dicio_evaluation.benchmark.match
import java.io.FileInputStream
import kotlin.time.measureTimedValue

/**
 * The bruteforce algorithm produces O((u+1) * binom(u+r, u)) options
 */
fun optionCountBruteforce(userInputLength: Int, refLength: Int): Long {
    var result = (userInputLength + 1).toLong()

    for (i in 1..refLength) {
        result *= userInputLength + i
        result /= i
    }

    return result
}

fun main() {
    val dataPoints = FileInputStream("data/data.json").use {
        @OptIn(ExperimentalSerializationApi::class)
        Json.decodeFromStream<List<RawData>>(it)
    }.map {
        it.toData()
    }

    val doBenchmark = false;

    for (data in dataPoints) {
        for (userInput in data.user) {
            for ((component, ref) in data.ref) {
                println("User input: $userInput")
                println("Reference: $ref")
                val optionCountBruteforce = optionCountBruteforce(
                    userInput.length, ref.count { it == ' ' } + 1)
                println("Option count when bruteforcing: $optionCountBruteforce")
                for (strategy in Strategies.entries) {
                    if (strategy.isBruteforce && optionCountBruteforce > 1000000) {
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
