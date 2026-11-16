import { api } from './api'
import type { ApplicationInput, ManagedApplication } from '../types/domain'

export async function listApplications(projectId?: string) {
  return (await api.get<ManagedApplication[]>('/applications', { params: projectId ? { projectId } : undefined })).data
}

export async function createApplication(input: ApplicationInput) {
  return (await api.post<ManagedApplication>('/applications', input)).data
}

export async function updateApplication(id: string, input: ApplicationInput) {
  return (await api.put<ManagedApplication>(`/applications/${id}`, input)).data
}

export async function deleteApplication(id: string) {
  await api.delete(`/applications/${id}`)
}
