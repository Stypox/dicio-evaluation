package org.stypox.dicio_evaluation

import org.stypox.dicio_evaluation.component.MatchResult
import kotlin.time.Duration

data class MatchInfo(
    val options: Int,
    val score: Double,
    val result: MatchResult,
    val time: Duration,
)
