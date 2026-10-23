import { useState } from "react"
import { ChevronDown, Shield, Lock } from "lucide-react"

interface FooterProps {
  onOpenLogin: () => void
  onOpenDemo: () => void
  onOpenDashboard: () => void
}

export function Footer({ onOpenLogin, onOpenDemo, onOpenDashboard }: FooterProps) {
  const [openLegalPanel, setOpenLegalPanel] = useState<"privacy" | "terms" | null>(null)

  const scrollTo = (id: string) => {
    const el = document.getElementById(id)
    if (el) el.scrollIntoView({ behavior: "smooth" })
  }

  const toggleLegalPanel = (panel: "privacy" | "terms") => {
    setOpenLegalPanel((currentPanel) => (currentPanel === panel ? null : panel))
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
              <span className="rounded bg-white/60 px-2 py-0.5">Demo data only</span>
              <span className="rounded bg-white/60 px-2 py-0.5">Audit-ready export</span>
              <span className="rounded bg-white/60 px-2 py-0.5">Role-aware controls</span>
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
                <button
                  type="button"
                  onClick={onOpenDemo}
                  className="transition-colors hover:text-[#0b1220]"
                >
                  Institutional Contact
                </button>
              </li>
              <li>
                <button
                  type="button"
                  aria-controls="footer-privacy-policy"
                  aria-expanded={openLegalPanel === "privacy"}
                  onClick={() => toggleLegalPanel("privacy")}
                  className="flex items-center gap-1.5 transition-colors hover:text-[#0b1220]"
                >
                  <span>Privacy Policy</span>
                  <ChevronDown
                    className={`h-3.5 w-3.5 transition-transform ${openLegalPanel === "privacy" ? "rotate-180" : ""}`}
                  />
                </button>
              </li>
              <li>
                <button
                  type="button"
                  aria-controls="footer-terms-of-use"
                  aria-expanded={openLegalPanel === "terms"}
                  onClick={() => toggleLegalPanel("terms")}
                  className="flex items-center gap-1.5 transition-colors hover:text-[#0b1220]"
                >
                  <span>Terms of Use</span>
                  <ChevronDown
                    className={`h-3.5 w-3.5 transition-transform ${openLegalPanel === "terms" ? "rotate-180" : ""}`}
                  />
                </button>
              </li>
            </ul>

            {openLegalPanel === "privacy" && (
              <div
                id="footer-privacy-policy"
                role="region"
                aria-label="Privacy Policy"
                className="mt-4 rounded-lg border border-[#b8cbc7] bg-white/50 p-3 text-xs leading-relaxed text-[#4e6562]"
              >
                <p>
                  This landing-page preview keeps demo details in local browser state. It does not send email, create a
                  calendar event, or transmit your information to a backend service.
                </p>
              </div>
            )}

            {openLegalPanel === "terms" && (
              <div
                id="footer-terms-of-use"
                role="region"
                aria-label="Terms of Use"
                className="mt-4 rounded-lg border border-[#b8cbc7] bg-white/50 p-3 text-xs leading-relaxed text-[#4e6562]"
              >
                <p>
                  This landing-page preview is for demonstration purposes. Dashboard activity, alerts, and booking
                  availability shown here are illustrative and are not clinical advice or a live security decision.
                </p>
              </div>
            )}
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
