import { useEffect, useState, type ReactNode } from "react"
import { medsentinelApi, type AccessCapabilities, type PatientDetailViewModel, type PatientViewModel } from "@/lib/medsentinelApi"
import {
  Activity,
  ArrowDownToLine,
  ArrowLeft,
  ArrowRight,
  Bell,
  CalendarDays,
  Check,
  CheckCircle2,
  ChevronDown,
  ChevronRight,
  FileText,
  HeartPulse,
  HelpCircle,
  LockKeyhole,
  LogOut,
  Plus,
  Search,
  ShieldCheck,
  ShieldPlus,
  Users,
  X,
  ZoomIn,
  ZoomOut,
} from "lucide-react"

const findings = [
  { name: "Chest", title: "ECG review", text: "Your care team has recommended a follow-up to discuss your ECG result.", status: "Review required", x: 148, y: 143 },
  { name: "Head", title: "Routine assessment", text: "A routine assessment was recorded by your care team. No additional action is noted.", status: "Routine finding", x: 150, y: 52 },
  { name: "Abdomen", title: "Clinical examination", text: "Your clinical examination is available in your care record.", status: "Routine finding", x: 153, y: 206 },
  { name: "Right knee", title: "MRI review", text: "Follow-up required. Discuss this recorded finding with your treating clinician.", status: "Review required", x: 122, y: 342 },
  { name: "Left shoulder", title: "Mobility assessment", text: "A routine mobility assessment has been recorded.", status: "Routine finding", x: 198, y: 112 },
]

function Action({ children, onClick, secondary = false, disabled = false }: { children: ReactNode; onClick?: () => void; secondary?: boolean; disabled?: boolean }) {
  return <button className={`c-button ${secondary ? "secondary" : ""}`} onClick={onClick} disabled={disabled}>{children}</button>
}

function Panel({ title, subtitle, action, children, className = "" }: { title: string; subtitle?: string; action?: ReactNode; children: ReactNode; className?: string }) {
  return <section className={`c-panel ${className}`}><div className="c-panel-head"><div><h3>{title}</h3>{subtitle && <p>{subtitle}</p>}</div>{action}</div>{children}</section>
}

function initialsFor(name: string) {
  return name.split(" ").map((part) => part[0]).join("").slice(0, 2).toUpperCase() || "CL"
}

