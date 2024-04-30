package org.stypox.dicio_evaluation.component

import org.stypox.dicio_evaluation.context.MatchContext

class CapturingComponent(
    private val weight: Float
) : Component {
    override fun match(start: Int, end: Int, ctx: MatchContext): List<MatchResult> {
        return listOf(MatchResult(
            userMatched = (end-start) / 10.0f,
            userWeight = (end-start) / 10.0f,
            refMatched = if (start == end) 0.0f else weight,
            refWeight = weight,
            end = end,
            canGrow = true,
        ))
    }
}
