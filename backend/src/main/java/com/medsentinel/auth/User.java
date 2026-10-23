package com.medsentinel.auth;

import com.medsentinel.rbac.Role;
import java.time.LocalTime;

public class User {

    private Long userId;
    private String username;
    private String pwHash;
    private Role role;
    private Long wardId;
    private LocalTime shiftStart;
    private LocalTime shiftEnd;
    private UserStatus status = UserStatus.ACTIVE;

    public enum UserStatus { ACTIVE, DISABLED }

    public User() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPwHash() { return pwHash; }
    public void setPwHash(String pwHash) { this.pwHash = pwHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Long getWardId() { return wardId; }
    public void setWardId(Long wardId) { this.wardId = wardId; }
    public LocalTime getShiftStart() { return shiftStart; }
    public void setShiftStart(LocalTime shiftStart) { this.shiftStart = shiftStart; }
    public LocalTime getShiftEnd() { return shiftEnd; }
    public void setShiftEnd(LocalTime shiftEnd) { this.shiftEnd = shiftEnd; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
}
