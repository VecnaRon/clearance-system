"use client"

import { useEffect, useState } from "react"
import styled from "styled-components"
import Card from "../../components/Card"
import Button from "../../components/Button"
import api from "../../services/api"

// ─── Layout ──────────────────────────────────────────────────────────────────

const PageContainer = styled.div`
  max-width: 900px;
  margin: 0 auto;
  display: grid;
  gap: ${({ theme }) => theme.spacing(4)};
`

const HeaderCard = styled(Card)`
  padding: ${({ theme }) => theme.spacing(6)};
  background: linear-gradient(135deg, ${({ theme }) => theme.colors.primary}, ${({ theme }) => theme.colors.accent});
  color: white;
  border: none;
  box-shadow: ${({ theme }) => theme.shadow.xl};
`

const SectionCard = styled(Card)`
  padding: ${({ theme }) => theme.spacing(5)};
  background: white;
  border: 1px solid ${({ theme }) => theme.colors.border};
  box-shadow: ${({ theme }) => theme.shadow.lg};
`

const SectionTitle = styled.h3`
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 1.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
`

// ─── Search ───────────────────────────────────────────────────────────────────

const SearchRow = styled.div`
  display: flex;
  gap: ${({ theme }) => theme.spacing(2)};
  margin-bottom: ${({ theme }) => theme.spacing(4)};

  input {
    flex: 1;
    padding: ${({ theme }) => theme.spacing(2)} ${({ theme }) => theme.spacing(3)};
    border: 1px solid ${({ theme }) => theme.colors.border};
    border-radius: ${({ theme }) => theme.radius};
    font-size: 1rem;
    outline: none;

    &:focus {
      border-color: ${({ theme }) => theme.colors.primary};
      box-shadow: 0 0 0 3px ${({ theme }) => theme.colors.primary}22;
    }
  }
`

const StudentResult = styled.div`
  padding: ${({ theme }) => theme.spacing(3)} ${({ theme }) => theme.spacing(4)};
  border: 1px solid ${({ selected, theme }) =>
    selected ? theme.colors.primary : theme.colors.border};
  border-radius: ${({ theme }) => theme.radius};
  background: ${({ selected }) => (selected ? "#eff6ff" : "#ffffff")};
  cursor: pointer;
  margin-bottom: ${({ theme }) => theme.spacing(2)};
  transition: all 0.2s ease;

  &:hover {
    border-color: ${({ theme }) => theme.colors.primary};
    background: #eff6ff;
  }

  .student-name {
    font-weight: 700;
    color: #1e293b;
    font-size: 1rem;
  }

  .student-meta {
    font-size: 0.85rem;
    color: #64748b;
    margin-top: 2px;
  }
`

// ─── Log Form ─────────────────────────────────────────────────────────────────

const LogForm = styled.div`
  margin-top: ${({ theme }) => theme.spacing(4)};
  padding: ${({ theme }) => theme.spacing(4)};
  background: #f8fafc;
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.radius};

  label {
    display: block;
    font-size: 0.875rem;
    font-weight: 600;
    color: #374151;
    margin-bottom: ${({ theme }) => theme.spacing(1)};
  }

  textarea {
    width: 100%;
    padding: ${({ theme }) => theme.spacing(2)} ${({ theme }) => theme.spacing(3)};
    border: 1px solid ${({ theme }) => theme.colors.border};
    border-radius: ${({ theme }) => theme.radius};
    font-size: 1rem;
    resize: vertical;
    min-height: 100px;
    font-family: inherit;
    outline: none;
    box-sizing: border-box;

    &:focus {
      border-color: ${({ theme }) => theme.colors.primary};
      box-shadow: 0 0 0 3px ${({ theme }) => theme.colors.primary}22;
    }
  }
`

