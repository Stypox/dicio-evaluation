package org.stypox.dicio_evaluation.component

import org.stypox.dicio_evaluation.context.MatchContext
import org.stypox.dicio_evaluation.context.cumulativeWeight

class CapturingComponent(
    private val weight: Float
) : Component() {
    override fun match(start: Int, end: Int, ctx: MatchContext): List<MatchResult> {
        val cumulativeWeight = ctx.getOrTokenize("cumulativeWeight", ::cumulativeWeight)
        val userWeight = cumulativeWeight[end] - cumulativeWeight[start]

        return listOf(MatchResult(
            userMatched = userWeight,
            userWeight = userWeight,
            refMatched = if (start == end) 0.0f else weight,
            refWeight = weight,
            end = end,
            canGrow = true,
        ))
    }
}
