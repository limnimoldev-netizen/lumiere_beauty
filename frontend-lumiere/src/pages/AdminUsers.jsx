import { useEffect, useMemo, useState } from 'react'
import { UserPlus, Search, UserRound, Ban, Trash2 } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { userApi } from '../services/api'

const adminActionUnavailable = 'This action needs a protected backend admin endpoint.'

export default function AdminUsers() {
  const { user } = useAuth()
  const isAdmin = user?.role?.toUpperCase() === 'ADMIN'
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(isAdmin)
  const [error, setError] = useState('')
  const [retry, setRetry] = useState(0)
  const [query, setQuery] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', email: '', role: 'ADMIN' })

  useEffect(() => {
    if (!isAdmin) {
      setUsers([])
      setError('Administrator access is required to view user accounts.')
      setLoading(false)
      return
    }

    let cancelled = false
    const loadUsers = async () => {
      setLoading(true)
      setError('')
      try {
        const data = await userApi.getAll()
        if (!Array.isArray(data)) throw new Error('Unexpected users response')
        if (!cancelled) {
          setUsers(data.map(({ id, name, email, role }) => ({ id, name, email, role })))
        }
      } catch (err) {
        if (!cancelled) {
          setUsers([])
          setError(err.response?.status === 401 || err.response?.status === 403
            ? 'Your login cannot access the protected users API. Backend admin authentication is required.'
            : 'Unable to load user accounts. Check the backend connection and try again.')
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    loadUsers()
    return () => { cancelled = true }
  }, [isAdmin, retry])

  const filtered = useMemo(() => users.filter(account =>
    `${account.id} ${account.name} ${account.email} ${account.role}`.toLowerCase().includes(query.trim().toLowerCase())
  ), [users, query])
  const count = loading || error ? '–' : users.length
  const adminCount = loading || error ? '–' : users.filter(account => account.role?.toUpperCase() === 'ADMIN').length

  return <div>
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div><p className="eyebrow text-sage">Store access</p><h1 className="mt-2 font-display text-4xl text-wine">User management</h1><p className="mt-2 text-sm text-muted">Add administrators and manage account roles and access status.</p></div>
      <button onClick={() => setShowForm(value => !value)} className="btn-primary"><UserPlus size={16}/> Add user</button>
    </div>

    <div className="mt-7 grid gap-4 sm:grid-cols-3">
      <div className="rounded-2xl border border-line bg-white p-5"><p className="text-xs text-muted">Total accounts</p><p className="mt-2 font-display text-3xl text-wine">{count}</p></div>
      <div className="rounded-2xl border border-line bg-white p-5"><p className="text-xs text-muted">Administrators</p><p className="mt-2 font-display text-3xl text-wine">{adminCount}</p></div>
      <div className="rounded-2xl border border-line bg-white p-5"><p className="text-xs text-muted">Active accounts</p><p className="mt-2 font-display text-3xl text-wine">–</p></div>
    </div>

    {showForm && <form onSubmit={event => event.preventDefault()} className="mt-6 rounded-2xl border border-line bg-white p-6">
      <div className="flex items-center justify-between"><div><h2 className="font-display text-2xl text-wine">Add an account</h2><p className="mt-1 text-xs text-muted">Account creation needs a protected backend admin endpoint.</p></div><button type="button" onClick={() => setShowForm(false)} className="text-sm text-muted">Close</button></div>
      <div className="mt-5 grid gap-4 md:grid-cols-3">
        <label className="text-xs font-semibold text-ink">Full name<input required className="field mt-2 text-sm" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="e.g. Alex Morgan"/></label>
        <label className="text-xs font-semibold text-ink">Email address<input required type="email" className="field mt-2 text-sm" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} placeholder="name@example.com"/></label>
        <label className="text-xs font-semibold text-ink">Role<select className="field mt-2 text-sm" value={form.role} onChange={e => setForm({ ...form, role: e.target.value })}><option value="ADMIN">Admin</option><option value="USER">Customer</option></select></label>
      </div>
      <div className="mt-5 flex flex-wrap gap-3"><button disabled title={adminActionUnavailable} className="btn-primary disabled:cursor-not-allowed disabled:opacity-50">Save account</button><button type="button" onClick={() => setShowForm(false)} className="btn-outline">Cancel</button></div>
    </form>}

    {error && <div role="alert" className="mt-4 rounded-xl border border-[#E8DED4] bg-[#F5F3E9] p-4 text-sm text-wine">{error}{isAdmin && <button onClick={() => setRetry(value => value + 1)} className="ml-3 font-semibold underline">Retry</button>}</div>}

    <div className="mt-7">
      <label className="relative block w-full max-w-xl">
        <Search size={18} className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-muted"/>
        <input className="field !h-12 !min-h-12 !rounded-xl !pl-11 !pr-4 text-sm leading-normal" placeholder="Search name, email or role" value={query} onChange={e => setQuery(e.target.value)}/>
      </label>
    </div>
    <div className="mt-5 overflow-hidden rounded-2xl border border-line bg-white">
      <div className="hidden grid-cols-[minmax(180px,1.4fr)_minmax(190px,1.5fr)_minmax(125px,1fr)_minmax(135px,1fr)_112px] items-center gap-4 bg-cream px-5 py-4 text-[11px] font-bold uppercase tracking-wider text-muted md:grid"><span>User</span><span>Email</span><span>Role</span><span>Status</span><span className="text-center">Actions</span></div>
      {!loading && !error && filtered.map(account => <div key={account.id} className="grid gap-4 border-t border-line p-4 md:grid-cols-[minmax(180px,1.4fr)_minmax(190px,1.5fr)_minmax(125px,1fr)_minmax(135px,1fr)_112px] md:items-center md:gap-4 md:px-5 md:py-5">
        <div className="flex items-center gap-3"><span className="grid h-10 w-10 place-items-center rounded-full bg-blush text-rose"><UserRound size={18}/></span><div><p className="text-sm font-semibold text-ink">{account.name}</p><p className="text-[11px] text-muted">Account #{account.id}</p></div></div>
        <span className="break-all text-xs text-muted">{account.email}</span>
        <label className="text-[10px] text-muted md:text-xs">Role<select disabled title={adminActionUnavailable} aria-label={`Role for ${account.name}`} className="field mt-1 py-2 text-xs" value={account.role}><option value="USER">Customer</option><option value="ADMIN">Admin</option>{!['USER', 'ADMIN'].includes(account.role) && <option value={account.role}>{account.role}</option>}</select></label>
        <label className="text-[10px] text-muted md:text-xs">Status<select disabled title="Account status is not provided by the backend." aria-label={`Status for ${account.name}`} className="field mt-1 py-2 text-xs" value="unavailable"><option value="unavailable">Unavailable</option></select></label>
        <div className="flex items-center justify-start gap-2 md:justify-center">
          <button disabled title="Account status is not supported by the backend." aria-label={`Suspend ${account.name}`} className="grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-cream text-sage disabled:cursor-not-allowed disabled:opacity-50"><Ban size={17}/></button>
          <button disabled title={adminActionUnavailable} aria-label={`Remove ${account.name}`} className="grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-cream text-rose disabled:cursor-not-allowed disabled:opacity-50"><Trash2 size={17}/></button>
        </div>
      </div>)}
      {loading && <p role="status" className="p-10 text-center text-sm text-muted">Loading accounts…</p>}
      {!loading && !error && filtered.length === 0 && <p className="p-10 text-center text-sm text-muted">{users.length === 0 ? 'No accounts found.' : 'No matching accounts.'}</p>}
    </div>
    <p className="mt-4 rounded-xl border border-[#E8DED4] bg-[#F5F3E9] p-4 text-xs leading-6 text-muted"><strong className="text-wine">Backend limitation:</strong> Viewing accounts requires server-issued admin authentication. Account status and protected admin actions are not available yet.</p>
  </div>
}
