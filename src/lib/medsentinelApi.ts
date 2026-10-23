export interface LoginResponseDto {
  authenticated?: boolean
  userId?: number | string | null
  username?: string | null
  name?: string | null
  fullName?: string | null
  role?: string | null
  wardId?: number | string | null
  permissions?: unknown
}

export interface AlertDto {
  alertId?: number | string | null
  userId?: number | string | null
  username?: string | null
  user?: string | null
  role?: string | null
  userRole?: string | null
  ruleCodes?: string | null
  score?: number | string | null
  severity?: string | null
  status?: string | null
  explanation?: string | null
  eventCount?: number | string | null
  evidenceLogIds?: unknown
  firstSeen?: string | null
  lastSeen?: string | null
}

export interface AccessLogDto {
  logId?: number | string | null
  ts?: string | null
  userId?: number | string | null
  usernameAttempted?: string | null
  username?: string | null
  ip?: string | null
  action?: string | null
  resourceType?: string | null
  resourceId?: number | string | null
  patientId?: number | string | null
  outcome?: string | null
  reason?: string | null
  prevHash?: string | null
  rowHash?: string | null
  hash?: string | null
  userRole?: string | null
  wardName?: string | null
}

export interface PatientDto {
  patientId?: number | string | null
  refId?: string | null
  name?: string | null
  dob?: string | null
  wardId?: number | string | null
  wardName?: string | null
  assignedDoctor?: string | null
  status?: string | null
}

export interface PatientRecordDto {
  recordId?: number | string | null
  patientId?: number | string | null
  type?: string | null
  content?: string | null
}

export interface UserDto {
  userId?: number | string | null
  username?: string | null
  name?: string | null
  fullName?: string | null
  role?: string | { name?: string | null; permissions?: unknown } | null
  wardId?: number | string | null
  wardName?: string | null
  shiftStart?: string | null
  shiftEnd?: string | null
  status?: string | null
}

export interface BaselineDto {
  baselineId?: number | string | null
  roleId?: number | string | null
  scope?: string | null
  maxRecordsPer10Min?: number | string | null
  multiplier?: number | string | null
}

export interface StatsViewModel {
  openAlerts: number
  highSeverity: number
  failedLogins: number
}

export interface SessionUser {
  userId: number
  username: string
  name: string
  role: string
  wardId: number | null
  permissions: string[]
}

export interface AccessCapabilities {
  canViewStats: boolean
  canViewAlerts: boolean
  canReviewAlerts: boolean
  canViewLogs: boolean
  canViewPatients: boolean
  canViewClinicalRecords: boolean
  canViewBillingRecords: boolean
  canExport: boolean
  canAdmin: boolean
  canBreakGlass: boolean
  canUseSecurityWorkspace: boolean
  canUseClinicalWorkspace: boolean
}

/**
 * Mirrors the backend route permissions so the UI can present useful actions
 * without pretending that a frontend check is a security boundary.
 */
export function getAccessCapabilities(role: string, permissions: string[] = []): AccessCapabilities {
  const normalizedRole = role.trim().toUpperCase()
  const permissionSet = new Set(permissions.map((permission) => permission.trim().toUpperCase()))
  const isAdmin = normalizedRole === "ADMIN"
  const canViewAlerts = isAdmin || permissionSet.has("VIEW_ALERTS")
  const canViewLogs = isAdmin || permissionSet.has("VIEW_LOGS")
  const canViewPatients = isAdmin || permissionSet.has("VIEW_DEMOGRAPHICS")
  const canViewClinicalRecords = permissionSet.has("VIEW_CLINICAL")
  const canViewBillingRecords = permissionSet.has("VIEW_BILLING")
  const canAdmin = isAdmin && permissionSet.has("ADMIN")

  return {
    canViewStats: isAdmin || canViewAlerts || canViewLogs,
    canViewAlerts,
    canReviewAlerts: isAdmin || normalizedRole === "REVIEWER",
    canViewLogs,
    canViewPatients,
    canViewClinicalRecords,
    canViewBillingRecords,
    canExport: permissionSet.has("EXPORT"),
    canAdmin,
    canBreakGlass: normalizedRole === "DOCTOR" && canViewClinicalRecords,
    canUseSecurityWorkspace: isAdmin || canViewAlerts || canViewLogs,
    canUseClinicalWorkspace: canViewPatients,
  }
}

