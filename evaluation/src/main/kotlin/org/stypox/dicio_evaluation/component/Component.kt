package org.stypox.dicio_evaluation.component

import org.stypox.dicio_evaluation.context.MatchContext

abstract class Component {
    private var cache: Array<Array<List<MatchResult>?>> = arrayOf()

    protected abstract fun match(start: Int, end: Int, ctx: MatchContext): List<MatchResult>

    fun matchCached(start: Int, end: Int, ctx: MatchContext): List<MatchResult> {
        cache[start][end]?.let { return@matchCached it }
        val result = match(start, end, ctx)
        cache[start][end] = result
        return result
    }

    open fun setupCache(userInputLength: Int) {
        cache = Array(userInputLength + 1) { Array(userInputLength + 1) { null } }
    }
}
