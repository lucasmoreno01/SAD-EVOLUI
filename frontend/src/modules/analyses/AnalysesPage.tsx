import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { BarChart3, Play, RotateCcw } from 'lucide-react'
import { correctClassification, getAnalysis, listAnalyses, runAnalysis } from '../../services/analyses'
import { listApplications } from '../../services/applications'
import { listCategories } from '../../services/categories'
import { listProjects } from '../../services/projects'
import type { Sentiment } from '../../types/domain'
import { getErrorMessage } from '../../utils/errors'

const sentimentLabels: Record<Sentiment, string> = { POSITIVE: 'Positivo', NEUTRAL: 'Neutro', NEGATIVE: 'Negativo' }

export function AnalysesPage() {
  const client = useQueryClient()
  const projects = useQuery({ queryKey: ['projects'], queryFn: listProjects })
  const applications = useQuery({ queryKey: ['applications'], queryFn: () => listApplications() })
  const categories = useQuery({ queryKey: ['categories'], queryFn: listCategories })
  const history = useQuery({ queryKey: ['analyses'], queryFn: listAnalyses })
  const [scope, setScope] = useState('')
  const [selectedRun, setSelectedRun] = useState('')
  const [error, setError] = useState('')
  const detail = useQuery({ queryKey: ['analysis', selectedRun], queryFn: () => getAnalysis(selectedRun), enabled: Boolean(selectedRun) })
  const run = useMutation({ mutationFn: runAnalysis, onSuccess: async (result) => { setSelectedRun(result.id); await client.invalidateQueries({ queryKey: ['analyses'] }); setError('') }, onError: (cause) => setError(getErrorMessage(cause)) })
  const correction = useMutation({ mutationFn: ({ id, sentiment, categoryId }: { id: string; sentiment: Sentiment; categoryId: string }) => correctClassification(id, { sentiment, categoryId }), onSuccess: () => client.invalidateQueries({ queryKey: ['analysis', selectedRun] }), onError: (cause) => setError(getErrorMessage(cause)) })

  function execute() {
    if (!scope) { setError('Selecione um projeto ou aplicativo para analisar.'); return }
    setError('')
    run.mutate(scope.startsWith('app:') ? { projectId: null, applicationId: scope.slice(4) } : { projectId: scope.slice(8), applicationId: null })
  }

  return <div className="workspace-page">
    <section className="page-heading"><div><div className="heading-overline"><BarChart3 size={14} /> CLASSIFICAÇÃO E PADRÕES</div><h1>Análises</h1><p>Execute e revise classificações e grupos candidatos a partir dos feedbacks registrados.</p></div></section>
    <section className="panel analysis-launch"><div><h2>Nova análise</h2><p>Classifica sentimento a partir da nota, tema por palavras-chave e reúne comentários por similaridade lexical.</p></div><div className="analysis-launch-controls"><select className="scope-select" value={scope} onChange={(event) => setScope(event.target.value)}><option value="">Selecionar escopo…</option><optgroup label="Projetos">{projects.data?.map((item) => <option key={item.id} value={`project:${item.id}`}>{item.name}</option>)}</optgroup><optgroup label="Aplicativos">{applications.data?.map((item) => <option key={item.id} value={`app:${item.id}`}>{item.projectName} / {item.name}</option>)}</optgroup></select><button className="button button--primary" onClick={execute} disabled={run.isPending}><Play size={15} />{run.isPending ? 'Analisando…' : 'Executar'}</button></div></section>
    {error && <p className="form-error" role="alert">{error}</p>}
    {history.data && history.data.length > 0 && <section className="panel history-bar"><label className="field"><span>Histórico de análises</span><select value={selectedRun} onChange={(event) => setSelectedRun(event.target.value)}><option value="">Escolha uma execução</option>{history.data.map((item) => <option key={item.id} value={item.id}>{new Date(item.createdAt).toLocaleString('pt-BR')} · {item.methodVersion}</option>)}</select></label><span className="history-count">{history.data.length} execuções</span></section>}
    {detail.isLoading && <div className="panel state-panel">Carregando análise…</div>}{detail.isError && <div className="panel state-panel state-panel--error">{getErrorMessage(detail.error)}</div>}
    {detail.data && <>
      <div className="analysis-notice"><strong>Método: {detail.data.methodVersion}</strong><span>{detail.data.methodNotice}</span></div>
      <section className="metrics-grid analysis-metrics"><article className="metric-card"><span className="metric-topline">Feedbacks analisados</span><div className="summary-value">{detail.data.feedbackCount}</div></article>{(['POSITIVE', 'NEUTRAL', 'NEGATIVE'] as const).map((sentiment) => <article className="metric-card" key={sentiment}><span className="metric-topline">{sentimentLabels[sentiment]}</span><div className="summary-value">{detail.data.sentimentCounts[sentiment] ?? 0}</div></article>)}</section>
      <section className="panel data-panel"><div className="data-panel-heading"><div><h2>Distribuição temática</h2><p>Contagens derivadas desta execução.</p></div></div><div className="category-counts">{Object.entries(detail.data.categoryCounts).map(([name, count]) => <span className="category-count" key={name}><strong>{name}</strong><b>{count}</b></span>)}</div></section>
      <section className="panel data-panel"><div className="data-panel-heading"><div><h2>Grupos candidatos</h2><p>Jaccard ≥ 0,40 entre comentários da mesma categoria; confirme a coerência antes de propor uma melhoria.</p></div></div>{detail.data.groups.length === 0 ? <p className="history-empty">Nenhum grupo atingiu o critério de similaridade nesta execução.</p> : <div className="table-wrap"><table className="data-table"><thead><tr><th>TRECHO REPRESENTATIVO</th><th>CATEGORIA</th><th>FEEDBACKS</th></tr></thead><tbody>{detail.data.groups.map((group) => <tr key={group.id}><td className="feedback-comment">{group.title}</td><td>{group.category}</td><td><strong>{group.feedbackCount}</strong></td></tr>)}</tbody></table></div>}</section>
      <section className="panel data-panel"><div className="data-panel-heading"><div><h2>Classificações revisáveis</h2><p>Correções ficam marcadas como manuais e preservam a execução original.</p></div></div><div className="table-wrap"><table className="data-table"><thead><tr><th>COMENTÁRIO / NOTA</th><th>SENTIMENTO</th><th>CATEGORIA</th><th>ORIGEM</th><th><span className="sr-only">Ação</span></th></tr></thead><tbody>{detail.data.classifications.map((item) => <tr key={item.analysisId}><td className="feedback-comment">{item.comment}<small className="table-secondary table-block">Nota: {item.rating}/5</small>{item.originalSentiment && <small className="table-secondary table-block">Classificação automática original: {sentimentLabels[item.originalSentiment]} · {item.originalCategory}</small>}</td><td><select className="inline-select" value={item.sentiment} onChange={(event) => correction.mutate({ id: item.analysisId, sentiment: event.target.value as Sentiment, categoryId: item.categoryId })}>{Object.entries(sentimentLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></td><td><select className="inline-select" value={item.categoryId} onChange={(event) => correction.mutate({ id: item.analysisId, sentiment: item.sentiment, categoryId: event.target.value })}>{categories.data?.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></td><td><span className={`status-pill ${item.source === 'MANUAL' ? 'status-pill--blue' : 'status-pill--gray'}`}>{item.source === 'MANUAL' ? 'Revisão manual' : 'Heurística'}</span></td><td>{item.source === 'MANUAL' && <RotateCcw size={15} aria-label="Classificação revisada" />}</td></tr>)}</tbody></table></div></section>
    </>}
  </div>
}
