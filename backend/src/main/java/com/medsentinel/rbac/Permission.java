package com.medsentinel.rbac;

import java.util.Objects;

public class Permission {

    private Long permId;
    private String code;
    private String description;

    public Permission() {}

    public Permission(String code) {
        this.code = code;
    }

    public Permission(Long permId, String code, String description) {
        this.permId = permId;
        this.code = code;
        this.description = description;
    }

    public Long getPermId() { return permId; }
    public void setPermId(Long permId) { this.permId = permId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