export type AlertSeverity = "High" | "Medium" | "Low"
export type AlertStatus = "Open" | "Acknowledged" | "Escalated" | "False positive"
export type LogOutcome = "ALLOWED" | "DENIED" | "FAILED"

export interface AlertViewModel {
  alertId: string
  numericAlertId: number
  severity: AlertSeverity
  score: number
  user: string
  role: string
  rules: string[]
  text: string
  timeAgo: string
  count: number
  status: AlertStatus
  evidenceLogIds: number[]
  firstSeen: string | null
  lastSeen: string | null
}

export interface AccessLogViewModel {
  logId: number
  ts: string
  userId: number | null
  username: string
  userRole: string
  ip: string
  action: string
  resourceType: string
  resourceId: number | null
  patientId: number | null
  patientRef: string | null
  wardName: string | null
  outcome: LogOutcome
  reason: string
  prevHash: string
  hash: string
}

export interface AlertDetailViewModel {
  alert: AlertViewModel
  evidenceLogs: AccessLogViewModel[]
}

export interface IntegrityViewModel {
  intact: boolean
  brokenAtLogId: number | null
  rowsChecked: number
  totalRows: number
}

export interface PatientViewModel {
  patientId: number
  refId: string
  id: string
  name: string
  dob: string
  wardId: number | null
  wardName: string
  ward: string
  time: string
  assignedDoctor: string
  status: string
  initials: string
  color: string
}

export interface PatientRecordViewModel {
  recordId: number
  patientId: number
  type: string
  content: string
}

export interface PatientDetailViewModel {
  patient: PatientViewModel
  records: Record<string, PatientRecordViewModel[]>
  denied: Record<string, string>
}

export interface AdminUserViewModel {
  userId: number
  username: string
  fullName: string
  role: string
  wardId: number | null
  wardName: string
  shiftStart: string
  shiftEnd: string
  status: string
}

export interface BaselineViewModel {
  baselineId: number
  roleId: number
  scope: string
  maxRecordsPer10Min: number
  multiplier: number
}

export interface BreakGlassViewModel {
  success: boolean
  records: PatientRecordViewModel[]
}

export interface SimulatorResultViewModel {
  result: string
}

export class MedSentinelApiError extends Error {
  readonly status: number
  readonly details: unknown

  constructor(message: string, status = 0, details?: unknown) {
    super(message)
    this.name = "MedSentinelApiError"
    this.status = status
    this.details = details
  }
}

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim()
const normalizedConfiguredBaseUrl = configuredBaseUrl?.replace(/\/$/, "")
const API_BASE_URL = normalizedConfiguredBaseUrl
  ? normalizedConfiguredBaseUrl.endsWith("/api") ? normalizedConfiguredBaseUrl : `${normalizedConfiguredBaseUrl}/api`
  : "/api"

type JsonObject = Record<string, unknown>

function asObject(value: unknown): JsonObject {
  return value !== null && typeof value === "object" ? (value as JsonObject) : {}
}

function asArray<T>(value: unknown, key?: string): T[] {
  if (Array.isArray(value)) return value as T[]
  if (key) {
    const nested = asObject(value)[key]
    if (Array.isArray(nested)) return nested as T[]
  }
  return []
}

function asNumber(value: unknown, fallback = 0): number {
  const parsed = typeof value === "number" ? value : Number(value)
  return Number.isFinite(parsed) ? parsed : fallback
}

function asNullableNumber(value: unknown): number | null {
  if (value === null || value === undefined || value === "") return null
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

function asString(value: unknown, fallback = ""): string {
  return typeof value === "string" ? value : value === null || value === undefined ? fallback : String(value)
}

function asStringArray(value: unknown): string[] {
  return Array.isArray(value) ? value.filter((item): item is string => typeof item === "string") : []
}

function parseErrorMessage(payload: unknown, status: number): string {
  const object = asObject(payload)
  const nestedError = asObject(object.error)
  const message = object.message ?? nestedError.message ?? object.error
  return typeof message === "string" && message.trim() ? message : `Request failed (${status})`
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  if (init.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json")

  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      credentials: "include",
      headers,
    })
  } catch (cause) {
    throw new MedSentinelApiError("The MedSentinel backend is unavailable.", 0, cause)
  }

  const text = await response.text()
  let payload: unknown = undefined
  if (text) {
    try {
      payload = JSON.parse(text) as unknown
    } catch {
      payload = text
    }
  }

  if (!response.ok) {
    throw new MedSentinelApiError(parseErrorMessage(payload, response.status), response.status, payload)
  }

  return payload as T
}

