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

interface AuditTrailDialogProps {
  isOpen: boolean
  onClose: () => void
  alertId?: string
}

export function AuditTrailDialog({ isOpen, onClose, alertId = "A-101" }: AuditTrailDialogProps) {
  // Mock generated 38 records forensic audit stream
  const records = Array.from({ length: 38 }, (_, i) => {
    const minute = Math.floor(i / 6)
    const second = (i * 11) % 60
    const time = `09:4${2 + minute}:${second < 10 ? "0" + second : second}`
    const recordTypes = [
      "Oncology Biopsy Report",
      "Surgical Note (Cardiothoracic)",
      "Psychiatric Clinical Evaluation",
      "Pediatric ICU Chart",
      "Executive VIP Health Record",
      "Toxicology Lab Results",
    ]
    const wards = ["Ward 4B (Oncology)", "Ward 2A (Cardiac ICU)", "Ward 6C (Neurology)", "Main ED Bay 3"]

    return {
      id: `EVT-8942-${100 + i}`,
      time,
      patientId: `PT-${80000 + i * 231}-STJ`,
      type: recordTypes[i % recordTypes.length],
      ward: wards[i % wards.length],
      hash: `0x7f${(i * 37 + 192).toString(16)}...c4`,
    }
  })

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
              <Badge className="border-rose-500/30 bg-rose-500/15 text-rose-300">High</Badge>
              <DialogTitle className="text-lg font-bold text-white">Forensic Audit Trail — {alertId}</DialogTitle>
            </div>
            <div className="flex items-center gap-2 font-mono text-xs text-[#5eead4]">
              <ShieldCheck className="h-4 w-4" />
              <span>SHA-256 Chain Verified</span>
            </div>
          </div>
          <DialogDescription className="pt-2 text-xs text-slate-400">
            38 clinical records queried in 7 minutes by <strong>Elena Ruiz</strong> (Billing clerk · Patient Accounts).
            Role baseline for clinical records: <strong>0</strong>.
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
              {records.map((rec) => (
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
            <span>Immutable evidence package locked for legal hold</span>
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
