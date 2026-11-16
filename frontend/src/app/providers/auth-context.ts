import { createContext } from 'react'
import type { AuthResponse } from '../../types/auth'

export type AuthContextValue = { user: Omit<AuthResponse, 'token'> | null; accept: (response: AuthResponse) => void; logout: () => void }
export const AuthContext = createContext<AuthContextValue | null>(null)
