import { useEffect, useState, type ReactNode } from "react"
import Clinical from "@/Clinical"
import {
  MedSentinelApiError,
  getAccessCapabilities,
  medsentinelApi,
  type AccessCapabilities,
  type AlertStatus,
  type AlertViewModel,
  type AdminUserViewModel,
  type BaselineViewModel,
  type IntegrityViewModel,
  type StatsViewModel,
} from "@/lib/medsentinelApi"
import {
  Activity,
  AlertTriangle,
  ArrowLeft,
  Bell,
  Check,
  CheckCircle2,
  ChevronDown,
  CircleUserRound,
  Clock3,
  FileSearch,
  FlaskConical,
  HeartPulse,
  LayoutDashboard,
  LockKeyhole,
  LogOut,
  Menu,
  MoreHorizontal,
  Search,
  ShieldCheck,
  ShieldPlus,
  SlidersHorizontal,
  UserRound,
  UsersRound,
  X,
  Zap,
} from "lucide-react"

type View = "dashboard" | "alert" | "logs" | "clinician" | "simulator" | "baselines" | "users"
type Severity = AlertViewModel["severity"]

interface MedSentinelDashboardProps {
  onLogout: () => void
  initialView?: View
  initialRole?: string
  initialUser?: string
  initialUsername?: string
  initialPermissions?: string[]
}

function formatToday() {
  return new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(new Date())
}

function departmentForAlert(role: string) {
  const normalized = role.toLowerCase()
  if (normalized.includes("billing")) return "Patient Accounts"
  if (normalized.includes("reception")) return "Front Desk"
  if (normalized.includes("physician") || normalized.includes("nurse")) return "Clinical Operations"
  return "Security Operations"
}

function SeverityPill({ level }: { level: Severity }) {
  return (
    <span className={`severity severity-${level.toLowerCase()}`}>
      <span className="severity-dot" />
      {level}
    </span>
  )
}

function RuleChip({ children }: { children: ReactNode }) {
  return <span className="rule-chip">{children}</span>
}

function Header({
  title,
  eyebrow,
  userName,
  userRole,
  onMenu,
  onOpenAlerts,
  onLogout,
  canViewAlerts,
}: {
  title: string
  eyebrow: string
  userName: string
  userRole: string
  onMenu: () => void
  onOpenAlerts: () => void
  onLogout: () => void
  canViewAlerts: boolean
}) {
  const initials = userName
    .split(" ")
    .map((w) => w[0])
    .join("")
    .slice(0, 2)
    .toUpperCase()

  return (
    <header className="topbar">
      <div className="topbar-title">
        <button className="mobile-menu" onClick={onMenu} aria-label="Open menu">
          <Menu size={20} />
        </button>
        <div>
          <span className="eyebrow">{eyebrow}</span>
          <h1>{title}</h1>
        </div>
      </div>
      <div className="topbar-actions">
        <div className="hidden sm:flex items-center gap-1.5 px-3 py-1 rounded-full border border-teal-500/30 bg-teal-500/10 text-[10px] font-mono text-[#5eead4]">
          <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-pulse" />
           <span>MEDSENTINEL CORE · API SESSION</span>
        </div>
        {canViewAlerts && <button className="icon-button" aria-label="Notifications" title="3 unread security alerts" onClick={onOpenAlerts}>
          <Bell size={19} />
          <span className="notification-dot" />
        </button>}
        <div className="divider" />
        <div className="user-meta">
          <strong>{userName}</strong>
          <span>{userRole}</span>
        </div>
        <div className="avatar" title={userName}>
          {initials || "MC"}
        </div>
        <button
          onClick={onLogout}
          className="icon-button"
          aria-label="Sign out"
          title="Sign out to login page"
          style={{ marginLeft: 8 }}
        >
          <LogOut size={16} />
        </button>
      </div>
    </header>
  )
}

function Sidebar({
  view,
  setView,
  open,
  close,
  onLogout,
  capabilities,
}: {
  view: View
  setView: (view: View) => void
  open: boolean
  close: () => void
  onLogout: () => void
  capabilities: AccessCapabilities
}) {
  const nav = [
    { id: "dashboard" as View, label: "Overview", icon: LayoutDashboard, visible: capabilities.canViewStats },
    { id: "alert" as View, label: "Alerts", icon: AlertTriangle, badge: "4", visible: capabilities.canViewAlerts },
    { id: "logs" as View, label: "Log explorer", icon: FileSearch, visible: capabilities.canViewLogs },
    { id: "clinician" as View, label: "Clinical access", icon: UsersRound, visible: capabilities.canUseClinicalWorkspace },
  ].filter((item) => item.visible)

  return (
    <aside className={`sidebar ${open ? "sidebar-open" : ""}`}>
      <div className="brand">
        <div className="brand-mark">
          <ShieldPlus size={24} strokeWidth={2.25} />
        </div>
        <div>
          <strong>
            Karuna<i className="brand-dot">.</i>
          </strong>
          <span>ACCESS INTELLIGENCE</span>
        </div>
        <button className="sidebar-close" onClick={close} aria-label="Close menu">
          <X size={20} />
        </button>
      </div>
      <nav>
        <span className="nav-label">Monitor</span>
        {nav.map((item) => (
          <button
            key={item.id}
            className={`nav-item ${view === item.id ? "active" : ""}`}
            onClick={() => {
              setView(item.id)
              close()
            }}
          >
            <item.icon size={18} />
            <span>{item.label}</span>
            {item.badge && <b>{item.badge}</b>}
          </button>
        ))}
        {capabilities.canAdmin && <span className="nav-label nav-label-spaced">Tools</span>}
        {capabilities.canAdmin && <button
          className={`nav-item ${view === "simulator" ? "active" : ""}`}
          onClick={() => {
            setView("simulator")
            close()
          }}
        >
          <FlaskConical size={18} />
          <span>Scenario simulator</span>
        </button>}
        {capabilities.canAdmin && <button
          className={`nav-item ${view === "baselines" ? "active" : ""}`}
          onClick={() => {
            setView("baselines")
            close()
          }}
        >
          <SlidersHorizontal size={18} />
          <span>Baselines</span>
        </button>}
        {capabilities.canAdmin && <button
          className={`nav-item ${view === "users" ? "active" : ""}`}
          onClick={() => {
            setView("users")
            close()
          }}
        >
          <UserRound size={18} />
          <span>Users & roles</span>
        </button>}
      </nav>
      <div className="sidebar-status">
        <div className="status-row">
          <span className="live-dot" />
           Backend session active
        </div>
        <div className="integrity-row">
          <ShieldCheck size={16} />
           <span>{capabilities.canViewLogs ? "Integrity check available" : "Role-scoped access"}</span>
           <Check size={15} />
         </div>
         <small>{capabilities.canViewLogs ? "Verify in Log explorer" : "Only permitted workspaces are shown"}</small>
      </div>
      <button className="signout" onClick={onLogout}>
        <LogOut size={17} />
        Sign out
      </button>
    </aside>
  )
}

