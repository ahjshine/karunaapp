import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { ShieldCheck, Download, FileKey } from "lucide-react"
import { medsentinelApi, type AlertDetailViewModel } from "@/lib/medsentinelApi"
import { useEffect, useState } from "react"

interface AuditTrailDialogProps {
  isOpen: boolean
  onClose: () => void
  alertId?: string
}

export function AuditTrailDialog({ isOpen, onClose, alertId = "A-101" }: AuditTrailDialogProps) {
  const [detail, setDetail] = useState<AlertDetailViewModel | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState("")

  useEffect(() => {
    if (!isOpen) return

    let cancelled = false
    setIsLoading(true)
    setErrorMessage("")
    void medsentinelApi
      .getAlert(alertId)
      .then((nextDetail) => {
        if (!cancelled) setDetail(nextDetail)
      })
      .catch((error: unknown) => {
        if (!cancelled) {
          setDetail(null)
          setErrorMessage(error instanceof Error ? error.message : "Unable to load live audit evidence.")
        }
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [alertId, isOpen])

  const alert = detail?.alert
  const records = (detail?.evidenceLogs ?? []).map((log) => ({
    id: `LOG-${log.logId}`,
    time: log.ts,
    patientId: log.patientRef ?? "Patient ref unavailable",
    type: log.action.replaceAll("_", " "),
    ward: log.wardName ?? "Scope not recorded",
    hash: log.hash ? `${log.hash.slice(0, 8)}…${log.hash.slice(-4)}` : "Hash unavailable",
  }))

  const handleExport = () => {
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(records, null, 2))
    const downloadAnchor = document.createElement("a")
    downloadAnchor.setAttribute("href", dataStr)
    downloadAnchor.setAttribute("download", `karuna-audit-trail-${alertId}.json`)
    document.body.appendChild(downloadAnchor)
    downloadAnchor.click()
    downloadAnchor.remove()
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-3xl border border-[#26354b] bg-[#0c1425] text-slate-100 sm:rounded-2xl">
        <DialogHeader>
          <div className="flex flex-wrap items-center justify-between gap-2 border-b border-white/10 pb-3">
            <div className="flex items-center gap-2">
              <Badge className={alert?.severity === "High" ? "border-rose-500/30 bg-rose-500/15 text-rose-300" : alert?.severity === "Medium" ? "border-amber-500/30 bg-amber-500/15 text-amber-300" : "border-sky-500/30 bg-sky-500/15 text-sky-300"}>{alert?.severity ?? "Unknown"}</Badge>
              <DialogTitle className="text-lg font-bold text-white">Forensic Audit Trail — {alert?.alertId ?? alertId}</DialogTitle>
            </div>
            <div className="flex items-center gap-2 font-mono text-xs text-[#5eead4]">
              <ShieldCheck className="h-4 w-4" />
               <span>{alert && !errorMessage ? "Evidence returned by backend" : "Backend verification unavailable"}</span>
            </div>
          </div>
          <DialogDescription className="pt-2 text-xs text-slate-400">
             {errorMessage || alert?.text || "Live alert evidence is not available."} {alert && <>Actor: <strong>{alert.user}</strong> ({alert.role}).</>}
          </DialogDescription>
        </DialogHeader>

        {/* Audit Log Table */}
        <ScrollArea className="h-80 w-full rounded-xl border border-white/10 bg-[#0f172a]">
          <Table>
            <TableHeader className="bg-slate-900/90 font-mono text-[10px] text-slate-400 uppercase">
              <TableRow className="border-b border-white/10">
                <TableHead className="w-24 text-slate-300">Time</TableHead>
                <TableHead className="text-slate-300">Patient Ref</TableHead>
                <TableHead className="text-slate-300">Record Classification</TableHead>
                <TableHead className="text-slate-300">Ward Scope</TableHead>
                <TableHead className="text-right text-slate-300">Block Hash</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody className="divide-y divide-white/[0.06] font-mono text-xs">
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={5} className="py-8 text-center text-slate-400">Loading live evidence…</TableCell>
                </TableRow>
              ) : errorMessage ? (
                <TableRow>
                  <TableCell colSpan={5} className="py-8 text-center text-rose-300">{errorMessage}</TableCell>
                </TableRow>
              ) : records.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="py-8 text-center text-slate-400">No evidence logs were returned for this alert.</TableCell>
                </TableRow>
              ) : records.map((rec) => (
                <TableRow key={rec.id} className="hover:bg-white/[0.03]">
                  <TableCell className="font-semibold text-[#5eead4]">{rec.time}</TableCell>
                  <TableCell className="text-slate-300">{rec.patientId}</TableCell>
                  <TableCell className="font-sans font-medium text-white">{rec.type}</TableCell>
                  <TableCell className="text-rose-300/90">{rec.ward}</TableCell>
                  <TableCell className="text-right text-[10px] text-slate-500">{rec.hash}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </ScrollArea>

        <DialogFooter className="flex flex-wrap items-center justify-between gap-3 border-t border-white/10 pt-4">
          <div className="flex items-center gap-2 text-xs text-slate-400">
            <FileKey className="h-4 w-4 text-[#14b8a6]" />
             <span>Live evidence package ready for export</span>
          </div>

          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handleExport}
              className="gap-1.5 border-white/20 bg-white/5 text-xs text-white hover:bg-white/10"
            >
              <Download className="h-3.5 w-3.5" />
              <span>Export Forensic JSON</span>
            </Button>
            <Button
              size="sm"
              onClick={onClose}
              className="bg-[#14b8a6] text-xs font-semibold text-[#0b1220] hover:bg-[#0d9488] hover:text-white"
            >
              Close
            </Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
