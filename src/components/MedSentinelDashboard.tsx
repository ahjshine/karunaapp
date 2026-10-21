import { useState, type ReactNode } from "react"
import Clinical from "@/Clinical"
import { medEngine } from "@/lib/medsentinelEngine"
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

type View = "dashboard" | "alert" | "logs" | "clinician" | "simulator"
type Severity = "High" | "Medium" | "Low"

interface MedSentinelDashboardProps {
  onLogout: () => void
  initialView?: View
  initialRole?: string
  initialUser?: string
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
  onLogout,
}: {
  title: string
  eyebrow: string
  userName: string
  userRole: string
  onMenu: () => void
  onLogout: () => void
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
          <span>MEDSENTINEL CORE · PORT 5173 NATIVE</span>
        </div>
        <button className="icon-button" aria-label="Notifications" title="3 unread security alerts">
          <Bell size={19} />
          <span className="notification-dot" />
        </button>
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
}: {
  view: View
  setView: (view: View) => void
  open: boolean
  close: () => void
  onLogout: () => void
}) {
  const nav = [
    { id: "dashboard" as View, label: "Overview", icon: LayoutDashboard },
    { id: "alert" as View, label: "Alerts", icon: AlertTriangle, badge: "4" },
    { id: "logs" as View, label: "Log explorer", icon: FileSearch },
    { id: "clinician" as View, label: "Clinical access", icon: UsersRound },
  ]

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
        <span className="nav-label nav-label-spaced">Tools</span>
        <button
          className={`nav-item ${view === "simulator" ? "active" : ""}`}
          onClick={() => {
            setView("simulator")
            close()
          }}
        >
          <FlaskConical size={18} />
          <span>Scenario simulator</span>
        </button>
        <button
          className="nav-item"
          onClick={() => {
            setView("dashboard")
            close()
          }}
        >
          <SlidersHorizontal size={18} />
          <span>Baselines</span>
        </button>
        <button
          className="nav-item"
          onClick={() => {
            setView("clinician")
            close()
          }}
        >
          <UserRound size={18} />
          <span>Users & roles</span>
        </button>
      </nav>
      <div className="sidebar-status">
        <div className="status-row">
          <span className="live-dot" />
          System operational
        </div>
        <div className="integrity-row">
          <ShieldCheck size={16} />
          <span>Log chain verified</span>
          <Check size={15} />
        </div>
        <small>Checked 4 minutes ago</small>
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

function Dashboard({ openAlert }: { openAlert: (id?: string) => void }) {
  const [severityFilter, setSeverityFilter] = useState("All")
  const alerts = medEngine.getAlerts()

  return (
    <main className="page dashboard-page">
      <section className="welcome">
        <div>
          <h2>Good morning, Marcus</h2>
          <p>Here’s what needs your attention across St. Anne’s Hospital.</p>
        </div>
        <div className="date-chip">
          <Clock3 size={16} />
          Mon, 12 Oct 2026 <span>·</span> 09:42
        </div>
      </section>

      <section className="kpi-grid">
        <KPICard
          label="Open alerts"
          value={alerts.length}
          hint={
            <>
              <span className="up">↑ 1</span> since yesterday
            </>
          }
          icon={<Bell size={19} />}
        />
        <KPICard
          label="High severity"
          value={alerts.filter((a) => a.severity === "High").length}
          hint="Requires immediate review"
          icon={<AlertTriangle size={19} />}
          tone="danger"
        />
        <KPICard
          label="Failed logins · 24h"
          value="12"
          hint={
            <>
              <span className="down">↓ 20%</span> from prior day
            </>
          }
          icon={<LockKeyhole size={19} />}
        />
        <KPICard
          label="Log integrity"
          value={
            <span className="verified-value">
              <CheckCircle2 size={21} />
              Verified
            </span>
          }
          hint="12,408 rows · 4 min ago"
          icon={<ShieldCheck size={19} />}
          tone="success"
        />
      </section>

      <section className="dashboard-grid">
        <div className="panel alert-panel">
          <div className="panel-header">
            <div>
              <h3>Open alerts</h3>
              <p>Events that differ from role and ward baselines</p>
            </div>
            <button className="btn btn-ghost" onClick={() => setSeverityFilter("All")}>
              View all <span>→</span>
            </button>
          </div>
          <div className="filter-row">
            <button
              className={`filter ${severityFilter === "All" ? "active" : ""}`}
              onClick={() => setSeverityFilter("All")}
            >
              All <b>4</b>
            </button>
            <button
              className={`filter ${severityFilter === "High" ? "active" : ""}`}
              onClick={() => setSeverityFilter("High")}
            >
              High <b>1</b>
            </button>
            <button
              className={`filter ${severityFilter === "Medium" ? "active" : ""}`}
              onClick={() => setSeverityFilter("Medium")}
            >
              Medium <b>2</b>
            </button>
            <button
              className={`filter ${severityFilter === "Low" ? "active" : ""}`}
              onClick={() => setSeverityFilter("Low")}
            >
              Low <b>1</b>
            </button>
            <button className="filter filter-date">
              <Clock3 size={14} />
              Last 24 hours
              <ChevronDown size={14} />
            </button>
          </div>
          <div className="alert-list">
            {alerts
              .filter((alert) => severityFilter === "All" || alert.severity === severityFilter)
              .map((alert) => (
                <button key={alert.alertId} className="alert-row" onClick={() => openAlert(alert.alertId)}>
                  <div className={`alert-accent ${alert.severity.toLowerCase()}`} />
                  <div className="alert-main">
                    <div className="alert-row-top">
                      <SeverityPill level={alert.severity} />
                      <span className="alert-id">{alert.alertId}</span>
                      <span className="alert-id">{localStorage.getItem(`status-${alert.alertId}`) || "Open"}</span>
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
                <p>Events by hour · today</p>
              </div>
              <button className="mini-select">
                24h <ChevronDown size={13} />
              </button>
            </div>
            <div className="chart-legend">
              <span>
                <i className="allowed-dot" />
                Allowed
              </span>
              <span>
                <i className="failed-dot" />
                Failed
              </span>
            </div>
            <div className="chart">
              <div className="chart-labels">
                <span>800</span>
                <span>600</span>
                <span>400</span>
                <span>200</span>
                <span>0</span>
              </div>
              <svg viewBox="0 0 360 150" preserveAspectRatio="none" aria-label="Access events chart">
                <defs>
                  <linearGradient id="area" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#14b8a6" stopOpacity=".3" />
                    <stop offset="100%" stopColor="#14b8a6" stopOpacity="0" />
                  </linearGradient>
                </defs>
                <g className="grid-lines">
                  <path d="M0 10H360M0 42H360M0 74H360M0 106H360M0 138H360" />
                </g>
                <path
                  className="area"
                  d="M0 124 C35 118,44 112,67 115 S106 99,128 101 S164 75,186 82 S222 50,244 61 S281 40,301 62 S335 48,360 29 L360 138 L0 138Z"
                />
                <path
                  className="allowed-line"
                  d="M0 124 C35 118,44 112,67 115 S106 99,128 101 S164 75,186 82 S222 50,244 61 S281 40,301 62 S335 48,360 29"
                />
                <path
                  className="failed-line"
                  d="M0 135 L52 133 L92 136 L138 129 L178 135 L218 125 L260 133 L300 119 L360 129"
                />
                <circle cx="300" cy="119" r="3.5" fill="#ef4444" />
              </svg>
              <div className="chart-times">
                <span>00:00</span>
                <span>06:00</span>
                <span>12:00</span>
                <span>18:00</span>
                <span>Now</span>
              </div>
            </div>
          </div>
          <div className="panel risk-panel">
            <div className="panel-header compact">
              <div>
                <h3>Top-risk users</h3>
                <p>Ranked by current activity</p>
              </div>
              <button className="btn btn-ghost">View all</button>
            </div>
            {[
              ["ER", "Elena Ruiz", "Billing clerk", "75", "high"],
              ["AM", "Dr. Asha Mehta", "Physician", "55", "medium"],
              ["MP", "Maya Patel", "Receptionist", "40", "low"],
            ].map((user, index) => (
              <div className="risk-user" key={user[1]}>
                <span className="rank">0{index + 1}</span>
                <div className="small-avatar">{user[0]}</div>
                <div>
                  <strong>{user[1]}</strong>
                  <span>{user[2]}</span>
                </div>
                <b className={`score ${user[4]}`}>{user[3]}</b>
              </div>
            ))}
          </div>
          <div className="suppressed-note">
            <CheckCircle2 size={18} />
            <div>
              <strong>No alert — within baseline</strong>
              <span>Nurse Priya opened 40 on-ward charts in 9 min. Baseline: 60.</span>
            </div>
          </div>
        </aside>
      </section>
    </main>
  )
}

function AlertDetail({ alertId = "A-101", back }: { alertId?: string; back: () => void }) {
  const [status, updateStatus] = useState(localStorage.getItem(`status-${alertId}`) || "Open")
  const [confirm, setConfirm] = useState("")
  const [note, setNote] = useState("")
  const [error, setError] = useState(false)

  const setStatus = (value: string) => {
    updateStatus(value)
    localStorage.setItem(`status-${alertId}`, value)
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
            <SeverityPill level="High" />
            <span>{alertId}</span>
            <span>Detected 2 minutes ago</span>
          </div>
          <h2>Unusual clinical record access</h2>
        </div>
        <button
          className="status-select"
          onClick={() => setStatus(status === "Open" ? "Acknowledged" : "Open")}
        >
          <span className="live-dot" />
          {status}
          <ChevronDown size={15} />
        </button>
      </div>
      <section className="why-box">
        <div className="why-icon">
          <Zap size={21} />
        </div>
        <div>
          <span className="eyebrow">Why this was flagged</span>
          <p>
            Billing clerk opened <strong>38 clinical records in 7 minutes.</strong> Role baseline for clinical
            records is <strong>0.</strong>
          </p>
          <small>
            This is unusual for Elena Ruiz’s role and differs from her previous access patterns.
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
              75 <small>/ 100 · High</small>
            </strong>
          </div>
          <div className="score-bar">
            <span className="r2" />
            <span className="r3" />
            <span className="empty" />
          </div>
          <div className="score-keys">
            <span>
              <i className="r2-key" />
              <b>R2 Bulk access</b>
              <strong>+35</strong>
            </span>
            <span>
              <i className="r3-key" />
              <b>R3 Out-of-scope</b>
              <strong>+40</strong>
            </span>
          </div>
        </section>
        <section className="panel context-card">
          <div className="section-heading">
            <div>
              <h3>User context</h3>
              <p>Elena Ruiz · Billing clerk</p>
            </div>
            <div className="small-avatar">ER</div>
          </div>
          <dl>
            <div>
              <dt>Department</dt>
              <dd>Patient Accounts</dd>
            </div>
            <div>
              <dt>Shift</dt>
              <dd>08:00–16:00</dd>
            </div>
            <div>
              <dt>Clinical baseline</dt>
              <dd>0 records</dd>
            </div>
          </dl>
        </section>
      </div>
      <section className="panel evidence">
        <div className="panel-header">
          <div>
            <h3>Evidence</h3>
            <p>38 access events linked to this alert</p>
          </div>
          <button
            className="btn btn-secondary"
            onClick={() => {
              const data = [
                { id: "#12408", time: "09:38:12", action: "OPEN_CLINICAL", patient: "P-4812", outcome: "Allowed", hash: "8f3a…c91d" },
                { id: "#12407", time: "09:38:03", action: "OPEN_CLINICAL", patient: "P-1037", outcome: "Allowed", hash: "2b7e…a042" },
                { id: "#12406", time: "09:37:54", action: "OPEN_CLINICAL", patient: "P-2291", outcome: "Allowed", hash: "71cd…b813" },
                { id: "#12405", time: "09:37:42", action: "OPEN_CLINICAL", patient: "P-5510", outcome: "Allowed", hash: "c244…9e80" },
              ]
              const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(data, null, 2))
              const a = document.createElement("a")
              a.href = dataStr
              a.download = `evidence-${alertId}.json`
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
          {[
            ["#12408", "09:38:12", "OPEN_CLINICAL", "P-4812", "Allowed", "8f3a…c91d"],
            ["#12407", "09:38:03", "OPEN_CLINICAL", "P-1037", "Allowed", "2b7e…a042"],
            ["#12406", "09:37:54", "OPEN_CLINICAL", "P-2291", "Allowed", "71cd…b813"],
            ["#12405", "09:37:42", "OPEN_CLINICAL", "P-5510", "Allowed", "c244…9e80"],
          ].map((row) => (
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
      <div className="action-bar">
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
          <button className="btn btn-secondary" onClick={() => setStatus("Acknowledged")}>
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
      </div>
      {confirm && (
        <div className="modal-backdrop">
          <section className="reviewer-dialog" role="dialog" aria-modal="true" aria-labelledby="review-title">
            <h3 id="review-title">{confirm}</h3>
            <p>{alertId} · Your reviewer action will be recorded in the demo audit trail.</p>
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
                onClick={() => {
                  if (confirm === "Mark false positive" && !note.trim()) {
                    setError(true)
                    return
                  }
                  setStatus(confirm === "Escalate alert" ? "Escalated" : "False positive")
                  setConfirm("")
                  setNote("")
                }}
              >
                Confirm action
              </button>
            </div>
          </section>
        </div>
      )}
    </main>
  )
}

function LogExplorer() {
  const [integrity, setIntegrity] = useState<"idle" | "pass" | "fail">("idle")
  const [brokenId, setBrokenId] = useState<number | undefined>(undefined)
  const [search, setSearch] = useState("")

  const logs = medEngine.getLogs().filter((l) => {
    if (!search.trim()) return true
    const q = search.toLowerCase()
    return (
      l.username.toLowerCase().includes(q) ||
      (l.patientRef && l.patientRef.toLowerCase().includes(q)) ||
      l.action.toLowerCase().includes(q) ||
      l.reason.toLowerCase().includes(q)
    )
  })

  const handleVerify = () => {
    const res = medEngine.verifyIntegrity()
    setIntegrity(res.status)
    setBrokenId(res.brokenLogId)
  }

  const handleTamper = () => {
    medEngine.runScenario("Tamper log row")
    const res = medEngine.verifyIntegrity()
    setIntegrity(res.status)
    setBrokenId(res.brokenLogId)
  }

  const handleReset = () => {
    medEngine.resetTamper()
    setIntegrity("idle")
    setBrokenId(undefined)
  }

  return (
    <main className="page logs-page">
      <section className="welcome">
        <div>
          <h2>Log explorer</h2>
          <p>Search and verify every access event across the organization.</p>
        </div>
        <button className="btn btn-primary" onClick={handleVerify}>
          <ShieldCheck size={17} />
          Verify integrity
        </button>
      </section>
      <div className="audit-demo-controls">
        <span>Append-only SHA-256 cryptographic chain validation.</span>
        <button className="btn btn-secondary" onClick={handleTamper}>
          Run tamper demonstration
        </button>
        <button className="btn btn-ghost" onClick={handleReset}>
          Reset demo
        </button>
      </div>
      {integrity !== "idle" && (
        <div className={`integrity-banner ${integrity}`}>
          <div>
            {integrity === "pass" ? <CheckCircle2 size={20} /> : <AlertTriangle size={20} />}
            <strong>
              {integrity === "pass"
                ? "12,408 rows verified. Chain intact. Zero tampering detected."
                : `Chain broken at log #${brokenId ?? 8812}. Row hash modified.`}
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
            <button>
              All outcomes
              <ChevronDown size={14} />
            </button>
          </label>
          <label>
            <span>Action</span>
            <button>
              All actions
              <ChevronDown size={14} />
            </button>
          </label>
          <label>
            <span>Time range</span>
            <button>
              Last 24 hours
              <ChevronDown size={14} />
            </button>
          </label>
        </div>
        <div className="log-summary">
          <span>
            Showing <strong>1–{logs.length}</strong> of 12,408 events
          </span>
          <RuleChip>Live updates on</RuleChip>
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
          {logs.map((log) => (
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
        <div className="baseline-note">
          <CheckCircle2 size={17} />
          <strong>Suppressed: within on-ward baseline</strong>
          <span>Nurse Priya opened 40 charts on Cardiology ward in 9 min. Baseline is 60.</span>
        </div>
      </section>
    </main>
  )
}

function Simulator({ goDashboard }: { goDashboard: () => void }) {
  const [runningMsg, setRunningMsg] = useState<string | null>(null)
  const scenarios = [
    ["Normal nurse shift", "Routine on-ward access", "Expect: no alert", "ok"],
    ["Busy clinician burst", "40 charts within ward baseline", "Expect: no alert", "ok"],
    ["Brute-force login", "Repeated failures then success", "Expect: R1", "alert"],
    ["Billing clerk bulk pull", "38 clinical records in 7 min", "Expect: R2 + R3", "alert"],
    ["Receptionist reads clinical", "Attempted restricted note", "Expect: R3", "alert"],
    ["Break-glass use", "Off-ward emergency access", "Expect: review", "review"],
    ["Tamper log row", "Modify chain-protected event", "Expect: integrity fail", "danger"],
  ]

  const handleRun = async (scenarioName: string) => {
    const res = medEngine.runScenario(scenarioName)
    setRunningMsg(res.message)
  }

  return (
    <main className="page simulator-page">
      <section className="welcome">
        <div>
          <h2>Scenario simulator</h2>
          <p>Generate safe demo events to test detection rules and reviewer workflows.</p>
        </div>
        <span className="demo-badge">
          <FlaskConical size={15} />
          DEMO ENVIRONMENT
        </span>
      </section>
      {runningMsg && (
        <div className="scenario-result">
          <CheckCircle2 size={20} />
          <div>
            <strong>Scenario executed in engine</strong>
            <span>{runningMsg}</span>
          </div>
          <button className="btn btn-primary" onClick={goDashboard}>
            View on dashboard
          </button>
        </div>
      )}
      <div className="scenario-grid">
        {scenarios.map((s, i) => (
          <button className="scenario-card" key={s[0]} onClick={() => handleRun(s[0])}>
            <span className={`scenario-icon tone-${s[3]}`}>
              {i === 6 ? <ShieldCheck size={20} /> : i === 2 ? <LockKeyhole size={20} /> : <Activity size={20} />}
            </span>
            <div>
              <h3>{s[0]}</h3>
              <p>{s[1]}</p>
              <b className={`expect ${s[3]}`}>{s[2]}</b>
            </div>
            <span className="run-arrow">Run →</span>
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
}: MedSentinelDashboardProps) {
  const [role, setRole] = useState(
    initialRole.toLowerCase().includes("physician") || initialRole.toLowerCase().includes("nurse")
      ? "Clinician"
      : "Security reviewer"
  )
  const [view, setView] = useState<View>(initialView)
  const [activeAlertId, setActiveAlertId] = useState("A-101")
  const [menuOpen, setMenuOpen] = useState(false)

  // Switch between Clinician Workspace and Reviewer Workspace
  if (role !== "Security reviewer") {
    return (
      <div className="relative">
        <button
          className="reviewer-role-switch"
          onClick={() => setRole("Security reviewer")}
          title="Switch to Security Reviewer Console"
        >
          <ShieldPlus size={16} />
          <span>Security Reviewer Console</span>
          <ChevronDown size={14} />
        </button>
        <Clinical
          key={role}
          patientPortal={role === "Patient"}
          switchRole={(r) => {
            if (r === "Security reviewer") {
              setRole("Security reviewer")
            } else {
              setRole(r)
            }
          }}
        />
      </div>
    )
  }

  const reviewer = view !== "clinician"

  return (
    <div className={reviewer ? "app-shell" : "clinical-shell"}>
      {/* Floating switcher to jump directly into Clinician / Patient workspace */}
      <button
        className="reviewer-role-switch"
        onClick={() => setRole("Clinician")}
        title="Open Clinician EHR Workspace"
      >
        <HeartPulse size={15} />
        <span>Clinical Workspace</span>
        <ChevronDown size={14} />
      </button>

      {reviewer && (
        <Sidebar
          view={view}
          setView={setView}
          open={menuOpen}
          close={() => setMenuOpen(false)}
          onLogout={onLogout}
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
                    : "Demo controls"
            }
            eyebrow="ST. ANNE’S HOSPITAL"
            userName={initialUser}
            userRole={initialRole}
            onMenu={() => setMenuOpen(true)}
            onLogout={onLogout}
          />
        )}

        {view === "dashboard" && (
          <Dashboard
            openAlert={(id) => {
              if (id) setActiveAlertId(id)
              setView("alert")
            }}
          />
        )}
        {view === "alert" && <AlertDetail alertId={activeAlertId} back={() => setView("dashboard")} />}
        {view === "logs" && <LogExplorer />}
        {view === "simulator" && <Simulator goDashboard={() => setView("dashboard")} />}
      </div>
    </div>
  )
}

export default MedSentinelDashboard
