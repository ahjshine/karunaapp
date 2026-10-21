import { Eye, Search, FileSearch } from "lucide-react"

export function HowItWorksSection() {
  const steps = [
    {
      num: "01",
      title: "Observe",
      headline: "Bring relevant clinical access activity into view.",
      description:
        "Seamless telemetry ingestion across EHR systems (Epic, Oracle Health / Cerner, Meditech) without slowing down physician charting or nurse workflows.",
      icon: Eye,
    },
    {
      num: "02",
      title: "Identify",
      headline: "Surface deviations from expected patterns.",
      description:
        "Contextual ML engines compare every access event against role baselines, ward assignments, shift times, and patient care team rosters.",
      icon: Search,
    },
    {
      num: "03",
      title: "Investigate",
      headline: "The context needed to assess alerts.",
      description:
        "Reviewers receive forensic, pre-assembled incident dossiers with audit log signatures, behavioral risk scores, and clear clinical context.",
      icon: FileSearch,
    },
  ]

  return (
    <section id="how-it-works" className="relative bg-[#f8fafc] py-20 lg:py-28">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="max-w-3xl">
          <div className="flex items-center gap-2">
            <span className="h-px w-6 bg-[#14b8a6]" />
            <span className="font-mono text-xs font-bold tracking-widest text-[#496159] uppercase">How it works</span>
          </div>

          <h2 className="mt-4 text-3xl font-light tracking-tight text-[#0b1220] sm:text-5xl lg:text-6xl">
            A clearer path from access event to{" "}
            <span className="font-serif italic font-normal text-[#14b8a6]">investigation.</span>
          </h2>
        </div>

        {/* 3 Step Cards */}
        <div className="mt-16 grid grid-cols-1 gap-8 md:grid-cols-3">
          {steps.map((step) => {
            const Icon = step.icon
            return (
              <div
                key={step.num}
                className="group relative rounded-2xl border border-slate-200/80 bg-white p-8 shadow-sm transition-all hover:border-[#14b8a6]/40 hover:shadow-lg"
              >
                {/* Step Number Circle */}
                <div className="flex items-center justify-between">
                  <div className="flex h-12 w-12 items-center justify-center rounded-xl border border-[#14b8a6]/30 bg-[#14b8a6]/10 font-mono text-sm font-bold text-[#14b8a6] transition-transform group-hover:scale-105">
                    {step.num}
                  </div>
                  <Icon className="h-6 w-6 text-slate-300 transition-colors group-hover:text-[#14b8a6]" />
                </div>

                <div className="mt-6 font-serif text-2xl font-normal text-[#0b1220]">{step.title}</div>
                <h3 className="mt-2 text-base font-semibold text-[#0b1220]">{step.headline}</h3>
                <p className="mt-3 text-sm leading-relaxed text-slate-600">{step.description}</p>
              </div>
            )
          })}
        </div>
      </div>
    </section>
  )
}
