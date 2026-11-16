import { api } from './api'
import type { Category } from '../types/domain'
export type CategoryInput = Pick<Category, 'name' | 'description'>
export async function listCategories() { return (await api.get<Category[]>('/categories')).data }
export async function createCategory(input: CategoryInput) { return (await api.post<Category>('/categories', input)).data }
export async function updateCategory(id: string, input: CategoryInput) { return (await api.put<Category>(`/categories/${id}`, input)).data }
export async function deleteCategory(id: string) { await api.delete(`/categories/${id}`) }
