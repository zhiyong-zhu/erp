package com.erp.product.permission;

import com.erp.common.core.permission.PermissionDefinition;
import com.erp.common.core.permission.PermissionRegistry;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductPermissionRegistry implements PermissionRegistry {
    @Override
    public List<PermissionDefinition> listDefinitions() {
        return Arrays.stream(ProductPermissionDefinition.values())
                .map(permission -> (PermissionDefinition) permission)
                .toList();
    }
}
