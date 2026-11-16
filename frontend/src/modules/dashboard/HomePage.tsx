import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { ArrowRight, BarChart3, FolderKanban, MessageSquareText, Smartphone, Star } from 'lucide-react'
import { Link } from 'react-router'
import { getDashboardSummary } from '../../services/dashboard'
import { listProjects } from '../../services/projects'
import { listApplications } from '../../services/applications'
import { getErrorMessage } from '../../utils/errors'

export function HomePage() {
  const [projectId, setProjectId] = useState('')
  const [applicationId, setApplicationId] = useState('')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const projects = useQuery({ queryKey: ['projects'], queryFn: listProjects })
  const applications = useQuery({ queryKey: ['applications', projectId], queryFn: () => listApplications(projectId || undefined) })
  const filters = { projectId: projectId || undefined, applicationId: applicationId || undefined, from: from || undefined, to: to || undefined }
  const summaryQuery = useQuery({ queryKey: ['dashboard-summary', filters], queryFn: () => getDashboardSummary(filters) })
  const summary = summaryQuery.data
  const maxMonth = Math.max(1, ...(summary?.feedbacksByMonth.map((item) => item.count) ?? []))
  const sentimentNames = { POSITIVE: 'Positivos', NEUTRAL: 'Neutros', NEGATIVE: 'Negativos' } as const

  return <div className="dashboard">
    <section className="page-heading"><div><div className="heading-overline"><span className="live-dot" /> DADOS DO SEU ESPAÇO</div><h1>Visão geral</h1><p>Indicadores calculados com os registros da sua conta.</p></div><Link className="button button--primary" to="/analyses"><BarChart3 size={15} /> Abrir análises</Link></section>
    <section className="panel dashboard-filters"><label className="filter-field"><span>Projeto</span><select value={projectId} onChange={(event) => { setProjectId(event.target.value); setApplicationId('') }}><option value="">Todos</option>{projects.data?.map((project) => <option key={project.id} value={project.id}>{project.name}</option>)}</select></label><label className="filter-field"><span>Aplicativo</span><select value={applicationId} onChange={(event) => setApplicationId(event.target.value)}><option value="">Todos</option>{applications.data?.map((app) => <option key={app.id} value={app.id}>{app.name}</option>)}</select></label><label className="filter-field"><span>De</span><input type="date" value={from} onChange={(event) => setFrom(event.target.value)} /></label><label className="filter-field"><span>Até</span><input type="date" value={to} onChange={(event) => setTo(event.target.value)} /></label><button className="button button--secondary filter-clear" onClick={() => { setProjectId(''); setApplicationId(''); setFrom(''); setTo('') }}>Limpar filtros</button></section>
    {summaryQuery.isLoading && <div className="panel state-panel">Carregando indicadores…</div>}
    {summaryQuery.isError && <div className="panel state-panel state-panel--error" role="alert">{getErrorMessage(summaryQuery.error)} <button className="text-link" onClick={() => void summaryQuery.refetch()}>Tentar novamente</button></div>}
    {summary && <>
      <section className="metrics-grid" aria-label="Indicadores principais">
        <SummaryCard label="Projetos" value={summary.projects.toLocaleString('pt-BR')} icon={FolderKanban} href="/projects" />
        <SummaryCard label="Aplicativos" value={summary.applications.toLocaleString('pt-BR')} icon={Smartphone} href="/applications" />
        <SummaryCard label="Feedbacks" value={summary.feedbacks.toLocaleString('pt-BR')} icon={MessageSquareText} href="/feedbacks" />
        <SummaryCard label="Média das avaliações" value={summary.averageRating === null ? '—' : `${summary.averageRating.toLocaleString('pt-BR', { maximumFractionDigits: 2 })}/5`} icon={Star} href="/feedbacks" />
      </section>
      <section className="analysis-grid dashboard-analysis-grid">
        <article className="panel"><div className="panel-heading"><div><h2>Feedbacks ao longo do tempo</h2><p>Distribuição mensal do período; sem filtro, exibe os últimos seis meses.</p></div></div><div className="monthly-chart" role="img" aria-label="Quantidade de feedbacks por mês">{summary.feedbacksByMonth.map((item) => <div className="monthly-column" key={item.month}><strong>{item.count}</strong><span className="monthly-bar-track"><i style={{ height: `${Math.max(item.count ? 8 : 0, item.count / maxMonth * 100)}%` }} /></span><small>{new Date(`${item.month}-01T12:00:00Z`).toLocaleDateString('pt-BR', { month: 'short', timeZone: 'UTC' })}</small></div>)}</div></article>
        <article className="panel"><div className="panel-heading"><div><h2>Sentimento classificado</h2><p>Somente feedbacks com análise executada.</p></div></div><div className="sentiment-summary">{(['POSITIVE', 'NEUTRAL', 'NEGATIVE'] as const).map((key) => <div className="sentiment-summary-row" key={key}><span className={`sentiment-tag sentiment-tag--${key.toLowerCase()}`}>{sentimentNames[key]}</span><strong>{summary.sentimentCounts[key] ?? 0}</strong><span>{(summary.sentimentPercentages[key] ?? 0).toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%</span></div>)}</div></article>
      </section>
      <section className="dashboard-detail-grid">
        <article className="panel"><div className="panel-heading"><div><h2>Notas e avaliações</h2><p>Estatística descritiva do período selecionado.</p></div></div><div className="stats-list"><Stat label="Mediana" value={summary.medianRating?.toLocaleString('pt-BR') ?? '—'} /><Stat label="Moda" value={summary.modeRatings.length ? summary.modeRatings.join(', ') : '—'} /><Stat label="Mínima / máxima" value={summary.minimumRating === null ? '—' : `${summary.minimumRating} / ${summary.maximumRating}`} /><Stat label="Desvio padrão populacional" value={summary.standardDeviation?.toLocaleString('pt-BR', { maximumFractionDigits: 2 }) ?? '—'} /></div></article>
        <article className="panel"><div className="panel-heading"><div><h2>Feedbacks por categoria</h2><p>Classificações da análise mais recente de cada feedback.</p></div></div>{Object.keys(summary.categoryCounts).length ? <div className="category-bars">{Object.entries(summary.categoryCounts).sort((a, b) => b[1] - a[1]).map(([category, count]) => <div className="category-bar-row" key={category}><span>{category}</span><b>{count}</b><i style={{ width: `${Math.max(4, count / Math.max(1, ...Object.values(summary.categoryCounts)) * 100)}%` }} /></div>)}</div> : <p className="dashboard-empty">Ainda não há classificações para este filtro.</p>}</article>
      </section>
      <section className="analysis-grid dashboard-analysis-grid">
        <article className="panel"><div className="panel-heading"><div><h2>Problemas recorrentes</h2><p>Grupos candidatos da análise mais recente compatível com o filtro.</p></div><Link className="text-link" to="/analyses">Análises <ArrowRight size={14} /></Link></div>{summary.recurringIssues.length ? <div className="dashboard-list">{summary.recurringIssues.map((issue, index) => <div className="dashboard-list-row" key={`${issue.title}-${index}`}><span className="rank-badge">{index + 1}</span><span><strong>{issue.title}</strong><small>{issue.category}</small></span><b>{issue.feedbackCount}</b></div>)}</div> : <p className="dashboard-empty">Execute uma análise para identificar grupos candidatos.</p>}</article>
        <article className="panel"><div className="panel-heading"><div><h2>Status das melhorias</h2><p>Contagem das melhorias no escopo escolhido.</p></div><Link className="text-link" to="/improvements">Ver <ArrowRight size={14} /></Link></div><div className="stats-list">{Object.entries(summary.improvementStatuses).map(([status, count]) => <Stat key={status} label={{ PROPOSED: 'Propostas', IN_PROGRESS: 'Em andamento', DONE: 'Concluídas', CANCELLED: 'Canceladas' }[status] ?? status} value={String(count)} />)}</div></article>
      </section>
      <section className="panel"><div className="panel-heading"><div><h2>Prioridades da decisão mais recente</h2><p>Pontuações registradas na última rodada SAW; o ranking guarda os valores usados no cálculo.</p></div><Link className="text-link" to="/decisions">Decisões <ArrowRight size={14} /></Link></div>{summary.latestPriorities.length ? <div className="dashboard-list">{summary.latestPriorities.map((item) => <div className="dashboard-list-row" key={item.ranking}><span className="rank-badge">{item.ranking}</span><span><strong>{item.title}</strong><small>Pontuação SAW</small></span><b>{item.score.toLocaleString('pt-BR', { maximumFractionDigits: 5 })}</b></div>)}</div> : <p className="dashboard-empty">Nenhuma rodada de decisão foi calculada neste espaço.</p>}</section>
    </>}
  </div>
}

function SummaryCard({ label, value, icon: Icon, href }: { label: string; value: string; icon: typeof FolderKanban; href: string }) {
  return <Link className="metric-card summary-card" to={href}><div className="metric-topline"><span>{label}</span><span className="metric-icon metric-icon--blue"><Icon size={17} strokeWidth={1.9} /></span></div><div className="summary-value">{value}</div><span className="summary-card-link">Ver registros <ArrowRight size={14} /></span></Link>
}
function Stat({ label, value }: { label: string; value: string }) { return <div className="stat-row"><span>{label}</span><strong>{value}</strong></div> }
