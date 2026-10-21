import { Shield, Lock } from "lucide-react"

interface FooterProps {
  onOpenLogin: () => void
  onOpenDemo: () => void
  onOpenDashboard: () => void
}

export function Footer({ onOpenLogin, onOpenDemo, onOpenDashboard }: FooterProps) {
  const scrollTo = (id: string) => {
    const el = document.getElementById(id)
    if (el) el.scrollIntoView({ behavior: "smooth" })
  }

  return (
    <footer className="border-t border-[#b8cbc7] bg-[#dbe8e6] pt-16 pb-12 text-[#0b1220]">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 gap-12 lg:grid-cols-[1.5fr_1fr_1fr] lg:gap-16">
          {/* Col 1: Brand Info */}
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#0b1220] text-[#14b8a6]">
                <Shield className="h-5 w-5" />
              </div>
              <div className="flex items-center gap-3">
                <span className="text-2xl font-bold tracking-tight text-[#0b1220]">
                  Karuna<span className="text-[#14b8a6]">.</span>
                </span>
                <div className="h-6 w-px bg-[#a9bfbb]" />
                <div className="flex flex-col text-[9px] font-semibold tracking-wider text-[#536b68] uppercase">
                  <span>Clinical Access</span>
                  <span>Intelligence</span>
                </div>
              </div>
            </div>

            <p className="max-w-sm text-sm leading-relaxed text-[#4e6562]">
              Calm, contextual intelligence for protecting access to sensitive patient records across hospital networks.
            </p>

            <div className="flex flex-wrap items-center gap-3 pt-2 text-xs font-mono text-[#536b68]">
              <span className="rounded bg-white/60 px-2 py-0.5">HIPAA Compliant</span>
              <span className="rounded bg-white/60 px-2 py-0.5">SOC-2 Type II</span>
              <span className="rounded bg-white/60 px-2 py-0.5">HITECH Aligned</span>
            </div>
          </div>

          {/* Col 2: Explore Navigation */}
          <div>
            <h4 className="font-mono text-xs font-bold tracking-wider text-[#617a76] uppercase">Explore</h4>
            <ul className="mt-4 space-y-2.5 text-sm text-[#4e6562]">
              <li>
                <button onClick={() => scrollTo("platform")} className="transition-colors hover:text-[#0b1220]">
                  Platform Overview
                </button>
              </li>
              <li>
                <button onClick={() => scrollTo("how-it-works")} className="transition-colors hover:text-[#0b1220]">
                  How It Works
                </button>
              </li>
              <li>
                <button onClick={() => scrollTo("hospitals")} className="transition-colors hover:text-[#0b1220]">
                  For Hospitals
                </button>
              </li>
              <li>
                <button onClick={() => scrollTo("security")} className="transition-colors hover:text-[#0b1220]">
                  Security & Audit
                </button>
              </li>
              <li>
                <button onClick={onOpenDemo} className="font-medium text-[#0d514f] transition-colors hover:underline">
                  Request a Demo
                </button>
              </li>
            </ul>
          </div>

          {/* Col 3: Portal & Legal */}
          <div>
            <h4 className="font-mono text-xs font-bold tracking-wider text-[#617a76] uppercase">Perimeter Portal</h4>
            <ul className="mt-4 space-y-2.5 text-sm text-[#4e6562]">
              <li>
                <button
                  onClick={onOpenDashboard}
                  className="flex items-center gap-1.5 font-semibold text-[#0d514f] transition-colors hover:text-[#14b8a6]"
                >
                  <Shield className="h-3.5 w-3.5" />
                  <span>Security Cockpit Dashboard</span>
                </button>
              </li>
              <li>
                <button
                  onClick={onOpenLogin}
                  className="flex items-center gap-1.5 font-medium text-[#4e6562] transition-colors hover:text-[#0b1220]"
                >
                  <Lock className="h-3.5 w-3.5" />
                  <span>Zero-Trust Staff Login</span>
                </button>
              </li>
              <li>
                <a
                  href="#contact"
                  onClick={(e) => {
                    e.preventDefault()
                    onOpenDemo()
                  }}
                  className="transition-colors hover:text-[#0b1220]"
                >
                  Institutional Contact
                </a>
              </li>
              <li>
                <span className="text-slate-500 cursor-default">Privacy Policy</span>
              </li>
              <li>
                <span className="text-slate-500 cursor-default">Terms of Use</span>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="mt-12 flex flex-wrap items-center justify-between gap-4 border-t border-[#b8cbc7] pt-8 text-xs font-mono text-[#617a76]">
          <p>© 2026 Karuna. All rights reserved.</p>
          <p className="uppercase tracking-widest">Clinical Access Intelligence</p>
        </div>
      </div>
    </footer>
  )
}
