// services/ai_advisor_service.js
import { pool } from "../config/db.js";
import crypto from "crypto";

export async function buildClearanceSummary(clearanceId) {
  const [[clearance]] = await pool.query(
    "SELECT * FROM clearances WHERE id = ?",
    [clearanceId]
  );
  if (!clearance) {
    throw Object.assign(new Error("Clearance not found"), { status: 404 });
  }

  const [[student]] = await pool.query(
    "SELECT id, full_name, admission_number, class_form FROM users WHERE id = ?",
    [clearance.student_id]
  );

  const [steps] = await pool.query(
    `SELECT cs.id, cs.status, cs.comment, cs.decided_at,
            d.id AS department_id, d.name AS department_name
     FROM clearance_steps cs
     JOIN departments d ON d.id = cs.department_id
     WHERE cs.clearance_id = ?
     ORDER BY d.name`,
    [clearanceId]
  );

  const totalDepartments = steps.length;
  const approvedCount = steps.filter((s) => s.status === "approved").length;
  const rejectedCount = steps.filter((s) => s.status === "rejected").length;
  const pendingCount = steps.filter((s) => s.status === "pending").length;

  const readinessPercent = totalDepartments > 0
    ? Math.round((approvedCount / totalDepartments) * 100)
    : 0;

  const blockingDepartments = steps
    .filter((s) => s.status !== "approved")
    .map((s) => ({
      department_id: s.department_id,
      department_name: s.department_name,
      status: s.status,
      comment: s.comment || null,
    }));

  // Unresolved dues/records for this student, regardless of department
  const [outstandingRecords] = await pool.query(
    `SELECT dr.id, dr.description, dr.amount, dr.created_at, d.name AS department_name
     FROM department_records dr
     JOIN departments d ON d.id = dr.department_id
     WHERE dr.student_id = ? AND dr.status = 'unresolved'
     ORDER BY dr.created_at`,
    [clearance.student_id]
  );

  const totalOutstandingAmount = outstandingRecords.reduce(
    (sum, r) => sum + (r.amount ? Number(r.amount) : 0),
    0
  );

  const createdAt = new Date(clearance.created_at);
  const elapsedDays = Math.floor((Date.now() - createdAt.getTime()) / (1000 * 60 * 60 * 24));

  return {
    clearance_id: clearance.id,
    clearance_status: clearance.status,
    student: {
      id: student?.id ?? null,
      full_name: student?.full_name ?? "Unknown",
      admission_number: student?.admission_number ?? null,
      class_form: student?.class_form ?? null,
    },
    readiness_percent: readinessPercent,
    total_departments: totalDepartments,
    approved_count: approvedCount,
    rejected_count: rejectedCount,
    pending_count: pendingCount,
    blocking_departments: blockingDepartments,
    outstanding_records: outstandingRecords.map((r) => ({
      id: r.id,
      department_name: r.department_name,
      description: r.description,
      amount: r.amount,
      logged_at: r.created_at,
    })),
    total_outstanding_amount: totalOutstandingAmount,
    elapsed_days: elapsedDays,
  };
}

// Hash only the parts of the summary that should trigger a fresh AI call.
// elapsed_days is deliberately excluded — it ticks over daily on its own
// and shouldn't invalidate a cached analysis whose facts haven't changed.

const PROMPT_VERSION = "v2";
export function computeSummaryHash(summary) {
  // Strip fields that shouldn't invalidate the cache (elapsed_days ticks daily
  // on its own; student/clearance_id/department_name are identity, not facts).
  const { elapsed_days, student, clearance_id, department_name, ...relevant } = summary;
  return crypto.createHash("sha256").update(PROMPT_VERSION + JSON.stringify(relevant)).digest("hex");
}

// --- Rate limit: simple in-memory cooldown, NOT the cache ---
const lastRequestByUser = new Map(); // key: user email, value: timestamp
const COOLDOWN_MS = 30 * 1000;

