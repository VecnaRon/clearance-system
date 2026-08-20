package com.clearance.app.ai

/**
 * Output of AIAdvisorEngine.analyze(). Purely derived from data the
 * app already has (ClearanceDto + RecordDto) — no network call, no
 * external AI service, no fabricated data.
 */
enum class RiskLevel { LOW, MEDIUM, HIGH }

data class AIPriorityItem(
    val label: String,
    val reason: String
)

data class AIAdvisorResult(
    val progressPercent: Int,
    val clearedCount: Int,
    val totalCount: Int,
    val riskLevel: RiskLevel,
    val riskExplanation: String,
    val priority: AIPriorityItem?,
    val recommendations: List<String>,
    val readinessScore: Int,
    val estimatedCompletionText: String
)