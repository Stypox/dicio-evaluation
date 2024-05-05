package org.stypox.dicio_evaluation.context

import org.stypox.dicio_evaluation.component.MatchResult

class MatchContext(
    val userInput: String,
    val scoringFunction: (stats: MatchResult) -> Float,
    val pruningFunction: (options: MutableList<MatchResult>) -> Unit,
) {
    private val tokenizations: MutableMap<String, Any> = HashMap()

    fun <T> getOrTokenize(key: String, tokenizer: (MatchContext) -> T): T {
        tokenizations[key]?.let {
            @Suppress("UNCHECKED_CAST")
            return it as T
        }

        val tokenization = tokenizer(this)
        tokenizations[key] = tokenization as Any
        return tokenization
    }
}
