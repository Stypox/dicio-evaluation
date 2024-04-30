package org.stypox.dicio_evaluation.component

import org.stypox.dicio_evaluation.context.MatchContext
import org.stypox.dicio_evaluation.context.Token
import org.stypox.dicio_evaluation.context.findTokenStartingAt
import org.stypox.dicio_evaluation.context.splitWords
import java.util.regex.Pattern


data class WordComponent(
    private val text: String,
    private val weight: Float,
) : Component() {
    override fun match(start: Int, end: Int, ctx: MatchContext): List<MatchResult> {
        val token = ctx.getOrTokenize("splitWords", ::splitWords)
            .findTokenStartingAt(start)
        return listOf(
            if (token == null || token.text != text) {
                // canGrow=false since even if end was bigger we wouldn't match anything more
                MatchResult(0.0f, 0.0f, 0.0f, weight, start, false)
            } else if (token.end > end) {
                // canGrow=true since if end was bigger we would be able to match the word
                MatchResult(0.0f, 0.0f, 0.0f, weight, start, true)
            } else {
                // canGrow=false since WordComponent matches only one word at a time
                MatchResult(1.0f, 1.0f, weight, weight, token.end, false)
            }
        )
    }
}
