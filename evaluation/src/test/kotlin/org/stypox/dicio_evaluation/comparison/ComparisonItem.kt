package org.stypox.dicio_evaluation.comparison

import kotlinx.serialization.Serializable
import org.stypox.dicio_evaluation.benchmark.stringToComponent
import org.stypox.dicio_evaluation.component.Component

@Serializable
data class RawComparisonItem(
    val user: String,
    val ref: String,
    val betterThan: List<String>,
)

fun Map<String, RawComparisonItem>.toComparisonList(): List<Comparison> {
    val mapWithParsedRef = mapValues { Pair(it.value, stringToComponent(it.value.ref)) }
    val result = ArrayList<Comparison>()

    for ((k1, v1) in mapWithParsedRef) {
        for (k2 in v1.first.betterThan) {
            val v2 = mapWithParsedRef[k2] ?: throw Error("Unknown key $k2")

            result.add(
                Comparison(
                    betterKey = k1,
                    betterUser = v1.first.user,
                    betterRawRef = v1.first.ref,
                    betterRef = v1.second,
                    worseKey = k2,
                    worseUser = v2.first.user,
                    worseRawRef = v2.first.ref,
                    worseRef = v2.second,
                )
            )
        }
    }

    return result
}

data class Comparison(
    val betterKey: String,
    val betterUser: String,
    val betterRawRef: String,
    val betterRef: Component,
    val worseKey: String,
    val worseUser: String,
    val worseRawRef: String,
    val worseRef: Component,
)
