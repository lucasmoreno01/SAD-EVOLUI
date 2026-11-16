import { api } from './api'
import type { AuthResponse, LoginInput, RegisterInput } from '../types/auth'

export async function login(input: LoginInput) {
  return (await api.post<AuthResponse>('/auth/login', input)).data
}

export async function register(input: RegisterInput) {
  return (await api.post<AuthResponse>('/auth/register', input)).data
}
