import { api } from './api'
import type { AnalysisRun, AnalysisRunSummary, Sentiment } from '../types/domain'
export async function listAnalyses() { return (await api.get<AnalysisRunSummary[]>('/analyses')).data }
export async function getAnalysis(id: string) { return (await api.get<AnalysisRun>(`/analyses/${id}`)).data }
export async function runAnalysis(input: { projectId: string | null; applicationId: string | null }) { return (await api.post<AnalysisRun>('/analyses', input)).data }
export async function correctClassification(id: string, input: { sentiment: Sentiment; categoryId: string }) { return (await api.patch(`/analyses/classifications/${id}`, input)).data }
