package org.stypox.dicio_evaluation
import org.stypox.dicio_evaluation.component.CompositeComponent
import org.stypox.dicio_evaluation.component.WordComponent

fun main() {
    val time = benchmark {
        val component = CompositeComponent(listOf(
            WordComponent("a", 1.0f),
            WordComponent("c", 1.0f),
            WordComponent("d", 1.0f),
            WordComponent("g", 1.0f),
            WordComponent("e", 1.0f),
            WordComponent("c", 1.0f),
            WordComponent("g", 1.0f),
        ))

        val info = match(
            arrayOf(
                "a",
                "b",
                "c",
                "e",
                "d",
                "g",
                "c",
                "d",
                "e",
            ).joinToString(separator = " "),
            component,
            scoringFunction = ::scoringG,
            pruningFunction = pruningBestScore(::scoringG),
        )

        println(info)
    }

    println("Time: $time")
}
