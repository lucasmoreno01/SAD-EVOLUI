import { api } from './api'
import type { Improvement, ImprovementStatus } from '../types/domain'
export type ImprovementInput = { applicationId: string; issueGroupId: string | null; title: string; description: string; impact: number; urgency: number; frequency: number; effort: number; assessmentOrigin: string; status: ImprovementStatus }
export async function listImprovements() { return (await api.get<Improvement[]>('/improvements')).data }
export async function createImprovement(input: ImprovementInput) { return (await api.post<Improvement>('/improvements', input)).data }
export async function updateImprovement(id: string, input: ImprovementInput) { return (await api.put<Improvement>(`/improvements/${id}`, input)).data }
export async function deleteImprovement(id: string) { await api.delete(`/improvements/${id}`) }