function BodyMap({ selected, setSelected }: { selected: number; setSelected: (n: number) => void }) {
  const [side, setSide] = useState(false)
  const [zoom, setZoom] = useState(1)

  return <div className="body-map">
    <div className="body-controls"><span><button className={!side ? "selected" : ""} onClick={() => setSide(false)}>Front view</button><button className={side ? "selected" : ""} onClick={() => setSide(true)}>Side view</button></span><button aria-label="Reset diagram" onClick={() => { setZoom(1); setSide(false) }}>Reset</button></div>
    <svg viewBox="0 0 300 440" role="img" aria-label="Interactive anatomical diagram. Select a labeled region." className={side ? "side-body" : ""}>
      <defs><linearGradient id="skin" x1="0" x2="1"><stop stopColor="#d4e5e5"/><stop offset=".45" stopColor="#f3f8f7"/><stop offset="1" stopColor="#c4dddd"/></linearGradient><radialGradient id="bodyGlow"><stop stopColor="#d9f2ef"/><stop offset="1" stopColor="#fff" stopOpacity="0"/></radialGradient></defs>
      <ellipse cx="150" cy="228" rx="145" ry="205" fill="url(#bodyGlow)"/>
      <g transform={`translate(${150 - 150 * zoom},${220 - 220 * zoom}) scale(${zoom})`} fill="url(#skin)" stroke="#a8c4c5" strokeWidth="1.2">
        <path d={side ? "M143 27C160 25 178 37 171 58L160 73L157 91C178 100 183 118 178 145L167 224L167 285L161 386L176 406Q180 417 162 417L147 411L143 322L130 258L127 192L120 131Q116 106 138 92L139 72Q119 60 126 40Q129 27 143 27Z" : "M150 24C128 24 123 41 126 57Q129 70 140 76L138 92Q126 98 112 101Q95 103 89 123L72 191L60 230L63 248Q65 256 71 248L79 232L90 204L105 151L109 197Q105 220 116 242L114 289L116 343L112 395Q102 405 110 413L132 413Q140 410 137 399L139 342L147 275L153 275L161 342L163 399Q160 410 168 413L190 413Q198 405 188 395L184 343L186 289L184 242Q195 220 191 197L195 151L210 204L221 232L229 248Q235 256 237 248L240 230L228 191L211 123Q205 103 188 101Q174 98 162 92L160 76Q171 70 174 57C177 41 172 24 150 24Z"}/>
        {!side && <g fill="none" stroke="#a8c4c5" opacity=".55"><path d="M150 96V227M113 114Q131 129 147 120M187 114Q169 129 153 120M110 147Q129 153 145 141M190 147Q171 153 155 141M122 173Q138 185 147 180M178 173Q162 185 153 180M121 200Q139 212 147 205M179 200Q161 212 153 205M117 239Q133 244 147 258M183 239Q167 244 153 258M124 274L127 324M176 274L173 324M127 350L124 390M173 350L176 390"/><ellipse cx="127" cy="340" rx="8" ry="11"/><ellipse cx="173" cy="340" rx="8" ry="11"/><path d="M136 47Q150 43 164 47M142 65Q150 68 158 65"/></g>}
      </g>
      {findings.map((finding, index) => <g key={finding.name} role="button" tabIndex={0} aria-label={`${finding.name}: ${finding.status}`} onClick={() => setSelected(index)} onKeyDown={(event) => { if (event.key === "Enter" || event.key === " ") setSelected(index) }} className={`body-marker ${selected === index ? "active" : ""} ${finding.status === "Review required" ? "review" : ""}`} transform={`translate(${side ? 151 : finding.x},${finding.y})`}><circle r={selected === index ? 17 : 12} className="marker-halo"/><circle r="6"/><circle r="2" fill="white" stroke="none"/></g>)}
      <path d="M157 143H231L245 131" fill="none" stroke="#b3cfcb" strokeDasharray="3 3"/>
      <text x="216" y="123" fill="#64748b" fontSize="9">CHEST</text>
    </svg>
    <div className="zoom-controls"><button aria-label="Zoom out" onClick={() => setZoom(Math.max(.8, zoom - .1))}><ZoomOut size={16}/></button><span>{Math.round(zoom * 100)}%</span><button aria-label="Zoom in" onClick={() => setZoom(Math.min(1.3, zoom + .1))}><ZoomIn size={16}/></button></div>
    <div className="body-legend"><span><i/>Routine finding</span><span><i className="amber"/>Review required</span><span><i className="red"/>Urgent finding</span></div>
  </div>
}

