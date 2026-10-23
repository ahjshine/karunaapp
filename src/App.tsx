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
import { medsentinelApi, type SessionUser } from "@/lib/medsentinelApi"

type AppView = "landing" | "login" | "dashboard"

function getViewFromLocation(): AppView {
  const path = window.location.pathname.replace(/\/+$/, "") || "/"
  const hash = window.location.hash

  if (path === "/login" || hash === "#login") return "login"
  if (path === "/dashboard" || hash === "#dashboard") return "dashboard"
  return "landing"
}

export function App() {
  const [currentView, setCurrentView] = useState<AppView>("landing")
  const [demoOpen, setDemoOpen] = useState(false)
  const [scheduleInfo, setScheduleInfo] = useState<{ day: string; date?: string; slot: string } | undefined>(undefined)
  const [auditTrailOpen, setAuditTrailOpen] = useState(false)
  const [inspectedAlertId, setInspectedAlertId] = useState("A-101")
  const [sessionUser, setSessionUser] = useState<SessionUser | null>(null)
  const [authChecking, setAuthChecking] = useState(false)
  const [sessionError, setSessionError] = useState("")

  useEffect(() => {
    const handleLocationChange = () => setCurrentView(getViewFromLocation())

    handleLocationChange()
    window.addEventListener("hashchange", handleLocationChange)
    window.addEventListener("popstate", handleLocationChange)
    return () => {
      window.removeEventListener("hashchange", handleLocationChange)
      window.removeEventListener("popstate", handleLocationChange)
    }
  }, [])

  useEffect(() => {
    if (currentView !== "dashboard") return

    let cancelled = false
    setAuthChecking(true)
    setSessionError("")

    void medsentinelApi
      .me()
      .then((user) => {
        if (cancelled) return
        if (!user) {
          window.history.replaceState({}, "", "/login")
          setCurrentView("login")
          return
        }
        setSessionUser(user)
      })
      .catch((error: unknown) => {
        if (cancelled) return
        setSessionUser(null)
        setSessionError(error instanceof Error ? error.message : "The MedSentinel backend is unavailable.")
      })
      .finally(() => {
        if (!cancelled) setAuthChecking(false)
      })

    return () => {
      cancelled = true
    }
  }, [currentView])

  const openDemoModal = (initialSchedule?: { day: string; slot: string }) => {
    setScheduleInfo(initialSchedule)
    setDemoOpen(true)
  }

  const openAuditTrail = (alertId: string = "A-101") => {
    setInspectedAlertId(alertId)
    setAuditTrailOpen(true)
  }

  const navigateToLogin = () => {
    setSessionUser(null)
    setSessionError("")
    window.history.pushState({}, "", "/login")
    setCurrentView("login")
    window.scrollTo({ top: 0, behavior: "auto" })
  }

  const navigateToDashboard = (user: SessionUser) => {
    setSessionUser(user)
    setSessionError("")
    setAuthChecking(false)
    window.history.pushState({}, "", "/dashboard")
    setCurrentView("dashboard")
    window.scrollTo({ top: 0, behavior: "auto" })
  }

  const handleLogout = () => {
    void (async () => {
      try {
        await medsentinelApi.logout()
      } finally {
        setSessionUser(null)
        setSessionError("")
        window.history.pushState({}, "", "/")
        setCurrentView("landing")
        window.scrollTo({ top: 0, behavior: "auto" })
      }
    })()
  }

  const navigateToHome = () => {
    setSessionUser(null)
    setSessionError("")
    window.history.pushState({}, "", "/")
    setCurrentView("landing")
    window.scrollTo({ top: 0, behavior: "auto" })
  }

  return (
    <TooltipProvider>
      {currentView === "dashboard" ? sessionUser ? (
        <MedSentinelDashboard
           initialUser={sessionUser.name}
           initialRole={sessionUser.role}
           initialUsername={sessionUser.username}
           initialPermissions={sessionUser.permissions}
           onLogout={handleLogout}
         />
      ) : authChecking ? (
        <div className="flex min-h-screen items-center justify-center bg-[#f6f7f4] px-6 text-center text-[#0b1220]">
          <p className="text-sm text-slate-600">Verifying your MedSentinel session…</p>
        </div>
      ) : (
        <div className="flex min-h-screen items-center justify-center bg-[#f6f7f4] px-6 text-center text-[#0b1220]">
          <div className="max-w-md">
            <h1 className="text-xl font-semibold">Dashboard session unavailable</h1>
            <p className="mt-2 text-sm text-slate-600">{sessionError || "Sign in through the backend before opening the security dashboard."}</p>
            <button className="mt-5 rounded-md bg-[#0b1220] px-4 py-2 text-sm font-medium text-white" onClick={navigateToLogin}>
              Return to sign in
            </button>
          </div>
        </div>
      ) : currentView === "login" ? (
        <LoginPage
          onBackToHome={navigateToHome}
          onLoginSuccess={navigateToDashboard}
        />
      ) : (
        <div className="flex min-h-screen flex-col bg-[#f6f7f4] text-[#0b1220]">
          {/* Header Navigation */}
          <Navbar
            onOpenDemo={() => openDemoModal()}
            onOpenLogin={navigateToLogin}
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
            onOpenDashboard={navigateToLogin}
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
