package org.stypox.dicio_evaluation.component

data class MatchResult(
    val userMatched: Float,
    val userWeight: Float,
    val refMatched: Float,
    val refWeight: Float,

    /**
     * Exclusive index
     */
    val end: Int,
)
