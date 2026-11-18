import { useEffect, useMemo, useState } from 'react'
import { UserPlus, Search, UserRound, Ban, CircleCheck, Trash2 } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { userApi } from '../services/api'

export default function AdminUsers() {
  const { user, logout } = useAuth()
  const isAdmin = user?.role?.toUpperCase() === 'ADMIN'
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(isAdmin)
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')
  const [retry, setRetry] = useState(0)
  const [query, setQuery] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'CUSTOMER' })
  const [saving, setSaving] = useState(false)
  const [changingId, setChangingId] = useState(null)
  const [changingStatusId, setChangingStatusId] = useState(null)
  const [deletingId, setDeletingId] = useState(null)
  const isOwnAccount = account => String(account.id) === String(user?.id) || account.email?.toLowerCase() === user?.email?.toLowerCase()

  const actionFailure = (err, fallback) => {
    if (err.response?.status === 401) {
      logout()
    } else {
      setActionError(err.response?.status === 403 ? 'The server refused this change. You may not change your own account; check backend CORS and admin permissions if it affects another account.'
        : err.response?.status === 409 ? 'That email address is already registered.'
        : [404, 405].includes(err.response?.status) ? 'The backend is running an older version. Restart Spring Boot and try again.'
        : fallback)
    }
  }

  const addUser = async event => {
    event.preventDefault()
    setSaving(true)
    setActionError('')
    try {
      const created = await userApi.create(form)
      setUsers(current => [...current, { id: created.id, name: created.name, email: created.email, role: created.role, active: created.active }])
      setForm({ name: '', email: '', password: '', role: 'CUSTOMER' })
      setShowForm(false)
    } catch (err) {
      actionFailure(err, 'Unable to add this account. Check the fields and try again.')
    } finally {
      setSaving(false)
    }
  }

  const changeRole = async (account, role) => {
    setChangingId(account.id)
    setActionError('')
    try {
      const updated = await userApi.changeRole(account.id, role)
      setUsers(current => current.map(item => item.id === account.id ? { ...item, role: updated.role } : item))
    } catch (err) {
      actionFailure(err, 'Unable to change this role. Please try again.')
    } finally {
      setChangingId(null)
    }
  }

  const changeStatus = async (account, active) => {
    setChangingStatusId(account.id)
    setActionError('')
    try {
      const updated = await userApi.changeStatus(account.id, active)
      setUsers(current => current.map(item => item.id === account.id ? { ...item, active: updated.active } : item))
    } catch (err) {
      actionFailure(err, 'Unable to change this account status. Please try again.')
    } finally {
      setChangingStatusId(null)
    }
  }

  const removeUser = async account => {
    if (!window.confirm(`Delete ${account.name}'s account? This cannot be undone.`)) return
    setDeletingId(account.id)
    setActionError('')
    try {
      await userApi.remove(account.id)
      setUsers(current => current.filter(item => item.id !== account.id))
    } catch (err) {
      actionFailure(err, 'Unable to delete this account. Please try again.')
    } finally {
      setDeletingId(null)
    }
  }

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
          setUsers(data.map(({ id, name, email, role, active }) => ({ id, name, email, role: role?.toUpperCase() === 'USER' ? 'CUSTOMER' : role?.toUpperCase(), active: active !== false })))
        }
      } catch (err) {
        if (!cancelled) {
          setUsers([])
          if (err.response?.status === 401) {
            logout()
            setError('Your session expired. Please sign in again.')
          } else setError(err.response?.status === 403
            ? 'Administrator access is required to view user accounts.'
            : 'Unable to load user accounts. Check the backend connection and try again.')
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    loadUsers()
    return () => { cancelled = true }
  }, [isAdmin, retry, logout])

  const filtered = useMemo(() => users.filter(account =>
    `${account.id} ${account.name} ${account.email} ${account.role}`.toLowerCase().includes(query.trim().toLowerCase())
  ), [users, query])
  const count = loading || error ? '–' : users.length
  const adminCount = loading || error ? '–' : users.filter(account => account.role?.toUpperCase() === 'ADMIN').length
  const activeCount = loading || error ? '–' : users.filter(account => account.active).length

  return <div>
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div><p className="eyebrow text-sage">Store access</p><h1 className="mt-2 font-display text-4xl text-wine">User management</h1><p className="mt-2 text-sm text-muted">View accounts and manage access.</p></div>
      <button onClick={() => setShowForm(value => !value)} className="btn-primary"><UserPlus size={16}/> Add user</button>
    </div>

    <div className="mt-7 grid gap-4 sm:grid-cols-3">
      <div className="rounded-2xl border border-line bg-white p-5"><p className="text-xs text-muted">Total accounts</p><p className="mt-2 font-display text-3xl text-wine">{count}</p></div>
      <div className="rounded-2xl border border-line bg-white p-5"><p className="text-xs text-muted">Administrators</p><p className="mt-2 font-display text-3xl text-wine">{adminCount}</p></div>
      <div className="rounded-2xl border border-line bg-white p-5"><p className="text-xs text-muted">Active accounts</p><p className="mt-2 font-display text-3xl text-wine">{activeCount}</p></div>
    </div>

    {showForm && <form onSubmit={addUser} className="mt-6 rounded-2xl border border-line bg-white p-6">
      <div className="flex items-center justify-between"><div><h2 className="font-display text-2xl text-wine">Add an account</h2><p className="mt-1 text-xs text-muted">The new account can sign in with the password you set.</p></div><button type="button" onClick={() => setShowForm(false)} className="text-sm text-muted">Close</button></div>
      <div className="mt-5 grid gap-4 md:grid-cols-3">
        <label className="text-xs font-semibold text-ink">Full name<input required className="field mt-2 text-sm" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="e.g. Alex Morgan"/></label>
        <label className="text-xs font-semibold text-ink">Email address<input required type="email" className="field mt-2 text-sm" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} placeholder="name@example.com"/></label>
        <label className="text-xs font-semibold text-ink">Role<select className="field mt-2 text-sm" value={form.role} onChange={e => setForm({ ...form, role: e.target.value })}><option value="ADMIN">Admin</option><option value="CUSTOMER">Customer</option></select></label>
        <label className="text-xs font-semibold text-ink">Password<input required minLength={8} type="password" autoComplete="new-password" className="field mt-2 text-sm" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} placeholder="At least 8 characters"/></label>
      </div>
      <div className="mt-5 flex flex-wrap gap-3"><button disabled={saving} className="btn-primary disabled:cursor-not-allowed disabled:opacity-50">{saving ? 'Saving…' : 'Save account'}</button><button type="button" onClick={() => setShowForm(false)} className="btn-outline">Cancel</button></div>
    </form>}

    {error && <div role="alert" className="mt-4 rounded-xl border border-[#E8DED4] bg-[#F5F3E9] p-4 text-sm text-wine">{error}{isAdmin && <button onClick={() => setRetry(value => value + 1)} className="ml-3 font-semibold underline">Retry</button>}</div>}
    {actionError && <div role="alert" className="mt-4 rounded-xl border border-[#E8DED4] bg-[#F5F3E9] p-4 text-sm text-wine">{actionError}</div>}

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
        <label className="text-[10px] text-muted md:text-xs">Role<select disabled={changingId !== null || isOwnAccount(account)} title={isOwnAccount(account) ? 'You cannot change your own role.' : 'Change role'} aria-label={`Role for ${account.name}`} className="field mt-1 py-2 text-xs" value={account.role} onChange={event => changeRole(account, event.target.value)}><option value="CUSTOMER">Customer</option><option value="ADMIN">Admin</option>{!['CUSTOMER', 'ADMIN'].includes(account.role) && <option value={account.role}>{account.role}</option>}</select></label>
        <label className="text-[10px] text-muted md:text-xs">Status<select disabled={changingStatusId !== null || isOwnAccount(account)} title={isOwnAccount(account) ? 'You cannot change your own status.' : 'Change status'} aria-label={`Status for ${account.name}`} className="field mt-1 py-2 text-xs" value={account.active ? 'active' : 'disabled'} onChange={event => changeStatus(account, event.target.value === 'active')}><option value="active">Active</option><option value="disabled">Disabled</option></select></label>
        <div className="flex items-center justify-start gap-2 md:justify-center">
          <button type="button" disabled={changingStatusId !== null || isOwnAccount(account)} onClick={() => changeStatus(account, !account.active)} title={isOwnAccount(account) ? 'You cannot change your own status.' : account.active ? 'Disable account' : 'Activate account'} aria-label={`${account.active ? 'Disable' : 'Activate'} ${account.name}`} className="grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-cream text-sage disabled:cursor-not-allowed disabled:opacity-50">{account.active ? <Ban size={17}/> : <CircleCheck size={17}/>}</button>
          <button type="button" disabled={deletingId !== null || isOwnAccount(account)} onClick={() => removeUser(account)} title={isOwnAccount(account) ? 'You cannot delete your own account.' : 'Delete account'} aria-label={`Remove ${account.name}`} className="grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-cream text-rose disabled:cursor-not-allowed disabled:opacity-50"><Trash2 size={17}/></button>
        </div>
      </div>)}
      {loading && <p role="status" className="p-10 text-center text-sm text-muted">Loading accounts…</p>}
      {!loading && !error && filtered.length === 0 && <p className="p-10 text-center text-sm text-muted">{users.length === 0 ? 'No accounts found.' : 'No matching accounts.'}</p>}
    </div>
  </div>
}
