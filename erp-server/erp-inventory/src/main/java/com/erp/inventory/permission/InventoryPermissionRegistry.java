package com.erp.inventory.permission;

import com.erp.common.core.permission.PermissionDefinition;
import com.erp.common.core.permission.PermissionRegistry;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InventoryPermissionRegistry implements PermissionRegistry {
    @Override
    public List<PermissionDefinition> listDefinitions() {
        return Arrays.stream(InventoryPermissionDefinition.values())
                .map(permission -> (PermissionDefinition) permission)
                .toList();
    }
}