function formatRelativeTime(value: string | null | undefined): string {
  if (!value) return "Time unavailable"
  const timestamp = new Date(value).getTime()
  if (!Number.isFinite(timestamp)) return value

  const elapsedSeconds = Math.max(0, Math.floor((Date.now() - timestamp) / 1000))
  if (elapsedSeconds < 60) return "Just now"
  const minutes = Math.floor(elapsedSeconds / 60)
  if (minutes < 60) return `${minutes} min ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} hr ago`
  const days = Math.floor(hours / 24)
  return `${days} day${days === 1 ? "" : "s"} ago`
}

function mapSeverity(value: unknown): AlertSeverity {
  switch (asString(value).toUpperCase()) {
    case "HIGH":
      return "High"
    case "MEDIUM":
      return "Medium"
    default:
      return "Low"
  }
}

function mapAlertStatus(value: unknown): AlertStatus {
  switch (asString(value).toUpperCase()) {
    case "ACK":
    case "ACKNOWLEDGED":
      return "Acknowledged"
    case "ESCALATED":
      return "Escalated"
    case "FALSE_POSITIVE":
    case "FALSE POSITIVE":
      return "False positive"
    default:
      return "Open"
  }
}

function displayAlertId(value: unknown): { label: string; numeric: number } {
  const numeric = asNumber(typeof value === "string" ? value.replace(/^A-/i, "") : value)
  return { label: numeric ? `A-${numeric}` : asString(value, "Unknown alert"), numeric }
}

function mapAlert(dto: AlertDto): AlertViewModel {
  const id = displayAlertId(dto.alertId)
  const severity = mapSeverity(dto.severity)
  const rules = asString(dto.ruleCodes)
    .split(",")
    .map((rule) => rule.trim())
    .filter(Boolean)

  return {
    alertId: id.label,
    numericAlertId: id.numeric,
    severity,
    score: asNumber(dto.score),
    user: asString(dto.username ?? dto.user, dto.userId === null || dto.userId === undefined ? "Unknown actor" : `User #${dto.userId}`),
    role: asString(dto.role ?? dto.userRole, "Role unavailable"),
    rules,
    text: asString(dto.explanation, "No explanation provided by the backend."),
    timeAgo: formatRelativeTime(dto.lastSeen ?? dto.firstSeen),
    count: asNumber(dto.eventCount, 0),
    status: mapAlertStatus(dto.status),
    evidenceLogIds: asArray<unknown>(dto.evidenceLogIds).map((id) => asNumber(id)).filter((id) => id > 0),
    firstSeen: dto.firstSeen ?? null,
    lastSeen: dto.lastSeen ?? null,
  }
}

function mapAccessLog(dto: AccessLogDto): AccessLogViewModel {
  const patientId = asNullableNumber(dto.patientId)
  return {
    logId: asNumber(dto.logId),
    ts: asString(dto.ts, "Time unavailable"),
    userId: asNullableNumber(dto.userId),
    username: asString(dto.usernameAttempted ?? dto.username, "Unknown user"),
    userRole: asString(dto.userRole, "Role unavailable"),
    ip: asString(dto.ip, "IP unavailable"),
    action: asString(dto.action, "Unknown action"),
    resourceType: asString(dto.resourceType, "Unknown resource"),
    resourceId: asNullableNumber(dto.resourceId),
    patientId,
    patientRef: patientId === null ? null : `#${patientId}`,
    wardName: dto.wardName ?? null,
    outcome: dto.outcome === "ALLOWED" || dto.outcome === "DENIED" ? dto.outcome : "FAILED",
    reason: asString(dto.reason, "Reason unavailable"),
    prevHash: asString(dto.prevHash),
    hash: asString(dto.rowHash ?? dto.hash),
  }
}

function patientColor(patientId: number): string {
  return ["teal", "blue", "purple", "orange"][Math.abs(patientId) % 4]
}

