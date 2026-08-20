package com.clearance.app.data.api.dto

/**
 * Request body for POST /api/auth/register-student.
 *
 * Matches server/controllers/auth.controller.js exactly:
 *   const { full_name, admission_number, kcse_year, class_form,
 *           stream, phone, email, password } = req.body
 *
 * All fields are plain (non-null) Strings, even the ones that are
 * optional in the UI (kcse_year, class_form, stream, phone). This
 * mirrors the real web form (client/src/pages/auth/RegisterStudent.js),
 * which keeps every field as a controlled input starting at "" and
 * always sends it — none of these fields are ever omitted or sent as
 * null by the existing application, so this DTO does not invent
 * nullability that isn't actually there.
 */
data class RegisterStudentRequest(
    val full_name: String,
    val admission_number: String,
    val kcse_year: String,
    val class_form: String,
    val stream: String,
    val phone: String,
    val email: String,
    val password: String
)
