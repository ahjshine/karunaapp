import { useState, type FormEvent } from "react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { Shield, CheckCircle2, Loader2, Calendar } from "lucide-react"

interface DemoDialogProps {
  isOpen: boolean
  onClose: () => void
  scheduleInfo?: { day: string; date?: string; slot: string }
}

export function DemoDialog({ isOpen, onClose, scheduleInfo }: DemoDialogProps) {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isSuccess, setIsSuccess] = useState(false)
  const [requestId, setRequestId] = useState("")

  const [fullName, setFullName] = useState("")
  const [workEmail, setWorkEmail] = useState("")
  const [hospitalName, setHospitalName] = useState("")
  const [ehrSystem, setEhrSystem] = useState("Epic")
  const [notes, setNotes] = useState("")

  const selectedSchedule = scheduleInfo
    ? `${scheduleInfo.day}${scheduleInfo.date ? `, ${scheduleInfo.date}` : ""} at ${scheduleInfo.slot}`
    : ""

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)

    setTimeout(() => {
      const generatedRequestId = `DEMO-${Date.now().toString(36).toUpperCase()}`
      localStorage.setItem(
        `karuna-demo-request-${generatedRequestId}`,
        JSON.stringify({
          requestId: generatedRequestId,
          fullName,
          workEmail,
          hospitalName,
          ehrSystem,
          notes,
          schedule: scheduleInfo ?? null,
          capturedAt: new Date().toISOString(),
        }),
      )
      setRequestId(generatedRequestId)
      setIsSubmitting(false)
      setIsSuccess(true)
    }, 1000)
  }

  const handleReset = () => {
    setIsSubmitting(false)
    setIsSuccess(false)
    setRequestId("")
    setFullName("")
    setWorkEmail("")
    setHospitalName("")
    setEhrSystem("Epic")
    setNotes("")
    onClose()
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && handleReset()}>
      <DialogContent className="max-w-lg border border-[#c9d8d5] bg-[#f6f7f4] text-[#0b1220] sm:rounded-2xl">
        {!isSuccess ? (
          <>
            <DialogHeader>
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#0b1220] text-[#14b8a6]">
                  <Shield className="h-4 w-4" />
                </div>
                <DialogTitle className="text-xl font-bold">Request a Platform Demo</DialogTitle>
              </div>
              <DialogDescription className="text-xs text-[#586864]">
                See how Karuna detects unusual access, investigates anomalies, and protects EHR patient records.
              </DialogDescription>
            </DialogHeader>

            {scheduleInfo && (
              <div className="flex items-center justify-between rounded-lg border border-teal-500/20 bg-teal-500/10 px-3.5 py-2 text-xs font-medium text-[#0d514f]">
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-[#14b8a6]" />
                  <span>
                    Selected Slot: <strong>{selectedSchedule}</strong>
                  </span>
                </div>
                <Badge variant="outline" className="border-teal-700/30 text-[10px] text-[#0d514f]">
                  Preview
                </Badge>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4 pt-2">
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div className="space-y-1.5">
                  <Label htmlFor="demo-name" className="text-xs font-semibold text-slate-700">
                    Full Name
                  </Label>
                  <Input
                    id="demo-name"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="Dr. Jordan Hayes"
                    className="border-slate-300 bg-white text-sm"
                  />
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="demo-email" className="text-xs font-semibold text-slate-700">
                    Work Email
                  </Label>
                  <Input
                    id="demo-email"
                    type="email"
                    required
                    value={workEmail}
                    onChange={(e) => setWorkEmail(e.target.value)}
                    placeholder="jhayes@healthsystem.org"
                    className="border-slate-300 bg-white text-sm"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div className="space-y-1.5">
                  <Label htmlFor="demo-hospital" className="text-xs font-semibold text-slate-700">
                    Hospital / Health System
                  </Label>
                  <Input
                    id="demo-hospital"
                    required
                    value={hospitalName}
                    onChange={(e) => setHospitalName(e.target.value)}
                    placeholder="St. Anne's Medical Center"
                    className="border-slate-300 bg-white text-sm"
                  />
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="demo-ehr" className="text-xs font-semibold text-slate-700">
                    Primary EHR Platform
                  </Label>
                  <select
                    id="demo-ehr"
                    value={ehrSystem}
                    onChange={(e) => setEhrSystem(e.target.value)}
                    className="h-9 w-full rounded-md border border-slate-300 bg-white px-3 text-sm text-[#0b1220] focus:outline-none focus:ring-1 focus:ring-teal-500"
                  >
                    <option value="Epic">Epic Systems</option>
                    <option value="Cerner">Oracle Health / Cerner</option>
                    <option value="Meditech">Meditech</option>
                    <option value="AthenaHealth">AthenaHealth</option>
                    <option value="Other">Other / FHIR API</option>
                  </select>
                </div>
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="demo-notes" className="text-xs font-semibold text-slate-700">
                  Security Goals or Clinical Areas of Focus (Optional)
                </Label>
                <Textarea
                  id="demo-notes"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="e.g. VIP patient access monitoring, off-hours emergency break-glass auditing..."
                  rows={2}
                  className="border-slate-300 bg-white text-sm"
                />
              </div>

              <DialogFooter className="pt-2">
                <Button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full bg-[#14b8a6] font-semibold text-[#0b1220] hover:bg-[#0d9488] hover:text-white"
                >
                  {isSubmitting ? (
                    <span className="flex items-center gap-2">
                      <Loader2 className="h-4 w-4 animate-spin" />
                      <span>Scheduling Demo...</span>
                    </span>
                  ) : (
                    <span>Confirm Demo Request</span>
                  )}
                </Button>
              </DialogFooter>
            </form>
          </>
        ) : (
          <div className="py-6 text-center">
            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-emerald-100 text-emerald-600">
              <CheckCircle2 className="h-8 w-8" />
            </div>
            <DialogTitle className="mt-4 text-xl font-bold text-[#0b1220]">Demo Request Captured</DialogTitle>
            <p className="mt-2 text-sm text-slate-600">
              Thank you, <strong>{fullName || "Doctor"}</strong>. This demo-only preview captured your request details
              locally.
            </p>
            {selectedSchedule && (
              <p className="mt-3 text-sm text-slate-600">
                Requested time: <strong>{selectedSchedule}</strong>
              </p>
            )}
            {requestId && <p className="mt-2 font-mono text-xs text-slate-500">Local preview reference: {requestId}</p>}
            <p
              role="status"
              className="mx-auto mt-4 max-w-sm rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-left text-xs leading-relaxed text-amber-900"
            >
              Demo-only notice: no email, calendar invitation, or sandbox credentials were sent, and this preview did
              not submit your details to a backend service.
            </p>
            <div className="mt-6">
              <Button onClick={handleReset} className="bg-[#0b1220] px-6 text-white hover:bg-slate-800">
                Done
              </Button>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