function mapPatient(dto: PatientDto): PatientViewModel {
  const patientId = asNumber(dto.patientId)
  const name = asString(dto.name, `Patient #${patientId}`)
  return {
    patientId,
    refId: asString(dto.refId, `#${patientId}`),
    id: asString(dto.refId, `#${patientId}`),
    name,
    dob: asString(dto.dob, "Date of birth unavailable"),
    wardId: asNullableNumber(dto.wardId),
    wardName: asString(dto.wardName, dto.wardId === null || dto.wardId === undefined ? "Ward unavailable" : `Ward #${dto.wardId}`),
    ward: asString(dto.wardName, dto.wardId === null || dto.wardId === undefined ? "Ward unavailable" : `Ward #${dto.wardId}`),
    time: "—",
    assignedDoctor: asString(dto.assignedDoctor, "Assignment unavailable"),
    status: asString(dto.status, "Status unavailable"),
    initials: name.split(" ").map((part) => part[0]).join("").slice(0, 2).toUpperCase() || "PT",
    color: patientColor(patientId),
  }
}

function mapPatientRecord(dto: PatientRecordDto): PatientRecordViewModel {
  return {
    recordId: asNumber(dto.recordId),
    patientId: asNumber(dto.patientId),
    type: asString(dto.type, "RECORD"),
    content: asString(dto.content, "Record content unavailable."),
  }
}

function mapPatientDetail(payload: JsonObject): PatientDetailViewModel {
  const rawRecords = asObject(payload.records)
  const records: Record<string, PatientRecordViewModel[]> = {}
  for (const [type, value] of Object.entries(rawRecords)) {
    records[type] = asArray<PatientRecordDto>(value).map(mapPatientRecord)
  }

  const rawDenied = asObject(payload.denied)
  const denied: Record<string, string> = {}
  for (const [type, value] of Object.entries(rawDenied)) denied[type] = asString(value, "Access denied")

  return {
    patient: mapPatient(asObject(payload.patient) as PatientDto),
    records,
    denied,
  }
}

function mapRole(role: UserDto["role"]): string {
  if (typeof role === "string") return role
  return asString(role?.name, "Role unavailable")
}

function mapUser(dto: UserDto): AdminUserViewModel {
  const userId = asNumber(dto.userId)
  const role = mapRole(dto.role)
  return {
    userId,
    username: asString(dto.username, `user-${userId}`),
    fullName: asString(dto.fullName ?? dto.name ?? dto.username, `User #${userId}`),
    role,
    wardId: asNullableNumber(dto.wardId),
    wardName: asString(dto.wardName, dto.wardId === null || dto.wardId === undefined ? "Organization-wide" : `Ward #${dto.wardId}`),
    shiftStart: asString(dto.shiftStart, "—"),
    shiftEnd: asString(dto.shiftEnd, "—"),
    status: asString(dto.status, "UNKNOWN"),
  }
}

function mapSessionUser(dto: LoginResponseDto): SessionUser {
  const username = asString(dto.username, "Unknown user")
  const role = asString(dto.role, "USER")
  const demoNames: Record<string, string> = {
    "dr.asha": "Dr. Asha Mehta",
    "nurse.priya": "Nurse Priya Sharma",
    "clerk.ravi": "Ravi Kumar",
    "rec.neha": "Neha Kapoor",
    "rev.meera": "Meera Iyer",
    admin: "System Admin",
    "dr.elena.vance@stjude-health.org": "Dr. Elena Vance",
    "nurse.sarah.connor@stjude-health.org": "Nurse Sarah Connor",
  }
  return {
    userId: asNumber(dto.userId),
    username,
    name: asString(dto.name ?? dto.fullName, demoNames[username] ?? username),
    role,
    wardId: asNullableNumber(dto.wardId),
    permissions: asStringArray(dto.permissions),
  }
}

function mapBaseline(dto: BaselineDto): BaselineViewModel {
  return {
    baselineId: asNumber(dto.baselineId),
    roleId: asNumber(dto.roleId),
    scope: asString(dto.scope, "UNKNOWN"),
    maxRecordsPer10Min: asNumber(dto.maxRecordsPer10Min),
    multiplier: asNumber(dto.multiplier),
  }
}

function mapRecords(value: unknown): PatientRecordViewModel[] {
  return asArray<PatientRecordDto>(value).map(mapPatientRecord)
}

