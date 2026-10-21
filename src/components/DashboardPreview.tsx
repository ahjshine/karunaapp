import { useState } from "react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import {
  Shield,
  Lock,
  Activity,
  AlertTriangle,
  FileText,
  Users,
  CheckCircle2,
  TrendingUp,
  Clock,
  ChevronRight,
} from "lucide-react"

interface DashboardPreviewProps {
  onInspectAlert?: (alertId: string) => void
}

interface AlertItem {
  id: string
  severity: "high" | "medium" | "low"
  title: string
  description: string
  actor: string
  role: string
  tags: string[]
  timeAgo: string
}

const ALERTS: AlertItem[] = [
  {
    id: "A-101",
    severity: "high",
    title: "Billing clerk opened 38 clinical records in 7 minutes.",
    description: "Role baseline is 0.",
    actor: "Elena Ruiz",
    role: "Billing clerk",
    tags: ["R2 Bulk", "R3 Out-of-scope"],
    timeAgo: "2m ago",
  },
  {
    id: "A-102",
    severity: "medium",
    title: "5 failed logins, followed by a successful login.",
    description: "Source 10.2.4.77",
    actor: "dr.asha",
    role: "Physician",
    tags: ["R1 Failed logins"],
    timeAgo: "18m ago",
  },
  {
    id: "A-103",
    severity: "medium",
    title: "Emergency access used for an off-ward patient.",
    description: "Justification provided.",
    actor: "Dr. Asha Mehta",
    role: "Physician",
    tags: ["Break-glass"],
    timeAgo: "42m ago",
  },
  {
    id: "A-104",
    severity: "low",
    title: "Concurrent session detected across two workstations.",
    description: "ED Station 3 & Station 7",
    actor: "Dr. Alex Wong",
    role: "Emergency Physician",
    tags: ["Session anomaly"],
    timeAgo: "1h ago",
  },
]

