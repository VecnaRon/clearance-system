package com.clearance.app.data.api

import com.clearance.app.data.api.dto.AdminOverviewDto
import com.clearance.app.data.api.dto.AdminUserDto
import com.clearance.app.data.api.dto.AuditLogDto
import com.clearance.app.data.api.dto.ClearanceDto
import com.clearance.app.data.api.dto.ClearanceStartRequest
import com.clearance.app.data.api.dto.ClearanceStartResponse
import com.clearance.app.data.api.dto.CreateDepartmentRequest
import com.clearance.app.data.api.dto.CreateUserRequest
import com.clearance.app.data.api.dto.DepartmentDecisionRequest
import com.clearance.app.data.api.dto.DepartmentDecisionResponse
import com.clearance.app.data.api.dto.DepartmentDto
import com.clearance.app.data.api.dto.DepartmentQueueItemDto
import com.clearance.app.data.api.dto.DepartmentReportDto
import com.clearance.app.data.api.dto.DepartmentStepDetailDto
import com.clearance.app.data.api.dto.LogRecordRequest
import com.clearance.app.data.api.dto.LoginRequest
import com.clearance.app.data.api.dto.LoginResponse
import com.clearance.app.data.api.dto.MessageResponse
import com.clearance.app.data.api.dto.PendingUserDto
import com.clearance.app.data.api.dto.RecordDto
import com.clearance.app.data.api.dto.RegisterStudentRequest
import com.clearance.app.data.api.dto.StudentMeDto
import com.clearance.app.data.api.dto.UpdateDepartmentRequest
import com.clearance.app.data.api.dto.UpdateUserRequest
import com.clearance.app.data.api.dto.UserProfileDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Streaming

/**
 * ... (prior milestone history unchanged) ...
 * THIS MILESTONE adds: getUserProfile, getStaffStudents, getMyLogsForStaff,
 * getStudentRecordsForStaff, logRecordAsStaff, resolveRecordAsStaff —
 * all confirmed against server/routes/records.routes.js and
 * server/routes/user.routes.js.
 */
interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register-student")
    suspend fun registerStudent(@Body request: RegisterStudentRequest): Response<LoginResponse>

    @GET("api/student/me")
    suspend fun getStudentMe(): Response<StudentMeDto>

    @GET("api/clearance/my-latest")
    suspend fun getMyLatestClearance(): Response<ClearanceDto>

    @POST("api/clearance/start")
    suspend fun startClearance(@Body request: ClearanceStartRequest): Response<ClearanceStartResponse>

    @GET("api/records/mine")
    suspend fun getMyRecords(): Response<List<RecordDto>>

    @Streaming
    @GET("api/clearance/{id}/certificate")
    suspend fun downloadCertificate(@Path("id") id: Int): Response<ResponseBody>

    @GET("api/admin/overview")
    suspend fun getAdminOverview(): Response<List<AdminOverviewDto>>

    @GET("api/admin/pending-users")
    suspend fun getPendingUsers(): Response<List<PendingUserDto>>

    @GET("api/admin/audit-logs")
    suspend fun getAuditLogs(): Response<List<AuditLogDto>>

    @PUT("api/admin/users/{id}/activate")
    suspend fun activateUser(@Path("id") id: Int): Response<MessageResponse>

    @POST("api/clearance/{id}/final-approve")
    suspend fun finalApproveClearance(@Path("id") id: Int): Response<MessageResponse>

    @GET("api/admin/departments")
    suspend fun getDepartments(): Response<List<DepartmentDto>>

    @POST("api/admin/departments")
    suspend fun createDepartment(@Body request: CreateDepartmentRequest): Response<MessageResponse>

    @PUT("api/admin/departments/{id}")
    suspend fun updateDepartment(@Path("id") id: Int, @Body request: UpdateDepartmentRequest): Response<MessageResponse>

    @DELETE("api/admin/departments/{id}")
    suspend fun deleteDepartment(@Path("id") id: Int): Response<MessageResponse>

    @GET("api/department/queue")
    suspend fun getDepartmentQueue(): Response<List<DepartmentQueueItemDto>>

    @GET("api/department/step/{id}")
    suspend fun getDepartmentStep(@Path("id") id: Int): Response<DepartmentStepDetailDto>

    @POST("api/department/step/{id}/decision")
    suspend fun submitDepartmentDecision(
        @Path("id") id: Int,
        @Body request: DepartmentDecisionRequest
    ): Response<DepartmentDecisionResponse>

    @GET("api/admin/users")
    suspend fun getUsers(): Response<List<AdminUserDto>>

    @POST("api/admin/users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<MessageResponse>

    @PUT("api/admin/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body request: UpdateUserRequest): Response<MessageResponse>

    @DELETE("api/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<MessageResponse>

    @PUT("api/admin/users/{id}/deactivate")
    suspend fun deactivateUser(@Path("id") id: Int): Response<MessageResponse>

    @GET("api/admin/reports/clearance-summary")
    suspend fun getClearanceReport(): Response<List<DepartmentReportDto>>

    @GET("api/users/me")
    suspend fun getUserProfile(): Response<UserProfileDto>

    @GET("api/records/students")
    suspend fun getStaffStudents(): Response<List<AdminUserDto>>

    @GET("api/records/my-logs")
    suspend fun getMyLogsForStaff(): Response<List<RecordDto>>

    @GET("api/records/student/{studentId}")
    suspend fun getStudentRecordsForStaff(@Path("studentId") studentId: Int): Response<List<RecordDto>>

    @POST("api/records/log")
    suspend fun logRecordAsStaff(@Body request: LogRecordRequest): Response<MessageResponse>

    @PUT("api/records/{id}/resolve")
    suspend fun resolveRecordAsStaff(@Path("id") id: Int): Response<MessageResponse>
}