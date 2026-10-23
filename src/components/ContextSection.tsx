import { CheckCircle2, Layers, Eye } from "lucide-react"

export function ContextSection() {
  return (
    <section id="platform" className="relative bg-[#dbe8e6] py-20 lg:py-28">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 items-start gap-12 lg:grid-cols-2 lg:gap-16">
          {/* Left Column: Heading */}
          <div>
            <div className="flex items-center gap-2">
              <span className="h-px w-6 bg-[#14b8a6]" />
              <span className="font-mono text-xs font-bold tracking-widest text-[#496159] uppercase">
                Access needs context
              </span>
            </div>

            <h2 className="mt-6 text-3xl font-light tracking-tight text-[#0b1220] sm:text-5xl lg:text-6xl">
              Healthcare security starts with{" "}
              <span className="font-serif italic font-normal text-[#14b8a6]">understanding access.</span>
            </h2>
          </div>

          {/* Right Column: Narrative & Pills */}
          <div className="flex flex-col justify-center space-y-6 lg:pt-8">
            {/* Pill Highlights */}
            <div className="flex flex-wrap items-center gap-3">
              <span className="inline-flex items-center rounded-full bg-white/80 px-4 py-1.5 text-sm font-medium text-[#0b1220] shadow-sm">
                more context
              </span>
              <span className="h-1.5 w-1.5 rounded-full bg-[#14b8a6]" />
              <span className="inline-flex items-center rounded-full bg-white/80 px-4 py-1.5 text-sm font-medium text-[#0b1220] shadow-sm">
                clearer signals
              </span>
              <span className="h-1.5 w-1.5 rounded-full bg-[#14b8a6]" />
              <span className="inline-flex items-center rounded-full bg-[#14b8a6]/20 px-4 py-1.5 text-sm font-semibold text-[#0d514f]">
                better oversight.
              </span>
            </div>

            <p className="text-base leading-relaxed text-[#53635f] sm:text-lg">
              Clinical environments are complex. People, roles, wards, and emergencies change how patient records are
              accessed. Karuna helps security teams understand those patterns and identify activity that deserves
              attention.
            </p>

            {/* Clinical Reality Badges */}
            <div className="grid grid-cols-1 gap-3 pt-4 sm:grid-cols-3">
              <div className="rounded-xl border border-[#c9d8d5] bg-white/60 p-4 backdrop-blur-sm">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#14b8a6]/10 text-[#14b8a6]">
                  <Layers className="h-4 w-4" />
                </div>
                <div className="mt-3 text-xs font-bold text-[#0b1220]">Ward-Aware</div>
                <div className="mt-0.5 text-[11px] text-[#53635f]">Knows ICU vs ED vs Outpatient baseline</div>
              </div>

              <div className="rounded-xl border border-[#c9d8d5] bg-white/60 p-4 backdrop-blur-sm">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#14b8a6]/10 text-[#14b8a6]">
                  <Eye className="h-4 w-4" />
                </div>
                <div className="mt-3 text-xs font-bold text-[#0b1220]">Zero-Friction</div>
                <div className="mt-0.5 text-[11px] text-[#53635f]">Never disrupts active clinical patient care</div>
              </div>

              <div className="rounded-xl border border-[#c9d8d5] bg-white/60 p-4 backdrop-blur-sm">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#14b8a6]/10 text-[#14b8a6]">
                  <CheckCircle2 className="h-4 w-4" />
                </div>
                <div className="mt-3 text-xs font-bold text-[#0b1220]">Audit-ready</div>
                <div className="mt-0.5 text-[11px] text-[#53635f]">Exportable demo evidence and access history</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
