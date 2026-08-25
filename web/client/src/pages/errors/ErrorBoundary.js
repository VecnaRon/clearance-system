import React from "react"
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

const Icon = styled.div`
  font-size: 4rem;
  margin-bottom: 16px;
`

const Title = styled.h2`
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
`

const Message = styled.p`
  color: #64748b;
  font-size: 1rem;
  margin-bottom: 32px;
  max-width: 480px;
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

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error }
  }

  componentDidCatch(error, info) {
    console.error("ErrorBoundary caught:", error, info)
  }

  render() {
    if (this.state.hasError) {
      return (
        <Container>
          <Icon>⚠️</Icon>
          <Title>Something Went Wrong</Title>
          <Message>
            An unexpected error occurred. Please try refreshing the page. 
            If the problem persists, contact your system administrator.
          </Message>
          <Button onClick={() => {
            this.setState({ hasError: false, error: null })
            window.location.href = "/"
          }}>
            Go Back Home
          </Button>
        </Container>
      )
    }

    return this.props.children
  }
}