"use client"

import { useEffect, useState } from "react"
import styled from "styled-components"
import Card from "../../components/Card"
import ProgressTracker from "../../components/ProgressTracker"
import StatusBadge from "../../components/StatusBadge"
import Button from "../../components/Button"
import api from "../../services/api"

const StatusContainer = styled.div`
  max-width: 900px;
  margin: 0 auto;
  display: grid;
  gap: ${({ theme }) => theme.spacing(4)};
`

const HeaderCard = styled(Card)`
  padding: ${({ theme }) => theme.spacing(6)};
  background: linear-gradient(135deg, ${({ theme }) => theme.colors.primary}, ${({ theme }) => theme.colors.accent});
  color: white;
  text-align: center;
  border: none;
  box-shadow: ${({ theme }) => theme.shadow.xl};
`

const StatusCard = styled(Card)`
  padding: ${({ theme }) => theme.spacing(5)};
  background: white;
  border: 1px solid ${({ theme }) => theme.colors.border};
  box-shadow: ${({ theme }) => theme.shadow.lg};
`

const InfoGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: ${({ theme }) => theme.spacing(3)};
  margin-bottom: ${({ theme }) => theme.spacing(4)};
`

const InfoItem = styled.div`
  .label {
    font-size: 0.875rem;
    font-weight: 600;
    color: ${({ theme }) => theme.colors.gray[600]};
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: ${({ theme }) => theme.spacing(1)};
  }

  .value {
    font-size: 1.125rem;
    font-weight: 700;
    color: ${({ theme }) => theme.colors.text};
  }
`

const AlertCard = styled.div`
  padding: ${({ theme }) => theme.spacing(4)};
  background: ${({ variant }) =>
    variant === "success"
      ? "#d4edda"
      : variant === "warning"
        ? "#fff3cd"
        : variant === "error"
          ? "#f8d7da"
          : "#e2e3e5"};
  border: 1px solid ${({ variant }) =>
    variant === "success"
      ? "#c3e6cb"
      : variant === "warning"
        ? "#ffeaa7"
        : variant === "error"
          ? "#f5c6cb"
          : "#d6d8db"};
  border-radius: ${({ theme }) => theme.radius};
  margin-bottom: ${({ theme }) => theme.spacing(3)};
  color: ${({ variant }) =>
    variant === "success"
      ? "#155724"
      : variant === "warning"
        ? "#856404"
        : variant === "error"
          ? "#721c24"
          : "#495057"};

  h4 {
    margin: 0 0 ${({ theme }) => theme.spacing(2)} 0;
    font-weight: 700;
    display: flex;
    align-items: center;
    gap: ${({ theme }) => theme.spacing(2)};
  }

  p {
    margin: 0;
    line-height: 1.6;
  }
`

const ModernButton = styled(Button)`
  padding: ${({ theme }) => theme.spacing(3)} ${({ theme }) => theme.spacing(6)};
  border-radius: ${({ theme }) => theme.radius};
  font-weight: 700;
  font-size: 1.125rem;
  transition: all 0.3s ease;
  box-shadow: ${({ theme }) => theme.shadow.md};

  &:hover {
    transform: translateY(-2px);
    box-shadow: ${({ theme }) => theme.shadow.lg};
  }
`

const DepartmentGrid = styled.div`
  display: grid;
  gap: ${({ theme }) => theme.spacing(3)};
  margin-top: ${({ theme }) => theme.spacing(4)};
`

const DepartmentCard = styled.div`
  padding: ${({ theme }) => theme.spacing(4)};
  border: 2px solid ${({ status, theme }) =>
    status === "approved" ? theme.colors.success : status === "rejected" ? theme.colors.danger : theme.colors.border};
  border-radius: ${({ theme }) => theme.radius};
  background: ${({ status, theme }) =>
    status === "approved" ? "#f0fdf4" : status === "rejected" ? "#fef2f2" : "#ffffff"};
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: ${({ theme }) => theme.shadow.md};
  }

  .department-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: ${({ theme }) => theme.spacing(2)};
  }

  .department-name {
    font-size: 1.125rem;
    font-weight: 700;
    color: ${({ theme }) => theme.colors.text};
  }

  .department-details {
    font-size: 0.875rem;
    color: ${({ theme }) => theme.colors.gray[600]};
    line-height: 1.5;
  }

  .dues-info {
    margin-top: ${({ theme }) => theme.spacing(2)};
    padding: ${({ theme }) => theme.spacing(2)};
    background: ${({ theme }) => theme.colors.gray[50]};
    border-radius: ${({ theme }) => theme.radius};
    font-weight: 600;
    color: ${({ theme }) => theme.colors.danger};
  }
