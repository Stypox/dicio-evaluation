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
    override fun match(start: Int, end: Int, tokenizations: Tokenizations): MatchResult {
        val token = tokenizations.getOrTokenize("word", ::splitWords)
            .findTokenStartingAt(start)
        return if (token == null || token.end > end || token.text != text) {
            MatchResult(0.0f, 0.0f, 0.0f, weight, start)
        } else {
            MatchResult(1.0f, 1.0f, weight, weight, token.end)
        }
    }
}
