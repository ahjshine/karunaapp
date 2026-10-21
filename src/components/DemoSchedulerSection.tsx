import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Video, CheckCircle2, ArrowRight } from "lucide-react"

interface DemoSchedulerSectionProps {
  onOpenDemo: (initialSchedule?: { day: string; slot: string }) => void
}

export function DemoSchedulerSection({ onOpenDemo }: DemoSchedulerSectionProps) {
  const [selectedDay, setSelectedDay] = useState("Mon")
  const [selectedSlot, setSelectedSlot] = useState("10:00 AM")

  const weekdays = [
    { label: "Mon", date: "Oct 26" },
    { label: "Tue", date: "Oct 27" },
    { label: "Wed", date: "Oct 28" },
    { label: "Thu", date: "Oct 29" },
    { label: "Fri", date: "Oct 30" },
  ]

  const slots = ["10:00 AM", "1:30 PM", "3:00 PM", "4:30 PM"]

  const handleContinue = () => {
    onOpenDemo({ day: selectedDay, slot: selectedSlot })
  }

  return (
    <section id="demo" className="relative overflow-hidden bg-[#0d514f] py-20 text-white lg:py-28">
      {/* Concentric Decorative Rings */}
      <div className="pointer-events-none absolute -top-40 -right-40 h-[680px] w-[680px] rounded-full border border-white/10 shadow-[0_0_0_120px_rgba(94,234,212,0.02),0_0_0_240px_rgba(94,234,212,0.02)]" />

      <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 items-center gap-12 lg:grid-cols-2 lg:gap-16">
          {/* Left Column: Heading */}
          <div>
            <div className="flex items-center gap-2">
              <span className="h-px w-6 bg-[#14b8a6]" />
              <span className="font-mono text-xs font-bold tracking-widest text-[#c2d9d6] uppercase">
                See Karuna in action
              </span>
            </div>

            <h2 className="mt-4 text-3xl font-light tracking-tight text-[#f8fafc] sm:text-5xl lg:text-6xl">
              bring <span className="font-serif italic font-normal text-teal-200">clarity</span> to{" "}
              <span className="font-bold text-white">Clinical Access.</span>
            </h2>

            <p className="mt-6 text-base leading-relaxed text-[#c2d9d6] sm:text-lg">
              See how Karuna can help your team monitor access activity, investigate anomalies, and strengthen oversight
              of sensitive patient records.
            </p>

            <div className="mt-8 flex flex-col gap-3 font-mono text-xs text-[#c2d9d6]">
              <div className="flex items-center gap-2">
                <CheckCircle2 className="h-4 w-4 text-[#14b8a6]" />
                <span>Custom telemetry simulation on your EHR architecture</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="h-4 w-4 text-[#14b8a6]" />
                <span>30-minute tailored walk-through with a Clinical CISO</span>
              </div>
            </div>
          </div>

          {/* Right Column: Interactive Calendly Preview Card (shadcn) */}
          <div className="mx-auto w-full max-w-md">
            <Card className="border border-white/20 bg-[#f8fafc] text-[#0b1220] shadow-2xl">
              <CardHeader className="border-b border-slate-200 pb-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-[#0d514f] font-bold text-white text-xs">
                      C
                    </div>
                    <div>
                      <span className="text-xs font-bold text-[#0b1220]">Calendly</span>
                      <span className="ml-2 font-mono text-[10px] text-slate-500">Preview</span>
                    </div>
                  </div>
                  <Badge variant="outline" className="border-teal-700/20 text-[#0d514f] text-[10px]">
                    Live Booking
                  </Badge>
                </div>
              </CardHeader>

              <CardContent className="space-y-4 pt-4">
                <div>
                  <span className="font-mono text-[10px] text-slate-500 uppercase">Meeting Type</span>
                  <div className="text-sm font-bold text-[#0b1220]">Clinical Security Platform Demo</div>
                </div>

                {/* Weekday Selector */}
                <div>
                  <span className="font-mono text-[10px] text-slate-500 uppercase">Select Weekday</span>
                  <div className="mt-2 grid grid-cols-5 gap-2">
                    {weekdays.map((day) => (
                      <button
                        key={day.label}
                        type="button"
                        onClick={() => setSelectedDay(day.label)}
                        className={`flex flex-col items-center justify-center rounded-lg border py-2 transition ${
                          selectedDay === day.label
                            ? "border-[#0d514f] bg-[#0d514f] text-white shadow-sm"
                            : "border-slate-200 bg-white text-slate-700 hover:border-slate-400"
                        }`}
                      >
                        <span className="text-xs font-bold">{day.label}</span>
                        <span className="text-[9px] opacity-75">{day.date}</span>
                      </button>
                    ))}
                  </div>
                </div>

                {/* Time Slots */}
                <div>
                  <span className="font-mono text-[10px] text-slate-500 uppercase">Available Time Slots</span>
                  <div className="mt-2 grid grid-cols-2 gap-2 sm:grid-cols-4">
                    {slots.map((slot) => (
                      <button
                        key={slot}
                        type="button"
                        onClick={() => setSelectedSlot(slot)}
                        className={`rounded-lg border px-2 py-2 text-center text-xs font-semibold transition ${
                          selectedSlot === slot
                            ? "border-[#0d514f] bg-[#0d514f] text-white shadow-sm"
                            : "border-slate-200 bg-white text-slate-700 hover:border-slate-400"
                        }`}
                      >
                        {slot}
                      </button>
                    ))}
                  </div>
                </div>
              </CardContent>

              <CardFooter className="flex items-center justify-between border-t border-slate-200 pt-3 text-xs">
                <div className="flex items-center gap-1.5 text-slate-600">
                  <Video className="h-4 w-4 text-[#0d514f]" />
                  <span>30 min • Video call</span>
                </div>

                <Button
                  onClick={handleContinue}
                  className="gap-1.5 bg-[#0d514f] text-xs font-bold text-white hover:bg-[#14b8a6] hover:text-[#0b1220]"
                >
                  <span>Continue</span>
                  <ArrowRight className="h-3.5 w-3.5" />
                </Button>
              </CardFooter>
            </Card>
          </div>
        </div>
      </div>
    </section>
  )
}
