package org.stypox.dicio_evaluation.context

import java.util.regex.Pattern


data class WordToken(
    override val start: Int,
    override val end: Int,
    val text: String,
) : Token

val WORD_PATTERN: Pattern = Pattern.compile("\\p{L}+")
const val WORD_WEIGHT = 1.0f
const val CHAR_WEIGHT = 0.1f

fun splitWords(ctx: MatchContext): List<WordToken> {
    val result: MutableList<WordToken> = ArrayList()
    val matcher = WORD_PATTERN.matcher(ctx.userInput)
    while (matcher.find()) {
        // TODO remove diacritics?
        result.add(WordToken(matcher.start(), matcher.end(), matcher.group().lowercase()))
    }
    return result
}

fun cumulativeWeight(ctx: MatchContext): List<Float> {
    val words = ctx.getOrTokenize("splitWords", ::splitWords)

    val result: MutableList<Float> = ArrayList()
    result.add(0.0f)
    var lastEnd = 0

    for (word in words) {
        for (i in lastEnd..<word.start) {
            result.add(result.last() + CHAR_WEIGHT)
        }
        for (i in word.start..<word.end) {
            result.add(result.last() + WORD_WEIGHT / (word.end - word.start))
        }
        lastEnd = word.end
    }

    for (i in lastEnd..<ctx.userInput.length) {
        result.add(result.last() + CHAR_WEIGHT)
    }
    return result
}
