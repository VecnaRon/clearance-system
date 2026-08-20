package com.clearance.app.ai

import com.clearance.app.data.api.dto.ClearanceDepartmentDto
import com.clearance.app.data.api.dto.ClearanceDto
import com.clearance.app.data.api.dto.RecordDto

/**
 * Local rule-based expert system — NOT a call to any external AI/LLM
 * API. Every number/label below is deterministically computed from
 * real data already returned by the existing backend
 * (GET /clearance/my-latest, GET /records/mine). No network request,
 * no fabricated dates, no invented fields.
 *
 * Deliberately excludes ClearanceDepartmentDto.has_dues/.dues_amount
 * (confirmed hardcoded false/0 server-side, never real data) and
 * never claims a RecordDto belongs to a specific department (that
 * correlation isn't reliable in the current API — confirmed in prior
 * backend inspection).
 *
 * analyze() never throws — any unexpected shape returns null, and the
 * caller treats null as "hide the AI card", per the explicit
 * requirement that this feature must never block or crash the
 * clearance experience.
 */
object AIAdvisorEngine {

    fun analyze(clearance: ClearanceDto?, records: List<RecordDto>): AIAdvisorResult? {
        return try {
            val departments = clearance?.departments
            if (clearance == null || departments.isNullOrEmpty()) return null

            val total = departments.size
            val cleared = departments.count { it.status == "approved" }
            val rejected = departments.filter { it.status == "rejected" }
            val pending = departments.filter { it.status == "pending" }
            val unresolved = records.filter { it.status == "unresolved" }

            val progressPercent = if (total > 0) (cleared * 100) / total else 0

            val riskLevel: RiskLevel
            val riskExplanation: String
            when {
                clearance.status == "approved" -> {
                    riskLevel = RiskLevel.LOW
                    riskExplanation = "Your clearance has been fully approved."
                }
                rejected.isNotEmpty() || unresolved.size >= 2 -> {
                    riskLevel = RiskLevel.HIGH
                    riskExplanation = buildString {
                        if (rejected.isNotEmpty()) append("${rejected.size} department(s) have rejected your clearance. ")
                        if (unresolved.size >= 2) append("You have ${unresolved.size} unresolved outstanding items.")
                    }.trim()
                }
                pending.isNotEmpty() || unresolved.size == 1 -> {
                    riskLevel = RiskLevel.MEDIUM
                    riskExplanation = buildString {
                        if (pending.isNotEmpty()) append("${pending.size} department(s) have not reviewed your request yet. ")
                        if (unresolved.size == 1) append("You have 1 unresolved outstanding item.")
                    }.trim()
                }
                else -> {
                    riskLevel = RiskLevel.LOW
                    riskExplanation = "No rejections or unresolved items \u2014 your clearance is progressing well."
                }
            }

            val priority: AIPriorityItem? = when {
                rejected.isNotEmpty() -> {
                    val dept = rejected.first()
                    AIPriorityItem(
                        label = dept.department_name,
                        reason = if (dept.remarks.isNotBlank()) {
                            "This department rejected your clearance: \"${dept.remarks}\""
                        } else {
                            "This department has rejected your clearance step."
                        }
                    )
                }
                unresolved.isNotEmpty() -> {
                    val record = unresolved.first()
                    AIPriorityItem(
                        label = "Outstanding Item",
                        reason = "You have an unresolved item: \"${record.description ?: "unspecified"}\". Resolve this with the relevant department staff."
                    )
                }
                pending.isNotEmpty() -> {
                    val dept = pending.first()
                    AIPriorityItem(
                        label = dept.department_name,
                        reason = "This department has not yet reviewed your clearance step."
                    )
                }
                else -> null
            }

            val recommendations = buildList {
                rejected.forEach { dept ->
                    add(
                        if (dept.remarks.isNotBlank()) {
                            "Resolve the issue with ${dept.department_name} \u2014 rejected: \"${dept.remarks}\""
                        } else {
                            "Resolve the issue with ${dept.department_name} \u2014 your clearance was rejected here."
                        }
                    )
                }
                if (unresolved.isNotEmpty()) {
                    add("You have ${unresolved.size} unresolved outstanding item(s). Resolve these before your clearance can proceed.")
                }
                if (pending.isNotEmpty()) {
                    val names = pending.joinToString(", ") { it.department_name }
                    add("Your clearance is still pending in ${pending.size} department(s): $names.")
                }
                if (clearance.status == "awaiting_final") {
                    add("All departments have cleared you. Your request is awaiting final approval from the admin.")
                }
                if (clearance.status == "approved") {
                    add("Your clearance is fully approved.")
                }
                if (isEmpty()) {
                    add("Your clearance is progressing normally. No immediate action is required.")
                }
            }

            // Readiness score: transparent weighted formula, not a
            // machine-learning prediction. Base = department
            // completion percentage; -15 per rejected department,
            // -10 per unresolved record, floored at 0. Fully approved
            // clearances always read 100.
            val readinessScore = if (clearance.status == "approved") {
                100
            } else {
                (progressPercent - (rejected.size * 15) - (unresolved.size * 10)).coerceIn(0, 100)
            }

            // Heuristic estimate only — no historical timing data
            // exists anywhere in the current API, so this is never
            // presented as a data-driven prediction.
            val estimatedCompletionText = when {
                clearance.status == "approved" -> "Already completed"
                rejected.isNotEmpty() || unresolved.isNotEmpty() ->
                    "Cannot estimate yet \u2014 resolve outstanding issues first"
                pending.size <= 1 -> "Estimated 1\u20132 days (heuristic estimate \u2014 no historical timing data available)"
                pending.size <= 3 -> "Estimated 2\u20134 days (heuristic estimate \u2014 no historical timing data available)"
                else -> "Estimated 4+ days (heuristic estimate \u2014 no historical timing data available)"
            }

            AIAdvisorResult(
                progressPercent = progressPercent,
                clearedCount = cleared,
                totalCount = total,
                riskLevel = riskLevel,
                riskExplanation = riskExplanation,
                priority = priority,
                recommendations = recommendations,
                readinessScore = readinessScore,
                estimatedCompletionText = estimatedCompletionText
            )
        } catch (e: Exception) {
            // Never let AI analysis crash or block the clearance screen.
            null
        }
    }
}