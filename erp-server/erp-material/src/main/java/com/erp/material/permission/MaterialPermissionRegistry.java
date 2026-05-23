package com.erp.material.permission;

import com.erp.common.core.permission.PermissionDefinition;
import com.erp.common.core.permission.PermissionRegistry;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MaterialPermissionRegistry implements PermissionRegistry {
    @Override
    public List<PermissionDefinition> listDefinitions() {
        return Arrays.stream(MaterialPermissionDefinition.values())
                .map(permission -> (PermissionDefinition) permission)
                .toList();
    }
}
