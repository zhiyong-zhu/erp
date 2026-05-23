package com.erp.system.security;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class DataScopeContext {
    private final DataScopeLevel level;
    private final UUID userId;
    private final Set<UUID> departmentIds;

    public DataScopeContext(DataScopeLevel level, UUID userId, Set<UUID> departmentIds) {
        this.level = level;
        this.userId = userId;
        this.departmentIds = departmentIds == null ? Collections.emptySet() : departmentIds;
    }

    public DataScopeLevel getLevel() {
        return level;
    }

    public UUID getUserId() {
        return userId;
    }

    public Set<UUID> getDepartmentIds() {
        return departmentIds;
    }
}
