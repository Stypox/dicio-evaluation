package org.stypox.dicio_evaluation.component

class CapturingComponent(
    private val weight: Float
) : Component {
    override fun match(start: Int, end: Int, tokenizations: Tokenizations): MatchResult {
        return MatchResult(
            userMatched = (end-start) / 10.0f,
            userWeight = (end-start) / 10.0f,
            refMatched = if (start == end) 0.0f else weight,
            refWeight = weight,
            end = end,
        )
    }
}
