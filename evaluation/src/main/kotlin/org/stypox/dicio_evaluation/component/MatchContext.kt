package org.stypox.dicio_evaluation.component

class MatchContext(
    val userInput: String,
    val scoringFunction: (stats: MatchResult) -> Double,
    val pruningFunction: (options: MutableList<MatchResult>) -> Unit,
) {
    private val tokenizations: MutableMap<String, Any> = HashMap()

    fun <T> getOrTokenize(key: String, tokenizer: (String) -> T): T {
        tokenizations[key]?.let {
            @Suppress("UNCHECKED_CAST")
            return it as T
        }

        val tokenization = tokenizer(userInput)
        tokenizations[key] = tokenization as Any
        return tokenization
    }
}