export function DashboardPreview({ onInspectAlert }: DashboardPreviewProps) {
  const [activeTab, setActiveTab] = useState<"all" | "high" | "medium" | "low">("all")
  const [selectedAlertId, setSelectedAlertId] = useState<string>("A-101")

  const filteredAlerts = ALERTS.filter((a) => {
    if (activeTab === "all") return true
    return a.severity === activeTab
  })

  return (
    <div className="relative mx-auto w-full max-w-6xl">
      {/* Glow shadow behind frame */}
      <div className="pointer-events-none absolute -inset-2 rounded-2xl bg-teal-500/10 blur-2xl opacity-75" />

      {/* Browser Shell Frame */}
      <div className="relative overflow-hidden rounded-xl border border-[#35455c] bg-[#0f172a] shadow-[0px_35px_80px_0px_rgba(3,8,18,0.45)]">
        {/* Top Browser Bar */}
        <div className="flex h-10 items-center justify-between border-b border-[#2a3950] bg-[#18243a] px-4">
          <div className="flex items-center gap-1.5">
            <div className="h-2.5 w-2.5 rounded-full bg-rose-500/70" />
            <div className="h-2.5 w-2.5 rounded-full bg-amber-500/70" />
            <div className="h-2.5 w-2.5 rounded-full bg-emerald-500/70" />
          </div>

          <div className="flex items-center gap-2 rounded-md border border-[#2a3950] bg-[#0f172a]/70 px-4 py-1 text-xs text-[#738299]">
            <Lock className="h-3 w-3 text-emerald-400" />
            <span className="font-mono">secure.karuna.health</span>
          </div>

          <div className="font-mono text-[10px] tracking-wider text-[#738299] uppercase">
            Clinical Overview
          </div>
        </div>

        {/* Dashboard Content Grid */}
        <div className="grid grid-cols-1 md:grid-cols-[180px_1fr] lg:grid-cols-[200px_1fr]">
          {/* Sidebar */}
          <aside className="hidden flex-col justify-between border-r border-[#26354b] bg-[#0c1425] p-3 text-xs md:flex">
            <div className="space-y-4">
              <div className="flex items-center gap-2 border-b border-[#26354b] pb-3">
                <div className="flex h-6 w-6 items-center justify-center rounded-md bg-[#14b8a6]/20 text-[#14b8a6]">
                  <Shield className="h-3.5 w-3.5" />
                </div>
                <span className="font-bold text-white">
                  Karuna<span className="text-[#14b8a6]">.</span>
                </span>
              </div>

              <div>
                <span className="px-2 font-mono text-[9px] font-semibold tracking-wider text-[#607189] uppercase">
                  Monitor
                </span>
                <nav className="mt-1 space-y-1">
                  <button className="flex w-full items-center gap-2.5 rounded-md bg-[#14b8a6]/10 px-2.5 py-1.5 font-medium text-[#5eead4]">
                    <Activity className="h-3.5 w-3.5" />
                    <span>Overview</span>
                  </button>
                  <button className="flex w-full items-center justify-between rounded-md px-2.5 py-1.5 text-[#8fa0b6] hover:bg-white/5 hover:text-white">
                    <div className="flex items-center gap-2.5">
                      <AlertTriangle className="h-3.5 w-3.5" />
                      <span>Alerts</span>
                    </div>
                    <Badge variant="outline" className="border-rose-500/30 bg-rose-500/10 px-1 py-0 text-[10px] text-rose-300">
                      4
                    </Badge>
                  </button>
                  <button className="flex w-full items-center gap-2.5 rounded-md px-2.5 py-1.5 text-[#8fa0b6] hover:bg-white/5 hover:text-white">
                    <FileText className="h-3.5 w-3.5" />
                    <span>Log explorer</span>
                  </button>
                  <button className="flex w-full items-center gap-2.5 rounded-md px-2.5 py-1.5 text-[#8fa0b6] hover:bg-white/5 hover:text-white">
                    <Users className="h-3.5 w-3.5" />
                    <span>Clinical access</span>
                  </button>
                </nav>
              </div>

              <div>
                <span className="px-2 font-mono text-[9px] font-semibold tracking-wider text-[#607189] uppercase">
                  Tools
                </span>
                <nav className="mt-1 space-y-0.5 text-[#8fa0b6]">
                  <div className="rounded-md px-2.5 py-1 hover:text-white cursor-pointer">Scenario simulator</div>
                  <div className="rounded-md px-2.5 py-1 hover:text-white cursor-pointer">Baselines</div>
                  <div className="rounded-md px-2.5 py-1 hover:text-white cursor-pointer">Users & roles</div>
                </nav>
              </div>
            </div>

            {/* Bottom System Status Badge */}
            <div className="rounded-lg border border-[#284152] bg-[#132638] p-2.5">
              <div className="flex items-center gap-2 text-[11px] font-semibold text-[#b7f7e9]">
                <span className="h-2 w-2 rounded-full bg-emerald-500 shadow-[0_0_8px_#22c55e]" />
                <span>System operational</span>
              </div>
              <div className="mt-2 border-t border-[#294052] pt-1.5 font-mono text-[10px] text-[#94a3b8]">
                <div>Log chain verified</div>
                <div className="text-[9px] text-[#52647a]">Checked 4 min ago</div>
              </div>
            </div>
          </aside>

          {/* Main Cockpit Area */}
          <main className="flex flex-col bg-[#0f172a]">
            {/* Top Bar Header */}
            <div className="flex flex-wrap items-center justify-between border-b border-[#26354b] px-4 py-2.5 sm:px-6">
              <div>
                <div className="font-mono text-[10px] tracking-wider text-[#77879e] uppercase">
                  St. Anne’s Hospital
                </div>
                <div className="text-sm font-semibold text-[#f8fafc]">Security Overview</div>
              </div>

              <div className="flex items-center gap-3">
                <div className="text-right">
                  <div className="text-xs font-medium text-[#f8fafc]">Marcus Chen</div>
                  <div className="font-mono text-[10px] text-[#718298]">Security reviewer</div>
                </div>
                <Avatar className="h-8 w-8 border border-[#14b8a6]/40 bg-[#243249]">
                  <AvatarFallback className="bg-[#243249] text-xs font-semibold text-[#5eead4]">MC</AvatarFallback>
                </Avatar>
              </div>
            </div>

            {/* Welcome Banner */}
            <div className="flex flex-wrap items-end justify-between border-b border-[#24334a]/40 p-4 sm:p-5">
              <div>
                <h3 className="text-lg font-bold text-[#f8fafc]">Good morning, Marcus</h3>
                <p className="text-xs text-[#77879e]">Here’s what needs your attention across St. Anne’s Hospital.</p>
              </div>
              <div className="flex items-center gap-2 font-mono text-xs text-[#8ba39f]">
                <Clock className="h-3.5 w-3.5" />
                <span>Mon, 12 Oct · 09:42</span>
              </div>
            </div>

            {/* 4 Stat KPIs */}
            <div className="grid grid-cols-2 gap-3 p-4 sm:grid-cols-4 sm:p-5">
              <div className="rounded-lg border border-[#24334a] bg-[#111d31] p-3 transition hover:border-[#14b8a6]/40">
                <div className="font-mono text-[10px] text-[#8b9bb0]">Open alerts</div>
                <div className="mt-1 text-2xl font-bold tracking-tight text-[#f8fafc]">4</div>
                <div className="mt-1 flex items-center text-[10px] text-[#fb7185]">
                  <TrendingUp className="mr-1 h-3 w-3" /> ↑ 1 since yesterday
                </div>
              </div>

              <div className="rounded-lg border border-[#24334a] bg-[#111d31] p-3 transition hover:border-[#14b8a6]/40">
                <div className="font-mono text-[10px] text-[#8b9bb0]">High severity</div>
                <div className="mt-1 text-2xl font-bold tracking-tight text-[#f8fafc]">1</div>
                <div className="mt-1 text-[10px] text-[#6f8098]">Requires immediate review</div>
              </div>

              <div className="rounded-lg border border-[#24334a] bg-[#111d31] p-3 transition hover:border-[#14b8a6]/40">
                <div className="font-mono text-[10px] text-[#8b9bb0]">Failed logins · 24h</div>
                <div className="mt-1 text-2xl font-bold tracking-tight text-[#f8fafc]">12</div>
                <div className="mt-1 text-[10px] text-[#57cbbb]">↓ 20% from prior day</div>
              </div>

              <div className="rounded-lg border border-[#24334a] bg-[#111d31] p-3 transition hover:border-[#14b8a6]/40">
                <div className="font-mono text-[10px] text-[#8b9bb0]">Log integrity</div>
                <div className="mt-1 flex items-center gap-1.5 text-base font-bold text-[#5eead4]">
                  <CheckCircle2 className="h-4 w-4" /> Verified
                </div>
                <div className="mt-1 text-[10px] text-[#6f8098]">12,408 rows · 4 min ago</div>
              </div>
            </div>

            {/* Split Lower View: Live Alerts vs Activity & Risk Users */}
            <div className="grid grid-cols-1 gap-4 p-4 pt-0 lg:grid-cols-[1.5fr_1fr] sm:p-5 sm:pt-0">
              {/* Alert Feed Card */}
              <div className="rounded-lg border border-[#24334a] bg-[#111d31] p-4">
                <div className="flex items-center justify-between pb-3">
                  <div>
                    <h4 className="text-xs font-bold text-[#f8fafc]">Open Alerts</h4>
                    <p className="text-[10px] text-[#718197]">Events that differ from role and ward baselines</p>
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-7 text-[11px] font-semibold text-[#14b8a6] hover:bg-[#14b8a6]/10"
                    onClick={() => onInspectAlert?.("A-101")}
                  >
                    View All (4)
                  </Button>
                </div>

                {/* Filter Tabs */}
                <div className="mb-3 flex items-center gap-1.5 border-y border-[#24334a] py-1.5 text-[11px]">
                  <button
                    onClick={() => setActiveTab("all")}
                    className={`rounded px-2.5 py-0.5 font-medium transition ${
                      activeTab === "all" ? "bg-[#26374b] text-[#dbe5ee]" : "text-[#718198] hover:text-white"
                    }`}
                  >
                    All 4
                  </button>
                  <button
                    onClick={() => setActiveTab("high")}
                    className={`rounded px-2.5 py-0.5 font-medium transition ${
                      activeTab === "high" ? "bg-[#26374b] text-rose-300" : "text-[#718198] hover:text-white"
                    }`}
                  >
                    High 1
                  </button>
                  <button
                    onClick={() => setActiveTab("medium")}
                    className={`rounded px-2.5 py-0.5 font-medium transition ${
                      activeTab === "medium" ? "bg-[#26374b] text-amber-300" : "text-[#718198] hover:text-white"
                    }`}
                  >
                    Medium 2
                  </button>
                  <button
                    onClick={() => setActiveTab("low")}
                    className={`rounded px-2.5 py-0.5 font-medium transition ${
                      activeTab === "low" ? "bg-[#26374b] text-blue-300" : "text-[#718198] hover:text-white"
                    }`}
                  >
                    Low 1
                  </button>
                </div>

                {/* Alert Rows */}
                <div className="space-y-2">
                  {filteredAlerts.map((alert) => (
                    <div
                      key={alert.id}
                      onClick={() => {
                        setSelectedAlertId(alert.id)
                        onInspectAlert?.(alert.id)
                      }}
                      className={`group flex cursor-pointer items-start justify-between rounded-lg border p-3 transition ${
                        selectedAlertId === alert.id
                          ? "border-[#14b8a6] bg-[#14b8a6]/[0.06]"
                          : "border-[#24334a]/80 bg-[#0e1726]/60 hover:border-[#3a4d6b]"
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        {/* Severity Badge */}
                        <div
                          className={`mt-0.5 flex items-center gap-1.5 rounded px-2 py-0.5 text-[10px] font-semibold ${
                            alert.severity === "high"
                              ? "bg-rose-500/10 text-rose-300"
                              : alert.severity === "medium"
                              ? "bg-amber-500/10 text-amber-300"
                              : "bg-blue-500/10 text-blue-300"
                          }`}
                        >
                          <span
                            className={`h-1.5 w-1.5 rounded-full ${
                              alert.severity === "high"
                                ? "bg-rose-400"
                                : alert.severity === "medium"
                                ? "bg-amber-400"
                                : "bg-blue-400"
                            }`}
                          />
                          <span className="capitalize">{alert.severity}</span>
                        </div>

                        <div>
                          <div className="flex items-center gap-2">
                            <span className="font-mono text-[10px] text-[#5f7289]">{alert.id}</span>
                            <span className="font-mono text-[9px] text-emerald-400">OPEN</span>
                          </div>
                          <p className="mt-0.5 text-xs font-semibold text-[#f8fafc] group-hover:text-[#5eead4]">
                            {alert.title}
                          </p>
                          <p className="text-[11px] text-[#73849b]">{alert.description}</p>
                          <div className="mt-2 flex flex-wrap items-center gap-2 text-[10px] text-[#77899f]">
                            <span>
                              {alert.actor} · {alert.role}
                            </span>
                            {alert.tags.map((tag) => (
                              <Badge
                                key={tag}
                                variant="outline"
                                className="border-[#3b4a60] bg-[#222f43] px-1.5 py-0 text-[9px] text-[#90a1b6]"
                              >
                                {tag}
                              </Badge>
                            ))}
                          </div>
                        </div>
                      </div>

                      <div className="flex flex-col items-end gap-2 text-right">
                        <span className="font-mono text-[10px] text-[#73849b]">{alert.timeAgo}</span>
                        <ChevronRight className="h-4 w-4 text-slate-500 transition-transform group-hover:translate-x-1 group-hover:text-[#14b8a6]" />
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Right Telemetry Column */}
              <div className="space-y-4">
                {/* Access Activity Chart */}
                <div className="rounded-lg border border-[#24334a] bg-[#111d31] p-4">
                  <div className="flex items-center justify-between pb-2">
                    <div>
                      <div className="text-xs font-bold text-[#f8fafc]">Access Activity</div>
                      <div className="text-[10px] text-[#718197]">Events by hour · today</div>
                    </div>
                    <Badge variant="outline" className="border-teal-500/30 font-mono text-[10px] text-[#14b8a6]">
                      24h Live
                    </Badge>
                  </div>

                  <div className="flex items-center gap-4 py-1 text-[10px]">
                    <div className="flex items-center gap-1.5 text-slate-300">
                      <span className="h-2 w-2 rounded-full bg-[#14b8a6]" />
                      <span>Allowed</span>
                    </div>
                    <div className="flex items-center gap-1.5 text-slate-300">
                      <span className="h-2 w-2 rounded-full bg-rose-500" />
                      <span>Failed</span>
                    </div>
                  </div>

                  {/* Hourly activity bars representation */}
                  <div className="mt-3 flex h-24 items-end justify-between gap-1 border-b border-[#24334a] pb-2">
                    {[
                      { time: "00:00", allowed: 40, failed: 2 },
                      { time: "04:00", allowed: 25, failed: 1 },
                      { time: "08:00", allowed: 85, failed: 8 },
                      { time: "12:00", allowed: 95, failed: 4 },
                      { time: "16:00", allowed: 70, failed: 3 },
                      { time: "Now", allowed: 90, failed: 12 },
                    ].map((col) => (
                      <div key={col.time} className="flex flex-1 flex-col items-center gap-1">
                        <div className="flex w-full items-end justify-center gap-1">
                          <div
                            style={{ height: `${col.allowed}%` }}
                            className="w-2.5 rounded-t bg-[#14b8a6]/80 transition-all hover:bg-[#14b8a6]"
                          />
                          <div
                            style={{ height: `${col.failed * 2}%` }}
                            className="w-2.5 rounded-t bg-rose-500/80 transition-all hover:bg-rose-500"
                          />
                        </div>
                        <span className="font-mono text-[9px] text-[#5e6e84]">{col.time}</span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Top-Risk Users */}
                <div className="rounded-lg border border-[#24334a] bg-[#111d31] p-4">
                  <div className="pb-2">
                    <div className="text-xs font-bold text-[#f8fafc]">Top-Risk Users</div>
                    <div className="text-[10px] text-[#718197]">Ranked by current clinical activity</div>
                  </div>

                  <div className="divide-y divide-[#24334a]">
                    {[
                      { rank: "01", initials: "ER", name: "Elena Ruiz", role: "Billing clerk", score: 75, color: "text-rose-400 bg-rose-500/10" },
                      { rank: "02", initials: "AM", name: "Dr. Asha Mehta", role: "Physician", score: 55, color: "text-amber-400 bg-amber-500/10" },
                      { rank: "03", initials: "MP", name: "Maya Patel", role: "Receptionist", score: 40, color: "text-blue-400 bg-blue-500/10" },
                    ].map((user) => (
                      <div key={user.rank} className="flex items-center justify-between py-2 text-xs">
                        <div className="flex items-center gap-2.5">
                          <span className="font-mono text-[10px] text-[#516278]">{user.rank}</span>
                          <Avatar className="h-6 w-6 bg-[#194443] text-[10px] font-bold text-[#99f6e4]">
                            <AvatarFallback className="bg-[#194443] text-[10px]">{user.initials}</AvatarFallback>
                          </Avatar>
                          <div>
                            <div className="font-medium text-[#dbe5ee]">{user.name}</div>
                            <div className="text-[10px] text-[#65778d]">{user.role}</div>
                          </div>
                        </div>
                        <span className={`rounded px-2 py-0.5 font-mono text-[11px] font-bold ${user.color}`}>
                          {user.score}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </main>
        </div>
      </div>

      {/* High-priority floating badge card */}
      <div className="absolute -bottom-6 right-4 z-30 hidden rounded-xl border border-teal-500/40 border-l-4 border-l-[#14b8a6] bg-[#f8fafc] p-4 text-[#0b1220] shadow-2xl sm:block sm:w-64">
        <div className="flex items-center gap-1.5 text-[10px] font-bold tracking-wider text-rose-600 uppercase">
          <span className="h-2 w-2 rounded-full bg-rose-500" />
          <span>High Priority Signal</span>
        </div>
        <div className="mt-1 text-sm font-bold">Context, not just an alert.</div>
        <div className="text-xs text-slate-500">Role and ward-aware activity</div>
      </div>
    </div>
  )
}
