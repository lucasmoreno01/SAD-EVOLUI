import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { FolderKanban, Pencil, Plus, Trash2 } from 'lucide-react'
import { createProject, deleteProject, listProjects, updateProject } from '../../services/projects'
import type { Project } from '../../types/domain'
import { getErrorMessage } from '../../utils/errors'

export function ProjectsPage() {
  const queryClient = useQueryClient()
  const projectsQuery = useQuery({ queryKey: ['projects'], queryFn: listProjects })
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Project | null>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [formError, setFormError] = useState('')
  const saveMutation = useMutation({
    mutationFn: () => editing
      ? updateProject(editing.id, { name, description: description || null })
      : createProject({ name, description: description || null }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['projects'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
      closeForm()
    },
    onError: (error) => setFormError(getErrorMessage(error)),
  })
  const deleteMutation = useMutation({
    mutationFn: deleteProject,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['projects'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
    },
  })

  function closeForm() {
    setFormOpen(false)
    setEditing(null)
    setName('')
    setDescription('')
    setFormError('')
  }

  function openCreate() {
    closeForm()
    setFormOpen(true)
  }

  function openEdit(project: Project) {
    setEditing(project)
    setName(project.name)
    setDescription(project.description ?? '')
    setFormError('')
    setFormOpen(true)
  }

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFormError('')
    saveMutation.mutate()
  }

  return (
    <div className="workspace-page">
      <section className="page-heading">
        <div><div className="heading-overline"><FolderKanban size={14} /> ORGANIZAÇÃO</div><h1>Projetos</h1><p>Organize os aplicativos que você acompanha em cada projeto.</p></div>
        {!formOpen && <button className="button button--primary" onClick={openCreate}><Plus size={16} /> Novo projeto</button>}
      </section>

      {formOpen && (
        <form className="panel editor-panel" onSubmit={submit}>
          <div className="editor-heading"><div><h2>{editing ? 'Editar projeto' : 'Novo projeto'}</h2><p>Informe os dados usados para identificar este projeto.</p></div></div>
          {formError && <p className="form-error" role="alert">{formError}</p>}
          <div className="form-grid">
            <label className="field field--wide"><span>Nome do projeto</span><input autoFocus maxLength={120} required value={name} onChange={(event) => setName(event.target.value)} placeholder="Ex.: Experiência mobile" /></label>
            <label className="field field--wide"><span>Descrição <small>Opcional</small></span><textarea maxLength={2000} rows={3} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Contexto e objetivo do projeto" /></label>
          </div>
          <div className="form-actions"><button type="button" className="button button--secondary" onClick={closeForm}>Cancelar</button><button className="button button--primary" disabled={saveMutation.isPending}>{saveMutation.isPending ? 'Salvando…' : editing ? 'Salvar alterações' : 'Criar projeto'}</button></div>
        </form>
      )}

      {deleteMutation.error && <p className="form-error" role="alert">{getErrorMessage(deleteMutation.error)}</p>}
      {projectsQuery.isLoading && <div className="panel state-panel">Carregando projetos…</div>}
      {projectsQuery.isError && <div className="panel state-panel state-panel--error">{getErrorMessage(projectsQuery.error)} <button className="text-link" onClick={() => void projectsQuery.refetch()}>Tentar novamente</button></div>}
      {projectsQuery.isSuccess && projectsQuery.data.length === 0 && (
        <div className="panel empty-state"><span className="empty-state-icon"><FolderKanban size={22} /></span><h2>Nenhum projeto cadastrado</h2><p>Crie um projeto para começar a organizar os aplicativos e seus feedbacks.</p>{!formOpen && <button className="button button--primary" onClick={openCreate}><Plus size={16} /> Criar primeiro projeto</button>}</div>
      )}
      {projectsQuery.isSuccess && projectsQuery.data.length > 0 && (
        <section className="panel data-panel">
          <div className="data-panel-heading"><div><h2>Todos os projetos</h2><p>{projectsQuery.data.length} {projectsQuery.data.length === 1 ? 'projeto' : 'projetos'}</p></div></div>
          <div className="table-wrap"><table className="data-table"><thead><tr><th>PROJETO</th><th>DESCRIÇÃO</th><th>CRIADO EM</th><th><span className="sr-only">Ações</span></th></tr></thead><tbody>
            {projectsQuery.data.map((project) => <tr key={project.id}>
              <td><strong className="table-primary">{project.name}</strong></td>
              <td className="table-secondary">{project.description || '—'}</td>
              <td className="table-secondary">{new Date(project.createdAt).toLocaleDateString('pt-BR')}</td>
              <td className="table-actions"><button className="row-menu" aria-label={`Editar ${project.name}`} onClick={() => openEdit(project)}><Pencil size={15} /></button><button className="row-menu row-menu--danger" aria-label={`Excluir ${project.name}`} onClick={() => window.confirm(`Excluir o projeto “${project.name}”? Projetos com aplicativos vinculados não podem ser excluídos.`) && deleteMutation.mutate(project.id)} disabled={deleteMutation.isPending}><Trash2 size={15} /></button></td>
            </tr>)}
          </tbody></table></div>
        </section>
      )}
    </div>
  )
}
