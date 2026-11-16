import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { FolderTree, Pencil, Plus, Trash2 } from 'lucide-react'
import { createCategory, deleteCategory, listCategories, updateCategory } from '../../services/categories'
import type { Category } from '../../types/domain'
import { getErrorMessage } from '../../utils/errors'

export function CategoriesPage() {
  const client = useQueryClient()
  const query = useQuery({ queryKey: ['categories'], queryFn: listCategories })
  const [editing, setEditing] = useState<Category | null>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [error, setError] = useState('')
  const save = useMutation({ mutationFn: () => editing ? updateCategory(editing.id, { name, description }) : createCategory({ name, description }), onSuccess: async () => { await client.invalidateQueries({ queryKey: ['categories'] }); setEditing(null); setName(''); setDescription('') }, onError: (cause) => setError(getErrorMessage(cause)) })
  const remove = useMutation({ mutationFn: deleteCategory, onSuccess: () => client.invalidateQueries({ queryKey: ['categories'] }), onError: (cause) => setError(getErrorMessage(cause)) })
  function submit(event: FormEvent) { event.preventDefault(); setError(''); save.mutate() }
  function edit(category: Category) { setEditing(category); setName(category.name); setDescription(category.description); setError('') }

  return <div className="workspace-page">
    <section className="page-heading"><div><div className="heading-overline"><FolderTree size={14} /> TAXONOMIA</div><h1>Categorias</h1><p>Organize os temas usados na classificação dos feedbacks.</p></div></section>
    <form className="panel editor-panel" onSubmit={submit}><div className="editor-heading"><h2>{editing ? 'Editar categoria' : 'Nova categoria personalizada'}</h2><p>As categorias padrão são compartilhadas e protegidas. Categorias personalizadas pertencem à sua conta.</p></div>{error && <p role="alert" className="form-error">{error}</p>}<div className="form-grid"><label className="field"><span>Nome</span><input required maxLength={80} value={name} onChange={(event) => setName(event.target.value)} placeholder="Ex.: Pagamentos" /></label><label className="field"><span>Descrição</span><input required maxLength={500} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Quais feedbacks entram nesta categoria?" /></label></div><div className="form-actions">{editing && <button type="button" className="button button--secondary" onClick={() => { setEditing(null); setName(''); setDescription('') }}>Cancelar edição</button>}<button className="button button--primary" disabled={save.isPending}><Plus size={15} />{save.isPending ? 'Salvando…' : editing ? 'Salvar categoria' : 'Criar categoria'}</button></div></form>
    {query.isError && <div className="panel state-panel state-panel--error">{getErrorMessage(query.error)}</div>}
    {query.data && <section className="panel data-panel"><div className="data-panel-heading"><div><h2>Categorias disponíveis</h2><p>{query.data.length} categorias entre padrões e personalizadas</p></div></div><div className="table-wrap"><table className="data-table"><thead><tr><th>CATEGORIA</th><th>DESCRIÇÃO</th><th>ORIGEM</th><th><span className="sr-only">Ações</span></th></tr></thead><tbody>{query.data.map((category) => <tr key={category.id}><td><strong className="table-primary">{category.name}</strong></td><td className="table-secondary">{category.description}</td><td><span className={`status-pill ${category.system ? 'status-pill--blue' : 'status-pill--gray'}`}>{category.system ? 'Padrão' : 'Personalizada'}</span></td><td className="table-actions">{!category.system && <><button className="row-menu" aria-label={`Editar ${category.name}`} onClick={() => edit(category)}><Pencil size={15} /></button><button className="row-menu row-menu--danger" aria-label={`Excluir ${category.name}`} onClick={() => window.confirm(`Excluir a categoria ${category.name}?`) && remove.mutate(category.id)}><Trash2 size={15} /></button></>}</td></tr>)}</tbody></table></div></section>}
  </div>
}
