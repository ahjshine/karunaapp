import { Activity, ShieldAlert, FileCheck, ArrowUpRight } from "lucide-react"

export function ConnectedViewSection() {
  return (
    <section className="relative overflow-hidden bg-[#0b1220] py-24 text-white lg:py-32">
      {/* Background Watermark */}
      <div className="pointer-events-none absolute inset-x-0 bottom-[-5%] flex select-none justify-center overflow-hidden font-bold tracking-tighter text-white/[0.02] text-[22vw] leading-none">
        karuna
      </div>

      <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="max-w-3xl">
          <div className="flex items-center gap-2">
            <span className="h-px w-6 bg-[#14b8a6]" />
            <span className="font-mono text-xs font-bold tracking-widest text-[#94a3b8] uppercase">
              A connected view
            </span>
          </div>

          <h2 className="mt-4 text-3xl font-bold tracking-tight text-[#f8fafc] sm:text-5xl lg:text-6xl">
            From unusual activity to{" "}
            <span className="bg-gradient-to-r from-teal-300 to-[#14b8a6] bg-clip-text text-transparent">
              actionable insight.
            </span>
          </h2>

          <p className="mt-6 text-base leading-relaxed text-[#94a3b8] sm:text-lg">
            See risk signals, clinical context, and audit evidence together—so your team can focus attention where it
            matters.
          </p>
        </div>

        {/* 3 Telemetry Pillars Card Grid */}
        <div className="mt-14 grid grid-cols-1 gap-6 sm:grid-cols-3">
          <div className="group rounded-2xl border border-[#26354b] bg-[#0f172a]/90 p-8 backdrop-blur-sm transition-all hover:border-[#14b8a6]/50 hover:bg-[#111d31]">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-teal-500/10 text-[#5eead4]">
              <Activity className="h-6 w-6" />
            </div>
            <h3 className="mt-6 text-lg font-bold text-white group-hover:text-[#5eead4]">
              Clinical Access Events
            </h3>
            <p className="mt-2 text-sm leading-relaxed text-slate-400">
              Continuously stream and correlate authentication logs, chart views, prescription edits, and billing record
              lookups in real time.
            </p>
            <div className="mt-6 flex items-center gap-1.5 text-xs font-semibold text-[#14b8a6]">
              <span>Live ingestion</span>
              <ArrowUpRight className="h-3.5 w-3.5" />
            </div>
          </div>

          <div className="group rounded-2xl border border-[#26354b] bg-[#0f172a]/90 p-8 backdrop-blur-sm transition-all hover:border-amber-500/50 hover:bg-[#111d31]">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-amber-500/10 text-amber-400">
              <ShieldAlert className="h-6 w-6" />
            </div>
            <h3 className="mt-6 text-lg font-bold text-white group-hover:text-amber-400">
              Contextual Risk Signals
            </h3>
            <p className="mt-2 text-sm leading-relaxed text-slate-400">
              Behavioral baseline models factor in department, active shift, patient-ward relationship, and emergency
              break-glass overrides.
            </p>
            <div className="mt-6 flex items-center gap-1.5 text-xs font-semibold text-amber-400">
              <span>Risk scored 0-100</span>
              <ArrowUpRight className="h-3.5 w-3.5" />
            </div>
          </div>

          <div className="group rounded-2xl border border-[#26354b] bg-[#0f172a]/90 p-8 backdrop-blur-sm transition-all hover:border-[#38ef7d]/50 hover:bg-[#111d31]">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-500/10 text-[#38ef7d]">
              <FileCheck className="h-6 w-6" />
            </div>
            <h3 className="mt-6 text-lg font-bold text-white group-hover:text-[#38ef7d]">
              Verified Audit Evidence
            </h3>
            <p className="mt-2 text-sm leading-relaxed text-slate-400">
              Cryptographically signed evidence packs ready for internal compliance, legal review, and external OCR /
              HIPAA audits.
            </p>
            <div className="mt-6 flex items-center gap-1.5 text-xs font-semibold text-[#38ef7d]">
              <span>Immutable hash chain</span>
              <ArrowUpRight className="h-3.5 w-3.5" />
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
