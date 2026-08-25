"use client"
import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"  // ← add this
import Field from "../../components/FormField"
import Button from "../../components/Button"
import Card from "../../components/Card"
import api from "../../services/api"

export default function ClearanceForm() {
  const [me, setMe] = useState(null)
  const [reason, setReason] = useState("kcse_completion")
  const [reason_other, setReasonOther] = useState("")
  const [message, setMessage] = useState("")
  const [hasActive, setHasActive] = useState(false)  // ← add this
  const navigate = useNavigate()                      // ← add this

  useEffect(() => {
    ;(async () => {
      try {
        const { data: meData } = await api.get("/student/me")
        setMe(meData)

        // Check if student already has an active clearance
        const { data: clearanceData } = await api.get("/clearance/my-latest")
        if (clearanceData && clearanceData.id) {
          setHasActive(true)
        }
      } catch (error) {
        // No clearance found yet — that's fine, show the form
      }
    })()
  }, [])

  const submit = async (e) => {
    e.preventDefault()
    try {
      const { data } = await api.post("/clearance/start", { reason, reason_other })
      setMessage(`Clearance started. Ref: #${data.id}`)
      // Redirect to status page after 2 seconds
      setTimeout(() => navigate("/student/status"), 2000)
    } catch (err) {
      setMessage(err?.response?.data?.message || "Failed to start clearance")
    }
  }

  if (!me) return <div>Loading...</div>

  // If student already has an active clearance, show a message instead of the form
  if (hasActive) {
    return (
      <Card style={{ textAlign: "center", padding: "48px" }}>
        <div style={{ fontSize: "3rem", marginBottom: "16px" }}>📋</div>
        <h2 style={{ color: "#1e293b", marginBottom: "16px" }}>
          You Already Have an Active Clearance
        </h2>
        <p style={{ color: "#64748b", marginBottom: "24px" }}>
          You have already submitted a clearance request. 
          Track your progress on the status page.
        </p>
        <Button onClick={() => navigate("/student/status")} size="lg">
          View Clearance Status →
        </Button>
      </Card>
    )
  }

  return (
    <Card>
      <h2>Student Clearance Request Form</h2>
      <p>
        <b>Name:</b> {me.full_name} &nbsp; | &nbsp;
        <b>Admission:</b> {me.admission_number}
      </p>
      <form onSubmit={submit}>
        <Field>
          <label>Reason for Clearance</label>
          <select value={reason} onChange={(e) => setReason(e.target.value)}>
            <option value="kcse_completion">KCSE Completion</option>
            <option value="transfer">Transfer</option>
            <option value="withdrawal">Withdrawal</option>
            <option value="other">Other</option>
          </select>
        </Field>
        {reason === "other" && (
          <Field>
            <label>Other (specify)</label>
            <input
              value={reason_other}
              onChange={(e) => setReasonOther(e.target.value)}
            />
          </Field>
        )}
        <Button type="submit">Submit</Button>
      </form>
      {message && (
        <p style={{
          color: message.startsWith("Clearance started") ? "green" : "red",
          marginTop: "16px"
        }}>
          {message}
        </p>
      )}
    </Card>
  )
}
