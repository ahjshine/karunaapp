import { Button } from "@/components/ui/button"
import { DashboardPreview } from "@/components/DashboardPreview"
import { ArrowRight, ShieldCheck } from "lucide-react"

interface HeroProps {
  onOpenDemo: () => void
  onInspectAlert?: (alertId: string) => void
}

export function Hero({ onOpenDemo, onInspectAlert }: HeroProps) {
  const scrollToPlatform = () => {
    const el = document.getElementById("platform")
    if (el) el.scrollIntoView({ behavior: "smooth" })
  }

  return (
    <section className="relative overflow-hidden bg-[#c9d8d5] pt-12 pb-24 md:pt-16 md:pb-32">
      {/* Background Watermark */}
      <div className="pointer-events-none absolute inset-x-0 bottom-[-10%] flex select-none justify-center overflow-hidden font-bold tracking-tighter text-[#0b1220]/[0.08] text-[20vw] leading-none">
        karuna
      </div>

      <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        {/* Eyebrow */}
        <div className="flex items-center justify-center gap-2.5">
          <span className="h-px w-6 bg-[#14b8a6]" />
          <span className="font-mono text-xs font-bold tracking-widest text-[#496159] uppercase">
            Clinical Access Intelligence
          </span>
          <span className="h-px w-6 bg-[#14b8a6]" />
        </div>

        {/* Main Editorial Headline */}
        <div className="mx-auto mt-6 max-w-4xl text-center">
          <h1 className="text-4xl font-light tracking-tight text-[#0b1220] sm:text-6xl md:text-7xl">
            <span className="font-bold">every patient record, deserves</span>{" "}
            <span className="font-serif italic font-normal text-[#14b8a6]">protection.</span>
          </h1>

          <p className="mx-auto mt-6 max-w-2xl text-base leading-relaxed text-[#344b49] sm:text-lg">
            Karuna helps healthcare teams detect unusual access, investigate suspicious activity, and safeguard
            sensitive patient information without slowing down clinical care.
          </p>

          {/* Action CTAs */}
          <div className="mt-8 flex flex-wrap items-center justify-center gap-4">
            <Button
              onClick={onOpenDemo}
              size="lg"
              className="gap-2 bg-[#14b8a6] px-8 text-base font-semibold text-[#0b1220] shadow-md transition-all hover:bg-[#0d9488] hover:text-white"
            >
              Request a Demo
              <ArrowRight className="h-4 w-4" />
            </Button>

            <Button
              onClick={scrollToPlatform}
              size="lg"
              variant="outline"
              className="border-[#0b1220]/20 bg-[#0b1220]/5 px-8 text-base font-semibold text-[#0b1220] hover:bg-[#0b1220]/10"
            >
              Explore the Platform
            </Button>
          </div>

          <div className="mt-4 flex items-center justify-center gap-2 text-xs text-[#61706c]">
            <ShieldCheck className="h-4 w-4 text-[#14b8a6]" />
            <span>Built for the realities of clinical access.</span>
          </div>
        </div>

        {/* Embedded Interactive Product Frame */}
        <div className="mt-14 sm:mt-18">
          <DashboardPreview onInspectAlert={onInspectAlert} />
        </div>
      </div>
    </section>
  )
}
