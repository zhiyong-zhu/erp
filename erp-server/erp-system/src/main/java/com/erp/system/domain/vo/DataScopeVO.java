package com.erp.system.domain.vo;

import java.util.List;
import java.util.UUID;

public class DataScopeVO {
    private String level;
    private UUID userId;
    private List<UUID> departmentIds;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<UUID> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(List<UUID> departmentIds) {
        this.departmentIds = departmentIds;
    }
}
