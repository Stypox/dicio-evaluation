package org.stypox.dicio_evaluation.component

import org.stypox.dicio_evaluation.context.MatchContext

interface Component {
    fun match(start: Int, end: Int, ctx: MatchContext): List<MatchResult>
}
