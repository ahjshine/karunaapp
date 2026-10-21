package com.medsentinel.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoleBaselineRepository extends JpaRepository<RoleBaseline, Long> {
    List<RoleBaseline> findByRoleId(Long roleId);
    RoleBaseline findByRoleIdAndScope(Long roleId, RoleBaseline.BaselineScope scope);
}
