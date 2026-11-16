import { api } from './api'
import type { Project, ProjectInput } from '../types/domain'

export async function listProjects() {
  return (await api.get<Project[]>('/projects')).data
}

export async function createProject(input: ProjectInput) {
  return (await api.post<Project>('/projects', input)).data
}

export async function updateProject(id: string, input: ProjectInput) {
  return (await api.put<Project>(`/projects/${id}`, input)).data
}

export async function deleteProject(id: string) {
  await api.delete(`/projects/${id}`)
}
