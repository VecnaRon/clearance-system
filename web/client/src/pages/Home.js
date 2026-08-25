import { useState } from "react"
import styled from "styled-components"
import { Link, useNavigate } from "react-router-dom"
import Card from "../components/Card"
import Button from "../components/Button"
import { getUser } from "../services/storage"

const Hero = styled.section`
  position: relative;
  text-align: center;
  padding: ${({ theme }) => theme.spacing(12)} ${({ theme }) => theme.spacing(3)};
  background: linear-gradient(135deg, 
    ${({ theme }) => theme.colors.darkBg} 0%, 
    ${({ theme }) => theme.colors.gray[800]} 50%, 
    ${({ theme }) => theme.colors.primary} 100%);
  border-radius: ${({ theme }) => theme.radius};
  margin-bottom: ${({ theme }) => theme.spacing(6)};
  color: white;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.3);
    z-index: 1;
  }

  > * {
    position: relative;
    z-index: 2;
  }
`

const Title = styled.h1`
  font-size: 3.5rem;
  font-weight: 800;
  margin-bottom: ${({ theme }) => theme.spacing(3)};
  background: linear-gradient(135deg, #ffffff, #e2e8f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  
  @media (max-width: 768px) {
    font-size: 2.5rem;
  }
`

const Subtitle = styled.p`
  font-size: 1.375rem;
  margin-bottom: ${({ theme }) => theme.spacing(2)};
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
  opacity: 0.95;
  font-weight: 300;
`

const StatsSection = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: ${({ theme }) => theme.spacing(4)};
  margin: ${({ theme }) => theme.spacing(6)} 0;
  padding: ${({ theme }) => theme.spacing(4)};
  background: white;
  border-radius: ${({ theme }) => theme.radius};
  box-shadow: ${({ theme }) => theme.shadow.lg};
`

const StatCard = styled.div`
  text-align: center;
  padding: ${({ theme }) => theme.spacing(3)};
  
  .stat-number {
    font-size: 2.5rem;
    font-weight: 800;
    color: ${({ theme }) => theme.colors.primary};
    display: block;
  }
  
  .stat-label {
    color: ${({ theme }) => theme.colors.gray[600]};
    font-weight: 500;
    margin-top: ${({ theme }) => theme.spacing(1)};
  }
`

const Features = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: ${({ theme }) => theme.spacing(4)};
  margin-top: ${({ theme }) => theme.spacing(6)};
`

const FeatureCard = styled(Card)`
  text-align: left;
  padding: ${({ theme }) => theme.spacing(6)};
  border: 1px solid ${({ theme }) => theme.colors.border};
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: ${({ theme }) => theme.colors.primary};
    transform: scaleY(0);
    transition: transform 0.3s ease;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: ${({ theme }) => theme.shadow.xl};
    
    &::before {
      transform: scaleY(1);
    }
  }
  
  .feature-icon {
    font-size: 3rem;
    margin-bottom: ${({ theme }) => theme.spacing(3)};
    display: block;
  }
  
  h3 {
    color: ${({ theme }) => theme.colors.text};
    margin-bottom: ${({ theme }) => theme.spacing(3)};
    font-size: 1.5rem;
    font-weight: 700;
  }
  
  p {
    color: ${({ theme }) => theme.colors.gray[600]};
    line-height: 1.7;
    font-size: 1.1rem;
  }
`

const ClickableFeatureCard = styled(FeatureCard)`
  cursor: pointer;

  &:hover {
    border-color: ${({ theme }) => theme.colors.primary};
  }

  .cta {
    display: inline-block;
    margin-top: 16px;
    font-size: 0.875rem;
    font-weight: 700;
    color: ${({ theme }) => theme.colors.primary};
  }
`

const ButtonGroup = styled.div`
  display: flex;
  gap: ${({ theme }) => theme.spacing(3)};
  justify-content: center;
  flex-wrap: wrap;
  margin-top: ${({ theme }) => theme.spacing(6)};
`

const ModernButton = styled(Button)`
  padding: ${({ theme }) => theme.spacing(3)} ${({ theme }) => theme.spacing(6)};
  font-size: 1.125rem;
  font-weight: 600;
  border-radius: ${({ theme }) => theme.radius};
  transition: all 0.3s ease;
  box-shadow: ${({ theme }) => theme.shadow.md};
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: ${({ theme }) => theme.shadow.lg};
  }
`

