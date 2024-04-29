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
    val canGrow: Boolean,
) {
    companion object {
        fun empty(end: Int, canGrow: Boolean): MatchResult {
            return MatchResult(0.0f, 0.0f, 0.0f, 0.0f, end, canGrow)
        }
    }
}
