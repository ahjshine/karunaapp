package com.medsentinel.rbac;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "perm_id")
    private Long permId;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    public Permission() {}

    public Permission(String code) {
        this.code = code;
    }

    public Long getPermId() { return permId; }
    public void setPermId(Long permId) { this.permId = permId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
