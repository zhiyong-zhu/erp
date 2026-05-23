package com.erp.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.exception.BizException;
import com.erp.system.domain.dto.RoleCreateRequest;
import com.erp.system.domain.dto.RoleUpdateRequest;
import com.erp.system.domain.dto.StatusUpdateRequest;
import com.erp.system.domain.entity.SysRole;
import com.erp.system.domain.vo.PageVO;
import com.erp.system.domain.vo.RoleVO;
import com.erp.system.mapper.SysRoleMapper;
import com.erp.system.service.SysRoleService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl implements SysRoleService {
    private final SysRoleMapper sysRoleMapper;

    public SysRoleServiceImpl(SysRoleMapper sysRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
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
        return toRoleVO(role);
    }

    @Override
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
        return toRoleVO(role);
    }

    @Override
    public void updateStatus(UUID id, StatusUpdateRequest request) {
        SysRole role = getRole(id);
        role.setStatus(request.getStatus());
        role.setUpdatedAt(OffsetDateTime.now());
        sysRoleMapper.updateById(role);
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

    private RoleVO toRoleVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setCode(role.getCode());
        vo.setDescription(role.getDescription());
        vo.setDataScope(role.getDataScope());
        vo.setStatus(role.getStatus());
        vo.setCreatedAt(role.getCreatedAt());
        return vo;
    }
}