export const medsentinelApi = {
  async login(username: string, password: string): Promise<SessionUser> {
    const payload = await request<LoginResponseDto>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    })
    if (payload?.authenticated === false) {
      throw new MedSentinelApiError("Invalid credentials.", 401, payload)
    }
    return mapSessionUser(payload)
  },

  async logout(): Promise<void> {
    await request<unknown>("/auth/logout", { method: "POST" })
  },

  async me(): Promise<SessionUser | null> {
    try {
      const payload = await request<LoginResponseDto>("/auth/me")
      if (payload?.authenticated === false) return null
      return mapSessionUser(payload)
    } catch (error) {
      if (error instanceof MedSentinelApiError && error.status === 401) return null
      throw error
    }
  },

  async getStats(): Promise<StatsViewModel> {
    const payload = await request<Partial<StatsViewModel>>("/stats")
    return {
      openAlerts: asNumber(payload?.openAlerts),
      highSeverity: asNumber(payload?.highSeverity),
      failedLogins: asNumber(payload?.failedLogins),
    }
  },

  async listAlerts(): Promise<AlertViewModel[]> {
    const payload = await request<AlertDto[]>("/alerts")
    return asArray<AlertDto>(payload).map(mapAlert)
  },

  async getAlert(alertId: string | number): Promise<AlertDetailViewModel> {
    const numericId = asNumber(typeof alertId === "string" ? alertId.replace(/^A-/i, "") : alertId)
    if (!numericId) throw new MedSentinelApiError("The selected alert ID is invalid.", 400)
    const payload = await request<JsonObject>(`/alerts/${numericId}`)
    const rawAlert = asObject(payload.alert ?? payload) as AlertDto
    return {
      alert: mapAlert(rawAlert),
      evidenceLogs: asArray<AccessLogDto>(payload.evidenceLogs).map(mapAccessLog),
    }
  },

  async updateAlertAction(alertId: string | number, action: string, note = ""): Promise<AlertViewModel> {
    const numericId = asNumber(typeof alertId === "string" ? alertId.replace(/^A-/i, "") : alertId)
    if (!numericId) throw new MedSentinelApiError("The selected alert ID is invalid.", 400)
    const payload = await request<JsonObject>(`/alerts/${numericId}/action`, {
      method: "POST",
      body: JSON.stringify({ action, note }),
    })
    return mapAlert(asObject(payload.alert ?? payload) as AlertDto)
  },

  async listLogs(): Promise<AccessLogViewModel[]> {
    const payload = await request<AccessLogDto[]>("/logs")
    return asArray<AccessLogDto>(payload).map(mapAccessLog)
  },

  async verifyIntegrity(): Promise<IntegrityViewModel> {
    const payload = await request<Partial<IntegrityViewModel>>("/integrity")
    return {
      intact: payload?.intact === true,
      brokenAtLogId: asNullableNumber(payload?.brokenAtLogId),
      rowsChecked: asNumber(payload?.rowsChecked),
      totalRows: asNumber(payload?.totalRows),
    }
  },

  async listPatients(query?: string): Promise<PatientViewModel[]> {
    const suffix = query?.trim() ? `?q=${encodeURIComponent(query.trim())}` : ""
    const payload = await request<PatientDto[]>(`/patients${suffix}`)
    return asArray<PatientDto>(payload).map(mapPatient)
  },

  async getPatient(patientId: number): Promise<PatientDetailViewModel> {
    const payload = await request<JsonObject>(`/patients/${patientId}`)
    return mapPatientDetail(payload)
  },

  async breakGlass(patientId: number, justification: string): Promise<BreakGlassViewModel> {
    const payload = await request<JsonObject>(`/patients/${patientId}/break-glass`, {
      method: "POST",
      body: JSON.stringify({ justification }),
    })
    return {
      success: payload.success === true,
      records: mapRecords(payload.records),
    }
  },

  async runSimulator(scenario: string): Promise<SimulatorResultViewModel> {
    const payload = await request<{ result?: unknown }>(`/simulator/${scenario}`, { method: "POST" })
    return { result: asString(payload?.result, "Scenario completed without a result message.") }
  },

  async listUsers(): Promise<AdminUserViewModel[]> {
    const payload = await request<UserDto[]>("/admin/users")
    return asArray<UserDto>(payload).map(mapUser)
  },

  async listBaselines(): Promise<BaselineViewModel[]> {
    const payload = await request<BaselineDto[]>("/admin/baselines")
    return asArray<BaselineDto>(payload).map(mapBaseline)
  },
}
