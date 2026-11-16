import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Lightbulb, Pencil, Plus, Trash2 } from 'lucide-react'
import { listApplications } from '../../services/applications'
import { listAnalyses, getAnalysis } from '../../services/analyses'
import { createImprovement, deleteImprovement, listImprovements, updateImprovement, type ImprovementInput } from '../../services/improvements'
import type { Improvement, ImprovementStatus } from '../../types/domain'
import { getErrorMessage } from '../../utils/errors'

const statusLabels: Record<ImprovementStatus, string> = { PROPOSED: 'Proposta', IN_PROGRESS: 'Em andamento', DONE: 'Concluída', CANCELLED: 'Cancelada' }
const blank = { applicationId: '', issueGroupId: '', title: '', description: '', impact: '', urgency: '', effort: '', assessmentOrigin: '', status: 'PROPOSED' as ImprovementStatus }

export function ImprovementsPage() {
  const client = useQueryClient()
  const apps = useQuery({ queryKey: ['applications'], queryFn: () => listApplications() })
  const query = useQuery({ queryKey: ['improvements'], queryFn: listImprovements })
  const history = useQuery({ queryKey: ['analyses'], queryFn: listAnalyses })
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Improvement | null>(null)
  const [form, setForm] = useState(blank)
  const [error, setError] = useState('')
  const latestAppAnalysis = history.data?.find((run) => run.applicationId === form.applicationId)
  const groups = useQuery({ queryKey: ['analysis', latestAppAnalysis?.id], queryFn: () => getAnalysis(latestAppAnalysis!.id), enabled: Boolean(formOpen && latestAppAnalysis?.id) })
  const mutation = useMutation({ mutationFn: (input: ImprovementInput) => editing ? updateImprovement(editing.id, input) : createImprovement(input), onSuccess: async () => { await client.invalidateQueries({ queryKey: ['improvements'] }); setFormOpen(false); setEditing(null); setForm(blank) }, onError: (cause) => setError(getErrorMessage(cause)) })
  const remove = useMutation({ mutationFn: deleteImprovement, onSuccess: () => client.invalidateQueries({ queryKey: ['improvements'] }), onError: (cause) => setError(getErrorMessage(cause)) })
  function beginCreate() { setEditing(null); setForm({ ...blank, applicationId: apps.data?.[0]?.id ?? '' }); setError(''); setFormOpen(true) }
  function beginEdit(item: Improvement) { setEditing(item); setForm({ applicationId: item.applicationId, issueGroupId: item.issueGroupId ?? '', title: item.title, description: item.description, impact: String(item.impact), urgency: String(item.urgency), effort: String(item.effort), assessmentOrigin: item.assessmentOrigin, status: item.status }); setError(''); setFormOpen(true) }
  function submit(event: FormEvent) { event.preventDefault(); setError(''); mutation.mutate({ ...form, issueGroupId: form.issueGroupId || null, impact: Number(form.impact), urgency: Number(form.urgency), effort: Number(form.effort) }) }
  const selectedGroup = groups.data?.groups.find((group) => group.id === form.issueGroupId)
  const linkedFeedbackCount = selectedGroup?.feedbackCount ?? (editing?.issueGroupId === form.issueGroupId ? editing.frequency : 0)

  return <div className="workspace-page">
    <section className="page-heading"><div><div className="heading-overline"><Lightbulb size={14} /> OPORTUNIDADES</div><h1>Melhorias</h1><p>Registre propostas com critérios explícitos para acompanhar e priorizar.</p></div>{!formOpen && <button className="button button--primary" onClick={beginCreate} disabled={!apps.data?.length}><Plus size={16} /> Nova melhoria</button>}</section>
    {!apps.isLoading && apps.data?.length === 0 && <div className="panel empty-state"><h2>Cadastre um aplicativo primeiro</h2><p>Uma melhoria deve estar vinculada a um aplicativo.</p></div>}
    {formOpen && <form className="panel editor-panel" onSubmit={submit}><div className="editor-heading"><h2>{editing ? 'Editar melhoria' : 'Nova oportunidade'}</h2><p>Impacto, urgência e esforço são avaliados por você. A frequência vem automaticamente das avaliações no grupo associado.</p></div>{error && <p className="form-error" role="alert">{error}</p>}<div className="form-grid">
      <label className="field"><span>Aplicativo</span><select required value={form.applicationId} onChange={(event) => setForm({ ...form, applicationId: event.target.value, issueGroupId: '' })}><option value="">Selecione</option>{apps.data?.map((item) => <option key={item.id} value={item.id}>{item.projectName} / {item.name}</option>)}</select></label>
      <label className="field"><span>Grupo de problemas</span><select disabled={groups.isLoading} value={form.issueGroupId} onChange={(event) => setForm({ ...form, issueGroupId: event.target.value })}><option value="">Sem grupo associado (0 avaliações)</option>{editing?.issueGroupId && !groups.data?.groups.some((group) => group.id === editing.issueGroupId) && <option value={editing.issueGroupId}>{editing.issueGroupTitle ?? 'Grupo associado anteriormente'} · {editing.frequency} avaliações</option>}{groups.data?.groups.map((group) => <option key={group.id} value={group.id}>{group.title} · {group.feedbackCount} avaliações</option>)}</select><small>{form.issueGroupId ? `${linkedFeedbackCount} avaliações associadas serão usadas como frequência.` : 'Selecione um grupo para incluir a recorrência dos feedbacks na prioridade.'}</small>{!groups.isLoading && !latestAppAnalysis && <small>Execute uma análise para este aplicativo para disponibilizar grupos.</small>}</label>
      <label className="field"><span>Título</span><input required maxLength={160} value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} /></label>
      <label className="field"><span>Status</span><select value={form.status} onChange={(event) => setForm({ ...form, status: event.target.value as ImprovementStatus })}>{Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
      <label className="field field--wide"><span>Descrição</span><textarea required maxLength={2000} rows={3} value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} /></label>
      {(['impact', 'urgency', 'effort'] as const).map((key) => <label className="field" key={key}><span>{{ impact: 'Impacto', urgency: 'Urgência', effort: 'Esforço' }[key]} (1–5)</span><select required value={form[key]} onChange={(event) => setForm({ ...form, [key]: event.target.value })}><option value="">Avaliar</option>{[1, 2, 3, 4, 5].map((value) => <option key={value} value={value}>{value}</option>)}</select></label>)}
      <label className="field field--wide"><span>Origem e justificativa das avaliações manuais</span><textarea required maxLength={500} rows={2} value={form.assessmentOrigin} onChange={(event) => setForm({ ...form, assessmentOrigin: event.target.value })} placeholder="Ex.: impacto e urgência avaliados pela equipe…" /></label>
    </div><div className="form-actions"><button type="button" className="button button--secondary" onClick={() => { setFormOpen(false); setEditing(null) }}>Cancelar</button><button className="button button--primary" disabled={mutation.isPending}>{mutation.isPending ? 'Salvando…' : 'Salvar melhoria'}</button></div></form>}
    {error && !formOpen && <p className="form-error" role="alert">{error}</p>}
    {query.isLoading && <div className="panel state-panel">Carregando melhorias…</div>}{query.isError && <div className="panel state-panel state-panel--error">{getErrorMessage(query.error)}</div>}
    {query.data?.length === 0 && apps.data?.length && <div className="panel empty-state"><h2>Nenhuma melhoria cadastrada</h2><p>As avaliações registradas serão usadas no cálculo de prioridade.</p>{!formOpen && <button className="button button--primary" onClick={beginCreate}><Plus size={15} /> Criar melhoria</button>}</div>}
    {!!query.data?.length && <section className="panel data-panel"><div className="data-panel-heading"><div><h2>Oportunidades registradas</h2><p>{query.data.length} melhorias; frequência baseada nos feedbacks associados</p></div></div><div className="table-wrap"><table className="data-table"><thead><tr><th>MELHORIA</th><th>APLICATIVO</th><th>IMPACTO / URGÊNCIA / AVALIAÇÕES / ESFORÇO</th><th>STATUS</th><th><span className="sr-only">Ações</span></th></tr></thead><tbody>{query.data.map((item) => <tr key={item.id}><td><strong className="table-primary">{item.title}</strong><small className="table-secondary table-block">{item.assessmentOrigin}</small>{item.issueGroupTitle && <small className="table-secondary table-block">Grupo: {item.issueGroupTitle}</small>}</td><td><strong className="table-primary">{item.applicationName}</strong><small className="table-secondary table-block">{item.projectName}</small></td><td className="table-secondary">{item.impact} / {item.urgency} / {item.frequency} / {item.effort}</td><td><span className="status-pill status-pill--blue">{statusLabels[item.status]}</span></td><td className="table-actions"><button className="row-menu" aria-label={`Editar ${item.title}`} onClick={() => beginEdit(item)}><Pencil size={15} /></button><button className="row-menu row-menu--danger" aria-label={`Excluir ${item.title}`} onClick={() => window.confirm(`Excluir ${item.title}? Ela não pode ser removida se fizer parte de uma decisão histórica.`) && remove.mutate(item.id)}><Trash2 size={15} /></button></td></tr>)}</tbody></table></div></section>}
  </div>
}