const SelectedStudentBanner = styled.div`
  padding: ${({ theme }) => theme.spacing(3)};
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: ${({ theme }) => theme.radius};
  margin-bottom: ${({ theme }) => theme.spacing(3)};
  display: flex;
  justify-content: space-between;
  align-items: center;

  .info {
    font-size: 0.95rem;
    color: #1e40af;
    font-weight: 600;
  }

  .change {
    font-size: 0.8rem;
    color: #3b82f6;
    cursor: pointer;
    text-decoration: underline;
  }
`

// ─── My Logs ──────────────────────────────────────────────────────────────────

const StatusPill = styled.span`
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 700;
  background: ${({ status }) => (status === "resolved" ? "#dcfce7" : "#fee2e2")};
  color: ${({ status }) => (status === "resolved" ? "#166534" : "#991b1b")};
  text-transform: uppercase;
  letter-spacing: 0.5px;
`

const ResolveButton = styled.button`
  padding: 5px 12px;
  font-size: 0.8rem;
  font-weight: 600;
  border: 1px solid #22c55e;
  border-radius: 6px;
  background: white;
  color: #16a34a;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #dcfce7;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`

const FeedbackMessage = styled.p`
  margin-top: ${({ theme }) => theme.spacing(2)};
  padding: ${({ theme }) => theme.spacing(2)} ${({ theme }) => theme.spacing(3)};
  border-radius: ${({ theme }) => theme.radius};
  font-size: 0.9rem;
  font-weight: 600;
  background: ${({ success }) => (success ? "#d4edda" : "#f8d7da")};
  color: ${({ success }) => (success ? "#155724" : "#721c24")};
  border: 1px solid ${({ success }) => (success ? "#c3e6cb" : "#f5c6cb")};
`

const LoadingSpinner = styled.div`
  display: flex;
  justify-content: center;
  padding: ${({ theme }) => theme.spacing(6)};

  &::after {
    content: '';
    width: 32px;
    height: 32px;
    border: 3px solid ${({ theme }) => theme.colors.gray[300]};
    border-top: 3px solid ${({ theme }) => theme.colors.primary};
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
`

const EmptyState = styled.div`
  text-align: center;
  padding: ${({ theme }) => theme.spacing(6)};
  color: #94a3b8;

  .icon {
    font-size: 2rem;
    margin-bottom: 0.75rem;
  }

  p {
    margin: 0;
    font-size: 0.95rem;
  }
`

const FilterRow = styled.div`
  display: flex;
  gap: ${({ theme }) => theme.spacing(2)};
  margin-bottom: ${({ theme }) => theme.spacing(3)};

  select {
    padding: ${({ theme }) => theme.spacing(1)} ${({ theme }) => theme.spacing(3)};
    border: 1px solid ${({ theme }) => theme.colors.border};
    border-radius: ${({ theme }) => theme.radius};
    font-size: 0.9rem;
    background: white;
    cursor: pointer;
    outline: none;

    &:focus {
      border-color: ${({ theme }) => theme.colors.primary};
    }
  }
`

// ─── Main Component ───────────────────────────────────────────────────────────

