"use client"

import { useEffect, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import Card from "../../components/Card"
import Button from "../../components/Button"
import api from "../../services/api"
import styled from "styled-components"

const ReviewContainer = styled.div`
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 80px);
`

const StudentHeader = styled.div`
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  color: white;
  padding: 32px;
  border-radius: 16px;
  margin-bottom: 32px;
  text-align: center;
  
  h1 {
    font-size: 2rem;
    font-weight: 800;
    margin-bottom: 8px;
    color: white;
  }
  
  .admission {
    font-family: monospace;
    font-size: 1.125rem;
    opacity: 0.9;
    margin-bottom: 16px;
  }
  
  .department {
    background: rgba(255, 255, 255, 0.1);
    padding: 8px 16px;
    border-radius: 20px;
    display: inline-block;
    font-weight: 600;
  }
`

const FormGrid = styled.div`
  display: grid;
  gap: 24px;
`

const Field = styled.div`
  label {
    display: block;
    font-weight: 600;
    color: #374151;
    margin-bottom: 8px;
    font-size: 14px;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }
  
  select, textarea, input {
    width: 100%;
    padding: 12px 16px;
    border: 2px solid #e5e7eb;
    border-radius: 12px;
    font-size: 16px;
    transition: all 0.2s ease;
    background: white;
    
    &:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }
  }
  
  textarea {
    min-height: 120px;
    resize: vertical;
  }
  
  input[type="checkbox"] {
    width: auto;
    margin-right: 8px;
  }
  
  input[type="number"] {
    font-family: monospace;
  }
`

const CheckboxField = styled(Field)`
  label {
    display: flex;
    align-items: center;
    font-weight: 500;
    text-transform: none;
    letter-spacing: normal;
    cursor: pointer;
    
    input {
      margin-right: 12px;
      width: 18px;
      height: 18px;
    }
  }
`

const ButtonGroup = styled.div`
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 32px;
  
  @media (max-width: 640px) {
    flex-direction: column;
  }
`

const StatusBadge = styled.span`
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background: ${({ status }) =>
    status === "pending"
      ? "linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)"
      : status === "cleared"
        ? "linear-gradient(135deg, #10b981 0%, #059669 100%)"
        : "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)"};
  color: white;
`

const RecordsSection = styled.div`
  margin-top: 24px;
  padding: 16px;
  background: #fef9f0;
  border: 1px solid #f59e0b;
  border-radius: 12px;

  h3 {
    margin: 0 0 16px 0;
    color: #92400e;
    font-size: 1rem;
  }
`

const AdvisorSection = styled.div`
  margin-top: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
  border: 1px solid #ddd6fe;
  border-radius: 12px;

  h3 {
    margin: 0 0 16px 0;
    color: #4c1d95;
    font-size: 1rem;
  }
`

const RiskBadge = styled.span`
  display: inline-block;
  padding: 4px 14px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: ${({ level }) =>
    level === "high" ? "#fee2e2" : level === "medium" ? "#fef3c7" : "#dcfce7"};
  color: ${({ level }) =>
    level === "high" ? "#991b1b" : level === "medium" ? "#92400e" : "#166534"};
`

const AdvisorRecommendation = styled.li`
  padding: 8px 0;
  border-bottom: 1px solid #e9d5ff;
  color: #4c1d95;
  line-height: 1.5;
  font-size: 0.9rem;

  &:last-child {
    border-bottom: none;
  }
`

function formatRecommendation(text, keywords) {
  if (!keywords || keywords.length === 0) return text
  const escaped = keywords.map((k) => k.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"))
  const pattern = new RegExp(`\\b(${escaped.join("|")})\\b`, "gi")
  const parts = text.split(pattern)
  return parts.map((part, i) =>
    keywords.some((k) => k.toLowerCase() === part.toLowerCase())
      ? <strong key={i}>{part}</strong>
      : part
  )
}

export default function DeptStudentReview() {
  const { id } = useParams()
  const nav = useNavigate()
  const [item, setItem] = useState(null)
  const [status, setStatus] = useState("pending")
  const [remarks, setRemarks] = useState("")
  const [has_dues, setHasDues] = useState(false)
  const [dues_amount, setDuesAmount] = useState(0)
  const [records, setRecords] = useState([])
  const [advisorResult, setAdvisorResult] = useState(null)
  const [advisorLoading, setAdvisorLoading] = useState(false)
  const [advisorError, setAdvisorError] = useState(null)


  useEffect(() => {
    ;(async () => {
      const { data } = await api.get(`/department/step/${id}`)
      setItem(data)
      setStatus(data.status)
      setRemarks(data.remarks || "")
      setHasDues(Boolean(data.has_dues))
      setDuesAmount(data.dues_amount || 0)
      setRecords(data.records || [])
    })()
  }, [id])

  const hasUnresolved = records.some(r => r.status === "unresolved")
  const save = async (newStatus) => {
    await api.post(`/department/step/${id}/decision`, {
      status: newStatus || status,
      remarks,
      has_dues,
      dues_amount,
    })
    nav("/department/dashboard")
  }

  const thStyle = { padding: "10px 12px", textAlign: "left", fontWeight: "700", color: "#374151", fontSize: "0.8rem", textTransform: "uppercase" }
  const tdStyle = { padding: "10px 12px", color: "#1e293b", verticalAlign: "middle" }

  const runAdvisor = async () => {
    if (!item?.clearanceId) return
    setAdvisorLoading(true)
    setAdvisorError(null)
    try {
      const { data } = await api.post(`/clearance/${item.clearanceId}/advisor`)
      setAdvisorResult(data)
    } catch (error) {
      console.error("Error running advisor:", error)
      setAdvisorError(
        error.response?.data?.message || "Could not get AI analysis. Please try again."
      )
    } finally {
      setAdvisorLoading(false)
    }
  }

  if (!item)
    return (
      <ReviewContainer>
        <Card style={{ textAlign: "center", padding: "48px" }}>
          <div style={{ fontSize: "18px", color: "#64748b" }}>Loading student details...</div>
        </Card>
      </ReviewContainer>
    )
   

  return (
    <ReviewContainer>
      <StudentHeader>
        <h1>{item.full_name}</h1>
        <div className="admission">{item.admission_number}</div>
        <div className="department">{item.department_name}</div>
      </StudentHeader>

      <Card>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "24px" }}>
          <h2 style={{ margin: 0 }}>Review Clearance</h2>
          <StatusBadge status={status}>{status}</StatusBadge>
        </div>

        <FormGrid>
          <Field>
            <label>Clearance Status</label>
            <select value={status} onChange={(e) => setStatus(e.target.value)}>
              <option value="pending">Pending Review</option>
              <option value="cleared">Cleared</option>
              <option value="rejected">Rejected</option>
            </select>
          </Field>

          <Field>
            <label>Remarks & Comments</label>
            <textarea
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              placeholder="Add any comments or reasons for your decision..."
            />
          </Field>

          <CheckboxField>
            <label>
              <input type="checkbox" checked={has_dues} onChange={(e) => setHasDues(e.target.checked)} />
              Student has outstanding dues
            </label>
          </CheckboxField>

          {has_dues && (
            <Field>
              <label>Outstanding Dues Amount (KSh)</label>
              <input
                type="number"
                value={dues_amount}
                onChange={(e) => setDuesAmount(Number(e.target.value))}
                placeholder="0.00"
                min="0"
                step="0.01"
              />
            </Field>
          )}
        </FormGrid>

        {records.length > 0 && (
          <RecordsSection>
            <h3>⚠️ Outstanding Department Records ({records.length})</h3>
            <table style={{ width: "100%", borderCollapse: "collapse", fontSize: "0.9rem" }}>
              <thead>
                <tr style={{ background: "#fef3c7" }}>
                  <th style={thStyle}>Description</th>
                  <th style={thStyle}>Amount (KES)</th>
                  <th style={thStyle}>Logged By</th>
                  <th style={thStyle}>Date</th>
                  <th style={thStyle}>Status</th>
                </tr>
              </thead>
              <tbody>
                {records.map(r => (
                  <tr key={r.id} style={{ borderBottom: "1px solid #e5e7eb" }}>
                    <td style={tdStyle}>{r.description}</td>
                    <td style={tdStyle}>{r.amount != null ? `KES ${Number(r.amount).toLocaleString()}` : "—"}</td>
                    <td style={tdStyle}>{r.loggedBy}</td>
                    <td style={tdStyle}>{new Date(r.createdAt).toLocaleDateString("en-KE", { day: "numeric", month: "short", year: "numeric" })}</td>
                    <td style={tdStyle}>
                      <span className="badge">{r.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr style={{ background: "#fef9f0", fontWeight: "700" }}>
                  <td style={tdStyle}>Total Outstanding</td>
                  <td style={tdStyle}>
                    KES {records
                      .filter(r => r.status === "unresolved" && r.amount != null)
                      .reduce((sum, r) => sum + r.amount, 0)
                      .toLocaleString()}
                  </td>
                  <td colSpan="3" style={tdStyle}></td>
                </tr>
              </tfoot>
            </table>
          </RecordsSection>
        )}

        {records.length === 0 && (
          <RecordsSection style={{ background: "#f0fdf4", borderColor: "#10b981" }}>
            <h3 style={{ color: "#065f46" }}>✅ No outstanding records for this student</h3>
          </RecordsSection>
        )}

        {/*AI Advisor Secion */}
        <AdvisorSection>
          <h3>🤖 AI Clearance Advisor</h3>

          {!advisorResult && !advisorLoading && !advisorError && (
            <div style={{ textAlign: "center", padding: "0.5rem 0" }}>
              <p style={{ color: "#6d28d9", marginBottom: "1rem", fontSize: "0.9rem" }}>
                Get an AI-generated summary of this student's clearance risk and what's blocking them.
              </p>
              <Button onClick={runAdvisor} style={{ backgroundColor: "#7c3aed", color: "white" }}>
                ✨ Analyze This Student
              </Button>
            </div>
          )}

          {advisorLoading && (
            <div style={{ textAlign: "center", padding: "1rem 0", color: "#6d28d9" }}>Analyzing…</div>
          )}

          {advisorError && (
            <div>
              <p style={{ color: "#991b1b", fontSize: "0.9rem" }}>{advisorError}</p>
              <Button onClick={runAdvisor} style={{ backgroundColor: "#7c3aed", color: "white" }}>
                Try Again
              </Button>
            </div>
          )}

          {advisorResult && !advisorLoading && (
            <>
              <div style={{ display: "flex", alignItems: "center", gap: "1rem", marginBottom: "1rem" }}>
                <RiskBadge level={advisorResult.risk_level}>{advisorResult.risk_level} risk</RiskBadge>
                <div style={{ fontSize: "0.85rem", color: "#4c1d95" }}>
                  Est. completion: <strong>{advisorResult.estimated_completion}</strong>
                </div>
              </div>

              <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
                {advisorResult.recommendations.map((rec, i) => (
                  <AdvisorRecommendation key={i}>
                    • {formatRecommendation(rec, [item.department_name, "Approved", "Rejected", "Pending"])}
                  </AdvisorRecommendation>
                ))}
              </ul>
              
              <div style={{ marginTop: "1rem", textAlign: "right" }}>
                <button
                  onClick={runAdvisor}
                  style={{ background: "none", border: "none", color: "#7c3aed", fontWeight: 600, cursor: "pointer", fontSize: "0.85rem" }}
                >
                  🔄 Re-analyze
                </button>
              </div>
            </>
          )}
        </AdvisorSection> 

        <ButtonGroup>
          <Button onClick={() => save("cleared")} size="lg" disabled={hasUnresolved}>
            ✓ Mark as Cleared
          </Button>
          <Button variant="danger" onClick={() => save("rejected")} size="lg">
            ✗ Reject Clearance
          </Button>
          <Button variant="secondary" onClick={() => save("pending")} size="lg">
            💾 Save as Pending
          </Button>
        </ButtonGroup>
      </Card>
    </ReviewContainer>
  )
}
