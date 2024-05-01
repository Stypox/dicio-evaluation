package org.stypox.dicio_evaluation
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.stypox.dicio_evaluation.benchmark.RawData
import org.stypox.dicio_evaluation.benchmark.Strategies
import org.stypox.dicio_evaluation.benchmark.benchmark
import org.stypox.dicio_evaluation.benchmark.match
import java.io.FileInputStream

/**
 * The bruteforce algorithm produces O((u+1)² * binom(u+r, u+1)) options
 * TODO does not work in some cases
 */
fun optionCountBruteforce(userInputLength: Int, refLength: Int): Int {
    var result = (userInputLength + 1) * (userInputLength + 1)

    for (i in 2..refLength) {
        result *= userInputLength + i
    }
    for (i in 2..refLength) {
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

    for (data in dataPoints) {
        for (userInput in data.user) {
            for ((component, ref) in data.ref) {
                println("User input: $userInput")
                println("Reference: $ref")
                println("Option count when bruteforcing: ${
                    optionCountBruteforce(userInput.length, ref.count { it == ' ' } + 1)}")
                for (strategy in Strategies.entries) {
                    val (result, time) = benchmark {
                        match(
                            userInput = userInput,
                            component = component,
                            scoringFunction = strategy.scoringFunction,
                            pruningFunction = strategy.pruningFunction,
                        )
                    }
                    println("$strategy: $time, ${result.options} options, score ${
                        "%.2f".format(result.score)}, result ${result.result}")
                }
                println()
            }
        }
    }
}
