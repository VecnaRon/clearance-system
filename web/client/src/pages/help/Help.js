import styled from "styled-components"
import { getRole } from "../../services/storage"

const Container = styled.div`
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
`

const Header = styled.div`
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  color: white;
  padding: 40px;
  border-radius: 16px;
  margin-bottom: 32px;
  text-align: center;

  h1 {
    font-size: 2rem;
    font-weight: 800;
    margin: 0 0 8px 0;
    color: white;
  }

  p {
    opacity: 0.85;
    margin: 0;
    font-size: 1rem;
  }
`

const Section = styled.div`
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
`

const SectionTitle = styled.h2`
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 24px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #f1f5f9;
  display: flex;
  align-items: center;
  gap: 8px;
`

const Step = styled.div`
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  align-items: flex-start;

  &:last-child {
    margin-bottom: 0;
  }
`

const StepNumber = styled.div`
  width: 32px;
  height: 32px;
  min-width: 32px;
  background: linear-gradient(135deg, #1e293b 0%, #3b82f6 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.875rem;
`

const StepContent = styled.div`
  h4 {
    margin: 0 0 4px 0;
    color: #1e293b;
    font-size: 0.95rem;
    font-weight: 700;
  }

  p {
    margin: 0;
    color: #64748b;
    font-size: 0.9rem;
    line-height: 1.6;
  }
`

const NoRole = styled.div`
  text-align: center;
  padding: 48px;
  color: #64748b;
  background: white;
  border-radius: 16px;
  border: 1px solid #e5e7eb;

  h3 {
    color: #1e293b;
    margin-bottom: 8px;
  }
`

// ─── Content per role ────────────────────────────────────────────────────────

const content = {
  student: {
    icon: "🎓",
    title: "Student Guide",
    steps: [
      {
        title: "Register Your Account",
        body: "Go to the Register page and fill in your details including your admission number. Your account will be inactive until the admin approves it."
      },
      {
        title: "Wait for Account Activation",
        body: "Once registered, wait for the administrator to activate your account. You will not be able to log in until this is done."
      },
      {
        title: "Log In",
        body: "Once activated, log in using your email and password on the Login page."
      },
      {
        title: "Start Your Clearance",
        body: "Navigate to Start Clearance and submit your clearance request. You can only submit once."
      },
      {
        title: "Track Your Progress",
        body: "Visit the Clearance Status page to see which departments have cleared you and which are still pending."
      },
      {
        title: "Resolve Outstanding Items",
        body: "If a department has flagged outstanding items against you such as unreturned books or unpaid fees, resolve them directly with the relevant department staff."
      },
      {
        title: "Download Your Certificate",
        body: "Once all departments have cleared you and the admin gives final approval, go to the Downloads page to download your clearance certificate."
      }
    ]
  },

  staff: {
    icon: "👨‍💼",
    title: "Staff Guide",
    steps: [
      {
        title: "Log In",
        body: "Use your email and password on the Login page. You will be directed to the Staff Dashboard automatically."
      },
      {
        title: "Find a Student",
        body: "On your dashboard you will see a full list of all registered students. Use the search bar to filter by name or admission number."
      },
      {
        title: "Log an Outstanding Item",
        body: "Click on a student to open their panel. Enter a description of the outstanding item such as an unreturned book or unpaid fee, add an amount if applicable, then click Log Record."
      },
      {
        title: "View Existing Records",
        body: "When you click a student you will see all records previously logged against them in your department, including their current status."
      },
      {
        title: "Mark Items as Resolved",
        body: "Once a student has settled an outstanding item, find the record and click Mark Resolved. This updates the record and notifies the HOD during their review."
      },
      {
        title: "View Your Logged Records",
        body: "The My Logged Records section at the bottom of your dashboard shows all records you have ever logged with filters for resolved and unresolved."
      }
    ]
  },

  hod: {
    icon: "🏛️",
    title: "Head of Department Guide",
    steps: [
      {
        title: "Log In",
        body: "Use your email and password on the Login page. You will be directed to the Department Dashboard automatically."
      },
      {
        title: "View the Student Queue",
        body: "Your dashboard shows all students who have submitted clearance requests for your department along with their current status."
      },
      {
        title: "Review a Student",
        body: "Click Review Student next to any student to open their full clearance review page."
      },
      {
        title: "Check Outstanding Records",
        body: "On the review page you will see all outstanding items logged by your department staff against that student. Review these carefully before making a decision."
      },
      {
        title: "Approve or Reject",
        body: "If all items are resolved, mark the student as Cleared. If there are unresolved items, mark as Rejected and add a remark explaining why. You can also save as Pending if you need more time."
      },
      {
        title: "Change a Decision",
        body: "You can change your decision at any time by reopening the student review. If you reject after previously approving, the overall clearance status will automatically revert."
      }
    ]
  },

  admin: {
    icon: "⚙️",
    title: "Administrator Guide",
    steps: [
      {
        title: "Log In",
        body: "Use your admin email and password on the Login page. You will be directed to the Admin Dashboard."
      },
      {
        title: "Activate Student Accounts",
        body: "Newly registered students appear under Pending Activations on your dashboard. Review and activate legitimate accounts before they can log in."
      },
      {
        title: "Manage Departments",
        body: "Go to Manage Departments to add, edit, or deactivate departments in the system."
      },
      {
        title: "Manage Users",
        body: "Go to Manage Users to create HOD and staff accounts, assign them to departments, or deactivate any user account."
      },
      {
        title: "Give Final Approval",
        body: "When all departments have cleared a student the clearance appears as Awaiting Final Approval on your dashboard. Review and give final approval to allow the student to download their certificate."
      },
      {
        title: "View Reports",
        body: "The Reports page shows a department-wise summary of clearance progress across all students."
      },
      {
        title: "View Audit Logs",
        body: "The audit log on your dashboard records every significant action in the system including logins, approvals, rejections, and account changes."
      }
    ]
  }
}

// ─── Component ───────────────────────────────────────────────────────────────

export default function Help() {
  const role = getRole()
  const guide = content[role]

  return (
    <Container>
      <Header>
        <h1>📖 Help & Support</h1>
        <p>Step by step guide for using the School Clearance System</p>
      </Header>

      {!guide ? (
        <NoRole>
          <h3>Not Logged In</h3>
          <p>Please log in to see your personalised help guide.</p>
        </NoRole>
      ) : (
        <Section>
          <SectionTitle>
            {guide.icon} {guide.title}
          </SectionTitle>
          {guide.steps.map((step, index) => (
            <Step key={index}>
              <StepNumber>{index + 1}</StepNumber>
              <StepContent>
                <h4>{step.title}</h4>
                <p>{step.body}</p>
              </StepContent>
            </Step>
          ))}
        </Section>
      )}
    </Container>
  )
}