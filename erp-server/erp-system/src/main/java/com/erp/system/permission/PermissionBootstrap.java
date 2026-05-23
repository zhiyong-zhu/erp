package com.erp.system.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.core.permission.PermissionDefinition;
import com.erp.common.core.permission.PermissionRegistry;
import com.erp.system.domain.entity.SysPermission;
import com.erp.system.domain.entity.SysRole;
import com.erp.system.domain.entity.SysRolePermission;
import com.erp.system.mapper.SysPermissionMapper;
import com.erp.system.mapper.SysRoleMapperExt;
import com.erp.system.mapper.SysRolePermissionMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PermissionBootstrap implements ApplicationRunner {
    private final List<PermissionRegistry> permissionRegistries;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysRoleMapperExt sysRoleMapperExt;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    public PermissionBootstrap(List<PermissionRegistry> permissionRegistries,
                               SysPermissionMapper sysPermissionMapper,
                               SysRoleMapperExt sysRoleMapperExt,
                               SysRolePermissionMapper sysRolePermissionMapper) {
        this.permissionRegistries = permissionRegistries;
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysRoleMapperExt = sysRoleMapperExt;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<PermissionDefinition> definitions = permissionRegistries.stream()
                .flatMap(registry -> registry.listDefinitions().stream())
                .sorted(java.util.Comparator.comparing(PermissionDefinition::getSortOrder))
                .toList();

        Map<String, SysPermission> permissionByCode = new HashMap<>();
        for (SysPermission permission : sysPermissionMapper.selectList(new LambdaQueryWrapper<>())) {
            permissionByCode.put(permission.getCode(), permission);
        }

        for (PermissionDefinition definition : definitions) {
            SysPermission permission = permissionByCode.get(definition.getCode());
            SysPermission parent = definition.getParentCode() == null ? null : permissionByCode.get(definition.getParentCode());
            boolean isNew = permission == null;
            if (isNew) {
                permission = new SysPermission();
                permission.setId(UUID.randomUUID());
                permission.setCode(definition.getCode());
            }
            permission.setName(definition.getName());
            permission.setType(definition.getType());
            permission.setPath(definition.getPath());
            permission.setIcon(definition.getIcon());
            permission.setSortOrder(definition.getSortOrder());
            permission.setParentId(parent == null ? null : parent.getId());
            permission.setStatus(1);
            if (isNew) {
                sysPermissionMapper.insert(permission);
                permissionByCode.put(definition.getCode(), permission);
            } else {
                sysPermissionMapper.updateById(permission);
            }
        }

        grantAdminDefaults(permissionByCode, definitions);
    }

    private void grantAdminDefaults(Map<String, SysPermission> permissionByCode, List<PermissionDefinition> definitions) {
        SysRole adminRole = sysRoleMapperExt.selectByCode("ADMIN");
        if (adminRole == null) {
            return;
        }
        List<UUID> existing = sysRolePermissionMapper.selectPermissionIdsByRoleId(adminRole.getId());
        for (PermissionDefinition definition : definitions) {
            if (!definition.grantToAdminByDefault()) {
                continue;
            }
            SysPermission permission = permissionByCode.get(definition.getCode());
            if (permission == null || existing.contains(permission.getId())) {
                continue;
            }
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setRoleId(adminRole.getId());
            rolePermission.setPermissionId(permission.getId());
            sysRolePermissionMapper.insert(rolePermission);
        }
    }
}
