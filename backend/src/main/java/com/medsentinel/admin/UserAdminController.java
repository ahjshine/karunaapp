package com.medsentinel.admin;

import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.rbac.RoleBaseline;
import com.medsentinel.rbac.RoleService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class UserAdminController {

    private final UserRepository userRepo;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserAdminController(UserRepository userRepo, RoleService roleService,
                                PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        User user = userRepo.findById(id).orElse(null);
        if (user != null) {
            user.setStatus(user.getStatus() == User.UserStatus.ACTIVE
                ? User.UserStatus.DISABLED : User.UserStatus.ACTIVE);
            userRepo.save(user);
            redirectAttrs.addFlashAttribute("successMessage",
                "User " + user.getUsername() + " " + user.getStatus().name().toLowerCase());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/baselines")
    public String baselines(Model model) {
        model.addAttribute("baselines", roleService.getAllBaselines());
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/baselines";
    }

    @PostMapping("/baselines/{id}")
    public String updateBaseline(@PathVariable Long id,
                                  @RequestParam Integer maxRecords,
                                  @RequestParam BigDecimal multiplier,
                                  RedirectAttributes redirectAttrs) {
        roleService.updateBaseline(id, maxRecords, multiplier);
        redirectAttrs.addFlashAttribute("successMessage", "Baseline updated");
        return "redirect:/admin/baselines";
    }
}
