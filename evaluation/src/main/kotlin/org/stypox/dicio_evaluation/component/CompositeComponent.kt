package org.stypox.dicio_evaluation.component

import org.stypox.dicio_evaluation.context.MatchContext
import org.stypox.dicio_evaluation.context.cumulativeWeight
import java.util.ArrayList

class CompositeComponent(
    private val components: List<Component>
) : Component() {
    override fun match(start: Int, end: Int, ctx: MatchContext): List<MatchResult> {
        val cumulativeWeight = ctx.getOrTokenize("cumulativeWeight", ::cumulativeWeight)
        val mem: Array<Array<List<MatchResult>?>> =
            Array(end-start+1) { Array(components.size) { null } }

        fun dp(compStart: Int, j: Int): List<MatchResult> {
            if (j >= components.size) {
                return listOf(MatchResult.empty(compStart, false))
            }
            mem[compStart - start][j]?.let { return it }

            val results = ArrayList<MatchResult>()
            var compResults = listOf<MatchResult>()
            for (compEnd in compStart..end) {
                if (compResults.isEmpty() || compResults.any { it.canGrow }) {
                    compResults = components[j].matchCached(compStart, compEnd, ctx)
                }
                val dpResults = dp(compEnd, j+1)
                for (compResult in compResults) {
                    val skippedWordsWeight =
                        cumulativeWeight[compEnd] - cumulativeWeight[compResult.end]
                    results.addAll(
                        dpResults.map {
                            MatchResult(
                                userMatched = compResult.userMatched + it.userMatched,
                                userWeight = compResult.userWeight + skippedWordsWeight +
                                        it.userWeight,
                                refMatched = compResult.refMatched + it.refMatched,
                                refWeight = compResult.refWeight + it.refWeight,
                                end = it.end,
                                canGrow = compResult.canGrow || it.canGrow,
                            )
                        }
                    )
                }
            }

            ctx.pruningFunction(results)
            mem[compStart - start][j] = results
            return results
        }

        return dp(start, 0)
    }

    override fun setupCache(userInputLength: Int) {
        super.setupCache(userInputLength)
        for (component in components) {
            component.setupCache(userInputLength)
        }
    }
}
