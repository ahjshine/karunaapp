import { useState, useEffect } from "react"
import { TooltipProvider } from "@/components/ui/tooltip"
import { Navbar } from "@/components/Navbar"
import { Hero } from "@/components/Hero"
import { CapabilityStrip } from "@/components/CapabilityStrip"
import { ContextSection } from "@/components/ContextSection"
import { ValueSections } from "@/components/ValueSections"
import { ConnectedViewSection } from "@/components/ConnectedViewSection"
import { HowItWorksSection } from "@/components/HowItWorksSection"
import { AtmosphereSection } from "@/components/AtmosphereSection"
import { SecuritySection } from "@/components/SecuritySection"
import { DemoSchedulerSection } from "@/components/DemoSchedulerSection"
import { Footer } from "@/components/Footer"
import { DemoDialog } from "@/components/DemoDialog"
import { AuditTrailDialog } from "@/components/AuditTrailDialog"
import { LoginPage } from "@/components/LoginPage"
import { MedSentinelDashboard } from "@/components/MedSentinelDashboard"

export function App() {
  const [currentView, setCurrentView] = useState<"landing" | "login" | "dashboard">("landing")
  const [demoOpen, setDemoOpen] = useState(false)
  const [scheduleInfo, setScheduleInfo] = useState<{ day: string; slot: string } | undefined>(undefined)
  const [auditTrailOpen, setAuditTrailOpen] = useState(false)
  const [inspectedAlertId, setInspectedAlertId] = useState("A-101")
  const [loggedInRole, setLoggedInRole] = useState("Doctor — Attending Physician")
  const [loggedInName, setLoggedInName] = useState("Dr. Elena Vance")

  // Check URL hash for direct deep-linking
  useEffect(() => {
    const handleHash = () => {
      const hash = window.location.hash
      if (hash === "#login") {
        setCurrentView("login")
      } else if (hash === "#dashboard") {
        setCurrentView("dashboard")
      } else {
        setCurrentView("landing")
      }
    }

    handleHash()
    window.addEventListener("hashchange", handleHash)
    return () => window.removeEventListener("hashchange", handleHash)
  }, [])

  const openDemoModal = (initialSchedule?: { day: string; slot: string }) => {
    setScheduleInfo(initialSchedule)
    setDemoOpen(true)
  }

  const openAuditTrail = (alertId: string = "A-101") => {
    setInspectedAlertId(alertId)
    setAuditTrailOpen(true)
  }

  const navigateToLogin = () => {
    window.location.hash = "#login"
    setCurrentView("login")
    window.scrollTo({ top: 0, behavior: "smooth" })
  }

  const navigateToDashboard = (role: string = loggedInRole, name: string = loggedInName) => {
    setLoggedInRole(role)
    setLoggedInName(name)
    window.location.hash = "#dashboard"
    setCurrentView("dashboard")
    window.scrollTo({ top: 0, behavior: "smooth" })
  }

  const navigateToHome = () => {
    window.location.hash = ""
    setCurrentView("landing")
    window.scrollTo({ top: 0, behavior: "smooth" })
  }

  return (
    <TooltipProvider>
      {currentView === "dashboard" ? (
        <MedSentinelDashboard
          initialUser={loggedInName}
          initialRole={loggedInRole}
          onLogout={navigateToHome}
        />
      ) : currentView === "login" ? (
        <LoginPage
          onBackToHome={navigateToHome}
          onLoginSuccess={(role, name) => navigateToDashboard(role, name)}
        />
      ) : (
        <div className="flex min-h-screen flex-col bg-[#f6f7f4] text-[#0b1220]">
          {/* Header Navigation */}
          <Navbar
            onOpenDemo={() => openDemoModal()}
            onOpenLogin={navigateToLogin}
            onOpenDashboard={() => navigateToDashboard()}
          />

          {/* Main Landing Sections */}
          <main className="flex-1">
            <Hero onOpenDemo={() => openDemoModal()} onInspectAlert={openAuditTrail} />
            <CapabilityStrip />
            <ContextSection />
            <ValueSections onOpenDemo={() => openDemoModal()} onInspectAlert={openAuditTrail} />
            <ConnectedViewSection />
            <HowItWorksSection />
            <AtmosphereSection />
            <SecuritySection onInspectAlert={openAuditTrail} />
            <DemoSchedulerSection onOpenDemo={openDemoModal} />
          </main>

          {/* Footer */}
          <Footer
            onOpenDemo={() => openDemoModal()}
            onOpenLogin={navigateToLogin}
            onOpenDashboard={() => navigateToDashboard()}
          />

          {/* Interactive Modals / Dialogs */}
          <DemoDialog
            isOpen={demoOpen}
            onClose={() => setDemoOpen(false)}
            scheduleInfo={scheduleInfo}
          />
        </div>
      )}

      {/* Global Audit Trail Dialog available across all views */}
      <AuditTrailDialog
        isOpen={auditTrailOpen}
        onClose={() => setAuditTrailOpen(false)}
        alertId={inspectedAlertId}
      />
    </TooltipProvider>
  )
}

export default App
