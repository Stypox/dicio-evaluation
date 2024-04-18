package org.stypox.dicio_evaluation

import kotlin.time.Duration

data class MatchInfo(
    val options: Int,
    val score: Double,
    val stats: Stats,
    val time: Duration,
)
