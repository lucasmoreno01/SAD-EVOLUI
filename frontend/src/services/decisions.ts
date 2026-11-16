import { api } from './api'
import type { DecisionRun, DecisionRunSummary } from '../types/domain'
export type DecisionInput = { name: string; impactWeight: number; urgencyWeight: number; frequencyWeight: number; effortWeight: number; improvementIds: string[] }
export async function listDecisions() { return (await api.get<DecisionRunSummary[]>('/decisions')).data }
export async function getDecision(id: string) { return (await api.get<DecisionRun>(`/decisions/${id}`)).data }
export async function calculateDecision(input: DecisionInput) { return (await api.post<DecisionRun>('/decisions', input)).data }
