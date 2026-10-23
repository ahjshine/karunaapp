package com.medsentinel.rbac;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for role and baseline management.
 */
public class RoleService {

    private final RoleRepository roleRepo;
    private final RoleBaselineRepository baselineRepo;

    public RoleService() {
        this(new RoleRepository(), new RoleBaselineRepository());
    }

    public RoleService(RoleRepository roleRepo, RoleBaselineRepository baselineRepo) {
        this.roleRepo = roleRepo;
        this.baselineRepo = baselineRepo;
    }

    public List<Role> getAllRoles() {
        return roleRepo.findAll();
    }

    public Role getRole(Long id) {
        return roleRepo.findById(id).orElse(null);
    }

    public List<RoleBaseline> getBaselines(Long roleId) {
        return baselineRepo.findByRoleId(roleId);
    }

    public List<RoleBaseline> getAllBaselines() {
        return baselineRepo.findAll();
    }

    public RoleBaseline updateBaseline(Long baselineId, Integer maxRecords, BigDecimal multiplier) {
        RoleBaseline baseline = baselineRepo.findById(baselineId)
            .orElseThrow(() -> new IllegalArgumentException("Baseline not found: " + baselineId));
        if (maxRecords != null) baseline.setMaxRecordsPer10Min(maxRecords);
        if (multiplier != null) baseline.setMultiplier(multiplier);
        return baselineRepo.save(baseline);
    }
}
