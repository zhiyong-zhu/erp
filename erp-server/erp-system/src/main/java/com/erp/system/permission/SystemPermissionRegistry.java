package com.erp.system.permission;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SystemPermissionRegistry implements PermissionRegistry {
    @Override
    public List<PermissionDefinition> listDefinitions() {
        return Arrays.stream(SystemPermissionDefinition.values())
                .map(permission -> (PermissionDefinition) permission)
                .toList();
    }
}
