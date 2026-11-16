import axios from 'axios'

export function getErrorMessage(error: unknown) {
  if (axios.isAxiosError<{ detail?: string; errors?: Record<string, string> }>(error)) {
    const fieldError = error.response?.data.errors && Object.values(error.response.data.errors)[0]
    return fieldError ?? error.response?.data.detail ?? 'Não foi possível concluir a operação.'
  }
  return 'Não foi possível concluir a operação.'
}
