package org.stypox.dicio_evaluation
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.FileInputStream

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
                val time = benchmark {
                    match(
                        userInput = userInput,
                        component = component,
                        scoringFunction = ::scoringG,
                        pruningFunction = pruningBestScore(::scoringG),
                    )
                }
                println("User input: $userInput")
                println("Ref: $ref")
                println("Time: $time")
                println()
            }
        }
    }
}
