package org.stypox.dicio_evaluation.example

import kotlinx.serialization.Serializable
import org.stypox.dicio_evaluation.benchmark.stringToComponent
import org.stypox.dicio_evaluation.component.CapturingComponent
import org.stypox.dicio_evaluation.component.Component
import org.stypox.dicio_evaluation.component.CompositeComponent
import org.stypox.dicio_evaluation.component.WordComponent

@Serializable
data class RawExample(
    val user: List<String>,
    val ref: List<String>,
) {
    fun toExample() = Example(
        user = user,
        ref = ref.map { Pair(stringToComponent(it), it) },
    )
}

data class Example(
    val user: List<String>,
    val ref: List<Pair<Component, String>>,
)