export default function StaffDashboard() {
  const [me, setMe] = useState(null)

 // Student list
  const [students, setStudents] = useState([])
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedStudent, setSelectedStudent] = useState(null)
  const [studentRecords, setStudentRecords] = useState([])
  const [loadingRecords, setLoadingRecords] = useState(false)

  // Log form
  const [description, setDescription] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const [logMessage, setLogMessage] = useState(null)
  const [amount, setAmount] = useState("")

  // My logs
  const [myLogs, setMyLogs] = useState([])
  const [logsLoading, setLogsLoading] = useState(true)
  const [filterStatus, setFilterStatus] = useState("all")
  const [resolvingId, setResolvingId] = useState(null)

  // Load current staff profile and their logs
  useEffect(() => {
  ;(async () => {
    try {
      const { data } = await api.get("/users/me")
      setMe(data)
    } catch (error) {
      console.error("Error loading staff profile:", error)
    }
    try {
      const { data: studentData } = await api.get("/records/students")
      setStudents(studentData || [])
    } catch (error) {
      console.error("Error loading students:", error)
    }
    })()
    fetchMyLogs()
  }, [])

  const fetchMyLogs = async () => {
    setLogsLoading(true)
    try {
      const { data } = await api.get("/records/my-logs")
      setMyLogs(data || [])
    } catch (error) {
      console.error("Error loading logs:", error)
      setMyLogs([])
    } finally {
      setLogsLoading(false)
    }
  }

  const fetchStudentRecords = async (studentId) => {
    setLoadingRecords(true)
    try {
      const { data } = await api.get(`/records/student/${studentId}`)
      setStudentRecords(data || [])
    } catch (error) {
      setStudentRecords([])
    } finally {
      setLoadingRecords(false)
    }
  } 

  // Submit a new outstanding record
  const handleLogRecord = async () => {
    if (!selectedStudent || !description.trim()) return
    setSubmitting(true)
    setLogMessage(null)
    try {
      await api.post("/records/log", {
        studentId: selectedStudent.id,
        description: description.trim(),
        amount: amount !== "" ? parseFloat(amount) : null,
      })
      setLogMessage({ success: true, text: `Record logged successfully for ${selectedStudent.full_name}.` })
      setDescription("")
      setAmount("")
      fetchMyLogs()
      fetchStudentRecords(selectedStudent.id)
    } catch (error) {
      setLogMessage({
        success: false,
        text: error?.response?.data?.error || "Failed to log record. Please try again.",
      })
    } finally {
      setSubmitting(false)
    }
  }

  // Mark a record as resolved
  const handleResolve = async (recordId) => {
    setResolvingId(recordId)
    try {
      await api.put(`/records/${recordId}/resolve`)
      fetchMyLogs()
    } catch (error) {
      console.error("Error resolving record:", error)
      alert(error?.response?.data?.error || "Failed to resolve record.")
    } finally {
      setResolvingId(null)
    }
  }

  const filteredLogs = myLogs.filter((r) => {
    if (filterStatus === "all") return true
    return r.status === filterStatus
  })

  const thStyle = { padding: "10px 12px", textAlign: "left", fontWeight: "700", color: "#374151", fontSize: "0.8rem", textTransform: "uppercase" }
  const tdStyle = { padding: "10px 12px", color: "#1e293b", verticalAlign: "middle" }

  return (
    <PageContainer>

      {/* ── Header ── */}
      <HeaderCard>
        <h1 style={{ margin: "0 0 0.5rem 0", fontSize: "1.75rem" }}>Staff Dashboard</h1>
        <p style={{ margin: 0, opacity: 0.9, fontSize: "1rem" }}>
          {me
            ? `Welcome, ${me.full_name} — ${me.department_name || "Department not assigned"}`
            : "Loading your profile..."}
        </p>
      </HeaderCard>

      {/* ── Log a New Record ── */}
      <SectionCard>
        <SectionTitle>👥 Students</SectionTitle>

        <SearchRow>
          <input
            type="text"
            placeholder="Filter by name or admission number..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </SearchRow>

        {students
          .filter((s) => {
            if (!searchQuery.trim()) return true
            const term = searchQuery.toLowerCase()
            return (
              s.full_name?.toLowerCase().includes(term) ||
              s.admission_number?.toLowerCase().includes(term)
            )
          })
          .map((student) => (
            <StudentResult
              key={student.id}
              selected={selectedStudent?.id === student.id}
              onClick={() => {
                setSelectedStudent(student)
                setDescription("")
                setLogMessage(null)
                fetchStudentRecords(student.id)
              }}
            >
              <div className="student-name">{student.full_name}</div>
              <div className="student-meta">
                Adm: {student.admission_number} &nbsp;|&nbsp;
                Form: {student.class_form || "N/A"} &nbsp;|&nbsp;
                Stream: {student.stream || "N/A"}
              </div>
            </StudentResult>
          ))}

        {/* Panel shown when a student is selected */}
        {selectedStudent && (
          <LogForm>
            <SelectedStudentBanner>
              <div className="info">
                Selected: {selectedStudent.full_name} ({selectedStudent.admission_number})
              </div>
              <span className="change" onClick={() => {
                setSelectedStudent(null)
                setStudentRecords([])
                setDescription("")
              }}>
                Close
              </span>
            </SelectedStudentBanner>

            {/* Existing records for this student */}
            <label style={{ marginBottom: "0.5rem", display: "block" }}>
              Existing Records for this Student
            </label>
            {loadingRecords ? (
              <p style={{ color: "#94a3b8", fontSize: "0.9rem" }}>Loading records...</p>
            ) : studentRecords.length === 0 ? (
              <p style={{ color: "#94a3b8", fontSize: "0.9rem", marginBottom: "1rem" }}>
                No records logged yet for this student.
              </p>
            ) : (
              <table style={{ width: "100%", borderCollapse: "collapse", marginBottom: "1rem", fontSize: "0.9rem" }}>
                <thead>
                  <tr style={{ background: "#f1f5f9" }}>
                    <th style={thStyle}>Student</th>
                    <th style={thStyle}>Description</th>
                    <th style={thStyle}>Amount (KES)</th>
                    <th style={thStyle}>Status</th>
                    <th style={thStyle}>Date</th>
                    <th style={thStyle}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {studentRecords.map((r) => (
                    <tr key={r.id} style={{ borderBottom: "1px solid #e5e7eb" }}>
                      <td style={tdStyle}>
                        <div style={{ fontWeight: "600" }}>{r.studentName || "Unknown"}</div>
                        {r.studentAdmission && (
                          <div style={{ fontSize: "0.75rem", color: "#94a3b8", fontFamily: "monospace" }}>
                            {r.studentAdmission}
                          </div>
                        )}
                      </td>
                      <td style={tdStyle}>{r.description}</td>
                      <td style={tdStyle}>{r.amount != null ? `KES ${Number(r.amount).toLocaleString()}` : "—"}</td>
                      <td style={tdStyle}>
                        <StatusPill status={r.status}>{r.status}</StatusPill>
                      </td>
                      <td style={tdStyle}>{new Date(r.createdAt).toLocaleDateString("en-KE", { day: "numeric", month: "short", year: "numeric" })}</td>
                      <td style={tdStyle}>
                        {r.status === "unresolved" && (
                          <ResolveButton
                            onClick={() => handleResolve(r.id).then(() => fetchStudentRecords(selectedStudent.id))}
                            disabled={resolvingId === r.id}
                          >
                            {resolvingId === r.id ? "Saving..." : "Mark Resolved"}
                          </ResolveButton>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr style={{ background: "#f8fafc", fontWeight: "700" }}>
                    <td style={tdStyle} colSpan = "2">Total Outstanding</td>
                    <td style={tdStyle}>
                      KES {studentRecords
                        .filter(r => r.status === "unresolved" && r.amount != null)
                        .reduce((sum, r) => sum + r.amount, 0)
                        .toLocaleString()}
                    </td>
                    <td colSpan="3" style={tdStyle}></td>
                  </tr>
                </tfoot>
              </table>
            )}

            {/* Add new record */}
            <label style={{ marginTop: "1rem", display: "block" }}>
              Log a New Outstanding Item
            </label>
            <textarea
              placeholder="e.g. Unreturned library book — Introduction to Java"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
            <label style={{ marginTop: "0.75rem", display: "block" }}>
              Amount (KES) — optional, leave blank if not applicable
            </label>
            <input
              type="number"
              min="0"
              step="0.01"
              placeholder="e.g. 3500"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              style={{
                width: "100%",
                padding: "10px 14px",
                border: "1px solid #e5e7eb",
                borderRadius: "8px",
                fontSize: "1rem",
                boxSizing: "border-box",
                marginTop: "4px"
              }}
            />
            
            <div style={{ marginTop: "1rem" }}>
              <Button
                onClick={handleLogRecord}
                disabled={submitting || !description.trim()}
              >
                {submitting ? "Logging..." : "Log Record"}
              </Button>
            </div>
          </LogForm>
        )}

        {logMessage && (
          <FeedbackMessage success={logMessage.success}>{logMessage.text}</FeedbackMessage>
        )}
      </SectionCard>

      {/* ── My Logged Records ── */}
      <SectionCard>
        <SectionTitle>📂 My Logged Records</SectionTitle>

        <FilterRow>
          <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
            <option value="all">All Records</option>
            <option value="unresolved">Unresolved Only</option>
            <option value="resolved">Resolved Only</option>
          </select>
          <span style={{ color: "#64748b", fontSize: "0.9rem", alignSelf: "center" }}>
            {filteredLogs.length} record{filteredLogs.length !== 1 ? "s" : ""}
          </span>
        </FilterRow>

        {logsLoading ? (
          <LoadingSpinner />
        ) : filteredLogs.length === 0 ? (
          <EmptyState>
            <div className="icon">📭</div>
            <p>No records found.</p>
          </EmptyState>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse", fontSize: "0.9rem" }}>
            <thead>
              <tr style={{ background: "#f1f5f9" }}>
                <th style={thStyle}>Student</th>
                <th style={thStyle}>Description</th>
                <th style={thStyle}>Amount (KES)</th>
                <th style={thStyle}>Status</th>
                <th style={thStyle}>Date Logged</th>
                <th style={thStyle}>Resolved On</th>
                <th style={thStyle}>Action</th>
              </tr>
            </thead>
            <tbody>
              {filteredLogs.map((record) => (
                <tr key={record.id} style={{ borderBottom: "1px solid #e5e7eb", background: record.status === "resolved" ? "#f0fdf4" : "#fff5f5" }}>
                   <td style={tdStyle}>
                  <div style={{ fontWeight: "600" }}>{record.studentName || "Unknown"}</div>
                  {record.studentAdmission && (
                    <div style={{ fontSize: "0.75rem", color: "#94a3b8", fontFamily: "monospace" }}>
                      {record.studentAdmission}
                    </div>
                  )}
                </td>
                  
                  <td style={tdStyle}>{record.description}</td>
                  <td style={tdStyle}>{record.amount != null ? `KES ${Number(record.amount).toLocaleString()}` : "—"}</td>
                  <td style={tdStyle}>
                    <StatusPill status={record.status}>{record.status}</StatusPill>
                  </td>
                  <td style={tdStyle}>
                    {new Date(record.createdAt).toLocaleDateString("en-KE", { day: "numeric", month: "short", year: "numeric" })}
                  </td>
                  <td style={tdStyle}>
                    {record.resolvedAt
                      ? new Date(record.resolvedAt).toLocaleDateString("en-KE", { day: "numeric", month: "short", year: "numeric" })
                      : "—"}
                  </td>
                  <td style={tdStyle}>
                    {record.status === "unresolved" && (
                      <ResolveButton
                        onClick={() => handleResolve(record.id)}
                        disabled={resolvingId === record.id}
                      >
                        {resolvingId === record.id ? "Saving..." : "Mark Resolved"}
                      </ResolveButton>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr style={{ background: "#f8fafc", fontWeight: "700" }}>
                <td style={tdStyle} colSpan="2">Total Outstanding</td>
                <td style={tdStyle}>
                  KES {filteredLogs
                    .filter(r => r.status === "unresolved" && r.amount != null)
                    .reduce((sum, r) => sum + r.amount, 0)
                    .toLocaleString()}
                </td>
                <td colSpan="4" style={tdStyle}></td>
              </tr>
            </tfoot>
          </table>
        )}
      </SectionCard>

    </PageContainer>
  )
}