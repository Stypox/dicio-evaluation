package org.stypox.dicio_evaluation

data class Stats(
    var userMatched: Int,
    var userWeight: Int,
    var refMatched: Int,
    var refWeight: Int,
) {
    fun plus(other: Stats) = Stats(
        userMatched + other.userMatched,
        userWeight + other.userWeight,
        refMatched + other.refMatched,
        refWeight + other.refWeight,
    )
}

