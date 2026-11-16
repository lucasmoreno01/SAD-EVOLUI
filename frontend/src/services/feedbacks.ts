import { api } from './api'
import type { Feedback, FeedbackInput } from '../types/domain'

export async function listFeedbacks(applicationId?: string) {
  return (await api.get<Feedback[]>('/feedbacks', { params: applicationId ? { applicationId } : undefined })).data
}

export async function createFeedback(input: FeedbackInput) {
  return (await api.post<Feedback>('/feedbacks', input)).data
}

export async function updateFeedback(id: string, input: FeedbackInput) {
  return (await api.put<Feedback>(`/feedbacks/${id}`, input)).data
}

export async function deleteFeedback(id: string) {
  await api.delete(`/feedbacks/${id}`)
}
