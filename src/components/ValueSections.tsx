import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  ArrowRight,
  CheckCircle2,
  Shield,
} from "lucide-react"

interface ValueSectionsProps {
  onOpenDemo: () => void
  onInspectAlert?: (alertId: string) => void
}

export function ValueSections({ onOpenDemo, onInspectAlert }: ValueSectionsProps) {
  return (
    <section className="bg-[#f6f7f4] py-20 lg:py-28">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        {/* Section Heading */}
        <div className="max-w-3xl">
          <div className="flex items-center gap-2">
            <span className="h-px w-6 bg-[#14b8a6]" />
            <span className="font-mono text-xs font-bold tracking-widest text-[#496159] uppercase">The platform</span>
          </div>

          <h2 className="mt-4 text-3xl font-light tracking-tight text-[#0b1220] sm:text-5xl lg:text-6xl">
            Built around the realities of{" "}
            <span className="font-serif italic font-normal text-[#14b8a6]">clinical access.</span>
          </h2>
        </div>

        {/* 4 Feature Articles */}
        <div className="mt-16 space-y-20 lg:space-y-28">
          {/* Article 01: Know the baseline */}
          <div className="grid grid-cols-1 items-center gap-12 border-t border-[#d7ddd9] pt-12 lg:grid-cols-2 lg:gap-16">
            <div className="space-y-4">
              <div className="flex items-center gap-2 font-mono text-xs font-bold text-[#14b8a6]">
                <span>01</span>
                <span>·</span>
                <span className="text-[#496159] uppercase">Know the baseline</span>
              </div>

              <h3 className="text-2xl font-light tracking-tight text-[#0b1220] sm:text-4xl">
                Understand normal access.
              </h3>

              <p className="text-base leading-relaxed text-[#586864]">
                Role-aware and ward-aware patterns help teams see how everyday clinical activity actually moves—across
                shifts, specialties, and care settings.
              </p>

              <div className="pt-2">
                <Button
                  variant="link"
                  onClick={onOpenDemo}
                  className="group h-auto p-0 text-sm font-semibold text-[#0d514f] hover:text-[#14b8a6]"
                >
                  <span>See the platform</span>
                  <ArrowRight className="ml-2 h-4 w-4 transition-transform group-hover:translate-x-1" />
                </Button>
              </div>
            </div>

            {/* Visual 01: Expected Activity Range */}
            <div className="rounded-2xl border border-[#26354b] bg-[#0f172a] p-6 shadow-2xl sm:p-8">
              <div className="flex items-center justify-between border-b border-[#26354b] pb-4">
                <span className="font-mono text-[10px] tracking-wider text-[#7f90a7] uppercase">
                  Expected Activity Range
                </span>
                <Badge variant="outline" className="border-teal-500/30 font-mono text-[10px] text-[#14b8a6]">
                  Live Telemetry
                </Badge>
              </div>

              {/* Dynamic waveform / baseline visualization */}
              <div className="my-8 flex h-40 items-center justify-center rounded-xl bg-slate-900/60 p-4">
                <div className="relative flex h-full w-full items-center justify-between gap-1">
                  {/* Baseline curve representation */}
                  {[
                    { hour: "06:00", value: 30, baseline: 35 },
                    { hour: "08:00", value: 65, baseline: 70 },
                    { hour: "10:00", value: 85, baseline: 80 },
                    { hour: "12:00", value: 95, baseline: 90 },
                    { hour: "14:00", value: 75, baseline: 80 },
                    { hour: "16:00", value: 80, baseline: 85 },
                    { hour: "18:00", value: 50, baseline: 55 },
                  ].map((pt) => (
                    <div key={pt.hour} className="flex h-full flex-1 flex-col items-center justify-end gap-2">
                      <div className="relative flex w-full items-end justify-center">
                        <div
                          style={{ height: `${pt.value}%` }}
                          className="w-full max-w-[20px] rounded-t bg-gradient-to-t from-teal-500/20 to-[#14b8a6]/70 transition-all hover:to-[#14b8a6]"
                        />
                      </div>
                      <span className="font-mono text-[9px] text-[#64758d]">{pt.hour}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="flex items-center justify-between text-xs text-[#64758d]">
                <div className="flex items-center gap-2">
                  <span className="h-2 w-2 rounded-full bg-[#14b8a6]" />
                  <span className="text-[#91a5a0]">Within role and ward baseline</span>
                </div>
                <span className="font-mono text-[10px] text-teal-400">99.4% compliant</span>
              </div>
            </div>
          </div>

          {/* Article 02: Find the signal */}
          <div className="grid grid-cols-1 items-center gap-12 border-t border-[#d7ddd9] pt-12 lg:grid-cols-2 lg:gap-16">
            {/* Visual 02: Activity Requiring Review (placed left on desktop) */}
            <div className="order-2 rounded-2xl border border-[#26354b] bg-[#0f172a] p-6 shadow-2xl sm:p-8 lg:order-1">
              <div className="flex items-center justify-between border-b border-[#26354b] pb-4">
                <span className="font-mono text-[10px] tracking-wider text-[#7f90a7] uppercase">
                  Activity Requiring Review
                </span>
                <Badge variant="outline" className="border-rose-500/30 bg-rose-500/10 font-mono text-[10px] text-rose-300">
                  3 signals
                </Badge>
              </div>

              <div className="mt-4 space-y-3">
                {/* Alert Item A-101 */}
                <div
                  onClick={() => onInspectAlert?.("A-101")}
                  className="cursor-pointer rounded-xl border border-rose-500/30 bg-rose-950/20 p-4 transition hover:border-rose-500/60"
                >
                  <div className="flex items-center justify-between">
                    <Badge className="border-rose-500/30 bg-rose-500/15 text-[10px] text-rose-300">High</Badge>
                    <span className="font-mono text-[10px] text-slate-400">2m ago</span>
                  </div>
                  <h4 className="mt-2 text-xs font-bold text-white">
                    Billing clerk opened 38 clinical records in 7 minutes.
                  </h4>
                  <p className="mt-0.5 text-[11px] text-[#73849b]">Role baseline is 0.</p>
                </div>

                {/* Alert Item A-102 */}
                <div
                  onClick={() => onInspectAlert?.("A-102")}
                  className="cursor-pointer rounded-xl border border-amber-500/30 bg-amber-950/20 p-4 transition hover:border-amber-500/60"
                >
                  <div className="flex items-center justify-between">
                    <Badge className="border-amber-500/30 bg-amber-500/15 text-[10px] text-amber-300">Medium</Badge>
                    <span className="font-mono text-[10px] text-slate-400">18m ago</span>
                  </div>
                  <h4 className="mt-2 text-xs font-bold text-white">
                    5 failed logins, followed by a successful login.
                  </h4>
                  <p className="mt-0.5 text-[11px] text-[#73849b]">Source 10.2.4.77</p>
                </div>

                {/* Role Baseline Comparison Pill */}
                <div className="flex items-center justify-between rounded-xl border-l-2 border-l-[#14b8a6] bg-[#14b8a6]/[0.08] p-3">
                  <div>
                    <span className="font-mono text-[9px] text-[#8090a5] uppercase">Role baseline</span>
                    <div className="text-xs font-bold text-white">38 records · expected 0</div>
                  </div>
                  <Badge variant="outline" className="border-[#14b8a6]/40 text-[#5eead4]">
                    Anomaly Flagged
                  </Badge>
                </div>
              </div>
            </div>

            {/* Narrative 02 */}
            <div className="order-1 space-y-4 lg:order-2">
              <div className="flex items-center gap-2 font-mono text-xs font-bold text-[#14b8a6]">
                <span>02</span>
                <span>·</span>
                <span className="text-[#496159] uppercase">Find the signal</span>
              </div>

              <h3 className="text-2xl font-light tracking-tight text-[#0b1220] sm:text-4xl">
                Detect meaningful anomalies.
              </h3>

              <p className="text-base leading-relaxed text-[#586864]">
                Bring unusual access volumes, failed logins, and activity outside expected clinical scope into a focused
                view for review.
              </p>

              <div className="pt-2">
                <Button
                  variant="link"
                  onClick={onOpenDemo}
                  className="group h-auto p-0 text-sm font-semibold text-[#0d514f] hover:text-[#14b8a6]"
                >
                  <span>See the platform</span>
                  <ArrowRight className="ml-2 h-4 w-4 transition-transform group-hover:translate-x-1" />
                </Button>
              </div>
            </div>
          </div>

          {/* Article 03: Review the context */}
          <div className="grid grid-cols-1 items-center gap-12 border-t border-[#d7ddd9] pt-12 lg:grid-cols-2 lg:gap-16">
            <div className="space-y-4">
              <div className="flex items-center gap-2 font-mono text-xs font-bold text-[#14b8a6]">
                <span>03</span>
                <span>·</span>
                <span className="text-[#496159] uppercase">Review the context</span>
              </div>

              <h3 className="text-2xl font-light tracking-tight text-[#0b1220] sm:text-4xl">
                Investigate with clarity.
              </h3>

              <p className="text-base leading-relaxed text-[#586864]">
                Give reviewers precise timestamps, severity, user role, and access details so they can understand what
                happened and decide what deserves follow-up.
              </p>

              <div className="pt-2">
                <Button
                  variant="link"
                  onClick={onOpenDemo}
                  className="group h-auto p-0 text-sm font-semibold text-[#0d514f] hover:text-[#14b8a6]"
                >
                  <span>See the platform</span>
                  <ArrowRight className="ml-2 h-4 w-4 transition-transform group-hover:translate-x-1" />
                </Button>
              </div>
            </div>

            {/* Visual 03: Contextual Grid Inspector */}
            <div className="rounded-2xl border border-[#26354b] bg-[#0f172a] p-6 shadow-2xl sm:p-8">
              <div className="flex items-center justify-between border-b border-[#26354b] pb-4">
                <Badge className="border-rose-500/30 bg-rose-500/15 text-[10px] text-rose-300">High</Badge>
                <span className="font-mono text-xs text-[#718198]">ALERT A-101</span>
              </div>

              <h4 className="mt-4 text-lg font-bold text-white">Unusual clinical record access</h4>

              <div className="mt-5 grid grid-cols-2 gap-px overflow-hidden rounded-xl border border-[#26354b] bg-[#26354b]">
                <div className="bg-[#0f172a] p-3.5">
                  <span className="font-mono text-[9px] text-[#6d7e94] uppercase">Observed</span>
                  <div className="mt-1 text-xs font-bold text-white">Today, 09:42</div>
                </div>

                <div className="bg-[#0f172a] p-3.5">
                  <span className="font-mono text-[9px] text-[#6d7e94] uppercase">User Role</span>
                  <div className="mt-1 text-xs font-bold text-white">Billing clerk</div>
                </div>

                <div className="bg-[#0f172a] p-3.5">
                  <span className="font-mono text-[9px] text-[#6d7e94] uppercase">Department</span>
                  <div className="mt-1 text-xs font-bold text-white">Patient Accounts</div>
                </div>

                <div className="bg-[#0f172a] p-3.5">
                  <span className="font-mono text-[9px] text-[#6d7e94] uppercase">Behavior</span>
                  <div className="mt-1 text-xs font-bold text-white">38 records in 7 min</div>
                </div>
              </div>

              <div className="mt-4 flex items-center gap-2 text-xs text-[#8ca49f]">
                <CheckCircle2 className="h-4 w-4 text-[#14b8a6]" />
                <span>Context assembled from access and audit events</span>
              </div>
            </div>
          </div>

          {/* Article 04: Preserve accountability */}
          <div className="grid grid-cols-1 items-center gap-12 border-t border-[#d7ddd9] pt-12 lg:grid-cols-2 lg:gap-16">
            {/* Visual 04: Audit Log Integrity */}
            <div className="order-2 rounded-2xl border border-[#26354b] bg-[#0f172a] p-6 shadow-2xl sm:p-8 lg:order-1">
              <div className="flex items-center gap-4 rounded-xl border border-[#14b8a6]/30 bg-[#14b8a6]/[0.08] p-4">
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-[#14b8a6]/20 text-[#5eead4]">
                  <Shield className="h-6 w-6" />
                </div>
                <div>
                  <span className="font-mono text-[10px] text-[#79908c] uppercase">Audit Log Integrity</span>
                  <div className="text-xl font-bold text-[#5eead4]">Verified</div>
                </div>
              </div>

              {/* Forensic Hash-Chained Timeline */}
              <div className="mt-6 space-y-3 divide-y divide-[#26354b]">
                <div className="flex items-center gap-3 pt-2 text-xs">
                  <span className="h-2 w-2 rounded-full bg-[#14b8a6]" />
                  <span className="font-mono text-[#718198]">09:42:18</span>
                  <span className="font-medium text-[#f8fafc]">Record access observed</span>
                </div>

                <div className="flex items-center gap-3 pt-3 text-xs">
                  <span className="h-2 w-2 rounded-full bg-[#14b8a6]" />
                  <span className="font-mono text-[#718198]">09:42:19</span>
                  <span className="font-medium text-[#f8fafc]">Event signature confirmed</span>
                </div>

                <div className="flex items-center gap-3 pt-3 text-xs">
                  <span className="h-2 w-2 rounded-full bg-[#14b8a6]" />
                  <span className="font-mono text-[#718198]">09:42:21</span>
                  <span className="font-medium text-[#f8fafc]">Context linked to alert</span>
                </div>
              </div>
            </div>

            {/* Narrative 04 */}
            <div className="order-1 space-y-4 lg:order-2">
              <div className="flex items-center gap-2 font-mono text-xs font-bold text-[#14b8a6]">
                <span>04</span>
                <span>·</span>
                <span className="text-[#496159] uppercase">Preserve accountability</span>
              </div>

              <h3 className="text-2xl font-light tracking-tight text-[#0b1220] sm:text-4xl">
                Maintain confidence in audit trails.
              </h3>

              <p className="text-base leading-relaxed text-[#586864]">
                Verified log integrity and traceable activity create a dependable record for investigation, oversight,
                and responsible access.
              </p>

              <div className="pt-2">
                <Button
                  variant="link"
                  onClick={onOpenDemo}
                  className="group h-auto p-0 text-sm font-semibold text-[#0d514f] hover:text-[#14b8a6]"
                >
                  <span>See the platform</span>
                  <ArrowRight className="ml-2 h-4 w-4 transition-transform group-hover:translate-x-1" />
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
