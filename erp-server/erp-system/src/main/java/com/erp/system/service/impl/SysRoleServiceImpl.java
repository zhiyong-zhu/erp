package com.erp.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.exception.BizException;
import com.erp.system.domain.dto.RoleCreateRequest;
import com.erp.system.domain.dto.RolePermissionUpdateRequest;
import com.erp.system.domain.dto.RoleUpdateRequest;
import com.erp.system.domain.dto.StatusUpdateRequest;
import com.erp.system.domain.entity.SysPermission;
import com.erp.system.domain.entity.SysRole;
import com.erp.system.domain.entity.SysRolePermission;
import com.erp.system.domain.vo.PageVO;
import com.erp.system.domain.vo.PermissionVO;
import com.erp.system.domain.vo.RoleVO;
import com.erp.system.mapper.SysPermissionMapper;
import com.erp.system.mapper.SysRoleMapper;
import com.erp.system.mapper.SysRolePermissionMapper;
import com.erp.system.service.SysRoleService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysRoleServiceImpl implements SysRoleService {
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    public SysRoleServiceImpl(SysRoleMapper sysRoleMapper,
                              SysPermissionMapper sysPermissionMapper,
                              SysRolePermissionMapper sysRolePermissionMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
    }

    @Override
    public PageVO<RoleVO> listRoles(long pageNum, long pageSize) {
        Page<SysRole> page = sysRoleMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getCreatedAt));
        List<RoleVO> records = page.getRecords().stream().map(this::toRoleVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public RoleVO createRole(RoleCreateRequest request) {
        validateUnique(request.getCode(), null);
        SysRole role = new SysRole();
        role.setId(UUID.randomUUID());
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        role.setDataScope(request.getDataScope() == null ? 1 : request.getDataScope());
        role.setStatus(1);
        role.setCreatedAt(OffsetDateTime.now());
        role.setUpdatedAt(OffsetDateTime.now());
        sysRoleMapper.insert(role);
        bindPermissions(role.getId(), request.getPermissionIds());
        return toRoleVO(role);
    }

    @Override
    @Transactional
    public RoleVO updateRole(UUID id, RoleUpdateRequest request) {
        SysRole role = getRole(id);
        if (request.getCode() != null) {
            validateUnique(request.getCode(), id);
            role.setCode(request.getCode());
        }
        if (request.getName() != null) {
            role.setName(request.getName());
        }
        role.setDescription(request.getDescription());
        role.setDataScope(request.getDataScope() == null ? 1 : request.getDataScope());
        role.setUpdatedAt(OffsetDateTime.now());
        sysRoleMapper.updateById(role);
        if (request.getPermissionIds() != null) {
            bindPermissions(id, request.getPermissionIds());
        }
        return toRoleVO(role);
    }

    @Override
    public void updateStatus(UUID id, StatusUpdateRequest request) {
        SysRole role = getRole(id);
        role.setStatus(request.getStatus());
        role.setUpdatedAt(OffsetDateTime.now());
        sysRoleMapper.updateById(role);
    }

    @Override
    public RoleVO getRoleDetail(UUID id) {
        return toRoleVO(getRole(id));
    }

    @Override
    public List<PermissionVO> listPermissionTree() {
        List<SysPermission> permissions = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getStatus, 1)
                .orderByAsc(SysPermission::getSortOrder)
                .orderByAsc(SysPermission::getName));
        LinkedHashMap<UUID, PermissionVO> byId = new LinkedHashMap<>();
        for (SysPermission permission : permissions) {
            byId.put(permission.getId(), toPermissionVO(permission));
        }
        List<PermissionVO> roots = new ArrayList<>();
        for (PermissionVO permission : byId.values()) {
            if (permission.getParentId() != null && byId.containsKey(permission.getParentId())) {
                byId.get(permission.getParentId()).getChildren().add(permission);
            } else {
                roots.add(permission);
            }
        }
        sortPermissions(roots);
        return roots;
    }

    @Override
    @Transactional
    public void updateRolePermissions(UUID id, RolePermissionUpdateRequest request) {
        getRole(id);
        bindPermissions(id, request.getPermissionIds());
    }

    private SysRole getRole(UUID id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BizException(10006, "角色不存在");
        }
        return role;
    }

    private void validateUnique(String code, UUID excludedId) {
        SysRole existing = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, code));
        if (existing != null && !existing.getId().equals(excludedId)) {
            throw new BizException(10007, "角色编码已存在");
        }
    }

    private void bindPermissions(UUID roleId, List<UUID> permissionIds) {
        sysRolePermissionMapper.deleteByRoleId(roleId);
        if (permissionIds == null) {
            return;
        }
        for (UUID permissionId : permissionIds) {
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            sysRolePermissionMapper.insert(rolePermission);
        }
    }

    private RoleVO toRoleVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setCode(role.getCode());
        vo.setDescription(role.getDescription());
        vo.setDataScope(role.getDataScope());
        vo.setStatus(role.getStatus());
        vo.setCreatedAt(role.getCreatedAt());
        vo.setPermissionIds(sysRolePermissionMapper.selectPermissionIdsByRoleId(role.getId()));
        return vo;
    }

    private PermissionVO toPermissionVO(SysPermission permission) {
        PermissionVO vo = new PermissionVO();
        vo.setId(permission.getId());
        vo.setParentId(permission.getParentId());
        vo.setName(permission.getName());
        vo.setCode(permission.getCode());
        vo.setType(permission.getType());
        vo.setPath(permission.getPath());
        vo.setIcon(permission.getIcon());
        vo.setSortOrder(permission.getSortOrder());
        vo.setStatus(permission.getStatus());
        return vo;
    }

    private void sortPermissions(List<PermissionVO> permissions) {
        permissions.sort(Comparator.comparing(PermissionVO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(PermissionVO::getName, Comparator.nullsLast(String::compareTo)));
        for (PermissionVO permission : permissions) {
            sortPermissions(permission.getChildren());
        }
    }
}
