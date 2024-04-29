package org.stypox.dicio_evaluation.component

import java.util.regex.Pattern


data class WordToken(
    override val start: Int,
    override val end: Int,
    val text: String,
) : Token

val WORD_PATTERN: Pattern = Pattern.compile("\\p{L}+")

fun splitWords(userInput: String): List<WordToken> {
    val result: MutableList<WordToken> = ArrayList()
    val matcher = WORD_PATTERN.matcher(userInput)
    while (matcher.find()) {
        // TODO remove diacritics?
        result.add(WordToken(matcher.start(), matcher.end(), matcher.group().lowercase()))
    }
    return result
}

data class WordComponent(
    private val text: String,
    private val weight: Float,
) : Component {
    override fun match(start: Int, end: Int, tokenizations: Tokenizations): List<MatchResult> {
        val token = tokenizations.getOrTokenize("word", ::splitWords)
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
