import { useState, type FormEvent } from 'react'
import { Activity, ArrowRight, LockKeyhole, Mail, UserRound } from 'lucide-react'
import { Link, useNavigate } from 'react-router'
import { login, register } from '../../services/auth'
import { useAuth } from '../../app/providers/useAuth'
import { getErrorMessage } from '../../utils/errors'

export function AuthPage({ register: isRegister = false }: { register?: boolean }) {
  const navigate = useNavigate()
  const { accept } = useAuth()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [pending, setPending] = useState(false)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setPending(true)
    try {
      const response = isRegister
        ? await register({ name, email, password })
        : await login({ email, password })
      accept(response)
      navigate('/', { replace: true })
    } catch (cause) {
      setError(getErrorMessage(cause))
    } finally {
      setPending(false)
    }
  }

  return <main className="auth-page">
    <section className="auth-card">
      <Link className="brand auth-brand" to="/" aria-label="SAD Evolui"><span className="brand-mark"><Activity size={20} strokeWidth={2.5} /></span><span className="brand-copy">sad<span>evolui</span><small>DECISÕES COM SINAIS REAIS</small></span></Link>
      <div className="auth-heading"><span className="heading-overline">ESPAÇO DE TRABALHO</span><h1>{isRegister ? 'Crie sua conta' : 'Acesse sua conta'}</h1><p>{isRegister ? 'Comece organizando feedbacks e decisões em um só lugar.' : 'Entre para acompanhar seus projetos e avaliações.'}</p></div>
      <form className="auth-form" onSubmit={submit}>
        {isRegister && <label className="field"><span>Nome</span><div className="auth-input"><UserRound size={17} /><input autoComplete="name" maxLength={120} required value={name} onChange={(event) => setName(event.target.value)} placeholder="Seu nome" /></div></label>}
        <label className="field"><span>E-mail</span><div className="auth-input"><Mail size={17} /><input type="email" autoComplete="email" maxLength={254} required value={email} onChange={(event) => setEmail(event.target.value)} placeholder="voce@empresa.com" /></div></label>
        <label className="field"><span>Senha</span><div className="auth-input"><LockKeyhole size={17} /><input type="password" autoComplete={isRegister ? 'new-password' : 'current-password'} minLength={isRegister ? 10 : undefined} maxLength={72} required value={password} onChange={(event) => setPassword(event.target.value)} placeholder={isRegister ? 'Mínimo de 10 caracteres' : 'Sua senha'} /></div></label>
        {error && <p className="form-error" role="alert">{error}</p>}
        <button className="button button--primary auth-submit" disabled={pending}>{pending ? 'Aguarde…' : isRegister ? 'Criar conta' : 'Entrar'} <ArrowRight size={16} /></button>
      </form>
      <p className="auth-switch">{isRegister ? 'Já tem uma conta?' : 'Ainda não tem conta?'} <Link to={isRegister ? '/login' : '/register'}>{isRegister ? 'Entrar' : 'Criar conta'}</Link></p>
    </section>
  </main>
}
