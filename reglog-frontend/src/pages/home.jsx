import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/axios.js'

export default function Home() {
  const navigate = useNavigate()
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    api
      .get('/api/auth/me')
      .then((res) => {
        if (active) setUser(res.data?.data || null)
      })
      .catch((err) => {
        if (active) {
          if (err.response?.status === 401) {
            navigate('/login', { replace: true })
          } else {
            setError(err.response?.data?.message || 'Failed to load profile.')
          }
        }
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [navigate])

  const handleLogout = async () => {
    try {
      await api.post('/api/auth/logout')
    } catch {
      // ignore: always redirect
    } finally {
      navigate('/login', { replace: true })
    }
  }

  if (loading) {
    return (
      <div className="home-page">
        <div className="home-card">
          <p className="home-loading">Loading...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="home-page">
        <div className="home-card">
          <div className="alert alert-error">{error}</div>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/login')}>
            Back to Login
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="home-page">
      <div className="home-card">
        <h1 className="home-title">Welcome, {user?.name || 'User'}</h1>
        <button type="button" className="btn btn-danger" onClick={handleLogout}>
          Logout
        </button>
      </div>
    </div>
  )
}