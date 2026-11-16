import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Pencil, Plus, Smartphone, Trash2 } from 'lucide-react'
import { Link } from 'react-router'
import { createApplication, deleteApplication, listApplications, updateApplication } from '../../services/applications'
import { listProjects } from '../../services/projects'
import type { ManagedApplication } from '../../types/domain'
import { getErrorMessage } from '../../utils/errors'

export function ApplicationsPage() {
  const queryClient = useQueryClient()
  const projectsQuery = useQuery({ queryKey: ['projects'], queryFn: listProjects })
  const [projectFilter, setProjectFilter] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<ManagedApplication | null>(null)
  const [projectId, setProjectId] = useState('')
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [formError, setFormError] = useState('')
  const applicationsQuery = useQuery({ queryKey: ['applications', projectFilter], queryFn: () => listApplications(projectFilter || undefined) })
  const saveMutation = useMutation({
    mutationFn: () => editing
      ? updateApplication(editing.id, { projectId, name, description: description || null })
      : createApplication({ projectId, name, description: description || null }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['applications'] })
      await queryClient.invalidateQueries({ queryKey: ['projects'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
      closeForm()
    },
    onError: (error) => setFormError(getErrorMessage(error)),
  })
  const deleteMutation = useMutation({
    mutationFn: deleteApplication,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['applications'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
    },
  })

  function closeForm() {
    setFormOpen(false); setEditing(null); setProjectId(''); setName(''); setDescription(''); setFormError('')
  }
  function openCreate() {
    closeForm(); setProjectId(projectFilter || projectsQuery.data?.[0]?.id || ''); setFormOpen(true)
  }
  function openEdit(application: ManagedApplication) {
    setEditing(application); setProjectId(application.projectId); setName(application.name); setDescription(application.description ?? ''); setFormError(''); setFormOpen(true)
  }
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setFormError(''); saveMutation.mutate()
  }

  return (
    <div className="workspace-page">
      <section className="page-heading"><div><div className="heading-overline"><Smartphone size={14} /> PORTFÓLIO</div><h1>Aplicativos</h1><p>Associe aplicativos aos projetos e acompanhe os feedbacks de cada um.</p></div>{!formOpen && <button className="button button--primary" onClick={openCreate} disabled={!projectsQuery.data?.length}><Plus size={16} /> Novo aplicativo</button>}</section>
      {projectsQuery.isSuccess && projectsQuery.data.length === 0 && <div className="panel empty-state"><span className="empty-state-icon"><Smartphone size={22} /></span><h2>Cadastre um projeto primeiro</h2><p>Todo aplicativo precisa estar associado a um projeto.</p><Link className="button button--primary" to="/projects">Ir para projetos</Link></div>}
      {formOpen && <form className="panel editor-panel" onSubmit={submit}>
        <div className="editor-heading"><div><h2>{editing ? 'Editar aplicativo' : 'Novo aplicativo'}</h2><p>Selecione o projeto ao qual este aplicativo pertence.</p></div></div>
        {formError && <p className="form-error" role="alert">{formError}</p>}
        <div className="form-grid">
          <label className="field"><span>Projeto</span><select required value={projectId} onChange={(event) => setProjectId(event.target.value)}><option value="">Selecione um projeto</option>{projectsQuery.data?.map((project) => <option key={project.id} value={project.id}>{project.name}</option>)}</select></label>
          <label className="field"><span>Nome do aplicativo</span><input autoFocus maxLength={120} required value={name} onChange={(event) => setName(event.target.value)} placeholder="Ex.: Aplicativo iOS" /></label>
          <label className="field field--wide"><span>Descrição <small>Opcional</small></span><textarea maxLength={2000} rows={3} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Plataforma ou contexto do aplicativo" /></label>
        </div>
        <div className="form-actions"><button type="button" className="button button--secondary" onClick={closeForm}>Cancelar</button><button className="button button--primary" disabled={saveMutation.isPending}>{saveMutation.isPending ? 'Salvando…' : editing ? 'Salvar alterações' : 'Criar aplicativo'}</button></div>
      </form>}
      {deleteMutation.error && <p className="form-error" role="alert">{getErrorMessage(deleteMutation.error)}</p>}
      {applicationsQuery.isLoading && <div className="panel state-panel">Carregando aplicativos…</div>}
      {applicationsQuery.isError && <div className="panel state-panel state-panel--error">{getErrorMessage(applicationsQuery.error)} <button className="text-link" onClick={() => void applicationsQuery.refetch()}>Tentar novamente</button></div>}
      {applicationsQuery.isSuccess && projectsQuery.data?.length && applicationsQuery.data.length === 0 && <div className="panel empty-state"><span className="empty-state-icon"><Smartphone size={22} /></span><h2>Nenhum aplicativo encontrado</h2><p>Adicione um aplicativo ao projeto selecionado para começar a registrar feedbacks.</p>{!formOpen && <button className="button button--primary" onClick={openCreate}><Plus size={16} /> Novo aplicativo</button>}</div>}
      {applicationsQuery.isSuccess && applicationsQuery.data.length > 0 && <section className="panel data-panel">
        <div className="data-panel-heading"><div><h2>Aplicativos cadastrados</h2><p>{applicationsQuery.data.length} {applicationsQuery.data.length === 1 ? 'aplicativo' : 'aplicativos'}</p></div><label className="filter-field"><span>Projeto</span><select value={projectFilter} onChange={(event) => setProjectFilter(event.target.value)}><option value="">Todos os projetos</option>{projectsQuery.data?.map((project) => <option key={project.id} value={project.id}>{project.name}</option>)}</select></label></div>
        <div className="table-wrap"><table className="data-table"><thead><tr><th>APLICATIVO</th><th>PROJETO</th><th>DESCRIÇÃO</th><th>CRIADO EM</th><th><span className="sr-only">Ações</span></th></tr></thead><tbody>{applicationsQuery.data.map((application) => <tr key={application.id}>
          <td><strong className="table-primary">{application.name}</strong></td><td className="table-secondary">{application.projectName}</td><td className="table-secondary">{application.description || '—'}</td><td className="table-secondary">{new Date(application.createdAt).toLocaleDateString('pt-BR')}</td>
          <td className="table-actions"><button className="row-menu" aria-label={`Editar ${application.name}`} onClick={() => openEdit(application)}><Pencil size={15} /></button><button className="row-menu row-menu--danger" aria-label={`Excluir ${application.name}`} onClick={() => window.confirm(`Excluir o aplicativo “${application.name}”? Aplicativos com feedbacks vinculados não podem ser excluídos.`) && deleteMutation.mutate(application.id)} disabled={deleteMutation.isPending}><Trash2 size={15} /></button></td>
        </tr>)}</tbody></table></div>
      </section>}
    </div>
  )
}
