import { Routes, Route } from "react-router-dom"
import { useEffect, useCallback } from "react"        
import { useNavigate } from "react-router-dom"        
import { getToken, clearAll } from "./services/storage" 
import Navbar from "./components/Navbar"
import Page from "./components/Page"
import Home from "./pages/Home"
import Login from "./pages/auth/Login"
import RegisterStudent from "./pages/auth/RegisterStudent"
import StartClearance from "./pages/student/StartClearance"
import ClearanceForm from "./pages/student/ClearanceForm"
import ClearanceStatus from "./pages/student/ClearanceStatus"
import Downloads from "./pages/student/Downloads"
import DeptLogin from "./pages/department/DeptLogin"
import DeptDashboard from "./pages/department/DeptDashboard"
import DeptStudentReview from "./pages/department/DeptStudentReview"
import AdminLogin from "./pages/admin/AdminLogin"
import AdminDashboard from "./pages/admin/AdminDashboard"
import ManageDepartments from "./pages/admin/ManageDepartments"
import ManageUsers from "./pages/admin/ManageUsers"
import Reports from "./pages/admin/Reports"
import StaffDashboard from "./pages/staff/StaffDashboard"
import ProtectedRoute from "./routes/ProtectedRoute"
import NotFound from "./pages/errors/NotFound"
import ErrorBoundary from "./pages/errors/ErrorBoundary"
import Help from "./pages/help/Help"

// 30 minutes in milliseconds
const INACTIVITY_LIMIT = 30 * 60 * 1000

export default function App() {
  const navigate = useNavigate()

  const logout = useCallback(() => {
    clearAll()
    navigate("/login")
  }, [navigate])

  useEffect(() => {
    let timer

    const resetTimer = () => {
      // Only start the timer if someone is actually logged in
      if (!getToken()) return

      clearTimeout(timer)
      timer = setTimeout(() => {
        alert("You have been logged out due to inactivity.")
        logout()
      }, INACTIVITY_LIMIT)
    }

    // Listen for any user activity
    const events = ["mousemove", "keydown", "click", "scroll", "touchstart"]
    events.forEach((event) => window.addEventListener(event, resetTimer))

    // Start the timer on mount
    resetTimer()

    // Cleanup on unmount
    return () => {
      clearTimeout(timer)
      events.forEach((event) => window.removeEventListener(event, resetTimer))
    }
  }, [logout])

  return (
    <ErrorBoundary>
      <Navbar />
      <Page>
        <Routes>
          <Route path="/" element={<Home />} />

          {/* Auth Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<RegisterStudent />} />

          {/* Student */}
          <Route element={<ProtectedRoute roles={["student", "admin"]} />}>
            <Route path="/student/start" element={<StartClearance />} />
            <Route path="/student/form" element={<ClearanceForm />} />
            <Route path="/student/status" element={<ClearanceStatus />} />
            <Route path="/student/downloads" element={<Downloads />} />
          </Route>

          {/* Department */}
          <Route path="/department/login" element={<DeptLogin />} />
          <Route element={<ProtectedRoute roles={["hod", "admin"]} />}>
            <Route path="/department/dashboard" element={<DeptDashboard />} />
            <Route path="/department/student/:id" element={<DeptStudentReview />} />
          </Route>

          {/* Staff */}                                               {/* ← new */}
          <Route element={<ProtectedRoute roles={["staff", "admin"]} />}>
            <Route path="/staff/dashboard" element={<StaffDashboard />} />
          </Route>

          {/* Admin */}
          <Route path="/admin/login" element={<AdminLogin />} />
          <Route element={<ProtectedRoute roles={["admin"]} />}>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
            <Route path="/admin/departments" element={<ManageDepartments />} />
            <Route path="/admin/users" element={<ManageUsers />} />
            <Route path="/admin/reports" element={<Reports />} />
          </Route>

          <Route path="/help" element={<Help />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Page>
    </ErrorBoundary>
  )
}