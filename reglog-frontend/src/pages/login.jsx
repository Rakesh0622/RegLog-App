import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import api from '../api/axios.js'

export default function Login() {
  const navigate = useNavigate()
  const location = useLocation()
  const successMessage = location.state?.message

  const [form, setForm] = useState({ name: '', password: '' })
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    setErrors((prev) => ({ ...prev, [name]: '' }))
    setFormError('')
  }

  const validate = () => {
    const next = {}
    if (!form.name.trim()) next.name = 'Username is required'
    if (!form.password) next.password = 'Password is required'
    return next
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const next = validate()
    if (Object.keys(next).length > 0) {
      setErrors(next)
      return
    }

    setLoading(true)
    setFormError('')
    try {
      await api.post('/api/auth/login', {
        name: form.name.trim(),
        password: form.password,
      })
      navigate('/home')
    } catch (err) {
      if (err.response?.status === 401) {
        setFormError('Invalid username or password')
      } else {
        setFormError(err.response?.data?.message || 'Something went wrong. Please try again.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">RegLog</h1>
        <p className="auth-subtitle">Welcome back</p>

        {successMessage && <div className="alert alert-success">{successMessage}</div>}
        {formError && <div className="alert alert-error">{formError}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="name">Username</label>
            <input
              id="name"
              name="name"
              type="text"
              value={form.name}
              onChange={handleChange}
              placeholder="Enter username"
              className={errors.name ? 'input-error' : ''}
            />
            {errors.name && <span className="field-error">{errors.name}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              placeholder="Enter password"
              className={errors.password ? 'input-error' : ''}
            />
            {errors.password && <span className="field-error">{errors.password}</span>}
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <p className="auth-link">
          Don&apos;t have an account? <Link to="/signup">Signup</Link>
        </p>
      </div>
    </div>
  )
}