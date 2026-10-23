import { useState, type FormEvent } from "react"
import { ThreeDnaCanvas } from "@/components/ThreeDnaCanvas"
import { medsentinelApi, type SessionUser } from "@/lib/medsentinelApi"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Switch } from "@/components/ui/switch"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  Shield,
  ShieldAlert,
  ShieldCheck,
  Eye,
  EyeOff,
  ChevronDown,
  ArrowRight,
  ArrowLeft,
  Lock,
  Mail,
  KeyRound,
  Loader2,
  Fingerprint,
} from "lucide-react"

interface LoginPageProps {
  onBackToHome: () => void
  onLoginSuccess?: (user: SessionUser) => void
}

type RoleKey = "doctor" | "dr_asha" | "nurse" | "nurse_priya" | "clerk_ravi" | "rec_neha" | "reviewer_meera" | "admin"

interface RoleData {
  title: string
  name: string
  email: string
  defaultPass: string
  badgeColor: string
  level: string
  tokenTag: string
}

const ROLES: Record<RoleKey, RoleData> = {
  dr_asha: {
    title: "Dr. Asha Mehta — Attending Physician",
    name: "Dr. Asha Mehta",
    email: "dr.asha",
    defaultPass: "password123",
    badgeColor: "bg-[#00f2fe]",
    level: "Tier 1 Cardiology Attending",
    tokenTag: "EHR-FULL",
  },
  nurse_priya: {
    title: "Nurse Priya Sharma — Ward Nurse",
    name: "Nurse Priya Sharma",
    email: "nurse.priya",
    defaultPass: "password123",
    badgeColor: "bg-[#10b981]",
    level: "Tier 2 Ward Cardiology",
    tokenTag: "VITALS-RW",
  },
  clerk_ravi: {
    title: "Ravi Kumar — Billing Clerk",
    name: "Ravi Kumar",
    email: "clerk.ravi",
    defaultPass: "password123",
    badgeColor: "bg-[#f59e0b]",
    level: "Tier 3 Patient Accounts",
    tokenTag: "BILLING-ONLY",
  },
  reviewer_meera: {
    title: "Meera Iyer — SOC Reviewer",
    name: "Meera Iyer",
    email: "rev.meera",
    defaultPass: "password123",
    badgeColor: "bg-[#c084fc]",
    level: "Tier 4 SOC Forensic Reviewer",
    tokenTag: "SOC-TIER4",
  },
  rec_neha: {
    title: "Neha Kapoor — Front Desk",
    name: "Neha Kapoor",
    email: "rec.neha",
    defaultPass: "password123",
    badgeColor: "bg-[#f97316]",
    level: "Tier 3 Demographics Only",
    tokenTag: "DEMOGRAPHICS",
  },
  doctor: {
    title: "Dr. Elena Vance — Federated Clinical Gate",
    name: "Dr. Elena Vance",
    email: "dr.elena.vance@stjude-health.org",
    defaultPass: "password123",
    badgeColor: "bg-[#00f2fe]",
    level: "Tier 1 Clinical Access",
    tokenTag: "EHR-FULL",
  },
  nurse: {
    title: "Nurse Sarah Connor — ICU Charge Nurse",
    name: "Nurse Sarah Connor",
    email: "nurse.sarah.connor@stjude-health.org",
    defaultPass: "password123",
    badgeColor: "bg-[#10b981]",
    level: "Tier 2 ICU Ward Access",
    tokenTag: "VITALS-RW",
  },
  admin: {
    title: "Security Administrator — SysAdmin",
    name: "System Admin",
    email: "admin",
    defaultPass: "password123",
    badgeColor: "bg-[#ef4444]",
    level: "Root Security Operations",
    tokenTag: "ROOT-PERM",
  },
}

