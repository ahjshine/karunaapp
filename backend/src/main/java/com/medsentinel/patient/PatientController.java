package com.medsentinel.patient;

import com.medsentinel.auth.AppUserDetails;
import com.medsentinel.breakglass.BreakGlassService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;
    private final BreakGlassService breakGlassService;

    public PatientController(PatientService patientService, BreakGlassService breakGlassService) {
        this.patientService = patientService;
        this.breakGlassService = breakGlassService;
    }

    @GetMapping
    public String listPatients(Model model) {
        model.addAttribute("patients", patientService.listPatients());
        return "patients/list";
    }

    @GetMapping("/{id}")
    public String viewPatient(@PathVariable Long id, Model model,
                              @AuthenticationPrincipal AppUserDetails user) {
        Patient patient = patientService.getPatient(id);
        if (patient == null) {
            return "redirect:/patients";
        }

        model.addAttribute("patient", patient);
        PatientService.PatientRecordBundle bundle = patientService.getAccessibleRecords(id);
        model.addAttribute("records", bundle.getRecords());
        model.addAttribute("denied", bundle.getDenied());
        model.addAttribute("userRole", user.getRoleName());

        return "patients/view";
    }

    @PostMapping("/{id}/break-glass")
    public String breakGlass(@PathVariable Long id,
                             @RequestParam String justification,
                             @AuthenticationPrincipal AppUserDetails user,
                             RedirectAttributes redirectAttrs) {
        try {
            BreakGlassService.BreakGlassResult result = breakGlassService.executeBreakGlass(user, id, justification);
            redirectAttrs.addFlashAttribute("breakGlassRecords", result.records());
            redirectAttrs.addFlashAttribute("successMessage", "Break-glass access granted. A review alert has been created.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/patients/" + id;
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Model model) {
        if (q != null && !q.isBlank()) {
            List<Patient> results = patientService.searchPatients(q);
            model.addAttribute("patients", results);
            model.addAttribute("query", q);
        }
        return "patients/list";
    }
}
