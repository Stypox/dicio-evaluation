package org.stypox.dicio_evaluation.component

import org.stypox.dicio_evaluation.context.MatchContext
import org.stypox.dicio_evaluation.context.cumulativeWeight
import org.stypox.dicio_evaluation.context.cumulativeWhitespace

class CapturingComponent(
    private val weight: Float
) : Component() {
    override fun match(start: Int, end: Int, ctx: MatchContext): List<MatchResult> {
        val cumulativeWeight = ctx.getOrTokenize("cumulativeWeight", ::cumulativeWeight)
        val cumulativeWhitespace = ctx.getOrTokenize("cumulativeWhitespace", ::cumulativeWhitespace)
        val userWeight = cumulativeWeight[end] - cumulativeWeight[start]
        val whitespace = cumulativeWhitespace[end] - cumulativeWhitespace[start]

        return listOf(MatchResult(
            userMatched = userWeight,
            userWeight = userWeight,
            refMatched = if (whitespace == end-start) 0.0f else weight,
            refWeight = weight,
            end = end,
            canGrow = end != ctx.userInput.length,
        ))
    }
}
