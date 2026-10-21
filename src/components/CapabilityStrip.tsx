import { Activity, Search, ShieldCheck, UserCheck } from "lucide-react"

export function CapabilityStrip() {
  const capabilities = [
    {
      num: "01",
      title: "Clinical access monitoring",
      desc: "Real-time telemetry across all EHR wards and clinics.",
      icon: Activity,
    },
    {
      num: "02",
      title: "Anomaly detection",
      desc: "ML behavioral models tuned specifically for healthcare environments.",
      icon: Search,
    },
    {
      num: "03",
      title: "Investigation-ready audit trails",
      desc: "Cryptographically verified, tamper-evident forensic log chains.",
      icon: ShieldCheck,
    },
    {
      num: "04",
      title: "Role-aware access intelligence",
      desc: "Understands shift schedules, department scopes, and emergency break-glass.",
      icon: UserCheck,
    },
  ]

  return (
    <div className="border-y border-[#0b1220] bg-[#0b1220] py-8 text-white">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 divide-y divide-[#26354b] sm:grid-cols-2 sm:divide-y-0 sm:divide-x lg:grid-cols-4">
          {capabilities.map((cap) => {
            const Icon = cap.icon
            return (
              <div
                key={cap.num}
                className="group flex flex-col justify-between p-6 transition-colors hover:bg-white/[0.02]"
              >
                <div>
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-xs font-bold text-[#5eead4]">{cap.num}</span>
                    <Icon className="h-4 w-4 text-slate-500 transition-colors group-hover:text-[#5eead4]" />
                  </div>
                  <h3 className="mt-3 text-base font-bold tracking-tight text-[#f8fafc] group-hover:text-[#5eead4]">
                    {cap.title}
                  </h3>
                </div>
                <p className="mt-2 text-xs text-slate-400">{cap.desc}</p>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
