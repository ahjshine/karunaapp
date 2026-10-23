import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Clock, MapPin, Activity, ArrowRight, FileText } from "lucide-react"

interface SecuritySectionProps {
  onInspectAlert: (alertId: string) => void
}

export function SecuritySection({ onInspectAlert }: SecuritySectionProps) {
  return (
    <section id="security" className="bg-[#f8fafc] py-20 lg:py-28">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 items-center gap-12 lg:grid-cols-2 lg:gap-16">
          {/* Left Column */}
          <div className="space-y-6">
            <div>
              <div className="flex items-center gap-2">
                <span className="h-px w-6 bg-[#14b8a6]" />
                <span className="font-mono text-xs font-bold tracking-widest text-[#496159] uppercase">
                  Security & accountability
                </span>
              </div>

              <h2 className="mt-4 text-3xl font-bold tracking-tight text-[#0b1220] sm:text-5xl">
                Visibility you can{" "}
                <span className="font-serif italic font-normal text-[#13b49a]">investigate.</span>
              </h2>

              <p className="mt-4 text-base leading-relaxed text-[#64748b]">
                Traceable events, contextual alerts, and verified audit activity give reviewers a clearer, more dependable
                record of access.
              </p>
            </div>

            {/* Checklist */}
            <div className="space-y-3 pt-2">
              <div className="flex items-center gap-3 text-sm text-[#53635f]">
                <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-teal-500/10 text-[#14b8a6]">
                  <Clock className="h-4 w-4" />
                </div>
                <span className="font-medium">Precise event timing & millisecond precision</span>
              </div>

              <div className="flex items-center gap-3 text-sm text-[#53635f]">
                <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-teal-500/10 text-[#14b8a6]">
                  <MapPin className="h-4 w-4" />
                </div>
                <span className="font-medium">Clinical role, specialty, and hospital area context</span>
              </div>

              <div className="flex items-center gap-3 text-sm text-[#53635f]">
                <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-teal-500/10 text-[#14b8a6]">
                  <Activity className="h-4 w-4" />
                </div>
                <span className="font-medium">Continuous severity calculation and behavior scoring</span>
              </div>
            </div>
          </div>

          {/* Right Column: Alert Detail Inspector Card */}
          <div className="overflow-hidden rounded-2xl border border-[#26354b] bg-[#0f172a] shadow-2xl">
            <div className="flex items-center justify-between border-b border-[#26354b] px-6 py-4">
              <span className="font-mono text-[10px] tracking-wider text-[#718198] uppercase">ALERT DETAIL</span>
              <span className="font-mono text-xs font-bold text-[#8292a8]">A-101 · DEMO</span>
            </div>

            <div className="p-6 sm:p-8">
              <Badge className="border-rose-500/30 bg-rose-500/15 font-mono text-[10px] text-rose-300">
                High severity
              </Badge>

              <h3 className="mt-4 text-xl font-bold text-white sm:text-2xl">Unusual clinical record access</h3>
              <p className="mt-1 text-xs text-[#8898ad]">
                Billing clerk opened 38 clinical records in 7 minutes. Role baseline for clinical records is 0.
              </p>

              {/* Detail Matrix */}
              <div className="mt-6 grid grid-cols-2 gap-px overflow-hidden rounded-xl border border-[#26354b] bg-[#26354b]">
                <div className="bg-[#0f172a] p-4">
                  <span className="font-mono text-[9px] text-[#64758c] uppercase">RISK SCORE</span>
                <div className="mt-1 font-mono text-sm font-bold text-rose-400">75 / 100 · Sample score</div>
                </div>

                <div className="bg-[#0f172a] p-4">
                  <span className="font-mono text-[9px] text-[#64758c] uppercase">USER ROLE</span>
                  <div className="mt-1 text-sm font-bold text-white">Elena Ruiz · Billing clerk</div>
                </div>

                <div className="bg-[#0f172a] p-4">
                  <span className="font-mono text-[9px] text-[#64758c] uppercase">DEPARTMENT</span>
                  <div className="mt-1 text-sm font-bold text-white">Patient Accounts</div>
                </div>

                <div className="bg-[#0f172a] p-4">
                  <span className="font-mono text-[9px] text-[#64758c] uppercase">ACCESS BEHAVIOR</span>
                  <div className="mt-1 text-sm font-bold text-white">38 records · baseline 0</div>
                </div>
              </div>

              {/* Action Banner */}
              <div className="mt-6 flex flex-wrap items-center justify-between gap-4 rounded-xl border border-[#14b8a6]/30 bg-[#14b8a6]/[0.08] p-4">
                <div className="flex items-center gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-[#14b8a6]/20 text-[#14b8a6]">
                    <FileText className="h-5 w-5" />
                  </div>
                  <div>
                    <div className="text-sm font-bold text-white">38 access events linked</div>
                    <div className="font-mono text-[11px] text-[#718a86]">R2 Bulk access · R3 Out-of-scope</div>
                  </div>
                </div>

                <Button
                  onClick={() => onInspectAlert("A-101")}
                  className="bg-[#14b8a6] text-xs font-bold text-[#0b1220] hover:bg-[#0d9488] hover:text-white"
                >
                  View Trail
                  <ArrowRight className="ml-1 h-3.5 w-3.5" />
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