function KPICard({
  label,
  value,
  hint,
  icon,
  tone = "",
}: {
  label: string
  value: ReactNode
  hint: ReactNode
  icon: ReactNode
  tone?: string
}) {
  return (
    <div className={`kpi-card ${tone}`}>
      <div className="kpi-top">
        <span>{label}</span>
        <div className="kpi-icon">{icon}</div>
      </div>
      <div className="kpi-value">{value}</div>
      <div className="kpi-hint">{hint}</div>
    </div>
  )
}

function Dashboard({ openAlert, onOpenAlerts, userName }: { openAlert: (id?: string) => void; onOpenAlerts: () => void; userName: string }) {
  const [severityFilter, setSeverityFilter] = useState("All")
  const [alerts, setAlerts] = useState<AlertViewModel[]>([])
  const [stats, setStats] = useState<StatsViewModel | null>(null)
  const [integrity, setIntegrity] = useState<IntegrityViewModel | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState("")
  const [dateSnapshot] = useState(() => ({
    date: formatToday(),
    time: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
  }))

  useEffect(() => {
    let cancelled = false
    setIsLoading(true)
    setErrorMessage("")
    void Promise.all([medsentinelApi.listAlerts(), medsentinelApi.getStats(), medsentinelApi.verifyIntegrity()])
      .then(([nextAlerts, nextStats, nextIntegrity]) => {
        if (cancelled) return
        setAlerts(nextAlerts)
        setStats(nextStats)
        setIntegrity(nextIntegrity)
      })
      .catch((error: unknown) => {
        if (!cancelled) setErrorMessage(error instanceof Error ? error.message : "Unable to load live dashboard data.")
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const highCount = alerts.filter((alert) => alert.severity === "High").length
  const mediumCount = alerts.filter((alert) => alert.severity === "Medium").length
  const lowCount = alerts.filter((alert) => alert.severity === "Low").length
  const openAlertCount = stats?.openAlerts ?? (errorMessage ? "—" : alerts.length)
  const highSeverityCount = stats?.highSeverity ?? (errorMessage ? "—" : highCount)
  const failedLoginCount = stats?.failedLogins ?? (errorMessage ? "—" : "…")

  return (
    <main className="page dashboard-page">
      <section className="welcome">
        <div>
           <h2>Good morning, {userName}</h2>
           <p>Here’s what needs your attention across St. Anne’s Hospital.</p>
         </div>
         <div className="date-chip">
           <Clock3 size={16} />
           {dateSnapshot.date} <span>·</span> {dateSnapshot.time}
         </div>
      </section>

      {errorMessage && <div className="baseline-note" role="status">Live dashboard data is unavailable: {errorMessage}</div>}

      <section className="kpi-grid">
        <KPICard
          label="Open alerts"
          value={isLoading ? "…" : openAlertCount}
          hint={
            <>
            {isLoading ? "Loading from backend" : "Current backend count"}
            </>
          }
          icon={<Bell size={19} />}
        />
        <KPICard
          label="High severity"
          value={isLoading ? "…" : highSeverityCount}
          hint={errorMessage ? "Backend count unavailable" : "Requires immediate review"}
          icon={<AlertTriangle size={19} />}
          tone="danger"
        />
        <KPICard
          label="Failed logins · 24h"
          value={isLoading ? "…" : failedLoginCount}
          hint="Backend count · rolling 24 hours"
          icon={<LockKeyhole size={19} />}
        />
        <KPICard
          label="Log integrity"
          value={
            <span className="verified-value">
              <ShieldCheck size={21} />
              {isLoading ? "Checking" : integrity ? integrity.intact ? "Verified" : "Broken" : "Unavailable"}
            </span>
          }
          hint={integrity ? `${integrity.rowsChecked} checked · ${integrity.totalRows} total` : "Verify in Log explorer"}
          icon={<ShieldCheck size={19} />}
          tone={integrity?.intact ? "success" : integrity ? "danger" : ""}
        />
      </section>

      <section className="dashboard-grid">
        <div className="panel alert-panel">
          <div className="panel-header">
            <div>
              <h3>Open alerts</h3>
              <p>Events that differ from role and ward baselines</p>
            </div>
           <button className="btn btn-ghost" onClick={onOpenAlerts}>
             View all <span>→</span>
            </button>
          </div>
          <div className="filter-row">
            <button
              className={`filter ${severityFilter === "All" ? "active" : ""}`}
              onClick={() => setSeverityFilter("All")}
            >
               All <b>{alerts.length}</b>
            </button>
            <button
              className={`filter ${severityFilter === "High" ? "active" : ""}`}
              onClick={() => setSeverityFilter("High")}
            >
               High <b>{highCount}</b>
            </button>
            <button
              className={`filter ${severityFilter === "Medium" ? "active" : ""}`}
              onClick={() => setSeverityFilter("Medium")}
            >
               Medium <b>{mediumCount}</b>
            </button>
            <button
              className={`filter ${severityFilter === "Low" ? "active" : ""}`}
              onClick={() => setSeverityFilter("Low")}
            >
               Low <b>{lowCount}</b>
            </button>
            <button className="filter filter-date">
              <Clock3 size={14} />
              Last 24 hours
              <ChevronDown size={14} />
            </button>
          </div>
          <div className="alert-list">
             {isLoading ? (
               <div className="baseline-note">Loading live alerts…</div>
             ) : errorMessage ? (
               <div className="baseline-note">No live alerts are displayed until the backend is reachable.</div>
             ) : alerts.length === 0 ? (
               <div className="baseline-note">No open alerts were returned by the backend.</div>
             ) : alerts
               .filter((alert) => severityFilter === "All" || alert.severity === severityFilter)
               .map((alert) => (
                <button key={alert.alertId} className="alert-row" onClick={() => openAlert(alert.alertId)}>
                  <div className={`alert-accent ${alert.severity.toLowerCase()}`} />
                  <div className="alert-main">
                    <div className="alert-row-top">
                      <SeverityPill level={alert.severity} />
                      <span className="alert-id">{alert.alertId}</span>
                       <span className="alert-id">{alert.status}</span>
                      <span className="alert-time">{alert.timeAgo}</span>
                      <MoreHorizontal size={18} />
                    </div>
                    <p>{alert.text}</p>
                    <div className="alert-footer">
                      <span className="person">
                        <CircleUserRound size={15} />
                        {alert.user}
                        <small>{alert.role}</small>
                      </span>
                      <span className="rule-list">
                        {alert.rules.map((rule) => (
                          <RuleChip key={rule}>{rule}</RuleChip>
             ))}
                      </span>
                      <span className="event-count">×{alert.count} events</span>
                    </div>
                  </div>
                </button>
              ))}
          </div>
        </div>

        <aside className="right-column">
           <div className="panel activity-panel">
             <div className="panel-header compact">
               <div>
                 <h3>Access activity</h3>
                 <p>Aggregate counts are available from the backend</p>
               </div>
               <RuleChip>Stats API</RuleChip>
             </div>
             <div className="baseline-note">Hourly activity data is outside the current API contract, so no illustrative chart is shown.</div>
           </div>
           <div className="panel risk-panel">
             <div className="panel-header compact">
               <div>
                 <h3>Top-risk users</h3>
                 <p>User risk ranking is not returned by the API</p>
               </div>
               <button className="btn btn-ghost" onClick={onOpenAlerts}>View alerts</button>
             </div>
             <div className="baseline-note">Review live alert context and backend logs for actor-level evidence.</div>
           </div>
           <div className="suppressed-note">
             <CheckCircle2 size={18} />
             <div>
               <strong>Backend baseline context</strong>
               <span>Hourly activity and user-risk rankings are outside the current API response.</span>
             </div>
           </div>
        </aside>
      </section>
    </main>
  )
}

function AlertDetail({ alertId = "A-101", back, canReviewActions }: { alertId?: string; back: () => void; canReviewActions: boolean }) {
  const [detail, setDetail] = useState<Awaited<ReturnType<typeof medsentinelApi.getAlert>> | null>(null)
  const [status, updateStatus] = useState<AlertStatus>("Open")
  const [confirm, setConfirm] = useState("")
  const [note, setNote] = useState("")
  const [error, setError] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState("")
  const [actionError, setActionError] = useState("")
  const [actionLoading, setActionLoading] = useState(false)

  useEffect(() => {
    let cancelled = false
    setIsLoading(true)
    setErrorMessage("")
    const loadAlert = async () => {
      try {
        return await medsentinelApi.getAlert(alertId)
      } catch (error: unknown) {
        if (!(error instanceof MedSentinelApiError) || error.status !== 404 || alertId !== "A-101") throw error
        const alerts = await medsentinelApi.listAlerts()
        const firstAlert = alerts[0]
        if (!firstAlert) throw error
        return medsentinelApi.getAlert(firstAlert.alertId)
      }
    }
    void loadAlert()
      .then((nextDetail) => {
        if (cancelled) return
        setDetail(nextDetail)
        updateStatus(nextDetail.alert.status)
      })
      .catch((nextError: unknown) => {
        if (!cancelled) {
          setDetail(null)
          setErrorMessage(nextError instanceof Error ? nextError.message : "Unable to load the live alert.")
        }
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [alertId])

  const alert = detail?.alert

  if (isLoading) {
    return (
      <main className="page detail-page">
        <button className="back-link" onClick={back}>
          <ArrowLeft size={17} />
          Back to alerts
        </button>
        <section className="panel p-6">
          <h2>Loading live alert…</h2>
          <p className="muted">Fetching alert evidence from the MedSentinel backend.</p>
        </section>
      </main>
    )
  }

  if (!alert || !detail) {
    return (
      <main className="page detail-page">
        <button className="back-link" onClick={back}>
          <ArrowLeft size={17} />
          Back to alerts
        </button>
        <section className="panel p-6">
          <h2>Live alert unavailable</h2>
          <p className="muted">{errorMessage || "This alert was not returned by the backend."}</p>
        </section>
      </main>
    )
  }

  const evidenceRows: string[][] = detail.evidenceLogs.length > 0
    ? detail.evidenceLogs.slice(0, 4).map((log) => [
        `#${log.logId}`,
        log.ts,
        log.action,
        log.patientRef || "—",
        log.outcome,
        log.hash ? `${log.hash.slice(0, 4)}…${log.hash.slice(-4)}` : "—",
      ])
    : [["—", "—", "No evidence", "—", "—", "—"]]

  const statusAction = (value: AlertStatus) => {
    switch (value) {
      case "Acknowledged":
        return "acknowledge"
      case "Escalated":
        return "escalate"
      case "False positive":
        return "false_positive"
      default:
        return "open"
    }
  }

  const setStatus = async (value: AlertStatus, reason = "Status updated", actionNote = "") => {
    setActionLoading(true)
    setActionError("")
    try {
      const updated = await medsentinelApi.updateAlertAction(alert.numericAlertId, statusAction(value), actionNote)
      updateStatus(updated.status)
      setDetail((current) => current ? { ...current, alert: updated } : current)
      return true
    } catch (nextError: unknown) {
      setActionError(nextError instanceof Error ? nextError.message : `Unable to apply ${reason.toLowerCase()}.`)
      return false
    } finally {
      setActionLoading(false)
    }
  }

  const handleConfirm = async () => {
    if (confirm === "Mark false positive" && !note.trim()) {
      setError(true)
      return
    }
    const targetStatus: AlertStatus = confirm === "Escalate alert" ? "Escalated" : "False positive"
    const updated = await setStatus(targetStatus, confirm, note)
    if (updated) {
      setConfirm("")
      setNote("")
      setError(false)
    }
  }

  return (
    <main className="page detail-page">
      <button className="back-link" onClick={back}>
        <ArrowLeft size={17} />
        Back to alerts
      </button>
      <div className="detail-title">
        <div>
          <div className="title-line">
            <SeverityPill level={alert.severity} />
            <span>{alert.alertId}</span>
            <span>Detected {alert.timeAgo}</span>
          </div>
          <h2>{alert.text.split(".")[0]}</h2>
        </div>
         {canReviewActions ? <button
           className="status-select"
           onClick={() => void setStatus(status === "Open" ? "Acknowledged" : "Open")}
         >
           <span className="live-dot" />
           {status}
           <ChevronDown size={15} />
         </button> : <span className="status-select"><span className="live-dot" />{status}</span>}
      </div>
      <section className="why-box">
        <div className="why-icon">
          <Zap size={21} />
        </div>
        <div>
          <span className="eyebrow">Why this was flagged</span>
          <p>{alert.text}</p>
          <small>
            Rules involved: <strong>{alert.rules.join(" · ")}</strong>. Review the linked evidence before deciding on a
            follow-up action.
          </small>
        </div>
      </section>
      <div className="detail-grid">
        <section className="panel breakdown">
          <div className="section-heading">
            <div>
              <h3>Score breakdown</h3>
              <p>Demonstration risk score · not a probability of wrongdoing</p>
            </div>
            <strong>
              {alert.score} <small>/ 100 · {alert.severity}</small>
            </strong>
          </div>
          <div className="score-bar">
            <span className="r2" style={{ width: `${alert.score}%` }} />
            <span className="empty" style={{ width: `${100 - alert.score}%` }} />
          </div>
          <div className="score-keys">
             {alert.rules.map((rule, index) => (
               <span key={rule}>
                <i className={index % 2 === 0 ? "r2-key" : "r3-key"} />
                <b>{rule}</b>
                <strong>+{Math.round(alert.score / alert.rules.length)}</strong>
              </span>
            ))}
          </div>
        </section>
        <section className="panel context-card">
          <div className="section-heading">
            <div>
              <h3>User context</h3>
              <p>{alert.user} · {alert.role}</p>
            </div>
            <div className="small-avatar">{alert.user.split(" ").map((part) => part[0]).join("").slice(0, 2).toUpperCase()}</div>
          </div>
          <dl>
            <div>
              <dt>Department</dt>
              <dd>{departmentForAlert(alert.role)}</dd>
            </div>
            <div>
              <dt>Shift</dt>
              <dd>Role schedule</dd>
            </div>
            <div>
              <dt>Clinical baseline</dt>
              <dd>{alert.role.toLowerCase().includes("billing") ? "0 records" : "Role-scoped"}</dd>
            </div>
          </dl>
        </section>
      </div>
      <section className="panel evidence">
        <div className="panel-header">
          <div>
            <h3>Evidence</h3>
            <p>{alert.count} access {alert.count === 1 ? "event" : "events"} linked to this alert</p>
          </div>
          <button
            className="btn btn-secondary"
            onClick={() => {
              const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(evidenceRows, null, 2))
              const a = document.createElement("a")
              a.href = dataStr
              a.download = `evidence-${alert.alertId}.json`
              a.click()
            }}
          >
            Export evidence
          </button>
        </div>
        <div className="evidence-table">
          <div className="evidence-head">
            <span>LOG ID</span>
            <span>TIME</span>
            <span>ACTION</span>
            <span>PATIENT</span>
            <span>OUTCOME</span>
            <span>HASH</span>
          </div>
          {evidenceRows.map((row) => (
            <div className="evidence-row" key={row[0]}>
              {row.map((cell, i) => (
                <span key={cell} className={i === 4 ? "outcome-ok" : ""}>
                  {i === 4 && <i />}
                  {cell}
                </span>
              ))}
            </div>
          ))}
        </div>
      </section>
       {canReviewActions && <div className="action-bar">
        <span>
          <ShieldCheck size={18} />
          All actions are logged with your reviewer ID
        </span>
        <div>
          <button
            className="btn btn-secondary"
            onClick={() => {
              setConfirm("Mark false positive")
              setError(false)
            }}
          >
            Mark false positive
          </button>
           <button className="btn btn-secondary" onClick={() => void setStatus("Acknowledged", "Alert acknowledged")} disabled={actionLoading}>
            Acknowledge
          </button>
          <button
            className="btn btn-danger"
            onClick={() => {
              setConfirm("Escalate alert")
              setError(false)
            }}
          >
            Escalate alert
          </button>
        </div>
       </div>}
      {confirm && (
        <div className="modal-backdrop">
          <section className="reviewer-dialog" role="dialog" aria-modal="true" aria-labelledby="review-title">
            <h3 id="review-title">{confirm}</h3>
             <p>{alertId} · Your reviewer action will be recorded by the backend.</p>
            <label>
              {confirm === "Mark false positive" ? "Explanation (required)" : "Reviewer note (optional)"}
              <textarea value={note} onChange={(e) => setNote(e.target.value)} />
            </label>
            {error && <p className="dialog-error">An explanation is required.</p>}
            <div>
              <button className="btn btn-secondary" onClick={() => setConfirm("")}>
                Cancel
              </button>
              <button
                className="btn btn-primary"
                 onClick={() => void handleConfirm()}
                 disabled={actionLoading}
               >
                 {actionLoading ? "Saving…" : "Confirm action"}
               </button>
             </div>
             {actionError && <p className="dialog-error">{actionError}</p>}
          </section>
        </div>
      )}
    </main>
  )
}

function LogExplorer({ canAdmin }: { canAdmin: boolean }) {
  const [integrity, setIntegrity] = useState<"idle" | "pass" | "fail">("idle")
  const [brokenId, setBrokenId] = useState<number | undefined>(undefined)
  const [integrityDetails, setIntegrityDetails] = useState<IntegrityViewModel | null>(null)
  const [logs, setLogs] = useState<Awaited<ReturnType<typeof medsentinelApi.listLogs>>>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isVerifying, setIsVerifying] = useState(false)
  const [errorMessage, setErrorMessage] = useState("")
  const [search, setSearch] = useState("")
  const [outcomeFilter, setOutcomeFilter] = useState("All")
  const [actionFilter, setActionFilter] = useState("All")
  const [timeRange, setTimeRange] = useState("24h")

  useEffect(() => {
    let cancelled = false
    setIsLoading(true)
    setErrorMessage("")
    void medsentinelApi
      .listLogs()
      .then((nextLogs) => {
        if (!cancelled) setLogs(nextLogs)
      })
      .catch((error: unknown) => {
        if (!cancelled) setErrorMessage(error instanceof Error ? error.message : "Unable to load live audit logs.")
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const filteredLogs = logs.filter((l) => {
    const q = search.toLowerCase().trim()
    const matchesSearch = !q || (
      l.username.toLowerCase().includes(q) ||
      (l.patientRef && l.patientRef.toLowerCase().includes(q)) ||
      l.action.toLowerCase().includes(q) ||
      l.reason.toLowerCase().includes(q)
    )
    const matchesOutcome = outcomeFilter === "All" || l.outcome === outcomeFilter
    const matchesAction = actionFilter === "All" || l.action === actionFilter
    return matchesSearch && matchesOutcome && matchesAction
  })

  const applyIntegrity = (result: IntegrityViewModel) => {
    setIntegrityDetails(result)
    setIntegrity(result.intact ? "pass" : "fail")
    setBrokenId(result.brokenAtLogId ?? undefined)
  }

  const handleVerify = async () => {
    setIsVerifying(true)
    setErrorMessage("")
    try {
      applyIntegrity(await medsentinelApi.verifyIntegrity())
    } catch (error: unknown) {
      setErrorMessage(error instanceof Error ? error.message : "Unable to verify log integrity.")
    } finally {
      setIsVerifying(false)
    }
  }

  const handleTamper = async () => {
    setIsVerifying(true)
    setErrorMessage("")
    try {
      await medsentinelApi.runSimulator("tamper-log")
      applyIntegrity(await medsentinelApi.verifyIntegrity())
    } catch (error: unknown) {
      setErrorMessage(error instanceof Error ? error.message : "Unable to run the backend tamper scenario.")
    } finally {
      setIsVerifying(false)
    }
  }

  const handleReset = () => {
    setIntegrity("idle")
    setIntegrityDetails(null)
    setBrokenId(undefined)
  }

  return (
    <main className="page logs-page">
      <section className="welcome">
        <div>
          <h2>Log explorer</h2>
          <p>Search and verify every access event across the organization.</p>
        </div>
        <button className="btn btn-primary" onClick={() => void handleVerify()} disabled={isVerifying}>
          <ShieldCheck size={17} />
          {isVerifying ? "Checking…" : "Verify integrity"}
        </button>
      </section>
      {errorMessage && <div className="baseline-note" role="status">Live log data is unavailable: {errorMessage}</div>}
      <div className="audit-demo-controls">
        <span>Append-only SHA-256 cryptographic chain validation.</span>
         {canAdmin && <button className="btn btn-secondary" onClick={() => void handleTamper()} disabled={isVerifying}>
           Run backend tamper scenario
         </button>}
        <button className="btn btn-ghost" onClick={handleReset}>
          Clear result
        </button>
      </div>
      {integrity !== "idle" && (
        <div className={`integrity-banner ${integrity}`}>
          <div>
            {integrity === "pass" ? <CheckCircle2 size={20} /> : <AlertTriangle size={20} />}
            <strong>
              {integrity === "pass"
                 ? `${integrityDetails?.rowsChecked ?? 0} rows checked. Chain intact.`
                 : `Chain broken at log #${brokenId ?? "unknown"}. Backend reported a hash mismatch.`}
            </strong>
          </div>
          <span>{integrity === "pass" ? "Completed just now" : "Integrity review required immediately"}</span>
        </div>
      )}
      <section className="panel log-panel">
        <div className="log-filters">
          <label>
            <span>Search user or patient</span>
            <div>
              <Search size={16} />
              <input
                placeholder="Name, ID, or email"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
          </label>
          <label>
            <span>Outcome</span>
            <select value={outcomeFilter} onChange={(e) => setOutcomeFilter(e.target.value)}>
              <option value="All">All outcomes</option>
              <option value="ALLOWED">Allowed</option>
              <option value="DENIED">Denied</option>
              <option value="FAILED">Failed</option>
            </select>
          </label>
          <label>
            <span>Action</span>
            <select value={actionFilter} onChange={(e) => setActionFilter(e.target.value)}>
              <option value="All">All actions</option>
              <option value="LOGIN">Login</option>
              <option value="OPEN_CHART">Open chart</option>
              <option value="OPEN_CLINICAL">Open clinical</option>
              <option value="OPEN_NOTE">Open note</option>
              <option value="BREAK_GLASS">Break-glass</option>
              <option value="VIEW">Review action</option>
            </select>
          </label>
          <label>
            <span>Time range</span>
            <select value={timeRange} onChange={(e) => setTimeRange(e.target.value)}>
              <option value="24h">Last 24 hours</option>
              <option value="7d">Last 7 days</option>
               <option value="all">All retained events</option>
            </select>
          </label>
        </div>
        <div className="log-summary">
          <span>
              Showing <strong>{filteredLogs.length}</strong> matching backend {filteredLogs.length === 1 ? "event" : "events"} · {timeRange === "all" ? "all retained data" : timeRange === "7d" ? "last 7 days" : "last 24 hours"}
           </span>
           <RuleChip>Backend API</RuleChip>
        </div>
        <div className="full-log-table">
          {integrity === "fail" && (
            <div className="tampered-row" style={{ padding: 12, background: "#ef444422", color: "#f87171" }}>
              #{brokenId ?? 8812} · Row modified · Hash mismatch · Cryptographic tamper detected
            </div>
          )}
          <div className="log-head">
            <span>TIME</span>
            <span>USER / ROLE</span>
            <span>ACTION</span>
            <span>PATIENT</span>
            <span>OUTCOME</span>
            <span>WARD</span>
            <span>LOG ID</span>
            <span>SHA-256 CHAIN</span>
          </div>
           {isLoading ? (
             <div className="baseline-note">Loading live audit events…</div>
           ) : errorMessage ? (
             <div className="baseline-note">No live events are displayed until the backend is reachable.</div>
           ) : filteredLogs.length === 0 ? (
             <div className="baseline-note">No backend events match the current filters.</div>
           ) : filteredLogs.map((log) => (
            <div className="log-row" key={log.logId}>
              <span>{log.ts}</span>
              <span>{log.username} · {log.userRole}</span>
              <span className="font-semibold text-teal-300">{log.action}</span>
              <span>{log.patientRef || "—"}</span>
              <span className={`outcome ${log.outcome.toLowerCase()}`}>
                <i />
                {log.outcome}
              </span>
              <span>{log.wardName || "—"}</span>
              <span>#{log.logId}</span>
              <span className="font-mono text-[10px] text-teal-400/80 cursor-pointer" title={`Full SHA-256 Digest:\n${log.hash}\nPrevious Hash:\n${log.prevHash}`}>
                {log.hash ? `${log.hash.slice(0, 6)}…${log.hash.slice(-4)}` : "—"}
              </span>
            </div>
          ))}
        </div>
      </section>
    </main>
  )
}

function Baselines({ onOpenAlerts }: { onOpenAlerts: () => void }) {
  const [baselines, setBaselines] = useState<BaselineViewModel[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState("")

  useEffect(() => {
    let cancelled = false
    setIsLoading(true)
    setErrorMessage("")
    void medsentinelApi
      .listBaselines()
      .then((nextBaselines) => {
        if (!cancelled) setBaselines(nextBaselines)
      })
      .catch((error: unknown) => {
        if (!cancelled) setErrorMessage(error instanceof Error ? error.message : "Unable to load live baselines.")
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  return (
    <main className="page">
      <section className="welcome">
        <div>
          <h2>Role & ward baselines</h2>
          <p>Reference thresholds returned by the MedSentinel backend.</p>
        </div>
        <button className="btn btn-primary" onClick={onOpenAlerts}>Review alerts <ArrowLeft size={15} className="rotate-180" /></button>
      </section>
      {errorMessage && <div className="baseline-note" role="status">Live baselines are unavailable: {errorMessage}</div>}
      <section className="kpi-grid">
        {isLoading ? <div className="baseline-note">Loading live baselines…</div> : baselines.length === 0 ? <div className="baseline-note">No baselines were returned by the backend.</div> : baselines.map((baseline) => (
          <div className="kpi-card" key={baseline.baselineId}>
            <div className="kpi-top"><span>{baseline.scope} · Role #{baseline.roleId}</span><div className="kpi-icon"><SlidersHorizontal size={17} /></div></div>
            <div className="kpi-value text-lg">{baseline.maxRecordsPer10Min} records / 10 min</div>
            <div className="kpi-hint">Multiplier {baseline.multiplier}</div>
          </div>
        ))}
      </section>
      <section className="panel p-5">
        <div className="panel-header !px-0 !pt-0">
          <div><h3>How to read a baseline</h3><p>Baselines explain backend alert context; they are not clinical decisions.</p></div>
        </div>
        <div className="grid gap-3 p-4 sm:grid-cols-3">
          <RuleChip>Role scope</RuleChip>
          <RuleChip>Ward assignment</RuleChip>
          <RuleChip>Shift timing</RuleChip>
        </div>
      </section>
    </main>
  )
}

function displayRole(role: string) {
  return role === "DOCTOR"
    ? "Doctor"
    : role === "NURSE"
      ? "Nurse"
      : role === "BILLING"
        ? "Billing"
        : role === "RECEPTIONIST"
          ? "Reception"
          : role === "ADMIN"
            ? "Administrator"
            : "Security reviewer"
}

function UsersRoles({ onOpenClinical }: { onOpenClinical: () => void }) {
  const [users, setUsers] = useState<AdminUserViewModel[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState("")

  useEffect(() => {
    let cancelled = false
    setIsLoading(true)
    setErrorMessage("")
    void medsentinelApi
      .listUsers()
      .then((nextUsers) => {
        if (!cancelled) setUsers(nextUsers)
      })
      .catch((error: unknown) => {
        if (!cancelled) setErrorMessage(error instanceof Error ? error.message : "Unable to load live users.")
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  return (
    <main className="page">
      <section className="welcome">
        <div>
          <h2>Users & roles</h2>
          <p>Directory entries and role scope returned by the MedSentinel backend.</p>
        </div>
        <button className="btn btn-primary" onClick={onOpenClinical}><UsersRound size={15} /> Open clinical access</button>
      </section>
      <section className="panel">
        <div className="panel-header">
          <div><h3>Identity directory</h3><p>{isLoading ? "Loading live identities…" : `${users.length} identities loaded from the backend`}</p></div>
        </div>
        <div className="full-log-table">
          <div className="log-head"><span>NAME</span><span>USERNAME</span><span>ROLE</span><span>WARD</span><span>STATUS</span><span>SCOPE</span><span>USER ID</span><span>ACCESS</span></div>
          {errorMessage ? <div className="baseline-note">Live users are unavailable: {errorMessage}</div> : isLoading ? <div className="baseline-note">Loading live users…</div> : users.length === 0 ? <div className="baseline-note">No users were returned by the backend.</div> : users.map((user) => (
            <div className="log-row" key={user.userId}>
              <span>{user.fullName}</span>
              <span>{user.username}</span>
              <span>{displayRole(user.role)}</span>
              <span>{user.wardName || "Organization-wide"}</span>
              <span className="outcome"><i />{user.status}</span>
              <span>{user.role === "BILLING" ? "Billing only" : user.role === "REVIEWER" ? "SOC review" : "Role scoped"}</span>
              <span>#{user.userId}</span>
               <span><button className="btn btn-ghost" onClick={onOpenClinical}>Open clinical access</button></span>
            </div>
          ))}
        </div>
      </section>
    </main>
  )
}

function Simulator({ goDashboard }: { goDashboard: () => void }) {
  const [runningMsg, setRunningMsg] = useState<string | null>(null)
  const [runningScenario, setRunningScenario] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState("")
  const scenarios = [
    { name: "Normal nurse shift", description: "Routine on-ward access", expectation: "Expect: no alert", tone: "ok", slug: "normal-nurse" },
    { name: "Busy clinician burst", description: "40 charts within ward baseline", expectation: "Expect: no alert", tone: "ok", slug: "busy-clinician" },
    { name: "Brute-force login", description: "Repeated failures then success", expectation: "Expect: R1", tone: "alert", slug: "brute-force" },
    { name: "Billing clerk bulk pull", description: "38 clinical records in 7 min", expectation: "Expect: R2 + R3", tone: "alert", slug: "billing-bulk" },
    { name: "Receptionist reads clinical", description: "Attempted restricted note", expectation: "Expect: R3", tone: "alert", slug: "receptionist-clinical" },
    { name: "Break-glass use", description: "Off-ward emergency access", expectation: "Expect: review", tone: "review", slug: "break-glass" },
    { name: "Tamper log row", description: "Modify chain-protected event", expectation: "Expect: integrity fail", tone: "danger", slug: "tamper-log" },
    { name: "Seed audit logs", description: "Generate retained backend log rows", expectation: "Expect: new events", tone: "review", slug: "seed" },
  ]

  const handleRun = async (scenario: (typeof scenarios)[number]) => {
    setRunningScenario(scenario.slug)
    setRunningMsg(null)
    setErrorMessage("")
    try {
      const result = await medsentinelApi.runSimulator(scenario.slug)
      setRunningMsg(result.result)
    } catch (error: unknown) {
      setErrorMessage(error instanceof Error ? error.message : "Unable to run the backend scenario.")
    } finally {
      setRunningScenario(null)
    }
  }

  return (
    <main className="page simulator-page">
      <section className="welcome">
        <div>
          <h2>Scenario simulator</h2>
          <p>Generate backend events to test detection rules and reviewer workflows.</p>
        </div>
        <span className="demo-badge">
          <FlaskConical size={15} />
          DEMO ENVIRONMENT
        </span>
      </section>
      {errorMessage && <div className="baseline-note" role="status">Scenario unavailable: {errorMessage}</div>}
      {runningMsg && (
        <div className="scenario-result">
          <CheckCircle2 size={20} />
          <div>
            <strong>Scenario executed by backend</strong>
            <span>{runningMsg}</span>
          </div>
          <button className="btn btn-primary" onClick={goDashboard}>
            View on dashboard
          </button>
        </div>
      )}
      <div className="scenario-grid">
         {scenarios.map((scenario) => (
           <button className="scenario-card" key={scenario.slug} onClick={() => void handleRun(scenario)} disabled={runningScenario !== null}>
             <span className={`scenario-icon tone-${scenario.tone}`}>
               {scenario.slug === "tamper-log" ? <ShieldCheck size={20} /> : scenario.slug === "brute-force" ? <LockKeyhole size={20} /> : <Activity size={20} />}
             </span>
             <div>
               <h3>{scenario.name}</h3>
               <p>{scenario.description}</p>
               <b className={`expect ${scenario.tone}`}>{scenario.expectation}</b>
             </div>
             <span className="run-arrow">{runningScenario === scenario.slug ? "Running…" : "Run →"}</span>
           </button>
        ))}
      </div>
    </main>
  )
}

export function MedSentinelDashboard({
  onLogout,
  initialView = "dashboard",
  initialRole = "Security Reviewer",
  initialUser = "Marcus Chen",
  initialUsername = "rev.meera",
  initialPermissions = [],
}: MedSentinelDashboardProps) {
  const capabilities = getAccessCapabilities(initialRole, initialPermissions)
  const [role, setRole] = useState(capabilities.canUseSecurityWorkspace ? "Security reviewer" : "Clinician")
  const [view, setView] = useState<View>(initialView)
  const [activeAlertId, setActiveAlertId] = useState("A-101")
  const [menuOpen, setMenuOpen] = useState(false)

  // Only expose workspaces that the authenticated backend session can use.
  if (role !== "Security reviewer" && capabilities.canUseClinicalWorkspace) {
    return (
      <div className="relative">
        {capabilities.canUseSecurityWorkspace && <button
          className="reviewer-role-switch"
          onClick={() => setRole("Security reviewer")}
          title="Switch to Security Reviewer Console"
        >
          <ShieldPlus size={16} />
          <span>Security Reviewer Console</span>
          <ChevronDown size={14} />
        </button>}
        <Clinical
          key={role}
          patientPortal={role === "Patient"}
          userName={initialUser}
          userRole={initialRole}
          username={initialUsername}
          onLogout={onLogout}
          capabilities={capabilities}
          switchRole={(r) => {
            if (r === "Security reviewer" && capabilities.canUseSecurityWorkspace) {
              setRole("Security reviewer")
            } else if ((r === "Clinician" || r === "Patient") && capabilities.canUseClinicalWorkspace) {
              setRole(r)
            }
          }}
        />
      </div>
    )
  }

  const reviewer = role === "Security reviewer"

  return (
    <div className={reviewer ? "app-shell" : "clinical-shell"}>
      {/* Floating switcher to jump directly into Clinician / Patient workspace */}
      {capabilities.canUseClinicalWorkspace && <button
        className="reviewer-role-switch"
        onClick={() => setRole("Clinician")}
        title="Open Clinician EHR Workspace"
      >
        <HeartPulse size={15} />
        <span>Clinical Workspace</span>
        <ChevronDown size={14} />
      </button>}

      {reviewer && (
        <Sidebar
          view={view}
          setView={(nextView) => {
            if (nextView === "clinician") {
              setRole("Clinician")
              return
            }
            setView(nextView)
          }}
          open={menuOpen}
           close={() => setMenuOpen(false)}
           onLogout={onLogout}
           capabilities={capabilities}
         />
      )}

      <div className={reviewer ? "content-shell" : ""}>
        {reviewer && (
          <Header
            title={
              view === "dashboard"
                ? "Security overview"
                : view === "alert"
                  ? "Alert investigation"
                  : view === "logs"
                    ? "Audit trail"
                    : view === "baselines"
                      ? "Baselines"
                      : view === "users"
                        ? "Users & roles"
                        : "Demo controls"
            }
            eyebrow="ST. ANNE’S HOSPITAL"
            userName={initialUser}
            userRole={initialRole}
            onMenu={() => setMenuOpen(true)}
            onOpenAlerts={() => setView("alert")}
            onLogout={onLogout}
            canViewAlerts={capabilities.canViewAlerts}
          />
        )}

        {view === "dashboard" && (
          <Dashboard
            openAlert={(id) => {
              if (id) setActiveAlertId(id)
              setView("alert")
            }}
            onOpenAlerts={() => setView("alert")}
            userName={initialUser}
          />
        )}
        {view === "alert" && <AlertDetail key={activeAlertId} alertId={activeAlertId} back={() => setView("dashboard")} canReviewActions={capabilities.canReviewAlerts} />}
        {view === "logs" && <LogExplorer canAdmin={capabilities.canAdmin} />}
        {view === "simulator" && <Simulator goDashboard={() => setView("dashboard")} />}
        {view === "baselines" && <Baselines onOpenAlerts={() => setView("alert")} />}
        {view === "users" && <UsersRoles onOpenClinical={() => setRole("Clinician")} />}
      </div>
    </div>
  )
}

export default MedSentinelDashboard