export function LoginPage({ onBackToHome, onLoginSuccess }: LoginPageProps) {
  const [selectedRole, setSelectedRole] = useState<RoleKey>("doctor")
  const [email, setEmail] = useState(ROLES.doctor.email)
  const [password, setPassword] = useState(ROLES.doctor.defaultPass)
  const [showPassword, setShowPassword] = useState(false)
  const [bindTerminal, setBindTerminal] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [authStatus, setAuthStatus] = useState<"idle" | "success" | "error">("idle")
  const [errorMessage, setErrorMessage] = useState("")
  const [authenticatedUser, setAuthenticatedUser] = useState<SessionUser | null>(null)

  const handleRoleChange = (role: RoleKey) => {
    setSelectedRole(role)
    setEmail(ROLES[role].email)
    setPassword(ROLES[role].defaultPass)
    setAuthStatus("idle")
    setErrorMessage("")
    setAuthenticatedUser(null)
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)
    setAuthStatus("idle")
    setErrorMessage("")

    try {
      const user = await medsentinelApi.login(email, password)
      setAuthenticatedUser(user)
      setAuthStatus("success")
    } catch (error: unknown) {
      setAuthStatus("error")
      setErrorMessage(error instanceof Error ? error.message : "Unable to authenticate with the MedSentinel backend.")
    } finally {
      setIsSubmitting(false)
    }
  }

  const current = ROLES[selectedRole]

  return (
    <div className="relative min-h-screen w-full bg-[#04070e] text-slate-100 antialiased selection:bg-[#00f2fe] selection:text-black">
      {/* Background Ambience */}
      <div className="pointer-events-none fixed inset-0 z-0 overflow-hidden">
        <div className="absolute -top-[15%] -left-[10%] h-[55vw] w-[55vw] rounded-full bg-gradient-to-br from-cyan-600/15 via-blue-900/10 to-transparent blur-[140px]" />
        <div className="absolute -bottom-[20%] left-[25%] h-[48vw] w-[48vw] rounded-full bg-gradient-to-tr from-emerald-600/10 via-cyan-950/20 to-transparent blur-[160px]" />
        <div className="absolute top-[25%] -right-[15%] h-[45vw] w-[45vw] rounded-full bg-gradient-to-l from-indigo-950/25 via-blue-950/20 to-transparent blur-[140px]" />
        <div className="absolute inset-0 bg-[linear-gradient(to_right,rgba(255,255,255,0.02)_1px,transparent_1px),linear-gradient(to_bottom,rgba(255,255,255,0.02)_1px,transparent_1px)] bg-[size:32px_32px] opacity-60" />
      </div>

      {/* Top Floating Bar */}
      <header className="relative z-30 flex items-center justify-between border-b border-white/[0.06] bg-[#070b14]/70 px-6 py-4 backdrop-blur-md sm:px-10">
        <Button
          variant="ghost"
          size="sm"
          onClick={onBackToHome}
          className="gap-2 text-xs font-medium text-slate-300 hover:bg-white/10 hover:text-white"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Public Site
        </Button>

        <div className="flex items-center gap-3">
          <Badge variant="outline" className="gap-1.5 border-teal-500/30 bg-teal-500/10 text-[11px] text-[#38ef7d]">
            <span className="relative flex h-2 w-2">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-[#38ef7d] opacity-75"></span>
              <span className="relative inline-flex h-2 w-2 rounded-full bg-[#38ef7d]"></span>
            </span>
             MEDSENTINEL BACKEND SESSION GATE ACTIVE
          </Badge>
          <span className="hidden font-mono text-[11px] text-slate-400 sm:inline">
            CLUSTER: <span className="text-[#00f2fe]">US-EAST-04 (ST. JUDE HEALTH)</span>
          </span>
        </div>
      </header>

      {/* Main Split Grid */}
      <div className="relative z-10 flex min-h-[calc(100vh-65px)] flex-col lg:flex-row">
        {/* ========================================================= */}
        {/* LEFT SECTION (58%) - 3D DNA Helix & Clinical Telemetry */}
        {/* ========================================================= */}
        <div className="relative flex w-full flex-col justify-between border-b border-white/[0.06] p-6 sm:p-10 lg:w-[58%] lg:border-r lg:border-b-0 lg:p-12">
          {/* Brand header */}
          <div className="z-20 flex flex-wrap items-center justify-between gap-4">
            <div className="flex items-center space-x-3.5">
              <div className="relative flex h-11 w-11 items-center justify-center rounded-xl border border-[#00f2fe]/40 bg-[#00f2fe]/10 shadow-[0_0_15px_rgba(0,242,254,0.3)]">
                <Shield className="h-5 w-5 text-[#00f2fe]" />
              </div>
              <div>
                <div className="flex items-center space-x-2.5">
                  <span className="text-2xl font-bold tracking-wider text-white">KARUNA</span>
                  <Badge className="border-[#00f2fe]/40 bg-[#00f2fe]/15 font-mono text-[9px] tracking-widest text-[#00f2fe]">
                    EHR SHIELD v4.2
                  </Badge>
                </div>
                <p className="font-mono text-[10px] tracking-wider text-slate-400 uppercase">
                  Zero-Trust EHR Perimeter Access
                </p>
              </div>
            </div>

          </div>

          {/* 3D DNA Canvas Viewport */}
          <div className="relative my-4 flex min-h-[460px] flex-1 items-center justify-center overflow-hidden lg:my-0 lg:min-h-[500px]">
            {/* Ambient circle glow */}
            <div className="pointer-events-none absolute h-72 w-72 rounded-full bg-gradient-to-r from-[#00f2fe]/10 via-purple-500/10 to-rose-500/10 blur-3xl" />

            {/* Three.js Canvas */}
            <div className="absolute inset-0 z-10">
              <ThreeDnaCanvas />
            </div>

          </div>

          {/* Bottom Manifesto */}
          <div className="relative z-20 max-w-xl">
            <h1 className="mb-2 text-2xl font-bold tracking-tight text-white sm:text-3xl">
              Trust is built into{" "}
              <span className="bg-gradient-to-r from-white via-cyan-100 to-[#00f2fe] bg-clip-text font-serif italic text-transparent">
                every single clinical access.
              </span>
            </h1>
            <p className="text-xs font-light leading-relaxed text-slate-400 sm:text-sm">
              Zero-Trust EHR Perimeter Access, continuous cryptographic audit trails, and predictive anomaly detection
              safeguarding sensitive patient telemetry across hospital networks.
            </p>
          </div>
        </div>

        {/* ========================================================= */}
        {/* RIGHT SECTION (42%) - Floating Glass Login Panel (shadcn) */}
        {/* ========================================================= */}
        <div className="relative z-20 flex w-full items-center justify-center p-6 sm:p-10 lg:w-[42%] lg:p-12">
          <div className="relative w-full max-w-md">
            {/* Glow backing */}
            <div className="pointer-events-none absolute -inset-1 rounded-3xl bg-gradient-to-b from-[#00f2fe]/20 via-transparent to-[#38ef7d]/15 blur-2xl opacity-70" />

            {/* shadcn Card */}
            <Card className="relative border-white/[0.1] bg-[#0c121e]/85 shadow-[0_24px_60px_-12px_rgba(0,0,0,0.9)] backdrop-blur-2xl">
              <CardHeader className="border-b border-white/[0.06] pb-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2.5">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg border border-[#00f2fe]/30 bg-[#00f2fe]/10 text-[#00f2fe]">
                      <Fingerprint className="h-4 w-4" />
                    </div>
                    <div>
                      <CardTitle className="font-mono text-xs font-semibold tracking-wider text-[#00f2fe] uppercase">
                        Access Gate 04
                      </CardTitle>
                      <CardDescription className="font-mono text-[10px] text-slate-400">
                        Zero-Trust Clinical Gateway
                      </CardDescription>
                    </div>
                  </div>
                  <div className="text-right">
                    <span className="block font-mono text-[9px] text-slate-400">LATENCY</span>
                    <span className="font-mono text-xs font-semibold text-[#38ef7d]">14ms</span>
                  </div>
                </div>
              </CardHeader>

              <CardContent className="space-y-4 pt-6">
                {/* Auth Error Banner */}
                {authStatus === "error" && (
                  <div className="flex items-start space-x-2.5 rounded-xl border border-rose-500/40 bg-rose-950/40 p-3 text-xs text-rose-200">
                    <ShieldAlert className="mt-0.5 h-4 w-4 shrink-0 text-[#ff5370]" />
                    <div>
                      <div className="font-mono text-[11px] font-bold text-[#ff5370]">
                         AUTHENTICATION GATE ERROR
                      </div>
                      <p className="mt-0.5 text-[11px] text-slate-300">
                         {errorMessage || "The backend rejected this authentication attempt."}
                      </p>
                    </div>
                  </div>
                )}

                {/* Auth Success Banner */}
                {authStatus === "success" && (
                  <div className="flex flex-col space-y-2 rounded-xl border border-emerald-400/40 bg-emerald-950/40 p-3.5 text-xs text-emerald-200">
                    <div className="flex items-center space-x-2">
                      <ShieldCheck className="h-4 w-4 text-[#38ef7d]" />
                      <span className="text-xs font-semibold text-white">Identity & Ward Cryptokey Verified</span>
                    </div>
                    <p className="text-[11px] text-slate-300">
                       Establishing isolated session for{" "}
                       <span className="font-mono text-[#00f2fe]">{authenticatedUser?.name || current.name}</span>. Enforcing audit policy...
                    </p>
                    <div className="mt-1 h-1.5 w-full overflow-hidden rounded-full bg-slate-900">
                      <div className="h-full w-full animate-pulse rounded-full bg-gradient-to-r from-[#00f2fe] to-[#38ef7d]" />
                    </div>
                    <Button
                      type="button"
                      size="sm"
                      onClick={() => authenticatedUser && onLoginSuccess?.(authenticatedUser)}
                      className="mt-2 bg-[#38ef7d] text-slate-950 hover:bg-[#34d399] font-bold text-xs"
                    >
                      Enter MedSentinel Dashboard Now →
                    </Button>
                  </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                  {/* Staff Role Selector (shadcn DropdownMenu) */}
                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between">
                      <Label className="font-mono text-[11px] tracking-wider text-slate-300 uppercase">
                        Staff Access Role
                      </Label>
                      <Badge variant="outline" className="border-teal-500/30 font-mono text-[9px] text-[#38ef7d]">
                        {current.level}
                      </Badge>
                    </div>

                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button
                          variant="outline"
                          className="w-full justify-between border-white/[0.1] bg-slate-950/80 px-3.5 py-2.5 text-left text-sm text-white hover:border-slate-500 hover:bg-slate-900"
                        >
                          <div className="flex items-center space-x-2.5">
                            <span className={`h-2.5 w-2.5 rounded-full ${current.badgeColor} shadow-[0_0_8px_#00f2fe]`} />
                            <span className="truncate font-medium">{current.title}</span>
                          </div>
                          <ChevronDown className="h-4 w-4 text-slate-400" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent className="w-80 border-[#00f2fe]/30 bg-[#080d1a] text-slate-200 backdrop-blur-2xl">
                        <DropdownMenuLabel className="font-mono text-[10px] text-slate-400 uppercase">
                          Select Clinical Role
                        </DropdownMenuLabel>
                        <DropdownMenuSeparator className="bg-white/10" />
                        {(Object.keys(ROLES) as RoleKey[]).map((key) => {
                          const r = ROLES[key]
                          return (
                            <DropdownMenuItem
                              key={key}
                              onClick={() => handleRoleChange(key)}
                              className="flex cursor-pointer items-center justify-between py-2 text-xs hover:bg-[#00f2fe]/15 hover:text-white"
                            >
                              <div className="flex items-center space-x-2">
                                <span className={`h-2 w-2 rounded-full ${r.badgeColor}`} />
                                <span className="font-medium">{r.title}</span>
                              </div>
                              <Badge variant="outline" className="border-white/20 font-mono text-[9px] text-[#00f2fe]">
                                {r.tokenTag}
                              </Badge>
                            </DropdownMenuItem>
                          )
                        })}
                      </DropdownMenuContent>
                    </DropdownMenu>

                    {/* Quick Demo Accounts Selection */}
                    <div className="pt-2">
                      <span className="block font-mono text-[9px] uppercase tracking-wider text-slate-400 mb-1.5">
                        Quick Demo Accounts:
                      </span>
                      <div className="flex flex-wrap gap-1.5">
                        {[
                          { key: "dr_asha" as RoleKey, label: "Dr. Asha" },
                          { key: "nurse_priya" as RoleKey, label: "Nurse Priya" },
                           { key: "clerk_ravi" as RoleKey, label: "Ravi (Billing)" },
                           { key: "rec_neha" as RoleKey, label: "Neha (Front Desk)" },
                           { key: "reviewer_meera" as RoleKey, label: "Meera (SOC)" },
                          { key: "doctor" as RoleKey, label: "Dr. Vance" },
                          { key: "admin" as RoleKey, label: "Admin" },
                        ].map((item) => (
                          <button
                            key={item.key}
                            type="button"
                            onClick={() => handleRoleChange(item.key)}
                            className={`rounded px-2 py-0.5 text-[10px] font-mono border transition cursor-pointer ${
                              selectedRole === item.key
                                ? "border-[#00f2fe] bg-[#00f2fe]/20 text-[#00f2fe] font-bold"
                                : "border-white/10 bg-white/5 text-slate-300 hover:border-slate-400"
                            }`}
                          >
                            {item.label}
                          </button>
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Institutional Email Field (shadcn Input) */}
                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="email" className="font-mono text-[11px] tracking-wider text-slate-300 uppercase">
                        Institutional ID / Email
                      </Label>
                      <span className="font-mono text-[9px] text-slate-500">LDAP / SSO FEDERATED</span>
                    </div>
                    <div className="relative">
                      <Mail className="absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2 text-slate-500" />
                      <Input
                        id="email"
                        type="text"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="name@stjude-health.org"
                        required
                        className="border-white/[0.1] bg-slate-950/80 pl-9 font-sans text-sm text-white placeholder:text-slate-500 focus-visible:border-[#00f2fe] focus-visible:ring-[#00f2fe]"
                      />
                    </div>
                  </div>

                  {/* Passphrase Field (shadcn Input) */}
                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between">
                      <Label
                        htmlFor="password"
                        className="font-mono text-[11px] tracking-wider text-slate-300 uppercase"
                      >
                        Biometric Master Passphrase
                      </Label>
                      <span className="font-mono text-[9px] text-slate-500">SESSION COOKIE AUTHENTICATION</span>
                    </div>
                    <div className="relative">
                      <KeyRound className="absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2 text-slate-500" />
                      <Input
                        id="password"
                        type={showPassword ? "text" : "password"}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="Enter cryptographic keyphrase"
                        required
                        className="border-white/[0.1] bg-slate-950/80 pr-10 pl-9 font-sans text-sm text-white placeholder:text-slate-500 focus-visible:border-[#00f2fe] focus-visible:ring-[#00f2fe]"
                      />
                      <button
                        type="button"
                        aria-label={showPassword ? "Hide passphrase" : "Show passphrase"}
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute top-1/2 right-3 -translate-y-1/2 text-slate-400 hover:text-[#00f2fe]"
                      >
                        {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                      </button>
                    </div>
                  </div>
                  {/* Terminal Binding Switch (shadcn Switch) */}
                  <div className="flex items-center justify-between py-1">
                    <div className="flex items-center space-x-2">
                      <Switch id="bind-terminal" checked={bindTerminal} onCheckedChange={setBindTerminal} />
                      <Label htmlFor="bind-terminal" className="cursor-pointer text-xs font-normal text-slate-300">
                        Bind session to terminal
                      </Label>
                    </div>
                    <span className="font-mono text-[10px] text-slate-400">30-day TLS token</span>
                  </div>

                  {/* Submit Button (shadcn Button with gradient) */}
                  <Button
                    type="submit"
                    disabled={isSubmitting}
                    className="relative w-full overflow-hidden bg-gradient-to-r from-[#00f2fe] via-[#4ee39d] to-[#38ef7d] py-5 font-bold tracking-wider text-slate-950 uppercase shadow-[0_0_24px_rgba(0,242,254,0.35)] transition-all hover:shadow-[0_0_35px_rgba(0,242,254,0.6)]"
                  >
                    {isSubmitting ? (
                      <span className="flex items-center space-x-2 font-mono text-xs">
                        <Loader2 className="h-4 w-4 animate-spin text-slate-950" />
                        <span>Verifying Clinical Token...</span>
                      </span>
                    ) : (
                      <span className="flex items-center justify-center space-x-1.5 text-xs font-bold text-slate-950">
                        <span>Authenticate Perimeter Access</span>
                        <ArrowRight className="h-4 w-4" />
                      </span>
                    )}
                  </Button>
                </form>
              </CardContent>

              <CardFooter className="flex flex-col space-y-3 border-t border-white/[0.06] pt-4 text-center">
                <div className="flex items-center justify-center space-x-1.5 font-mono text-[11px] text-slate-400">
                  <Lock className="h-3.5 w-3.5 text-[#00f2fe]" />
                  <span>Zero-Trust EHR Perimeter Access Enforced</span>
                </div>
                <p className="font-mono text-[10px] tracking-widest text-slate-500 uppercase">
                  GATEWAY: US-EAST-04 [ST. JUDE MEDICAL CLUSTER] • LATENCY: 14ms
                </p>

              </CardFooter>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}
