package com.erp.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.system.domain.dto.UserCreateRequest;
import com.erp.system.domain.dto.UserStatusUpdateRequest;
import com.erp.system.domain.dto.UserUpdateRequest;
import com.erp.system.domain.entity.SysDepartment;
import com.erp.system.domain.entity.SysPermission;
import com.erp.system.domain.entity.SysRole;
import com.erp.system.domain.entity.SysUser;
import com.erp.system.domain.entity.SysUserRole;
import com.erp.system.security.DataScopeContext;
import com.erp.system.security.DataScopeLevel;
import com.erp.system.security.DataScopeService;
import com.erp.system.security.FieldPermissionService;
import com.erp.system.domain.vo.UserFieldPermissionVO;
import com.erp.system.domain.vo.UserVO;
import com.erp.system.mapper.SysDepartmentMapper;
import com.erp.system.mapper.SysPermissionMapper;
import com.erp.system.mapper.SysRoleMapper;
import com.erp.system.mapper.SysUserMapper;
import com.erp.system.mapper.SysUserRoleMapper;
import com.erp.system.service.SysUserService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysUserServiceImpl implements SysUserService {
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final PasswordEncoder passwordEncoder;
    private final DataScopeService dataScopeService;
    private final FieldPermissionService fieldPermissionService;

    public SysUserServiceImpl(SysUserMapper sysUserMapper,
                              SysRoleMapper sysRoleMapper,
                              SysPermissionMapper sysPermissionMapper,
                              SysUserRoleMapper sysUserRoleMapper,
                              SysDepartmentMapper sysDepartmentMapper,
                              PasswordEncoder passwordEncoder,
                              DataScopeService dataScopeService,
                              FieldPermissionService fieldPermissionService) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysDepartmentMapper = sysDepartmentMapper;
        this.passwordEncoder = passwordEncoder;
        this.dataScopeService = dataScopeService;
        this.fieldPermissionService = fieldPermissionService;
    }

    @Override
    public SysUser getByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, false));
    }

    @Override
    public List<String> getRoleCodes(UUID userId) {
        return sysRoleMapper.selectByUserId(userId).stream().map(SysRole::getCode).collect(Collectors.toList());
    }

    @Override
    public List<String> getPermissions(UUID userId) {
        return sysPermissionMapper.selectByUserId(userId).stream().map(SysPermission::getCode).collect(Collectors.toList());
    }

    @Override
    public List<Integer> getRoleDataScopes(UUID userId) {
        return sysRoleMapper.selectByUserId(userId).stream()
                .map(SysRole::getDataScope)
                .collect(Collectors.toList());
    }

    @Override
    public PageVO<UserVO> listUsers(long pageNum, long pageSize) {
        DataScopeContext scope = dataScopeService.resolveCurrentScope();
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, false)
                .orderByDesc(SysUser::getCreatedAt);
        if (scope.getLevel() == DataScopeLevel.SELF && scope.getUserId() != null) {
            queryWrapper.eq(SysUser::getId, scope.getUserId());
        } else if (scope.getLevel() == DataScopeLevel.DEPARTMENT) {
            if (scope.getDepartmentIds().isEmpty()) {
                queryWrapper.isNull(SysUser::getId);
            } else {
                queryWrapper.in(SysUser::getDepartmentId, scope.getDepartmentIds());
            }
        }
        Page<SysUser> page = sysUserMapper.selectPage(
                new Page<>(pageNum, pageSize),
                queryWrapper);
        List<UserVO> records = page.getRecords().stream().map(this::toUserVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public UserVO createUser(UserCreateRequest request) {
        if (getByUsername(request.getUsername()) != null) {
            throw new BizException(10007, "用户名已存在");
        }
        if (!fieldPermissionService.canEditUserSensitiveFields()) {
            request.setPhone(null);
            request.setEmail(null);
        }
        SysUser user = new SysUser();
        user.setId(UUID.randomUUID());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDepartmentId(request.getDepartmentId());
        user.setStatus(1);
        user.setDeleted(false);
        user.setCreatedBy(SecurityUtils.getUserId());
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        sysUserMapper.insert(user);
        bindRoles(user.getId(), request.getRoleIds());
        return toUserVO(user);
    }

    @Override
    @Transactional
    public UserVO updateUser(UUID id, UserUpdateRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException(10006, "用户不存在");
        }
        user.setRealName(request.getRealName());
        if (fieldPermissionService.canEditUserSensitiveFields()) {
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());
        }
        user.setDepartmentId(request.getDepartmentId());
        user.setUpdatedBy(SecurityUtils.getUserId());
        user.setUpdatedAt(OffsetDateTime.now());
        sysUserMapper.updateById(user);
        if (request.getRoleIds() != null) {
            bindRoles(id, request.getRoleIds());
        }
        return toUserVO(user);
    }

    @Override
    public void updateStatus(UUID id, UserStatusUpdateRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException(10006, "用户不存在");
        }
        user.setStatus(request.getStatus());
        user.setUpdatedBy(SecurityUtils.getUserId());
        user.setUpdatedAt(OffsetDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Override
    public DataScopeContext getCurrentDataScope() {
        return dataScopeService.resolveCurrentScope();
    }

    @Override
    public UserFieldPermissionVO getCurrentFieldPermissions() {
        UserFieldPermissionVO vo = new UserFieldPermissionVO();
        vo.setCanViewSensitiveFields(fieldPermissionService.canViewUserSensitiveFields());
        vo.setCanEditSensitiveFields(fieldPermissionService.canEditUserSensitiveFields());
        return vo;
    }

    private void bindRoles(UUID userId, List<UUID> roleIds) {
        sysUserRoleMapper.deleteByUserId(userId);
        if (roleIds == null) {
            return;
        }
        for (UUID roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        }
    }

    private UserVO toUserVO(SysUser user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setRealName(user.getRealName());
        if (fieldPermissionService.canViewUserSensitiveFields()) {
            userVO.setPhone(user.getPhone());
            userVO.setEmail(user.getEmail());
        }
        userVO.setDepartmentId(user.getDepartmentId());
        if (user.getDepartmentId() != null) {
            SysDepartment department = sysDepartmentMapper.selectById(user.getDepartmentId());
            userVO.setDepartmentName(department == null ? null : department.getName());
        }
        userVO.setStatus(user.getStatus());
        userVO.setCreatedAt(user.getCreatedAt());
        userVO.setRoleIds(sysUserRoleMapper.selectRoleIdsByUserId(user.getId()));
        userVO.setRoleCodes(getRoleCodes(user.getId()));
        return userVO;
    }
}
