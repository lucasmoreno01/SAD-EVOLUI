import { useState } from 'react'
import { Activity, BarChart3, FileChartColumnIncreasing, FolderKanban, Gauge, Lightbulb, ListTree, LogOut, Menu, MessageSquareText, Smartphone, Sparkles, X } from 'lucide-react'
import { Link, Outlet, useLocation } from 'react-router'
import { useAuth } from '../providers/useAuth'

const navigation = [
  { label: 'Visão geral', icon: Gauge, to: '/', available: true },
  { label: 'Projetos', icon: FolderKanban, to: '/projects', available: true },
  { label: 'Aplicativos', icon: Smartphone, to: '/applications', available: true },
  { label: 'Feedbacks', icon: MessageSquareText, to: '/feedbacks', available: true },
  { label: 'Categorias', icon: ListTree, to: '/categories', available: true },
  { label: 'Análises', icon: BarChart3, to: '/analyses', available: true },
  { label: 'Melhorias', icon: Lightbulb, to: '/improvements', available: true },
  { label: 'Decisões', icon: FileChartColumnIncreasing, to: '/decisions', available: true },
]

export function MainLayout() {
  const [menuOpen, setMenuOpen] = useState(false)
  const location = useLocation()
  const currentPage = navigation.find(({ to }) => to === location.pathname)?.label ?? 'Visão geral'
  const { user, logout } = useAuth()

  return (
    <div className="app-shell">
      {menuOpen && <button className="sidebar-scrim" aria-label="Fechar menu" onClick={() => setMenuOpen(false)} />}
      <aside className={`sidebar${menuOpen ? ' sidebar--open' : ''}`}>
        <div className="sidebar-top">
          <Link className="brand" to="/" onClick={() => setMenuOpen(false)} aria-label="SAD Evolui, visão geral">
            <span className="brand-mark"><Activity size={20} strokeWidth={2.5} /></span>
            <span className="brand-copy">sad<span>evolui</span><small>DECISÕES COM SINAIS REAIS</small></span>
          </Link>
          <button className="icon-button sidebar-close" onClick={() => setMenuOpen(false)} aria-label="Fechar menu"><X size={19} /></button>
        </div>

        <div className="workspace-switcher">
          <div className="workspace-avatar">S</div>
          <div className="workspace-copy"><strong>SAD Evolui</strong><span>Ambiente local</span></div>
        </div>

        <nav className="primary-nav" aria-label="Navegação principal">
          <span className="nav-label">ESPAÇO DE TRABALHO</span>
          {navigation.map(({ label, icon: Icon, to, available }) => {
            const active = location.pathname === to
            if (!available) return <div key={label} className="nav-item nav-item--disabled" aria-disabled="true" title="Módulo ainda não disponível"><Icon size={18} strokeWidth={1.8} /><span>{label}</span><span className="nav-coming-soon">Em breve</span></div>
            return (
              <Link key={label} to={to} className={`nav-item${active ? ' nav-item--active' : ''}`} aria-current={active ? 'page' : undefined} onClick={() => setMenuOpen(false)}>
                <Icon size={18} strokeWidth={1.8} /><span>{label}</span>
              </Link>
            )
          })}
        </nav>

        <div className="sidebar-bottom">
          <div className="insight-callout">
            <span className="callout-icon"><Sparkles size={16} /></span>
            <strong>Insights em foco</strong>
            <p>Os sinais dos usuários ajudam a encontrar o próximo passo.</p>
            <div className="callout-sparkline" aria-hidden="true"><span /><span /><span /><span /><span /><span /><span /></div>
          </div>
          <div className="account-row"><span className="user-avatar">{user?.name.slice(0, 1).toUpperCase()}</span><div className="workspace-copy"><strong>{user?.name}</strong><span>{user?.email}</span></div><button className="icon-button logout-button" onClick={logout} aria-label="Sair"><LogOut size={16} /></button></div>
        </div>
      </aside>

      <div className="main-column">
        <header className="topbar">
          <div className="topbar-left">
            <button className="icon-button mobile-menu" onClick={() => setMenuOpen(true)} aria-label="Abrir menu"><Menu size={20} /></button>
            <div className="breadcrumb"><span>Espaço de trabalho</span><span className="breadcrumb-divider">/</span><strong>{currentPage}</strong></div>
          </div>
        </header>
        <main className="page-content"><Outlet /></main>
        <footer className="page-footer"><span>© 2026 SAD Evolui</span><span>Decisões melhores começam com escuta.</span></footer>
      </div>
    </div>
  )
}

