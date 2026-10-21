package com.medsentinel.auth;

import com.medsentinel.rbac.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "pw_hash", nullable = false)
    private String pwHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "ward_id")
    private Long wardId;

    @Column(name = "shift_start")
    private java.time.LocalTime shiftStart;

    @Column(name = "shift_end")
    private java.time.LocalTime shiftEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
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
    public java.time.LocalTime getShiftStart() { return shiftStart; }
    public void setShiftStart(java.time.LocalTime shiftStart) { this.shiftStart = shiftStart; }
    public java.time.LocalTime getShiftEnd() { return shiftEnd; }
    public void setShiftEnd(java.time.LocalTime shiftEnd) { this.shiftEnd = shiftEnd; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
}
