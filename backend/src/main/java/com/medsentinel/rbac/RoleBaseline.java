package com.medsentinel.rbac;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "role_baselines")
public class RoleBaseline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "baseline_id")
    private Long baselineId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private BaselineScope scope;

    @Column(name = "max_records_per_10min", nullable = false)
    private Integer maxRecordsPer10Min;

    @Column(name = "multiplier", nullable = false)
    private BigDecimal multiplier;

    public enum BaselineScope { ON_WARD, OFF_WARD, CLINICAL, ANY }

    public RoleBaseline() {}

    public Long getBaselineId() { return baselineId; }
    public void setBaselineId(Long baselineId) { this.baselineId = baselineId; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public BaselineScope getScope() { return scope; }
    public void setScope(BaselineScope scope) { this.scope = scope; }
    public Integer getMaxRecordsPer10Min() { return maxRecordsPer10Min; }
    public void setMaxRecordsPer10Min(Integer max) { this.maxRecordsPer10Min = max; }
    public BigDecimal getMultiplier() { return multiplier; }
    public void setMultiplier(BigDecimal multiplier) { this.multiplier = multiplier; }
}