const FAQSection = styled.div`
  margin-top: ${({ theme }) => theme.spacing(6)};
  background: white;
  border-radius: ${({ theme }) => theme.radius};
  padding: ${({ theme }) => theme.spacing(6)};
  box-shadow: ${({ theme }) => theme.shadow.lg};
`
//FAQ
const FAQItem = styled.div`
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};

  &:last-child {
    border-bottom: none;
  }
`

const FAQQuestion = styled.button`
  width: 100%;
  background: none;
  border: none;
  padding: ${({ theme }) => theme.spacing(4)} 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  text-align: left;

  h4 {
    font-size: 1rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0;
  }

  .chevron {
    font-size: 0.75rem;
    color: #64748b;
    transition: transform 0.2s ease;
    transform: ${({ open }) => (open ? "rotate(180deg)" : "rotate(0deg)")};
  }
`

const FAQAnswer = styled.div`
  max-height: ${({ open }) => (open ? "200px" : "0")};
  overflow: hidden;
  transition: max-height 0.3s ease;

  p {
    font-size: 0.95rem;
    color: #64748b;
    margin: 0 0 16px 0;
    line-height: 1.6;
  }
`

//Contact section
const ContactSection = styled.div`
  margin-top: ${({ theme }) => theme.spacing(6)};
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  border-radius: ${({ theme }) => theme.radius};
  padding: ${({ theme }) => theme.spacing(8)};
  color: white;

  h2 {
    font-size: 1.75rem;
    font-weight: 800;
    margin: 0 0 24px 0;
    color: white;
    text-align: center;
  }
`

const ContactGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: ${({ theme }) => theme.spacing(4)};
  margin-top: ${({ theme }) => theme.spacing(4)};
`

const ContactItem = styled.div`
  text-align: center;

  .icon {
    font-size: 1.75rem;
    margin-bottom: 8px;
  }

  .label {
    font-size: 0.8rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    opacity: 0.7;
    margin-bottom: 4px;
  }

  .value {
    font-size: 0.95rem;
    font-weight: 600;
    opacity: 0.95;
  }
`

const AboutSection = styled.div`
  margin-top: ${({ theme }) => theme.spacing(6)};
  background: white;
  border-radius: ${({ theme }) => theme.radius};
  padding: ${({ theme }) => theme.spacing(6)};
  box-shadow: ${({ theme }) => theme.shadow.lg};
  text-align: center;

  h2 {
    font-size: 1.75rem;
    font-weight: 800;
    color: #1e293b;
    margin: 0 0 16px 0;
  }

  p {
    font-size: 1.125rem;
    color: #64748b;
    line-height: 1.8;
    max-width: 700px;
    margin: 0 auto;
  }
