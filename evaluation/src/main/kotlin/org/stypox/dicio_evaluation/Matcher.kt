package org.stypox.dicio_evaluation

class Matcher(
    private val scoringFunction: (stats: Stats) -> Double,
    private val pruningFunction: (scoringFunction: (stats: Stats) -> Double, options: MutableList<Stats>) -> Unit,
    private val userWords: List<String>,
    private val refWords: List<String>,
) {
    private fun match(u: Int, r: Int): List<Stats> {
        if (u == userWords.size) {
            return listOf(Stats(0, 0, 0, refWords.size - r))
        } else if (r == refWords.size) {
            return listOf(Stats(0, userWords.size - u, 0, 0))
        }

        val res = mutableListOf<Stats>()
        res.addAll(match(u + 1, r).onEach { it.userWeight += 1 })
        res.addAll(match(u, r + 1).onEach { it.refWeight += 1 })

        val wordMatches = if (userWords[u] == refWords[r]) 1 else 0
        res.addAll(match(u + 1, r + 1).onEach {
            it.userMatched += wordMatches
            it.userWeight += 1
            it.refMatched += wordMatches
            it.refWeight += 1
        })

        pruningFunction(scoringFunction, res)
        return res
    }

    fun match(): MatchInfo {
        val options = match(0, 0)
        val bestStats = options.maxBy(scoringFunction)
        return MatchInfo(
            options = options.size,
            score = scoringFunction(bestStats),
            stats = bestStats,
        )
    }
}
