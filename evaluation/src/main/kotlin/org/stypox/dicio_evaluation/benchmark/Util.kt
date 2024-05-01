package org.stypox.dicio_evaluation.benchmark

import org.stypox.dicio_evaluation.component.CapturingComponent
import org.stypox.dicio_evaluation.component.Component
import org.stypox.dicio_evaluation.component.CompositeComponent
import org.stypox.dicio_evaluation.component.WordComponent

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
