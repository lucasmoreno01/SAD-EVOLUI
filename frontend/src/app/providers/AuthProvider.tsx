import { useEffect, useMemo, useState, type PropsWithChildren } from 'react'
import type { AuthResponse } from '../../types/auth'
import { AuthContext, type AuthContextValue } from './auth-context'

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<Omit<AuthResponse, 'token'> | null>(() => {
    if (!sessionStorage.getItem('sad-evolui-token')) return null
    const stored = sessionStorage.getItem('sad-evolui-user')
    return stored ? JSON.parse(stored) as Omit<AuthResponse, 'token'> : null
  })
  const value = useMemo<AuthContextValue>(() => ({
    user,
    accept: (response) => {
      sessionStorage.setItem('sad-evolui-token', response.token)
      const nextUser = { userId: response.userId, name: response.name, email: response.email }
      sessionStorage.setItem('sad-evolui-user', JSON.stringify(nextUser))
      setUser(nextUser)
    },
    logout: () => {
      sessionStorage.removeItem('sad-evolui-token')
      sessionStorage.removeItem('sad-evolui-user')
      setUser(null)
    },
  }), [user])
  useEffect(() => {
    const handleUnauthorized = () => value.logout()
    window.addEventListener('sad-evolui-unauthorized', handleUnauthorized)
    return () => window.removeEventListener('sad-evolui-unauthorized', handleUnauthorized)
  }, [value])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
