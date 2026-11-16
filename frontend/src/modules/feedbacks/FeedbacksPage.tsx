import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { MessageSquareText, Pencil, Plus, Star, Trash2 } from 'lucide-react'
import { Link } from 'react-router'
import { listApplications } from '../../services/applications'
import { createFeedback, deleteFeedback, listFeedbacks, updateFeedback } from '../../services/feedbacks'
import type { Feedback } from '../../types/domain'
import { getErrorMessage } from '../../utils/errors'

function toLocalDateTime(value: string) {
  const date = new Date(value)
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset())
  return date.toISOString().slice(0, 16)
}

export function FeedbacksPage() {
  const queryClient = useQueryClient()
  const applicationsQuery = useQuery({ queryKey: ['applications'], queryFn: () => listApplications() })
  const [applicationFilter, setApplicationFilter] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Feedback | null>(null)
  const [applicationId, setApplicationId] = useState('')
  const [rating, setRating] = useState('5')
  const [comment, setComment] = useState('')
  const [submittedAt, setSubmittedAt] = useState(() => toLocalDateTime(new Date().toISOString()))
  const [formError, setFormError] = useState('')
  const feedbacksQuery = useQuery({ queryKey: ['feedbacks', applicationFilter], queryFn: () => listFeedbacks(applicationFilter || undefined) })
  const saveMutation = useMutation({
    mutationFn: () => {
      const input = { applicationId, rating: Number(rating), comment, submittedAt: new Date(submittedAt).toISOString() }
      return editing ? updateFeedback(editing.id, input) : createFeedback(input)
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['feedbacks'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
      closeForm()
    },
    onError: (error) => setFormError(getErrorMessage(error)),
  })
  const deleteMutation = useMutation({
    mutationFn: deleteFeedback,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['feedbacks'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
    },
  })

  function closeForm() {
    setFormOpen(false); setEditing(null); setApplicationId(''); setRating('5'); setComment(''); setSubmittedAt(toLocalDateTime(new Date().toISOString())); setFormError('')
  }
  function openCreate() {
    closeForm(); setApplicationId(applicationFilter || applicationsQuery.data?.[0]?.id || ''); setFormOpen(true)
  }
  function openEdit(feedback: Feedback) {
    setEditing(feedback); setApplicationId(feedback.applicationId); setRating(String(feedback.rating)); setComment(feedback.comment); setSubmittedAt(toLocalDateTime(feedback.submittedAt)); setFormError(''); setFormOpen(true)
  }
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setFormError(''); saveMutation.mutate()
  }

  return (
    <div className="workspace-page">
      <section className="page-heading"><div><div className="heading-overline"><MessageSquareText size={14} /> VOZ DO USUÁRIO</div><h1>Feedbacks</h1><p>Registre avaliações recebidas e mantenha o contexto ligado ao aplicativo.</p></div>{!formOpen && <button className="button button--primary" onClick={openCreate} disabled={!applicationsQuery.data?.length}><Plus size={16} /> Registrar feedback</button>}</section>
      {applicationsQuery.isSuccess && applicationsQuery.data.length === 0 && <div className="panel empty-state"><span className="empty-state-icon"><MessageSquareText size={22} /></span><h2>Cadastre um aplicativo primeiro</h2><p>O feedback precisa estar associado a um aplicativo cadastrado.</p><Link className="button button--primary" to="/applications">Ir para aplicativos</Link></div>}
      {formOpen && <form className="panel editor-panel" onSubmit={submit}>
        <div className="editor-heading"><div><h2>{editing ? 'Editar feedback' : 'Registrar feedback'}</h2><p>A nota e o comentário serão armazenados sem classificação automática.</p></div></div>
        {formError && <p className="form-error" role="alert">{formError}</p>}
        <div className="form-grid">
          <label className="field"><span>Aplicativo</span><select required value={applicationId} onChange={(event) => setApplicationId(event.target.value)}><option value="">Selecione um aplicativo</option>{applicationsQuery.data?.map((application) => <option key={application.id} value={application.id}>{application.projectName} / {application.name}</option>)}</select></label>
          <label className="field"><span>Nota</span><select required value={rating} onChange={(event) => setRating(event.target.value)}><option value="5">5 — Excelente</option><option value="4">4 — Boa</option><option value="3">3 — Regular</option><option value="2">2 — Ruim</option><option value="1">1 — Muito ruim</option></select></label>
          <label className="field field--wide"><span>Comentário</span><textarea autoFocus={!editing} maxLength={4000} required rows={4} value={comment} onChange={(event) => setComment(event.target.value)} placeholder="Registre o comentário do usuário" /></label>
          <label className="field"><span>Data do feedback</span><input type="datetime-local" required value={submittedAt} max={toLocalDateTime(new Date().toISOString())} onChange={(event) => setSubmittedAt(event.target.value)} /></label>
        </div>
        <div className="form-actions"><button type="button" className="button button--secondary" onClick={closeForm}>Cancelar</button><button className="button button--primary" disabled={saveMutation.isPending}>{saveMutation.isPending ? 'Salvando…' : editing ? 'Salvar alterações' : 'Salvar feedback'}</button></div>
      </form>}
      {deleteMutation.error && <p className="form-error" role="alert">{getErrorMessage(deleteMutation.error)}</p>}
      {feedbacksQuery.isLoading && <div className="panel state-panel">Carregando feedbacks…</div>}
      {feedbacksQuery.isError && <div className="panel state-panel state-panel--error">{getErrorMessage(feedbacksQuery.error)} <button className="text-link" onClick={() => void feedbacksQuery.refetch()}>Tentar novamente</button></div>}
      {feedbacksQuery.isSuccess && applicationsQuery.data?.length && feedbacksQuery.data.length === 0 && <div className="panel empty-state"><span className="empty-state-icon"><MessageSquareText size={22} /></span><h2>Nenhum feedback encontrado</h2><p>Registre o primeiro feedback ou altere o filtro de aplicativo.</p>{!formOpen && <button className="button button--primary" onClick={openCreate}><Plus size={16} /> Registrar feedback</button>}</div>}
      {feedbacksQuery.isSuccess && feedbacksQuery.data.length > 0 && <section className="panel data-panel">
        <div className="data-panel-heading"><div><h2>Feedbacks registrados</h2><p>{feedbacksQuery.data.length} {feedbacksQuery.data.length === 1 ? 'feedback' : 'feedbacks'}</p></div><label className="filter-field"><span>Aplicativo</span><select value={applicationFilter} onChange={(event) => setApplicationFilter(event.target.value)}><option value="">Todos os aplicativos</option>{applicationsQuery.data?.map((application) => <option key={application.id} value={application.id}>{application.projectName} / {application.name}</option>)}</select></label></div>
        <div className="table-wrap"><table className="data-table"><thead><tr><th>COMENTÁRIO</th><th>APLICATIVO</th><th>NOTA</th><th>DATA</th><th><span className="sr-only">Ações</span></th></tr></thead><tbody>{feedbacksQuery.data.map((feedback) => <tr key={feedback.id}>
          <td className="feedback-comment">{feedback.comment}<small className="table-secondary table-block">{feedback.source === 'EXTERNAL' ? 'Recebido pela API' : 'Registrado manualmente'}</small></td><td><strong className="table-primary">{feedback.applicationName}</strong><small className="table-secondary table-block">{feedback.projectName}</small></td><td><span className="rating-value"><Star size={14} fill="currentColor" /> {feedback.rating}/5</span></td><td className="table-secondary">{new Date(feedback.submittedAt).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })}</td>
          <td className="table-actions"><button className="row-menu" aria-label="Editar feedback" onClick={() => openEdit(feedback)}><Pencil size={15} /></button><button className="row-menu row-menu--danger" aria-label="Excluir feedback" onClick={() => window.confirm('Excluir este feedback?') && deleteMutation.mutate(feedback.id)} disabled={deleteMutation.isPending}><Trash2 size={15} /></button></td>
        </tr>)}</tbody></table></div>
      </section>}
    </div>
  )
}