function checkCooldown(userEmail) {
  const last = lastRequestByUser.get(userEmail);
  const now = Date.now();
  if (last && now - last < COOLDOWN_MS) {
    const waitSeconds = Math.ceil((COOLDOWN_MS - (now - last)) / 1000);
    throw Object.assign(
      new Error(`Please wait ${waitSeconds}s before requesting another analysis`),
      { status: 429 }
    );
  }
  lastRequestByUser.set(userEmail, now);
}

// --- Provider adapter — swap this one function to change AI providers ---
async function callAiProvider(prompt) {
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    throw Object.assign(new Error("AI provider not configured"), { status: 500 });
  }

  const response = await fetch(
    `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=${apiKey}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{ parts: [{ text: prompt }] }],
        generationConfig: { responseMimeType: "application/json" },
      }),
    }
  );

  if (!response.ok) {
    const errText = await response.text();
    throw Object.assign(
      new Error(`AI provider error: ${response.status} ${errText}`),
      { status: 502 }
    );
  }

  const data = await response.json();
  const text = data.candidates?.[0]?.content?.parts?.[0]?.text;
  if (!text) {
    throw Object.assign(new Error("AI provider returned no content"), { status: 502 });
  }

  return JSON.parse(text);
}

async function buildDepartmentScopedSummary(clearanceId, departmentId) {
  const [[clearance]] = await pool.query("SELECT * FROM clearances WHERE id = ?", [clearanceId]);
  if (!clearance) {
    throw Object.assign(new Error("Clearance not found"), { status: 404 });
  }

  const [[step]] = await pool.query(
    `SELECT cs.status, cs.comment, d.name AS department_name
     FROM clearance_steps cs
     JOIN departments d ON d.id = cs.department_id
     WHERE cs.clearance_id = ? AND cs.department_id = ?`,
    [clearanceId, departmentId]
  );

  const [records] = await pool.query(
    `SELECT description, amount, created_at
     FROM department_records
     WHERE student_id = ? AND department_id = ? AND status = 'unresolved'`,
    [clearance.student_id, departmentId]
  );

  const totalOutstandingAmount = records.reduce(
    (sum, r) => sum + (r.amount ? Number(r.amount) : 0),
    0
  );

  const createdAt = new Date(clearance.created_at);
  const elapsedDays = Math.floor((Date.now() - createdAt.getTime()) / (1000 * 60 * 60 * 24));

  return {
    clearance_status: clearance.status,
    department_name: step ? step.department_name : "Unknown",
    department_status: step ? step.status : "unknown",
    department_comment: step ? step.comment || null : null,
    outstanding_records: records.map((r) => ({
      description: r.description,
      amount: r.amount,
      logged_at: r.created_at,
    })),
    total_outstanding_amount: totalOutstandingAmount,
    elapsed_days: elapsedDays,
  };
}

function buildPrompt(summary, scopeDepartmentName = null) {
  if (scopeDepartmentName) {
    const scoped = {
      clearance_status: summary.clearance_status,
      department_status: summary.department_status,
      department_comment: summary.department_comment,
      outstanding_records: summary.outstanding_records,
      total_outstanding_amount: summary.total_outstanding_amount,
      elapsed_days: summary.elapsed_days,
    };

    return `You are advising a Head of Department in the ${scopeDepartmentName} department about one student's clearance, from this department's perspective only. You have no visibility into any other department and must not mention or speculate about other departments.

  Respond with ONLY a JSON object (no markdown, no prose outside the JSON) with these exact keys:
  - "risk_level": one of "low", "medium", "high" (based on this department's own status and records only)
  - "priority_department": "${scopeDepartmentName}" if this department's status is not "approved", otherwise null
  - "recommendations": array of 2-4 short actionable strings, only about what this department or the student needs to do for this department's step
  - "estimated_completion": short human-readable string for this department's own step (e.g. "1-2 days", "Awaiting book return")
  - "Write recommendations as complete, grammatically correct sentences with correct punctuation. Refer to statuses using proper capitalization ("Approved", "Rejected", "Pending") rather than raw lowercase values, and capitalize department names properly."
  - "Prefix any monetary amount you mention with "KES" (e.g. "KES 2,750"), never a bare number."

  Data for the ${scopeDepartmentName} department only:
  ${JSON.stringify(scoped, null, 2)}`;
  }

  const anonymized = {
    clearance_status: summary.clearance_status,
    readiness_percent: summary.readiness_percent,
    total_departments: summary.total_departments,
    approved_count: summary.approved_count,
    rejected_count: summary.rejected_count,
    pending_count: summary.pending_count,
    blocking_departments: summary.blocking_departments.map((d) => ({
      department_name: d.department_name,
      status: d.status,
      comment: d.comment,
    })),
    outstanding_records: summary.outstanding_records.map((r) => ({
      department_name: r.department_name,
      description: r.description,
      amount: r.amount,
    })),
    total_outstanding_amount: summary.total_outstanding_amount,
    elapsed_days: summary.elapsed_days,
  };

  return `You are advising on a school clearance process (student identity withheld).
  Given this JSON summary of a clearance status, respond with ONLY a JSON object
  (no markdown, no prose outside the JSON) with these exact keys:
  - "risk_level": one of "low", "medium", "high"
  - "priority_department": name of the single most urgent blocking department, or null if none
  - "recommendations": array of 2-4 short actionable strings
  - "estimated_completion": short human-readable string (e.g. "1-2 days", "Blocked indefinitely")
  - "Write recommendations as complete, grammatically correct sentences with correct punctuation. Refer to statuses using proper capitalization ("Approved", "Rejected", "Pending") rather than raw lowercase values, and capitalize department names properly."
  - "- Prefix any monetary amount you mention with "KES" (e.g. "KES 2,750"), never a bare number."
  
  Summary:
  ${JSON.stringify(anonymized, null, 2)}`;
}

// --- Main entry point ---
export async function getAdvisorAnalysis(clearanceId, requester) {
  // requester = { email, role }
  let scopeDepartmentId = null;
  let scopeLabel = "all";

  if (requester.role === "hod") {
    const [[hodUser]] = await pool.query(
      "SELECT department_id FROM users WHERE email = ?",
      [requester.email]
    );
    if (!hodUser || !hodUser.department_id) {
      throw Object.assign(new Error("You are not assigned to a department"), { status: 400 });
    }
    scopeDepartmentId = hodUser.department_id;
    scopeLabel = `dept_${scopeDepartmentId}`;
  }

  const summary = scopeDepartmentId
    ? await buildDepartmentScopedSummary(clearanceId, scopeDepartmentId)
    : await buildClearanceSummary(clearanceId);

  const hash = computeSummaryHash(summary);

  const [[cached]] = await pool.query(
    "SELECT analysis, summary_hash FROM ai_advisor_cache WHERE clearance_id = ? AND scope = ?",
    [clearanceId, scopeLabel]
  );

  if (cached && cached.summary_hash === hash) {
    return { ...JSON.parse(cached.analysis), cached: true, summary };
  }

  checkCooldown(requester.email);

  const prompt = buildPrompt(summary, scopeDepartmentId ? summary.department_name : null);
  const analysis = await callAiProvider(prompt);

  await pool.query(
    `INSERT INTO ai_advisor_cache (clearance_id, scope, summary_hash, analysis, created_at)
     VALUES (?, ?, ?, ?, NOW())
     ON DUPLICATE KEY UPDATE summary_hash = ?, analysis = ?, created_at = NOW()`,
    [clearanceId, scopeLabel, hash, JSON.stringify(analysis), hash, JSON.stringify(analysis)]
  );

  return { ...analysis, cached: false, summary };
}