`

const LoadingSpinner = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  padding: ${({ theme }) => theme.spacing(8)};
  
  &::after {
    content: '';
    width: 40px;
    height: 40px;
    border: 4px solid ${({ theme }) => theme.colors.gray[300]};
    border-top: 4px solid ${({ theme }) => theme.colors.primary};
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
`

// ─── Outstanding Items Styles ───────────────────────────────────────────────

const SectionTitle = styled.h3`
  margin-bottom: 1.5rem;
  color: #1e293b;
  font-size: 1.25rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.5rem;
`

const OutstandingSummaryBar = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: ${({ theme }) => theme.spacing(3)};
  background: ${({ allClear }) => (allClear ? "#f0fdf4" : "#fff7ed")};
  border: 1px solid ${({ allClear }) => (allClear ? "#bbf7d0" : "#fed7aa")};
  border-radius: ${({ theme }) => theme.radius};
  margin-bottom: ${({ theme }) => theme.spacing(4)};
  font-size: 0.95rem;
  font-weight: 600;
  color: ${({ allClear }) => (allClear ? "#166534" : "#9a3412")};
`

const OutstandingItemCard = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: ${({ theme }) => theme.spacing(3)} ${({ theme }) => theme.spacing(4)};
  border: 1px solid ${({ status }) => (status === "resolved" ? "#bbf7d0" : "#fecaca")};
  border-left: 4px solid ${({ status }) => (status === "resolved" ? "#22c55e" : "#ef4444")};
  border-radius: ${({ theme }) => theme.radius};
  background: ${({ status }) => (status === "resolved" ? "#f0fdf4" : "#fff5f5")};
  margin-bottom: ${({ theme }) => theme.spacing(2)};
  transition: all 0.2s ease;

  &:hover {
    transform: translateX(2px);
    box-shadow: ${({ theme }) => theme.shadow.sm};
  }

  .item-left {
    display: flex;
    flex-direction: column;
    gap: 0.35rem;
    flex: 1;
  }

  .item-description {
    font-size: 0.95rem;
    font-weight: 600;
    color: #1e293b;
  }

  .item-meta {
    font-size: 0.8rem;
    color: #64748b;
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
  }

  .item-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 0.35rem;
    min-width: 100px;
  }
`

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

const AllClearBox = styled.div`
  text-align: center;
  padding: ${({ theme }) => theme.spacing(6)};
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: ${({ theme }) => theme.radius};
  color: #166534;

  .icon {
    font-size: 2.5rem;
    margin-bottom: 0.75rem;
  }

  h4 {
    margin: 0 0 0.5rem 0;
    font-size: 1.125rem;
    font-weight: 700;
  }

  p {
    margin: 0;
    font-size: 0.9rem;
    opacity: 0.8;
  }
`

const AdvisorCard = styled(Card)`
  padding: ${({ theme }) => theme.spacing(5)};
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
  border: 1px solid #ddd6fe;
  box-shadow: ${({ theme }) => theme.shadow.lg};
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
  padding: ${({ theme }) => theme.spacing(2)} 0;
  border-bottom: 1px solid #e9d5ff;
  color: #4c1d95;
  line-height: 1.5;

  &:last-child {
    border-bottom: none;
  }
`

// ─── Helper ──────────────────────────────────────────────────────────────────

function getDepartmentName(departmentId, departments) {
  if (!departments) return `Department #${departmentId}`
  const match = departments.find(
    (d) => d.department_id === departmentId || 
           d.id === departmentId ||
           Number(d.department_id) === Number(departmentId)
  )
  return match ? match.department_name || match.name : `Department #${departmentId}`
}

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

// ─── Main Component ──────────────────────────────────────────────────────────

