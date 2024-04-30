package org.stypox.dicio_evaluation.component

import org.stypox.dicio_evaluation.context.MatchContext

abstract class Component {
    private var cache: Array<MutableList<List<MatchResult>?>> = arrayOf()

    protected abstract fun match(start: Int, end: Int, ctx: MatchContext): List<MatchResult>

    fun matchCached(start: Int, end: Int, ctx: MatchContext): List<MatchResult> {
        val cacheAtStart = cache[start]
        if (cacheAtStart.size <= end-start) {
            // the last element couldn't grow, so use that directly
            return cacheAtStart.last()!!
        }
        cacheAtStart[end-start]?.let { return@matchCached it }

        val result = match(start, end, ctx)
        cacheAtStart[end-start] = result

        if (result.isNotEmpty() && !result.any { it.canGrow }) {
            // none of the returned elements can grow any more than past end,
            // so we can avoid calculating again just to get the same result
            for (i in end-start+1..<cacheAtStart.size) {
                cacheAtStart.removeLast()
            }
        }
        return result
    }

    open fun setupCache(userInputLength: Int) {
        cache = Array(userInputLength + 1) { i -> MutableList(userInputLength + 1 - i) { null } }
    }
}