`



export default function Home() {
  const user = getUser()
  const navigate = useNavigate()
  const [openFAQ, setOpenFAQ] = useState(null)

  const faqs = [
    {
      q: "Why can I not log in after registering?",
      a: "Your account needs to be activated by the administrator before you can log in. Please wait for activation or contact your school office."
    },
    {
      q: "Why is my clearance rejected?",
      a: "A department has found outstanding items against your account such as unreturned books or unpaid fees. Visit the relevant department to resolve the issue then ask the staff member to mark it as resolved."
    },
    {
      q: "How long does the clearance process take?",
      a: "This depends on how quickly each department reviews your request and how fast you resolve any outstanding items. Once all departments clear you the admin gives final approval."
    },
    {
      q: "Can I resubmit my clearance request?",
      a: "No. You can only submit one clearance request. If there are issues the relevant department will update your status directly."
    },
    {
      q: "Where do I download my clearance certificate?",
      a: "Once your clearance is fully approved go to the Downloads page from your navigation bar to download your certificate as a PDF."
    }
  ]

  return (
    <div>
      <Hero>
        <Title>Digital School Clearance System</Title>
        <Subtitle>Revolutionizing the clearance process for Kenyan high schools with modern technology</Subtitle>
        <p style={{ fontSize: "1.1rem", opacity: 0.9, marginBottom: "2rem" }}>
          Streamline operations, reduce paperwork, and track progress in real-time
        </p>
        <ButtonGroup>
          {!user ? (
            <>
              <Link to="/register">
                <ModernButton size="lg">Get Started Today</ModernButton>
              </Link>
              <Link to="/login">
                <ModernButton variant="secondary" size="lg">
                  Sign In
                </ModernButton>
              </Link>
            </>
          ) : (
            <>
              {user.role === "student" && (
                <Link to="/student/start">
                  <ModernButton size="lg">Start Your Clearance</ModernButton>
                </Link>
              )}
              {user.role === "hod" && (
                <Link to="/department/dashboard">
                  <ModernButton size="lg">Department Dashboard</ModernButton>
                </Link>
              )}
              {user.role === "staff" && (
                <Link to="/staff/dashboard">
                  <ModernButton size="lg">Staff Dashboard</ModernButton>
                </Link>
              )}
              {user.role === "admin" && (
                <Link to="/admin/dashboard">
                  <ModernButton size="lg">Admin Dashboard</ModernButton>
                </Link>
              )}
            </>
          )}
        </ButtonGroup>
      </Hero>

      <StatsSection>
        <StatCard>
          <span className="stat-number">98%</span>
          <span className="stat-label">Success Rate</span>
        </StatCard>
        <StatCard>
          <span className="stat-number">75%</span>
          <span className="stat-label">Time Saved</span>
        </StatCard>
        <StatCard>
          <span className="stat-number">24/7</span>
          <span className="stat-label">Available</span>
        </StatCard>
        <StatCard>
          <span className="stat-number">100+</span>
          <span className="stat-label">Schools Served</span>
        </StatCard>
      </StatsSection>

      <Features>
        {/* Student card — clickable */}
        <ClickableFeatureCard onClick={() => navigate("/register")}>
          <span className="feature-icon">🎓</span>
          <h3>For Students</h3>
          <p>
            Submit clearance requests online and track your progress through each department. No more running around
            campus or waiting in long queues. Get real-time updates and notifications on your clearance status.
          </p>
          <span className="cta">Get Started →</span>
        </ClickableFeatureCard>

        {/* Department card — not clickable */}
        <FeatureCard>
          <span className="feature-icon">🏢</span>
          <h3>For Departments</h3>
          <p>
            Efficiently review student clearances with our intuitive dashboard. Manage outstanding dues, add detailed
            remarks, and approve or reject requests with comprehensive tracking and reporting capabilities.
          </p>
        </FeatureCard>

        {/* Admin card — not clickable */}
        <FeatureCard>
          <span className="feature-icon">⚙️</span>
          <h3>For Administrators</h3>
          <p>
            Complete oversight of the entire clearance ecosystem. Manage departments and users, generate comprehensive
            reports, and maintain system integrity with advanced administrative tools and analytics.
          </p>
        </FeatureCard>
      </Features>

      {/* About Section */}
      <AboutSection>
        <h2>📋 About This System</h2>
        <p>
          The Digital School Clearance System is a modern web-based platform designed to streamline
          the clearance process for Kenyan high schools. It replaces
          manual paper-based workflows with a fast, transparent, and accountable digital process —
          connecting students, department staff, heads of department, and school administrators
          on a single platform.
        </p>
      </AboutSection>

      {/* FAQ Section */}
      <FAQSection>
        <h2 style={{ fontSize: "1.75rem", fontWeight: "800", color: "#1e293b", marginBottom: "24px" }}>
          ❓ Frequently Asked Questions
        </h2>
        {faqs.map((faq, index) => (
          <FAQItem key={index}>
            <FAQQuestion
              open={openFAQ === index}
              onClick={() => setOpenFAQ(openFAQ === index ? null : index)}
            >
              <h4>{faq.q}</h4>
              <span className="chevron">▼</span>
            </FAQQuestion>
            <FAQAnswer open={openFAQ === index}>
              <p>{faq.a}</p>
            </FAQAnswer>
          </FAQItem>
        ))}
      </FAQSection>

      {/* Contact Section */}
      <ContactSection>
        <h2>📬 Contact Us</h2>
        <ContactGrid>
          <ContactItem>
            <div className="icon">🏫</div>
            <div className="label">School</div>
            <div className="value">Mambotela High School</div>
          </ContactItem>
          <ContactItem>
            <div className="icon">📞</div>
            <div className="label">Phone</div>
            <div className="value">+254 700 000 000</div>
          </ContactItem>
          <ContactItem>
            <div className="icon">📧</div>
            <div className="label">Email</div>
            <div className="value">admin@clearance.com</div>
          </ContactItem>
          <ContactItem>
            <div className="icon">📍</div>
            <div className="label">Location</div>
            <div className="value">Nairobi, Kenya</div>
          </ContactItem>
        </ContactGrid>
      </ContactSection>
    </div>
  )
}