export default function ClearanceStatus() {
  const [clearance, setClearance] = useState(null)
  const [loading, setLoading] = useState(true)
  const [myRecords, setMyRecords] = useState([])
  const [itemsLoading, setItemsLoading] = useState(true)
  const [advisorResult, setAdvisorResult] = useState(null)
  const [advisorLoading, setAdvisorLoading] = useState(false)
  const [advisorError, setAdvisorError] = useState(null)
  
  useEffect(() => {
    ;(async () => {
      try {
        const { data } = await api.get("/clearance/my-latest")
        setClearance(data)
      } catch (error) {
        console.error("Error loading clearance:", error)
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  useEffect(() => {
    ;(async () => {
      try {
        const { data } = await api.get("/records/mine")
        setMyRecords(data || [])
      } catch (error) {
        console.error("Error loading outstanding items:", error)
        setMyRecords([])
      } finally {
        setItemsLoading(false)
      }
    })()
  }, [])

  const downloadCertificate = async () => {
    try {
      const response = await api.get(`/clearance/${clearance.id}/certificate`, {
        responseType: "blob",
      })

      const blob = new Blob([response.data], { type: "application/pdf" })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement("a")
      link.href = url
      link.download = `clearance_${clearance.admission_number}.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch (error) {
      console.error("Error downloading certificate:", error)
      alert("Error downloading certificate. Please try again.")
    }
  }

  if (loading) return <LoadingSpinner />

  if (!clearance || !clearance.id)
    return (
      <StatusContainer>
        <StatusCard>
          <div style={{ textAlign: "center", padding: "2rem" }}>
            <h3 style={{ color: "#64748b", marginBottom: "1rem" }}>No Clearance Request Found</h3>
            <p style={{ color: "#94a3b8" }}>You haven't submitted a clearance request yet.</p>
          </div>
        </StatusCard>
      </StatusContainer>
    )

  const clearedCount = clearance.departments?.filter((d) => d.status === "approved").length || 0
  const totalCount = clearance.departments?.length || 0
  const rejectedCount = clearance.departments?.filter((d) => d.status === "rejected").length || 0

  const unresolvedItems = myRecords.filter((i) => i.status === "unresolved")
  const resolvedItems = myRecords.filter((i) => i.status === "resolved")
  const allClear = myRecords.length === 0 || unresolvedItems.length === 0
  
  const runAdvisor = async () => {
    setAdvisorLoading(true)
    setAdvisorError(null)
    try {
      const { data } = await api.post(`/clearance/${clearance.id}/advisor`)
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

  return (
    <StatusContainer>
      <HeaderCard>
        <h1 style={{ margin: "0 0 1rem 0", fontSize: "2rem" }}>Clearance Status</h1>
        <p style={{ margin: 0, fontSize: "1.125rem", opacity: 0.9 }}>Track your clearance progress in real-time</p>
      </HeaderCard>

      <StatusCard>
        <InfoGrid>
          <InfoItem>
            <div className="label">Admission Number</div>
            <div className="value">{clearance.admission_number}</div>
          </InfoItem>
          <InfoItem>
            <div className="label">Current Status</div>
            <div className="value">
              <StatusBadge status={clearance.status} />
            </div>
          </InfoItem>
          <InfoItem>
            <div className="label">Submitted Date</div>
            <div className="value">{new Date(clearance.submitted_at).toLocaleDateString()}</div>
          </InfoItem>
          <InfoItem>
            <div className="label">Progress</div>
            <div className="value">
              {clearedCount}/{totalCount} Departments
            </div>
          </InfoItem>
        </InfoGrid>

        {clearance.status === "awaiting_final" && (
          <AlertCard variant="warning">
            <h4>🎓 Awaiting Final Approval</h4>
            <p>
              Congratulations! All departments have cleared you. Your clearance is now awaiting final approval from the
              Principal.
            </p>
          </AlertCard>
        )}

        {clearance.status === "approved" && (
          <AlertCard variant="success">
            <h4>✅ Clearance Completed!</h4>
            <p>Your clearance has been fully approved. You can now download your clearance certificate.</p>
            <div style={{ marginTop: "1rem" }}>
              <ModernButton
                onClick={downloadCertificate}
                style={{
                  backgroundColor: "#10b981",
                  color: "white",
                }}
              >
                📄 Download Certificate
              </ModernButton>
            </div>
          </AlertCard>
        )}

        <ProgressTracker cleared={clearedCount} total={totalCount} rejected={rejectedCount} />
      </StatusCard>

      {/*--AI advisor card---*/}
      <AdvisorCard>
        <SectionTitle>🤖 AI Clearance Advisor</SectionTitle>

        {!advisorResult && !advisorLoading && (
          <div style={{ textAlign: "center", padding: "1rem 0" }}>
            <p style={{ color: "#6d28d9", marginBottom: "1.5rem" }}>
              Get a personalized breakdown of what's holding up your clearance and what to do next.
            </p>
            <ModernButton
              onClick={runAdvisor}
              style={{ backgroundColor: "#7c3aed", color: "white" }}
            >
              ✨ Analyze My Clearance
            </ModernButton>
          </div>
        )}

        {advisorLoading && <LoadingSpinner />}

        {advisorError && (
          <AlertCard variant="error">
            <p>{advisorError}</p>
            <div style={{ marginTop: "1rem" }}>
              <ModernButton onClick={runAdvisor} style={{ backgroundColor: "#7c3aed", color: "white" }}>
                Try Again
              </ModernButton>
            </div>
          </AlertCard>
        )}

        {advisorResult && !advisorLoading && (
          <>
            <div style={{ display: "flex", alignItems: "center", gap: "1rem", marginBottom: "1.5rem" }}>
              <div>
                <div className="label" style={{ fontSize: "0.8rem", color: "#6d28d9", fontWeight: 600 }}>
                  RISK LEVEL
                </div>
                <RiskBadge level={advisorResult.risk_level}>{advisorResult.risk_level}</RiskBadge>
              </div>
              {advisorResult.priority_department && (
                <div>
                  <div className="label" style={{ fontSize: "0.8rem", color: "#6d28d9", fontWeight: 600 }}>
                    PRIORITY
                  </div>
                  <div style={{ fontWeight: 700, color: "#4c1d95" }}>
                    {advisorResult.priority_department}
                  </div>
                </div>
              )}
              <div>
                <div className="label" style={{ fontSize: "0.8rem", color: "#6d28d9", fontWeight: 600 }}>
                  EST. COMPLETION
                </div>
                <div style={{ fontWeight: 700, color: "#4c1d95" }}>
                  {advisorResult.estimated_completion}
                </div>
              </div>
            </div>

            <div style={{ fontWeight: 700, color: "#4c1d95", marginBottom: "0.5rem" }}>
              Recommendations
            </div>
            <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
              {advisorResult.recommendations.map((rec, i) => (
                <AdvisorRecommendation key={i}>
                  • {formatRecommendation(rec, [
                    ...(clearance.departments?.map((d) => d.department_name) || []),
                    "Approved", "Rejected", "Pending",
                  ])}
                </AdvisorRecommendation>
              ))}
            </ul>

            <div style={{ marginTop: "1.5rem", textAlign: "right" }}>
              <button
                onClick={runAdvisor}
                style={{
                  background: "none",
                  border: "none",
                  color: "#7c3aed",
                  fontWeight: 600,
                  cursor: "pointer",
                  fontSize: "0.875rem",
                }}
              >
                🔄 Re-analyze
              </button>
            </div>
          </>
        )}
      </AdvisorCard>
      
      {/* ── My Outstanding Items Section ── */}
      <StatusCard>
        <SectionTitle>⚠️ My Outstanding Items</SectionTitle>

        {itemsLoading ? (
          <LoadingSpinner />
        ) : myRecords.length === 0 ? (
          <AllClearBox>
            <div className="icon">✅</div>
            <h4>No Outstanding Items</h4>
            <p>No departments have flagged any items against your account.</p>
          </AllClearBox>
        ) : (
          <>
            <OutstandingSummaryBar allClear={allClear}>
              {allClear
                ? `✅ All ${myRecords.length} item(s) have been resolved.`
                : `⚠️ You have ${unresolvedItems.length} unresolved item(s) and ${resolvedItems.length} resolved item(s).`}
            </OutstandingSummaryBar>

            {myRecords.map((item) => (
              <OutstandingItemCard key={item.id} status={item.status}>
                <div className="item-left">
                  <div className="item-description">{item.description}</div>
                  <div className="item-meta">
                    <span>Dept: {getDepartmentName(item.departmentId, clearance.departments)}</span>
                    {item.amount != null && (
                      <span>Amount: KES {Number(item.amount).toLocaleString()}</span>
                    )}
                    <span>Logged: {new Date(item.createdAt).toLocaleDateString("en-KE", {
                      day: "numeric", month: "short", year: "numeric"
                    })}</span>
                    {item.resolvedAt && (
                      <span>Resolved: {new Date(item.resolvedAt).toLocaleDateString("en-KE", {
                        day: "numeric", month: "short", year: "numeric"
                      })}</span>
                    )}
                  </div>
                </div>
                <div className="item-right">
                  <StatusPill status={item.status}>{item.status}</StatusPill>
                </div>
              </OutstandingItemCard>
            ))}
          </>
        )}
      </StatusCard>

      {/* ── Department Status Details Section ── */}
      {clearance.departments && (
        <StatusCard>
          <h3 style={{ marginBottom: "1.5rem", color: "#1e293b" }}>Department Status Details</h3>
          <DepartmentGrid>
            {clearance.departments.map((dept) => (
              <DepartmentCard key={dept.id} status={dept.status}>
                <div className="department-header">
                  <span className="department-name">{dept.department_name}</span>
                  <StatusBadge status={dept.status} />
                </div>
                {dept.remarks && (
                  <div className="department-details">
                    <strong>Remarks:</strong> {dept.remarks}
                  </div>
                )}
                {dept.has_dues && (
                  <div className="dues-info">
                    💰 Outstanding Dues: KSh {dept.dues_amount?.toLocaleString()}
                  </div>
                )}
              </DepartmentCard>
            ))}
          </DepartmentGrid>
        </StatusCard>
      )}
    </StatusContainer>
  )
}