package org.stypox.dicio_evaluation

data class Stats(
    val userMatched: Int,
    val userWeight: Int,
    val refMatched: Int,
    val refWeight: Int,
) {
    fun plus(other: Stats) = Stats(
        userMatched + other.userMatched,
        userWeight + other.userWeight,
        refMatched + other.refMatched,
        refWeight + other.refWeight,
    )
}

