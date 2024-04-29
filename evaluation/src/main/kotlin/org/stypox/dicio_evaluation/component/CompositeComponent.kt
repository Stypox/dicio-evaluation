package org.stypox.dicio_evaluation.component

import java.util.ArrayList

class CompositeComponent(
    private val components: List<Component>
) : Component {
    fun match(userInput: String): List<MatchResult> {
        return match(0, userInput.length, Tokenizations(userInput))
    }

    override fun match(start: Int, end: Int, tokenizations: Tokenizations): List<MatchResult> {
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
                    compResults = components[j].match(compStart, compEnd, tokenizations)
                }
                val dpResults = dp(compEnd, j+1)
                for (compResult in compResults) {
                    val skippedWordsWeight = (compEnd - compResult.end) * 0.1f
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

            mem[compStart - start][j] = results
            return results
        }

        return dp(start, 0)
    }
}
