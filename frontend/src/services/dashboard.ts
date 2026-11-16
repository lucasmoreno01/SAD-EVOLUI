import { api } from './api'
import type { DashboardSummary } from '../types/domain'

export async function getDashboardSummary(filters?: { projectId?: string; applicationId?: string; from?: string; to?: string }) {
  return (await api.get<DashboardSummary>('/dashboard/summary', { params: filters })).data
}