export default function Clinical({
  patientPortal = false,
  switchRole,
  userName = "Clinician",
  userRole = "Clinician",
  username = "",
  onLogout,
  capabilities,
}: {
  patientPortal?: boolean
  switchRole: (role: string) => void
  userName?: string
  userRole?: string
  username?: string
  onLogout?: () => void
  capabilities: AccessCapabilities
}) {
  const [page, setPage] = useState("Overview")
  const [record, setRecord] = useState(false)
  const [query, setQuery] = useState("")
  const [filter] = useState("All patients")
  const [selected, setSelected] = useState(0)
  const [tab, setTab] = useState("Overview")
  const [modal, setModal] = useState<string | null>(null)
  const [toast, setToast] = useState("")
  const [reason, setReason] = useState("")
  const [error, setError] = useState(false)
  const [emergency, setEmergency] = useState(false)
  const [patients, setPatients] = useState<PatientViewModel[]>([])
  const [patientsLoading, setPatientsLoading] = useState(true)
  const [patientsError, setPatientsError] = useState("")
  const [selectedPatient, setSelectedPatient] = useState<PatientDetailViewModel | null>(null)
  const [patientLoading, setPatientLoading] = useState(false)
  const [displayDate] = useState(() => new Intl.DateTimeFormat("en-US", { weekday: "long", month: "long", day: "numeric", year: "numeric" }).format(new Date()))
  const [sessionTimestamp] = useState(() => new Date().toISOString())

  const notify = (text: string) => {
    setToast(text)
    window.setTimeout(() => setToast(""), 4000)
  }

  const nav = patientPortal ? ["Overview", "My Health", "Appointments", "Medications", "Documents"] : ["Overview", "Patients", "Appointments", "Clinical Records"]

  useEffect(() => {
    let cancelled = false
    setPatientsLoading(true)
    setPatientsError("")
    void medsentinelApi.listPatients(query).then((nextPatients) => {
      if (!cancelled) setPatients(nextPatients)
    }).catch((nextError: unknown) => {
      if (!cancelled) setPatientsError(nextError instanceof Error ? nextError.message : "Unable to load live patient access data.")
    }).finally(() => {
      if (!cancelled) setPatientsLoading(false)
    })
    return () => {
      cancelled = true
    }
  }, [query])

  const openPatient = (patientId: number | string) => {
    if (patientLoading) return
    const numericPatientId = Number(String(patientId).replace(/\D/g, ""))
    if (!Number.isFinite(numericPatientId) || numericPatientId <= 0) {
      setPatientsError("The selected patient ID is invalid.")
      return
    }
    setPatientLoading(true)
    setPatientsError("")
    void medsentinelApi.getPatient(numericPatientId).then((detail) => {
      setSelectedPatient(detail)
      const hasRecords = Object.values(detail.records).some((records) => records.length > 0)
      if (!hasRecords && Object.keys(detail.denied).length > 0) {
        setPatientsError(capabilities.canBreakGlass
          ? "Your role does not include the requested record type. You can request a logged emergency override."
          : "Your role can view the patient directory but not this record type.")
        setModal(capabilities.canBreakGlass ? "Access denied" : "Access unavailable")
        return
      }
      setRecord(true)
      setTab("Overview")
    }).catch((nextError: unknown) => {
      setPatientsError(nextError instanceof Error ? nextError.message : "Unable to open the live patient record.")
      setModal("Access unavailable")
    }).finally(() => setPatientLoading(false))
  }

  const finding = findings[selected]
  const patient = selectedPatient?.patient
  const recordEntries = Object.values(selectedPatient?.records ?? {}).flat()

  const downloadDocument = (name: string) => {
    if (!capabilities.canExport) {
      notify("Export is not enabled for this role.")
      return
    }
    const payload = {
      document: name,
      patient: patient?.name ?? "Selected patient",
      patientRef: patient?.refId ?? "Unavailable",
      requestedBy: username,
      generatedAt: sessionTimestamp,
      source: "Frontend export of the currently loaded record",
    }
    const url = URL.createObjectURL(new Blob([JSON.stringify(payload, null, 2)], { type: "application/json" }))
    const anchor = document.createElement("a")
    anchor.href = url
    anchor.download = `${name.toLowerCase().replace(/\s+/g, "-")}-${patient?.refId ?? "patient"}.json`
    anchor.click()
    URL.revokeObjectURL(url)
    notify(`${name} exported from the loaded patient record.`)
  }

  const documents = capabilities.canExport
    ? <div className="document-list">{["Loaded record export", "Clinical summary", "Care instructions"].map((name) => <button key={name} onClick={() => downloadDocument(name)}><span className="doc-icon"><FileText size={19}/></span><span><strong>{name}</strong><small>{patient?.refId ?? "Select a patient"} · JSON export</small></span><ArrowDownToLine size={16}/></button>)}</div>
    : <div className="care-plan"><FileText size={22}/><h3>Export is not enabled for this role.</h3><p>Records remain readable only within the permissions returned by the backend session.</p></div>
  const medications = <div className="med-list"><div className="care-plan"><HeartPulse size={22}/><h3>Medication data is API-scoped.</h3><p>The current backend contract does not expose a medication endpoint. No local medication data is displayed.</p></div></div>
  const schedule = <Panel title={patientPortal ? "Your next appointment" : "Today's schedule"} subtitle={patientPortal ? "Your care team is here for you" : displayDate} action={<button className="text-action" onClick={() => setPage("Appointments")}>View all <ArrowRight size={14}/></button>} className="schedule-panel">
    <div className="schedule-list">{patients.slice(0, patientPortal ? 1 : 3).map((currentPatient, index) => <button className={`schedule-item ${index === 0 ? "current" : ""}`} key={currentPatient.id} onClick={() => openPatient(currentPatient.patientId)}><div className="schedule-time"><span>{currentPatient.refId}</span><small>Patient access</small></div><div className="timeline-dot"/><div><span className="appointment-kind">Assigned patient</span><strong>{currentPatient.name}</strong><small>{currentPatient.ward} · Appointment data unavailable</small></div><ChevronRight size={16}/></button>)}</div>
    {patientsLoading ? <p className="schedule-empty">Loading live patient access…</p> : patientsError ? <p className="schedule-empty">Live patient access is unavailable.</p> : patients.length === 0 ? <p className="schedule-empty">No patient access entries were returned.</p> : null}
    <div className="schedule-footer"><span className="avatar-stack"><i>{initialsFor(userName)}</i><i>API</i><i>—</i></span><span>Connected to the backend.</span><ShieldCheck size={17}/></div>
  </Panel>

  const executeBreakGlass = async () => {
    if (!reason.trim()) {
      setError(true)
      return
    }
    if (!patient) {
      notify("Open a patient record before requesting emergency access.")
      return
    }
    try {
      await medsentinelApi.breakGlass(patient.patientId, reason.trim())
      setEmergency(true)
      setModal("Emergency request logged")
      notify("Break-glass request recorded by the backend.")
    } catch (nextError: unknown) {
      setPatientsError(nextError instanceof Error ? nextError.message : "Unable to record break-glass access.")
      setModal("Access unavailable")
    }
  }

  const patientDirectory = <Panel title="Assigned patient directory" subtitle="Only authorized records can be opened">
    <div className="patient-toolbar"><label><Search size={16}/><input aria-label="Search patient directory" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search name or patient ID"/></label></div>
    {patientsError && <p className="schedule-empty">Live patient access is unavailable: {patientsError}</p>}
    {patientsLoading ? <p className="schedule-empty">Loading live patient access…</p> : patients.length === 0 ? <p className="schedule-empty">No patients were returned by the backend.</p> : patients.filter((currentPatient) => filter === "All patients" || currentPatient.status === filter).map((currentPatient) => <button className="directory-row" onClick={() => openPatient(currentPatient.patientId)} key={currentPatient.id} disabled={patientLoading}><span className={`patient-avatar ${currentPatient.color}`}>{currentPatient.initials}</span><div><strong>{currentPatient.name}</strong><small>{currentPatient.id} · {currentPatient.ward}</small></div><span className="status-pill">{currentPatient.status}</span><ChevronRight size={17}/></button>)}
  </Panel>

  return <div className="care-app">
    <header className="care-top"><button className="care-brand" onClick={() => { setPage("Overview"); setRecord(false) }}><span><ShieldPlus size={25}/></span>Karuna<span className="brand-period">.</span></button><div className="hospital-switch">St. Anne’s Hospital · {userRole} <ChevronDown size={13}/></div><nav>{nav.map((item) => <button key={item} className={page === item && !record ? "active" : ""} onClick={() => { setPage(item); setRecord(false) }}>{item}</button>)}</nav><div className="care-tools"><button aria-label="Help" onClick={() => setModal("How can we help?")}><HelpCircle size={19}/></button><button aria-label="Notifications" onClick={() => setModal("Notifications")} className="notification-button"><Bell size={19}/><i/></button><span className="care-avatar">{patientPortal ? "PT" : initialsFor(userName)}</span><button aria-label="Switch workspace" onClick={() => setModal("Workspaces")}><ChevronDown size={15}/></button>{onLogout && <button aria-label="Sign out" onClick={onLogout}><LogOut size={17}/></button>}</div></header>
    <div className="care-context"><span><i/> {patientPortal ? "Your personal health workspace" : "Clinical workspace"} <ChevronRight size={12}/> {record ? "Patient record" : page}</span></div>
    <main className="care-main">
      {(patientsError || patientLoading) && <div className="schedule-empty" role="status">{patientLoading ? "Opening live patient record…" : patientsError}</div>}
      <section className="care-heading"><div>{record && <button className="text-action back-record" onClick={() => setRecord(false)}><ArrowLeft size={14}/>Back to patients</button>}<div className="greeting-eyebrow">{record ? "PATIENT OVERVIEW" : displayDate.toUpperCase()}</div><h1>{record ? patient?.name ?? "Patient record" : patientPortal ? `Good morning, ${userName}.` : `Good morning, ${userName}.`}<span className="greeting-dot"/></h1><p>{record ? `${patient?.refId ?? "Patient ID unavailable"} · ${patient?.ward ?? "Ward unavailable"} · Assigned access: ${userName}` : patientPortal ? "Here's an overview of the live clinical workspace." : "A clear picture of your day. Better care for every patient."}</p></div><div className="heading-actions">{record ? <><span className="status-pill amber">{patient?.status ?? "Accessed record"}</span><Action onClick={() => setModal("Schedule follow-up")}><Plus size={16}/>Schedule follow-up</Action></> : <><button className="date-button" onClick={() => setModal("Date selection")}><CalendarDays size={16}/>{displayDate}<ChevronDown size={14}/></button>{!patientPortal && <Action onClick={() => setModal("Add appointment")}><Plus size={16}/>Add appointment</Action>}</>}</div></section>
      {!record && page === "Overview" && <>
        <section className={`care-kpis ${patientPortal ? "" : "care-kpis-clinician"}`}>{(patientPortal ? [{ label: "Patient records", value: String(patients.length), hint: "Returned by backend", icon: FileText }, { label: "Patient search", value: "Live", hint: "API-backed directory", icon: Search }, { label: "Care plan", value: "API", hint: "No local record data", icon: HeartPulse }] : [{ label: "Assigned patients", value: String(patients.length), hint: patientsError ? "Backend unavailable" : "Returned by backend", icon: Users }, { label: "Patient search", value: "Live", hint: "API-backed directory", icon: Search }, { label: "Clinical records", value: "Scoped", hint: "Opened through backend access checks", icon: FileText }]).map((kpi, index) => <button key={kpi.label} className={`care-kpi kpi-${index}`} onClick={() => setPage(patientPortal ? ["Patients", "My Health", "Documents"][index] ?? "Overview" : ["Patients", "Patients", "Clinical Records"][index])}><div><span className="kpi-label">{kpi.label}</span><span className="care-kpi-icon"><kpi.icon size={19}/></span></div><strong>{kpi.value}</strong><small>{kpi.hint}</small></button>)}</section>
        {patientPortal ? <div className="portal-grid"><Panel title="Workspace context" subtitle="Illustrative layout; live records use the backend" className="portal-body"><BodyMap selected={selected} setSelected={setSelected}/></Panel><div className="portal-summary"><Panel title="Record access context" subtitle="From the authenticated workspace"><div className="finding-summary"><span className="status-pill amber">{finding.status}</span><h2>{finding.name} · {finding.title}</h2><p>Clinical record details are shown only after the backend returns an authorized patient record.</p><Action onClick={() => setPage("Patients")}>Open patient directory</Action></div></Panel><Panel title="Your care plan"><div className="care-plan"><span className="plan-icon"><HeartPulse size={22}/></span><h3>Live record access is enabled.</h3><p>Open a patient record to view the permissions and evidence returned by the backend.</p><button className="text-action" onClick={() => setPage("Patients")}>Open patient directory<ArrowRight size={14}/></button></div></Panel></div>{schedule}</div> : <div className="workload-grid">{patientDirectory}{schedule}</div>}
        <div className="care-lower"><Panel title="Clinical activity" subtitle="The current API contract does not expose hourly activity"><div className="baseline-note">Hourly activity remains hidden until the backend provides an activity series.</div></Panel><Panel title="Recent activity" subtitle="Backend access events"><div className="baseline-note">Open a patient record or the audit trail to view server-backed access events.</div></Panel></div>
      </>}
      {record && <><div className="record-tabs">{["Overview", "Clinical", "Diagnostics", "Medications", "Treatment", "Documents"].map((item) => <button key={item} className={tab === item ? "active" : ""} onClick={() => setTab(item)}>{item}</button>)}</div>{tab === "Medications" ? <Panel title="Current medications">{medications}</Panel> : tab === "Documents" ? <Panel title="Authorized documents">{documents}</Panel> : <div className="record-grid"><div><Panel title="Backend records" subtitle="Records returned for this authenticated session"><div className="diagnostic-list">{recordEntries.length === 0 ? <p className="schedule-empty">No accessible records were returned.</p> : recordEntries.map((entry) => <button key={entry.recordId} onClick={() => setModal(entry.type)}><span className="diagnostic-icon"><Activity size={17}/></span><div><strong>{entry.type}</strong><small>Record #{entry.recordId}</small><span className="status-pill">Available</span></div><ChevronRight size={15}/></button>)}</div></Panel><Panel title="Treatment timeline"><div className="treatment-list">{recordEntries.slice(0, 4).map((entry) => <button onClick={() => setModal(entry.type)} key={`timeline-${entry.recordId}`}><i/><div><small>Backend record</small><strong>{entry.type}</strong><span>{entry.content}</span></div></button>)}</div></Panel></div><Panel title="Anatomical overview" subtitle="Select a marker to explore recorded findings" className="anatomy-panel" action={<span className="tiny-badge">LIVE RECORD</span>}><BodyMap selected={selected} setSelected={setSelected}/><div className="selected-finding"><span className="status-pill amber">{finding.status}</span><h3>{finding.name} — {finding.title}</h3><p>{finding.text}</p><small>Displayed alongside the selected backend record.</small><button className="text-action" onClick={() => setModal(finding.title)}>View full result<ArrowRight size={14}/></button></div></Panel></div>}</>}
      {!record && page !== "Overview" && <>{page === "Patients" || (!patientPortal && page === "Clinical Records") ? patientDirectory : page === "Appointments" ? <div className="appointment-page">{schedule}<Panel title="Appointment information"><div className="care-plan"><CalendarDays size={30}/><h2>Appointment data is not in this API slice.</h2><p>Patient access remains available through the backend directory.</p><Action onClick={() => setPage("Patients")}>Open patient directory</Action></div></Panel></div> : page === "Medications" ? <Panel title="My medications">{medications}</Panel> : page === "Documents" ? <Panel title="My documents">{documents}</Panel> : <div className="portal-grid"><Panel title="My health"><BodyMap selected={selected} setSelected={setSelected}/></Panel><Panel title={finding.title}><div className="finding-summary"><span className="status-pill amber">{finding.status}</span><h2>{finding.name}</h2><p>{finding.text}</p><Action onClick={() => setModal(finding.title)}>View result</Action></div></Panel></div>}</>}
      <footer className="care-footer"><span><ShieldCheck size={13}/>Protected by Karuna. Access is monitored by the backend.</span><span>Backend access monitored<span className="footer-separator">·</span>Need help? <button onClick={() => setModal("Contact support")}>Contact support<ArrowRight size={12}/></button></span></footer>
    </main>
    {patientPortal && <nav className="patient-bottom-nav">{nav.map((item, index) => { const Icon = [HeartPulse, Activity, CalendarDays, Plus, FileText][index]; return <button className={page === item ? "active" : ""} key={item} onClick={() => setPage(item)}><Icon size={19}/>{item}</button> })}</nav>}
    {toast && <div className="care-toast" role="status"><CheckCircle2 size={18}/>{toast}</div>}
    {modal && <div className="care-modal-backdrop" onClick={() => setModal(null)}><section className={`care-modal ${modal === "Emergency access" ? "emergency-modal" : ""}`} role="dialog" aria-modal="true" aria-labelledby="dialog-title" onClick={(event) => event.stopPropagation()}><button className="modal-x" aria-label="Close dialog" onClick={() => setModal(null)}><X size={20}/></button><span className="modal-symbol">{modal === "Access denied" ? <LockKeyhole size={25}/> : <ShieldPlus size={25}/>}</span><h2 id="dialog-title">{modal}</h2>
      {modal === "Workspaces" ? <><p>Workspace navigation changes the view only; the backend remains the source of permissions and records.</p>{["Clinician", "Patient", "Security reviewer"].map((role) => <button className="role-choice" key={role} onClick={() => { setModal(null); switchRole(role) }}><span>{role}</span><ArrowRight size={17}/></button>)}</> : modal === "Access denied" ? <><div className="access-denial"><strong>Access denied. This attempt has been logged.</strong><p>Your authenticated role or ward does not permit access to this patient’s clinical records. Restricted information has not been displayed.</p></div><p>Emergency access requires a clinical justification and is sent to the backend.</p><Action onClick={() => { setReason(""); setError(false); setModal("Emergency access") }}>Request emergency access</Action></> : modal === "Access unavailable" ? <><p>{patientsError || "The backend could not load this clinical access request."}</p><Action onClick={() => setModal(null)}>Close</Action></> : modal === "Emergency access" ? <><p>This request will be sent to the backend and logged for reviewer follow-up.</p><label>Patient ID<input readOnly value={patient?.refId ?? "Select a patient"}/></label><label>Emergency reason<select><option>Emergency clinical need</option><option>Urgent consultation</option></select></label><label>Justification<textarea value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Explain why emergency access is required"/></label>{error && <p className="validation-error">A justification is required before proceeding.</p>}<Action onClick={() => void executeBreakGlass()} disabled={patientLoading}>Proceed and log</Action></> : modal === "Emergency request logged" ? <><span className="status-pill purple">{emergency ? "Break-glass · Backend request logged" : "Backend access"}</span><p>The justification and access event were recorded by the backend for reviewer follow-up.</p><Action onClick={() => switchRole("Security reviewer")}>Open reviewer workspace<ArrowRight size={15}/></Action></> : modal.includes("appointment") || modal === "Schedule follow-up" || modal === "Request reschedule" ? <><p>{patient?.name ?? "Select an assigned patient"} · {patient?.refId ?? "Patient ID unavailable"}</p><p>Appointment scheduling is not included in the current API contract.</p><Action onClick={() => setModal(null)}>Close</Action></> : modal === "Notifications" ? <><p>Notifications are available through backend alert and audit views.</p><Action onClick={() => setModal(null)}>Done<Check size={15}/></Action></> : <><p>{modal === "Date selection" ? "The current workspace displays the local date while live patient access comes from the backend." : `${modal} · Backend patient and access data is shown when available.`}</p><Action onClick={() => setModal(null)}>Done<Check size={15}/></Action></>}
    </section></div>}
  </div>
}
