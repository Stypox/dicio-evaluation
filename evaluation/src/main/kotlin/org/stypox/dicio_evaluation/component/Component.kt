package org.stypox.dicio_evaluation.component

interface Component {
    fun match(start: Int, end: Int, ctx: MatchContext): List<MatchResult>
}
