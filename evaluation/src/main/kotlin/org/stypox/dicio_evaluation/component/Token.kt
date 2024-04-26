package org.stypox.dicio_evaluation.component

interface Token {
    /**
     * Inclusive
     */
    val start: Int

    /**
     * Exclusive
     */
    val end: Int
}

fun <T: Token> List<T>.findTokenStartingAt(start: Int): T? {
    val insertionIndex = binarySearch { it.start.compareTo(start) }
    return getOrNull(insertionIndex)?.takeIf { it.start == start }
}
