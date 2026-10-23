package com.medsentinel.patient;

public class Ward {

    private Long wardId;
    private String name;

    public Ward() {}

    public Ward(String name) {
        this.name = name;
    }

    public Ward(Long wardId, String name) {
        this.wardId = wardId;
        this.name = name;
    }

    public Long getWardId() { return wardId; }
    public void setWardId(Long wardId) { this.wardId = wardId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
