package org.stypox.dicio_evaluation

import kotlinx.serialization.Serializable
import org.stypox.dicio_evaluation.component.CapturingComponent
import org.stypox.dicio_evaluation.component.Component
import org.stypox.dicio_evaluation.component.CompositeComponent
import org.stypox.dicio_evaluation.component.WordComponent

@Serializable
data class RawData(
    val user: List<String>,
    val ref: List<String>,
) {
    fun toData() = Data(
        user = user,
        ref = ref.map { Pair(stringToComponent(it), it) },
    )
}

fun stringToComponent(s: String): Component {
    val components = s
        .split(' ')
        .map {
            val (word, weight) = if (it.contains(':')) {
                val wordWeight = it.split(':', limit = 2)
                Pair(wordWeight[0], wordWeight[1].toFloat())
            } else {
                Pair(it, 1.0f)
            }

            return@map if (word == "..") {
                CapturingComponent(weight)
            } else {
                WordComponent(word, weight)
            }
        }
        .toList()
    return CompositeComponent(components)
}

data class Data(
    val user: List<String>,
    val ref: List<Pair<Component, String>>,
)
