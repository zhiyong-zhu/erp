package com.erp.system.permission;

import com.erp.system.domain.entity.SysPermission;
import com.erp.system.mapper.SysPermissionMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PermissionDefinitionValidator implements ApplicationRunner {
    private final List<PermissionRegistry> permissionRegistries;
    private final SysPermissionMapper sysPermissionMapper;

    public PermissionDefinitionValidator(List<PermissionRegistry> permissionRegistries,
                                         SysPermissionMapper sysPermissionMapper) {
        this.permissionRegistries = permissionRegistries;
        this.sysPermissionMapper = sysPermissionMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        Set<String> definedCodes = new HashSet<>();
        for (PermissionRegistry registry : permissionRegistries) {
            for (PermissionDefinition definition : registry.listDefinitions()) {
                if (!definedCodes.add(definition.getCode())) {
                    throw new IllegalStateException("Duplicate permission definition code: " + definition.getCode());
                }
            }
        }

        for (SysPermission permission : sysPermissionMapper.selectList(null)) {
            if (permission.getStatus() != null
                    && permission.getStatus() == 1
                    && permission.getCode() != null
                    && permission.getCode().startsWith("system:")
                    && !definedCodes.contains(permission.getCode())) {
                throw new IllegalStateException("Active system permission is not defined in code: " + permission.getCode());
            }
        }
    }
}
