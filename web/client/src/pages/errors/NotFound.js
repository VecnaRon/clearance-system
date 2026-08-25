import { useNavigate } from "react-router-dom"
import styled from "styled-components"

const Container = styled.div`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 24px;
  background: #f8fafc;
`

const Code = styled.h1`
  font-size: 6rem;
  font-weight: 900;
  background: linear-gradient(135deg, #1e293b 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
`

const Title = styled.h2`
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 16px 0 8px 0;
`

const Message = styled.p`
  color: #64748b;
  font-size: 1rem;
  margin-bottom: 32px;
`

const Button = styled.button`
  padding: 12px 32px;
  background: linear-gradient(135deg, #1e293b 0%, #3b82f6 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 0.9;
  }
`

export default function NotFound() {
  const navigate = useNavigate()

  return (
    <Container>
      <Code>404</Code>
      <Title>Page Not Found</Title>
      <Message>
        The page you are looking for does not exist or has been moved.
      </Message>
      <Button onClick={() => navigate("/")}>Go Back Home</Button>
    </Container>
  )
}