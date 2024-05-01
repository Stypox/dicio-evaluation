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

fun cumulativeWeight(ctx: MatchContext): FloatArray {
    val words = ctx.getOrTokenize("splitWords", ::splitWords)
    val result = FloatArray(ctx.userInput.length + 1)
    var lastEnd = 0

    for (word in words) {
        for (i in lastEnd..<word.start) {
            result[i+1] = result[i] + CHAR_WEIGHT
        }
        for (i in word.start..<word.end) {
            result[i+1] = result[i] + WORD_WEIGHT / (word.end - word.start)
        }
        lastEnd = word.end
    }

    for (i in lastEnd..<ctx.userInput.length) {
        result[i+1] = result[i] + CHAR_WEIGHT
    }
    return result
}

fun cumulativeWhitespace(ctx: MatchContext): IntArray {
    val result = IntArray(ctx.userInput.length + 1)
    for (i in 0..<ctx.userInput.length) {
        result[i+1] = result[i] + if (ctx.userInput[i].isWhitespace()) 1 else 0
    }
    return result
}
