import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { FileChartColumnIncreasing, Play, Scale } from 'lucide-react'
import { calculateDecision, getDecision, listDecisions } from '../../services/decisions'
import { listImprovements } from '../../services/improvements'
import { getErrorMessage } from '../../utils/errors'

const initialWeights = { impact: '1', urgency: '1', frequency: '1', effort: '1' }
const formatWeight = (value: number) => value.toLocaleString('pt-BR', { style: 'percent', maximumFractionDigits: 1 })

export function DecisionsPage() {
  const client = useQueryClient()
  const improvements = useQuery({ queryKey: ['improvements'], queryFn: listImprovements })
  const history = useQuery({ queryKey: ['decisions'], queryFn: listDecisions })
  const [name, setName] = useState('')
  const [weights, setWeights] = useState(initialWeights)
  const [selectedIds, setSelectedIds] = useState<string[]>([])
  const [selectedRun, setSelectedRun] = useState('')
  const [error, setError] = useState('')
  const detail = useQuery({ queryKey: ['decision', selectedRun], queryFn: () => getDecision(selectedRun), enabled: Boolean(selectedRun) })
  const mutation = useMutation({ mutationFn: calculateDecision, onSuccess: async (result) => { await client.invalidateQueries({ queryKey: ['decisions'] }); setSelectedRun(result.id); setError('') }, onError: (cause) => setError(getErrorMessage(cause)) })
  const total = Object.values(weights).reduce((sum, weight) => sum + (Number(weight) || 0), 0)

  function submit(event: FormEvent) {
    event.preventDefault()
    setError('')
    if (total <= 0) { setError('Atribua importância a pelo menos um critério.'); return }
    mutation.mutate({ name, impactWeight: Number(weights.impact), urgencyWeight: Number(weights.urgency), frequencyWeight: Number(weights.frequency), effortWeight: Number(weights.effort), improvementIds: selectedIds })
  }
  function toggle(id: string) { setSelectedIds((current) => current.includes(id) ? current.filter((value) => value !== id) : [...current, id]) }

  return <div className="workspace-page">
    <section className="page-heading"><div><div className="heading-overline"><FileChartColumnIncreasing size={14} /> MÉTODO SAW</div><h1>Decisões</h1><p>Compare melhorias com pesos definidos pelo gestor e consulte o histórico das avaliações.</p></div></section>
    <form className="panel editor-panel" onSubmit={submit}>
      <div className="editor-heading"><h2>Nova rodada de priorização</h2><p>Indique a importância relativa de cada critério de 0 a 10. Não precisa somar: o sistema ajusta os valores automaticamente. Impacto, urgência e frequência são benefícios; no esforço, menor valor favorece a prioridade.</p></div>
      {error && <p className="form-error" role="alert">{error}</p>}
      <div className="form-grid"><label className="field field--wide"><span>Nome da rodada</span><input required maxLength={120} value={name} onChange={(event) => setName(event.target.value)} placeholder="Ex.: Priorização do ciclo de novembro" /></label>
        {(['impact', 'urgency', 'frequency', 'effort'] as const).map((key) => <label key={key} className="field"><span>{{ impact: 'Importância do impacto', urgency: 'Importância da urgência', frequency: 'Importância da frequência', effort: 'Importância do esforço' }[key]}</span><input type="number" min="0" max="10" step="0.5" required value={weights[key]} onChange={(event) => setWeights({ ...weights, [key]: event.target.value })} /></label>)}
      </div>
      <div className="weight-total weight-total--valid"><Scale size={16} /><span className="weight-total-copy"><strong>Distribuição ajustada automaticamente</strong><span>O sistema converte essas importâncias em proporções que somam 100%.</span></span></div>
      <div className="decision-alternatives"><strong>Melhorias que serão comparadas</strong><div className="alternative-list">{improvements.data?.filter((item) => item.status !== 'CANCELLED' && item.status !== 'DONE').map((item) => <label className="alternative-option" key={item.id}><input type="checkbox" checked={selectedIds.includes(item.id)} onChange={() => toggle(item.id)} /><span><strong>{item.title}</strong><small>{item.applicationName} · Impacto {item.impact} · Urgência {item.urgency} · Frequência {item.frequency} · Esforço {item.effort}</small></span></label>)}</div>{improvements.data?.filter((item) => item.status !== 'CANCELLED' && item.status !== 'DONE').length === 0 && <p className="table-secondary">Cadastre melhorias em status aberto antes de calcular.</p>}</div>
      <div className="form-actions"><button className="button button--primary" disabled={mutation.isPending || !selectedIds.length || total <= 0}><Play size={15} />{mutation.isPending ? 'Calculando…' : 'Calcular ranking'}</button></div>
    </form>
    {detail.data && <section className="panel data-panel"><div className="data-panel-heading"><div><h2>{detail.data.name}</h2><p>Importância aplicada: impacto {formatWeight(detail.data.weights.impact)} · urgência {formatWeight(detail.data.weights.urgency)} · frequência {formatWeight(detail.data.weights.frequency)} · esforço {formatWeight(detail.data.weights.effort)}</p></div></div><div className="decision-method"><p>{detail.data.normalization}</p><p>{detail.data.criteriaMeaning}</p></div><div className="table-wrap"><table className="data-table"><thead><tr><th>POSIÇÃO</th><th>MELHORIA</th><th>VALORES I/U/F/E</th><th>NORMALIZADOS I/U/F/E</th><th>PONTUAÇÃO</th></tr></thead><tbody>{detail.data.ranking.map((score) => <tr key={score.improvementId}><td><span className="table-count">{score.ranking}</span></td><td><strong className="table-primary">{score.title}</strong></td><td className="table-secondary">{score.impact} / {score.urgency} / {score.frequency} / {score.effort}</td><td className="table-secondary">{[score.normalizedImpact, score.normalizedUrgency, score.normalizedFrequency, score.normalizedEffort].map((value) => value.toFixed(4)).join(' / ')}</td><td><strong className="table-primary">{score.finalScore.toFixed(5)}</strong></td></tr>)}</tbody></table></div></section>}
    <section className="panel data-panel"><div className="data-panel-heading"><div><h2>Histórico de decisões</h2><p>Rodadas anteriores e quantidade de alternativas avaliadas.</p></div><select className="history-select" value={selectedRun} onChange={(event) => setSelectedRun(event.target.value)}><option value="">Selecionar rodada</option>{history.data?.map((run) => <option key={run.id} value={run.id}>{run.name} · {new Date(run.createdAt).toLocaleDateString('pt-BR')}</option>)}</select></div>{history.data?.length === 0 && <p className="table-secondary history-empty">Ainda não há rodadas de decisão.</p>}</section>
  </div>
}
